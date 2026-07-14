"""Extract boot.img from both OTA zips using protobuf."""
import struct, hashlib, bz2, os, shutil, sys
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
    metadata_sig = payload_data[24+manifest_size:24+manifest_size+metadata_sig_size]
    data_offset = 24 + manifest_size + metadata_sig_size

    dam = um.DeltaArchiveManifest()
    dam.ParseFromString(manifest)
    print(f'  minor_version={dam.minor_version}, block_size={dam.block_size}')
    print(f'  partitions: {len(dam.partitions)}')

    boot_part = None
    for p in dam.partitions:
        print(f'    {p.partition_name}: size={p.new_partition_info.size if p.HasField("new_partition_info") else "?"}')
        if p.partition_name == 'boot':
            boot_part = p

    if boot_part is None:
        print('  WARNING: boot partition not found!')
        continue

    print(f'  boot partition:')
    for i, op in enumerate(boot_part.operations):
        print(f'    op[{i}]: type={op.type} ({um.InstallOperation.Type.Name(op.type)}), '
              f'data_offset={op.data_offset}, data_length={op.data_length}, '
              f'dst_length={op.dst_length}, '
              f'sha256={op.data_sha256_hash.hex() if op.data_sha256_hash else "none"}')

        raw = payload_data[data_offset + op.data_offset:data_offset + op.data_offset + op.data_length]
        print(f'      raw: {len(raw)} bytes, first 16 hex: {raw[:16].hex()}')

        boot_data = None
        if op.type == um.InstallOperation.REPLACE:
            boot_data = raw
        elif op.type == um.InstallOperation.REPLACE_BZ:
            boot_data = bz2.decompress(raw)
            print(f'      BZ2 decompressed: {len(boot_data)} bytes')
        elif op.type == um.InstallOperation.REPLACE_XZ:
            import lzma
            boot_data = lzma.decompress(raw)
        else:
            print(f'      Unsupported op type: {op.type}')
            continue

        if op.data_sha256_hash:
            actual_hash = hashlib.sha256(boot_data).digest()
            match = actual_hash == op.data_sha256_hash
            print(f'      SHA256 check: {"PASS" if match else "FAIL"}')

        if boot_data:
            out_path = os.path.join(temp_base, f'boot-{label.lower()}.img')
            with open(out_path, 'wb') as f:
                f.write(boot_data)
            print(f'      Written: {out_path} ({len(boot_data)} bytes, SHA256={hashlib.sha256(boot_data).hexdigest()})')

            # Check if it looks like a valid boot image
            if boot_data[:8] == b'ANDROID!':
                print(f'      Valid ANDROID! magic header confirmed')
            else:
                print(f'      WARNING: header is {boot_data[:16].hex()}')

print(f'\nTemp directory: {temp_base}')
import subprocess
subprocess.run(['explorer', temp_base])
