"""Parse OTA payload properly - extract boot partition info from protobuf manifest."""
import struct, zipfile, hashlib, os, shutil, sys

def decode_varint(data, offset):
    value = 0
    shift = 0
    while True:
        b = data[offset]
        value |= (b & 0x7f) << shift
        offset += 1
        if not (b & 0x80):
            break
        shift += 7
    return value, offset

def skip_field(data, offset):
    """Skip one protobuf field, return new offset."""
    key, offset = decode_varint(data, offset)
    wt = key & 0x7
    if wt == 0:  # varint
        _, offset = decode_varint(data, offset)
    elif wt == 1:  # 64-bit
        offset += 8
    elif wt == 2:  # length-delimited
        length, offset = decode_varint(data, offset)
        offset += length
    elif wt == 5:  # 32-bit
        offset += 4
    elif wt == 3:  # start group - skip to matching end group
        depth = 1
        while depth > 0 and offset < len(data):
            k2, offset = decode_varint(data, offset)
            w2 = k2 & 0x7
            if w2 == 3:
                depth += 1
            elif w2 == 4:
                depth -= 1
    elif wt == 4:  # end group
        pass
    return offset

def parse_partition_update(data, offset):
    """Parse PartitionUpdate message, return (name, size, operations_list, new_offset)."""
    pname = None
    psize = None
    operations = []
    
    while offset < len(data):
        key, offset = decode_varint(data, offset)
        fn = key >> 3
        wt = key & 0x7
        
        if fn == 1 and wt == 2:  # partition_name (string)
            length, offset = decode_varint(data, offset)
            pname = data[offset:offset+length].decode('utf-8')
            offset += length
        elif fn == 2 and wt == 0:  # size (uint64)
            psize, offset = decode_varint(data, offset)
        elif fn == 4 and wt == 2:  # operations (repeated InstallOperation)
            op_len, offset = decode_varint(data, offset)
            op_start = offset
            op_fields = {}
            while offset < op_start + op_len:
                k2, offset = decode_varint(data, offset)
                op_fn = k2 >> 3
                op_wt = k2 & 0x7
                if op_fn == 1 and op_wt == 0:  # type
                    op_fields['type'], offset = decode_varint(data, offset)
                elif op_fn == 2 and op_wt == 0:  # data_offset
                    op_fields['data_offset'], offset = decode_varint(data, offset)
                elif op_fn == 3 and op_wt == 0:  # data_length
                    op_fields['data_length'], offset = decode_varint(data, offset)
                elif op_fn == 5 and op_wt == 0:  # dst_length
                    op_fields['dst_length'], offset = decode_varint(data, offset)
                elif op_fn == 6 and op_wt == 2:  # src_sha256
                    sl, offset = decode_varint(data, offset)
                    op_fields['src_sha256'] = data[offset:offset+sl].hex()
                    offset += sl
                else:
                    offset = skip_field(data, offset)
            operations.append(op_fields)
        elif fn == 15 and wt == 2:  # partition_uuid
            ul, offset = decode_varint(data, offset)
            offset += ul
        else:
            offset = skip_field(data, offset)
    
    return pname, psize, operations

# Parse the official and rebuilt payloads
artifacts = r'D:\AndroidProjects\porting\waterlily-i001d-reconstruction\artifacts'
temp_base = os.path.join(os.environ.get('TEMP', r'C:\Users\home\AppData\Local\Temp'), 'opencode', 'boot_analysis_v2')
if os.path.exists(temp_base):
    shutil.rmtree(temp_base)
os.makedirs(temp_base)

for label, zip_name in [('OFFICIAL', 'Bliss-v19.6-I001D-OFFICIAL-gapps-20260616.zip'),
                         ('REBUILT', 'Bliss-v19.6-I001D-UNOFFICIAL-vanilla-20260712.zip')]:
    zip_path = os.path.join(artifacts, zip_name)
    print(f'\n{"="*60}')
    print(f'{label}: {zip_name}')
    print(f'{"="*60}')
    
    with zipfile.ZipFile(zip_path) as z:
        payload_data = z.read('payload.bin')
    
    magic = payload_data[0:4]
    version = struct.unpack('>Q', payload_data[4:12])[0]
    manifest_size = struct.unpack('>Q', payload_data[12:20])[0]
    manifest = payload_data[20:20+manifest_size]
    payload_start = 20 + manifest_size
    
    print(f'  Version: {version}, Manifest: {manifest_size} bytes')
    
    # Find boot partition in manifest
    # Skip leading zeros (padding bytes before protobuf data)
    moff = 0
    while moff < len(manifest) and manifest[moff] == 0:
        moff += 1
    if moff > 0:
        print(f'  Skipped {moff} leading zero padding bytes in manifest')
    
    # Parse DeltaArchiveManifest - look for partitions (field 1)
    boot_found = False
    while moff < len(manifest):
        key, moff = decode_varint(manifest, moff)
        fn = key >> 3
        wt = key & 0x7
        
        if fn == 1 and wt == 2:  # PartitionUpdate
            plen, moff = decode_varint(manifest, moff)
            pname, psize, ops = parse_partition_update(manifest, moff)
            print(f'  Partition: {pname}, size={psize}, operations={len(ops)}')
            moff += plen
            
            if pname == 'boot' and ops:
                for op_idx, op in enumerate(ops):
                    op_type = op.get('type', -1)
                    doff = op.get('data_offset', 0)
                    dlen = op.get('data_length', 0)
                    dst_len = op.get('dst_length', 0)
                    print(f'    Op {op_idx}: type={op_type} (REPLACE_BZ={1}), data_offset={doff}, data_length={dlen}, dst_length={dst_len}')
                    
                    if doff and dlen:
                        actual_offset = payload_start + doff
                        raw_data = payload_data[actual_offset:actual_offset+dlen]
                        
                        if op_type == 0:  # REPLACE (raw)
                            boot_data = raw_data
                            print(f'    REPLACE: extracted {len(boot_data)} bytes')
                        elif op_type == 1:  # REPLACE_BZ (bzip2)
                            import bz2
                            try:
                                boot_data = bz2.decompress(raw_data)
                                print(f'    REPLACE_BZ: decompressed {len(raw_data)} -> {len(boot_data)} bytes')
                            except Exception as e:
                                print(f'    BZ2 decompress error: {e}')
                                boot_data = None
                        elif op_type in (3, 8):  # REPLACE_XZ, REPLACE_ZSTD
                            print(f'    Compression type {op_type} not supported without libraries')
                            boot_data = None
                        else:
                            print(f'    Unknown compression type {op_type}')
                            boot_data = None
                        
                        if boot_data and boot_data[:8] == b'ANDROID!':
                            boot_path = os.path.join(temp_base, f'boot-{label.lower()}.img')
                            with open(boot_path, 'wb') as f:
                                f.write(boot_data)
                            sha = hashlib.sha256(boot_data).hexdigest()
                            print(f'    Written: {boot_path} ({len(boot_data)} bytes, SHA256={sha})')
                            boot_found = True
                        elif boot_data:
                            print(f'    Data does NOT start with ANDROID! (starts with {boot_data[:8].hex()})')
                        else:
                            print(f'    No boot data extracted')
        else:
            moff = skip_field(manifest, moff)
    
    if not boot_found:
        print(f'  WARNING: boot.img not extracted for {label}')

print(f'\nTemp dir: {temp_base}')
os.system(f'explorer {temp_base}')
