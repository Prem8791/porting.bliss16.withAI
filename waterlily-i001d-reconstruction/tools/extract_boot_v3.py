"""Extract boot.img from both OTA zips — properly assemble from all operations."""
import struct, hashlib, bz2, os, shutil, sys, lzma
import zipfile

sys.path.insert(0, os.path.dirname(__file__))
import update_metadata_pb2 as um

artifacts = r'D:\AndroidProjects\porting\waterlily-i001d-reconstruction\artifacts'
temp_base = os.path.join(os.environ.get('TEMP', r'C:\Users\home\AppData\Local\Temp'), 'opencode', 'boot_analysis_v3')
if os.path.exists(temp_base):
    shutil.rmtree(temp_base)
os.makedirs(temp_base)

zips = [
    ('OFFICIAL', 'Bliss-v19.6-I001D-OFFICIAL-gapps-20260616.zip'),
    ('REBUILT', 'Bliss-v19.6-I001D-UNOFFICIAL-vanilla-20260712.zip'),
]

for label, zip_name in zips:
    zip_path = os.path.join(artifacts, zip_name)
    print(f'\n{"="*60}')
    print(f'{label}: {zip_name}')

    with zipfile.ZipFile(zip_path) as z:
        payload_data = z.read('payload.bin')

    assert payload_data[:4] == b'CrAU', 'Bad magic'
    version = struct.unpack('>Q', payload_data[4:12])[0]
    manifest_size = struct.unpack('>Q', payload_data[12:20])[0]
    metadata_sig_size = struct.unpack('>I', payload_data[20:24])[0]
    manifest = payload_data[24:24+manifest_size]
    data_offset = 24 + manifest_size + metadata_sig_size

    dam = um.DeltaArchiveManifest()
    dam.ParseFromString(manifest)
    block_size = dam.block_size
    print(f'  block_size={block_size}')

    boot_part = None
    for p in dam.partitions:
        if p.partition_name == 'boot':
            boot_part = p
            break

    if boot_part is None:
        print('  WARNING: boot partition not found!')
        continue

    part_size = boot_part.new_partition_info.size if boot_part.HasField("new_partition_info") else 0
    print(f'  partition_size={part_size}, operations={len(boot_part.operations)}')

    # Accumulate all operations
    boot_data = bytearray(part_size) if part_size else bytearray()

    for i, op in enumerate(boot_part.operations):
        raw = payload_data[data_offset + op.data_offset:data_offset + op.data_offset + op.data_length]

        if op.type == um.InstallOperation.REPLACE:
            decompressed = raw
        elif op.type == um.InstallOperation.REPLACE_BZ:
            decompressed = bz2.decompress(raw)
        elif op.type == um.InstallOperation.REPLACE_XZ:
            decompressed = lzma.decompress(raw)
        else:
            print(f'    op[{i}]: UNSUPPORTED type={op.type}')
            continue

        # Determine write position from dst_extents
        if op.dst_extents:
            write_off = op.dst_extents[0].start_block * block_size
            if len(op.dst_extents) == 1 and op.dst_extents[0].num_blocks * block_size == len(decompressed):
                pass
        else:
            write_off = 0

        end = write_off + len(decompressed)
        if end > len(boot_data):
            boot_data.extend(b'\x00' * (end - len(boot_data)))
        boot_data[write_off:end] = decompressed

        if i < 5 or i >= len(boot_part.operations) - 2:
            de_type = um.InstallOperation.Type.Name(op.type)
            print(f'    op[{i}]: {de_type}, offset={write_off}, len={len(decompressed)}')

    # Verify final hash
    if boot_part.new_partition_info.hash:
        expected_hash = boot_part.new_partition_info.hash.hex()
        actual_hash = hashlib.sha256(boot_data).hexdigest()
        match = actual_hash == expected_hash
        print(f'  Partition hash check: {"PASS" if match else "FAIL"}')
        print(f'    Expected: {expected_hash}')
        print(f'    Actual:   {actual_hash}')

    out_path = os.path.join(temp_base, f'boot-{label.lower()}.img')
    with open(out_path, 'wb') as f:
        f.write(boot_data)
    print(f'  Written: {out_path} ({len(boot_data)} bytes, SHA256={hashlib.sha256(boot_data).hexdigest()})')

    if boot_data[:8] == b'ANDROID!':
        print(f'  Valid ANDROID! magic header confirmed')
        # Parse boot image header
        kernel_size = struct.unpack_from('<I', boot_data, 8)[0]
        kernel_load = struct.unpack_from('<I', boot_data, 16)[0]
        ramdisk_size = struct.unpack_from('<I', boot_data, 12)[0]
        ramdisk_load = struct.unpack_from('<I', boot_data, 20)[0]
        cmdline = boot_data[0x70:0x70+0x200].rstrip(b'\x00').decode('ascii', errors='replace')
        print(f'    kernel_size={kernel_size}, load_addr=0x{kernel_load:08x}')
        print(f'    ramdisk_size={ramdisk_size}, load_addr=0x{ramdisk_load:08x}')
        print(f'    cmdline="{cmdline}"')
    else:
        print(f'  WARNING: header is {boot_data[:16].hex()}')

print(f'\nTemp directory: {temp_base}')
import subprocess
subprocess.run(['explorer', temp_base])
