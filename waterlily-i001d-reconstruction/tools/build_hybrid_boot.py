"""Build an unsigned hybrid boot image from an OTA kernel and a port ramdisk."""

from __future__ import annotations

import argparse
import bz2
import hashlib
import json
import lzma
import shutil
import struct
import tempfile
import zipfile
from pathlib import Path

BOOT_MAGIC = b"ANDROID!"
OP_REPLACE = 0
OP_REPLACE_BZ = 1
OP_ZERO = 6
OP_REPLACE_XZ = 8


def sha256(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def align(value: int, alignment: int) -> int:
    return (value + alignment - 1) // alignment * alignment


def decode_varint(data: bytes, offset: int) -> tuple[int, int]:
    value = 0
    shift = 0
    while True:
        if offset >= len(data):
            raise ValueError("Truncated protobuf varint")
        byte = data[offset]
        offset += 1
        value |= (byte & 0x7F) << shift
        if not byte & 0x80:
            return value, offset
        shift += 7
        if shift >= 70:
            raise ValueError("Invalid protobuf varint")


def protobuf_fields(data: bytes):
    offset = 0
    while offset < len(data):
        key, offset = decode_varint(data, offset)
        number = key >> 3
        wire_type = key & 7
        if wire_type == 0:
            value, offset = decode_varint(data, offset)
        elif wire_type == 1:
            value = data[offset : offset + 8]
            offset += 8
        elif wire_type == 2:
            length, offset = decode_varint(data, offset)
            value = data[offset : offset + length]
            offset += length
        elif wire_type == 5:
            value = data[offset : offset + 4]
            offset += 4
        else:
            raise ValueError(f"Unsupported protobuf wire type: {wire_type}")
        yield number, wire_type, value


def field_values(data: bytes, number: int, wire_type: int) -> list[object]:
    return [
        value
        for field_number, field_wire_type, value in protobuf_fields(data)
        if field_number == number and field_wire_type == wire_type
    ]


def first_varint(data: bytes, number: int, default: int = 0) -> int:
    values = field_values(data, number, 0)
    return int(values[0]) if values else default


def extract_payload(ota: Path, destination: Path) -> None:
    with zipfile.ZipFile(ota) as archive:
        info = archive.getinfo("payload.bin")
        print(f"Extracting payload.bin ({info.file_size} bytes)...")
        with archive.open(info) as source, destination.open("wb") as output:
            shutil.copyfileobj(source, output, length=16 * 1024 * 1024)


def extract_partition(payload: Path, partition_name: str) -> bytes:
    with payload.open("rb") as stream:
        if stream.read(4) != b"CrAU":
            raise ValueError("Invalid OTA payload magic")

        version = struct.unpack(">Q", stream.read(8))[0]
        manifest_size = struct.unpack(">Q", stream.read(8))[0]
        metadata_signature_size = struct.unpack(">I", stream.read(4))[0]
        manifest_data = stream.read(manifest_size)
        data_offset = 24 + manifest_size + metadata_signature_size

        block_size = first_varint(manifest_data, 3, 4096)
        partition_data = None
        for candidate in field_values(manifest_data, 13, 2):
            names = field_values(bytes(candidate), 1, 2)
            if names and bytes(names[0]).decode("utf-8") == partition_name:
                partition_data = bytes(candidate)
                break

        if partition_data is None:
            raise ValueError(f"Partition {partition_name!r} not found in payload")

        partition_info_values = field_values(partition_data, 7, 2)
        if not partition_info_values:
            raise ValueError(f"Partition {partition_name!r} has no new partition info")
        partition_info = bytes(partition_info_values[0])
        size = first_varint(partition_info, 1)
        output = bytearray(size)

        for operation_data in field_values(partition_data, 8, 2):
            operation_data = bytes(operation_data)
            operation_type = first_varint(operation_data, 1)
            operation_offset = first_varint(operation_data, 2)
            operation_length = first_varint(operation_data, 3)
            stream.seek(data_offset + operation_offset)
            encoded = stream.read(operation_length)

            if operation_type == OP_REPLACE:
                decoded = encoded
            elif operation_type == OP_REPLACE_BZ:
                decoded = bz2.decompress(encoded)
            elif operation_type == OP_REPLACE_XZ:
                decoded = lzma.decompress(encoded)
            elif operation_type == OP_ZERO:
                decoded = b"\0" * sum(
                    first_varint(bytes(extent), 2) * block_size
                    for extent in field_values(operation_data, 6, 2)
                )
            else:
                raise ValueError(
                    f"Unsupported full-OTA operation type: {operation_type}"
                )

            cursor = 0
            for extent_data in field_values(operation_data, 6, 2):
                extent_data = bytes(extent_data)
                extent_size = first_varint(extent_data, 2) * block_size
                start = first_varint(extent_data, 1) * block_size
                output[start : start + extent_size] = decoded[
                    cursor : cursor + extent_size
                ]
                cursor += extent_size

            if cursor != len(decoded):
                raise ValueError(
                    f"Decoded operation length mismatch: wrote {cursor}, got {len(decoded)}"
                )

        hashes = field_values(partition_info, 2, 2)
        expected = bytes(hashes[0]).hex() if hashes else ""
        actual = sha256(output)
        if expected and actual != expected:
            raise ValueError(
                f"Extracted {partition_name} hash mismatch: {actual} != {expected}"
            )

        print(
            f"Extracted {partition_name}: version={version}, size={len(output)}, "
            f"sha256={actual}"
        )
        return bytes(output)


def parse_boot(image: bytes) -> dict[str, object]:
    if image[:8] != BOOT_MAGIC:
        raise ValueError("Not an Android boot image")

    kernel_size = struct.unpack_from("<I", image, 8)[0]
    ramdisk_size = struct.unpack_from("<I", image, 16)[0]
    second_size = struct.unpack_from("<I", image, 24)[0]
    page_size = struct.unpack_from("<I", image, 36)[0]
    header_version = struct.unpack_from("<I", image, 40)[0]

    kernel_offset = page_size
    ramdisk_offset = kernel_offset + align(kernel_size, page_size)
    second_offset = ramdisk_offset + align(ramdisk_size, page_size)
    cmdline = (image[64:576] + image[608:1632]).split(b"\0", 1)[0]

    return {
        "kernel_size": kernel_size,
        "ramdisk_size": ramdisk_size,
        "second_size": second_size,
        "page_size": page_size,
        "header_version": header_version,
        "kernel": image[kernel_offset : kernel_offset + kernel_size],
        "ramdisk": image[ramdisk_offset : ramdisk_offset + ramdisk_size],
        "second": image[second_offset : second_offset + second_size],
        "cmdline": cmdline.decode("ascii", errors="replace"),
    }


def legacy_boot_id(kernel: bytes, ramdisk: bytes, second: bytes) -> bytes:
    digest = hashlib.sha1()
    for component in (kernel, ramdisk, second):
        digest.update(component)
        digest.update(struct.pack("<I", len(component)))
    return digest.digest().ljust(32, b"\0")


def build_hybrid(official: bytes, port: bytes) -> tuple[bytes, dict[str, object]]:
    official_info = parse_boot(official)
    port_info = parse_boot(port)

    if official_info["header_version"] != 0 or port_info["header_version"] != 0:
        raise ValueError("This builder currently supports boot header version 0 only")
    if official_info["page_size"] != port_info["page_size"]:
        raise ValueError("Official and port boot images use different page sizes")

    kernel = official_info["kernel"]
    ramdisk = port_info["ramdisk"]
    second = port_info["second"]
    page_size = int(port_info["page_size"])

    header = bytearray(port[:page_size])
    struct.pack_into("<I", header, 8, len(kernel))
    struct.pack_into("<I", header, 16, len(ramdisk))
    struct.pack_into("<I", header, 24, len(second))
    header[576:608] = legacy_boot_id(kernel, ramdisk, second)

    output = bytearray(header)
    for component in (kernel, ramdisk, second):
        output.extend(component)
        output.extend(b"\0" * (align(len(component), page_size) - len(component)))

    details = {
        "official_boot_sha256": sha256(official),
        "port_boot_sha256": sha256(port),
        "official_kernel_size": len(kernel),
        "official_kernel_sha256": sha256(kernel),
        "port_ramdisk_size": len(ramdisk),
        "port_ramdisk_sha256": sha256(ramdisk),
        "hybrid_raw_size": len(output),
        "hybrid_raw_sha256": sha256(output),
        "page_size": page_size,
        "cmdline": port_info["cmdline"],
        "boot_devices_present": (
            "androidboot.boot_devices=soc/1d84000.ufshc" in str(port_info["cmdline"])
        ),
    }
    return bytes(output), details


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--official-ota", required=True, type=Path)
    parser.add_argument("--port-boot", required=True, type=Path)
    parser.add_argument("--out-dir", required=True, type=Path)
    args = parser.parse_args()

    args.out_dir.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="waterlily-hybrid-") as temp:
        payload = Path(temp) / "payload.bin"
        extract_payload(args.official_ota, payload)
        official_boot = extract_partition(payload, "boot")

    port_boot = args.port_boot.read_bytes()
    hybrid, details = build_hybrid(official_boot, port_boot)

    official_path = args.out_dir / "boot-official.img"
    raw_path = args.out_dir / "boot-hybrid-raw.img"
    manifest_path = args.out_dir / "hybrid-manifest.json"
    official_path.write_bytes(official_boot)
    raw_path.write_bytes(hybrid)
    manifest_path.write_text(json.dumps(details, indent=2) + "\n", encoding="ascii")

    print(f"Official boot: {official_path}")
    print(f"Hybrid raw:   {raw_path}")
    print(f"Manifest:     {manifest_path}")
    print(json.dumps(details, indent=2))


if __name__ == "__main__":
    main()
