"""Parse payload manifest using protobuf groups for PartitionUpdate."""
import struct, zipfile, hashlib, os, shutil

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
    if wt == 0:
        _, offset = decode_varint(data, offset)
    elif wt == 1:
        offset += 8
    elif wt == 2:
        length, offset = decode_varint(data, offset)
        offset += length
    elif wt == 5:
        offset += 4
    elif wt == 3:
        depth = 1
        while depth > 0:
            k2, offset = decode_varint(data, offset)
            w2 = k2 & 0x7
            if w2 == 3:
                depth += 1
            elif w2 == 4:
                depth -= 1
    elif wt == 4:
        pass
    return offset

def parse_group_fields(data, offset, end=None):
    """Parse fields within a group."""
    if end is None:
        end = len(data)
    fields = {}
    while offset < end:
        key, offset = decode_varint(data, offset)
        fn = key >> 3
        wt = key & 0x7
        
        if wt == 4 and fn == fields.get('_group_field', -1):
            # End of group
            break
        if wt == 4:
            # End group
            break
        
        if wt == 0:
            val, offset = decode_varint(data, offset)
            if fn in fields:
                if not isinstance(fields[fn], list):
                    fields[fn] = [fields[fn]]
                fields[fn].append(val)
            else:
                fields[fn] = val
        elif wt == 2:
            length, offset = decode_varint(data, offset)
            val = data[offset:offset+length]
            offset += length
            if fn in fields:
                if not isinstance(fields[fn], list):
                    fields[fn] = [fields[fn]]
                fields[fn].append(val)
            else:
                fields[fn] = val
        elif wt == 3:
            # Sub-group
            sub_fields = parse_group_fields(data, offset)
            fields[fn] = sub_fields
            offset = sub_fields.get('_end', offset)
        elif wt == 1:
            offset += 8
        elif wt == 5:
            offset += 4
        else:
            break
    fields['_end'] = offset
    return fields

def parse_partition_from_group(data, start):
    """Parse PartitionUpdate group fields."""
    result = {'name': None, 'size': None, 'operations': []}
    offset = start
    
    while offset < len(data):
        key, offset = decode_varint(data, offset)
        fn = key >> 3
        wt = key & 0x7
        
        if wt == 4:  # End group for field 1
            break
        
        if fn == 1 and wt == 2:  # partition_name
            length, offset = decode_varint(data, offset)
            result['name'] = data[offset:offset+length].decode('utf-8')
            offset += length
        elif fn == 2 and wt == 0:  # size
            result['size'], offset = decode_varint(data, offset)
        elif fn == 3 and wt == 2:  # hash
            length, offset = decode_varint(data, offset)
            result['hash'] = data[offset:offset+length].hex()
            offset += length
        elif fn == 4 and wt == 3:  # operations (InstallOperation as group)
            op = {'type': -1, 'data_offset': 0, 'data_length': 0, 'dst_length': 0}
            op_start = offset
            while offset < len(data):
                k2, offset = decode_varint(data, offset)
                op_fn = k2 >> 3
                op_wt = k2 & 0x7
                if op_wt == 4:  # End group for operation
                    break
                if op_fn == 1 and op_wt == 0:
                    op['type'], offset = decode_varint(data, offset)
                elif op_fn == 2 and op_wt == 0:
                    op['data_offset'], offset = decode_varint(data, offset)
                elif op_fn == 3 and op_wt == 0:
                    op['data_length'], offset = decode_varint(data, offset)
                elif op_fn == 5 and op_wt == 0:
                    op['dst_length'], offset = decode_varint(data, offset)
                elif op_fn == 6 and op_wt == 2:
                    sl, offset = decode_varint(data, offset)
                    op['src_sha256'] = data[offset:offset+sl].hex()
                    offset += sl
                else:
                    offset = skip_field(data, offset)
            result['operations'].append(op)
        else:
            offset = skip_field(data, offset)
    
    result['_end'] = offset
    return result

# Process both zips
artifacts = r'D:\AndroidProjects\porting\waterlily-i001d-reconstruction\artifacts'
temp_base = os.path.join(os.environ.get('TEMP', r'C:\Users\home\AppData\Local\Temp'), 'opencode', 'boot_v3')
if os.path.exists(temp_base):
    shutil.rmtree(temp_base)
os.makedirs(temp_base)

for label, zip_name in [('OFFICIAL', 'Bliss-v19.6-I001D-OFFICIAL-gapps-20260616.zip'),
                         ('REBUILT', 'Bliss-v19.6-I001D-UNOFFICIAL-vanilla-20260712.zip')]:
    zip_path = os.path.join(artifacts, zip_name)
    print(f'\n{"="*60}')
    print(f'{label}: {zip_name}')
    
    with zipfile.ZipFile(zip_path) as z:
        payload_data = z.read('payload.bin')
    
    version = struct.unpack('>Q', payload_data[4:12])[0]
    manifest_size = struct.unpack('>Q', payload_data[12:20])[0]
    manifest = payload_data[20:20+manifest_size]
    payload_start = 20 + manifest_size
    
    print(f'  Version: {version}, Manifest: {manifest_size} bytes')
    
    # Parse manifest - skip leading zeros, then look for field 1 (start group)
    offset = 0
    while offset < len(manifest) and manifest[offset] == 0:
        offset += 1
    
    while offset < len(manifest):
        key, offset = decode_varint(manifest, offset)
        fn = key >> 3
        wt = key & 0x7
        
        if fn == 1 and wt == 3:  # PartitionUpdate as group (wire_type 3)
            part = parse_partition_from_group(manifest, offset)
            offset = part['_end']
            
            if part['name']:
                print(f'  Partition: {part["name"]}, size={part["size"]}')
                for op in part['operations']:
                    print(f'    Op: type={op["type"]}, data_offset={op["data_offset"]}, data_length={op["data_length"]}, dst_length={op["dst_length"]}')
                
                if part['name'] == 'boot' and part.get('operations'):
                    op = part['operations'][0]
                    doff = op.get('data_offset', 0)
                    dlen = op.get('data_length', 0)
                    op_type = op.get('type', -1)
                    
                    if doff and dlen:
                        raw = payload_data[payload_start + doff : payload_start + doff + dlen]
                        print(f'    Raw data: {len(raw)} bytes, first 16 hex: {raw[:16].hex()}')
                        
                        if op_type == 0:  # REPLACE
                            boot_data = raw
                        elif op_type == 1:  # REPLACE_BZ
                            import bz2
                            boot_data = bz2.decompress(raw)
                            print(f'    BZ2 decompressed: {len(boot_data)} bytes')
                        else:
                            print(f'    Unsupported type {op_type}')
                            boot_data = None
                        
                        if boot_data:
                            sha = hashlib.sha256(boot_data).hexdigest()
                            print(f'    Boot data: {len(boot_data)} bytes, SHA256={sha}')
                            print(f'    Starts with: {boot_data[:16].hex()}')
                            
                            boot_path = os.path.join(temp_base, f'boot-{label.lower()}.img')
                            with open(boot_path, 'wb') as f:
                                f.write(boot_data)
                            print(f'    Written: {boot_path}')
        
        elif fn == 11 and wt == 0:  # minor_version
            minor, offset = decode_varint(manifest, offset)
            print(f'  minor_version={minor}')
        else:
            offset = skip_field(manifest, offset)

print(f'\nTemp directory: {temp_base}')
os.system(f'explorer {temp_base}')
