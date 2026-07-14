"""Patch one property in a boot v0 gzip/newc ramdisk and emit a raw boot image."""

from __future__ import annotations

import argparse
import gzip
import hashlib
import struct
from pathlib import Path


def align(value: int, alignment: int) -> int:
    return (value + alignment - 1) // alignment * alignment


def patch_newc_file(archive: bytes, path: str, property_line: str) -> bytes:
    output = bytearray()
    offset = 0
    patched = False

    while offset + 110 <= len(archive):
        header = archive[offset : offset + 110]
        if header[:6] not in (b"070701", b"070702"):
            raise ValueError(f"Invalid newc header at offset {offset}")

        fields = [int(header[6 + index * 8 : 14 + index * 8], 16) for index in range(13)]
        file_size = fields[6]
        name_size = fields[11]
        name_start = offset + 110
        name_end = name_start + name_size
        name_bytes = archive[name_start : name_end]
        name = name_bytes[:-1].decode("utf-8")
        data_start = align(name_end, 4)
        data_end = data_start + file_size
        data = archive[data_start:data_end]

        if name == path:
            text = data.decode("utf-8")
            key = property_line.split("=", 1)[0] + "="
            lines = [line for line in text.splitlines() if not line.startswith(key)]
            lines.append(property_line)
            data = ("\n".join(lines) + "\n").encode("utf-8")
            patched = True

        new_header = bytearray(header)
        new_header[54:62] = f"{len(data):08x}".encode("ascii")
        output.extend(new_header)
        output.extend(name_bytes)
        output.extend(b"\0" * (align(len(output), 4) - len(output)))
        output.extend(data)
        output.extend(b"\0" * (align(len(output), 4) - len(output)))

        offset = align(data_end, 4)
        if name == "TRAILER!!!":
            output.extend(archive[offset:])
            break

    if not patched:
        raise ValueError(f"Ramdisk file {path!r} was not found")
    return bytes(output)


def legacy_boot_id(kernel: bytes, ramdisk: bytes, second: bytes) -> bytes:
    digest = hashlib.sha1()
    for component in (kernel, ramdisk, second):
        digest.update(component)
        digest.update(struct.pack("<I", len(component)))
    return digest.digest().ljust(32, b"\0")


def patch_boot(source: bytes, property_line: str) -> tuple[bytes, dict[str, object]]:
    if source[:8] != b"ANDROID!":
        raise ValueError("Not an Android boot image")

    kernel_size = struct.unpack_from("<I", source, 8)[0]
    ramdisk_size = struct.unpack_from("<I", source, 16)[0]
    second_size = struct.unpack_from("<I", source, 24)[0]
    page_size = struct.unpack_from("<I", source, 36)[0]
    header_version = struct.unpack_from("<I", source, 40)[0]
    if header_version != 0:
        raise ValueError("Only boot header version 0 is supported")

    kernel_offset = page_size
    ramdisk_offset = kernel_offset + align(kernel_size, page_size)
    second_offset = ramdisk_offset + align(ramdisk_size, page_size)
    kernel = source[kernel_offset : kernel_offset + kernel_size]
    ramdisk_gzip = source[ramdisk_offset : ramdisk_offset + ramdisk_size]
    second = source[second_offset : second_offset + second_size]

    ramdisk = gzip.decompress(ramdisk_gzip)
    patched_ramdisk = patch_newc_file(ramdisk, "prop.default", property_line)
    patched_gzip = gzip.compress(patched_ramdisk, compresslevel=9, mtime=0)

    header = bytearray(source[:page_size])
    struct.pack_into("<I", header, 16, len(patched_gzip))
    header[576:608] = legacy_boot_id(kernel, patched_gzip, second)

    output = bytearray(header)
    for component in (kernel, patched_gzip, second):
        output.extend(component)
        output.extend(b"\0" * (align(len(component), page_size) - len(component)))

    return bytes(output), {
        "kernel_sha256": hashlib.sha256(kernel).hexdigest(),
        "original_ramdisk_sha256": hashlib.sha256(ramdisk_gzip).hexdigest(),
        "patched_ramdisk_sha256": hashlib.sha256(patched_gzip).hexdigest(),
        "patched_ramdisk_size": len(patched_gzip),
        "raw_boot_sha256": hashlib.sha256(output).hexdigest(),
        "raw_boot_size": len(output),
        "property": property_line,
    }


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", required=True, type=Path)
    parser.add_argument("--output", required=True, type=Path)
    parser.add_argument("--property", required=True)
    args = parser.parse_args()

    output, details = patch_boot(args.input.read_bytes(), args.property)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_bytes(output)
    for key, value in details.items():
        print(f"{key}: {value}")


if __name__ == "__main__":
    main()
