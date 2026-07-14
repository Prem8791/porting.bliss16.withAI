"""Extract boot.img from OTA payload.bin inside zip files."""
import struct, hashlib, os, sys, zipfile, shutil

# Protobuf wire format helpers
def decode_varint(data, offset):
    value = 0
    shift = 0
    while True:
        b = data[offset]
        value |= (b & 0x7f) << shift
        shift += 7
        offset += 1
        if not (b & 0x80):
            break
    return value, offset

def decode_field(data, offset):
    """Decode a protobuf field, return (field_number, wire_type, value, new_offset)"""
    key, offset = decode_varint(data, offset)
    field_num = key >> 3
    wire_type = key & 0x7
    
    if wire_type == 0:  # varint
        value, offset = decode_varint(data, offset)
    elif wire_type == 1:  # 64-bit
        value = struct.unpack('<Q', data[offset:offset+8])[0]
        offset += 8
    elif wire_type == 2:  # length-delimited
        length, offset = decode_varint(data, offset)
        value = data[offset:offset+length]
        offset += length
    elif wire_type == 5:  # 32-bit
        value = struct.unpack('<I', data[offset:offset+4])[0]
        offset += 4
    else:
        raise ValueError(f'Unknown wire type {wire_type}')
    
    return field_num, wire_type, value, offset

def parse_protobuf_fields(data, offset=0, end=None):
    """Parse protobuf fields, returning list of (field_num, value, wire_type)."""
    if end is None:
        end = len(data)
    fields = []
    while offset < end:
        fn, wt, val, offset = decode_field(data, offset)
        fields.append((fn, val, wt))
    return fields

def find_partition_in_payload(payload_path, target_name='boot'):
    """Extract boot partition data from OTA payload.bin."""
    with open(payload_path, 'rb') as f:
        f.seek(0)
        magic = f.read(4)
        assert magic == b'CrAU', f'Bad payload magic: {magic}'
        
        meta_size = struct.unpack('>Q', f.read(8))[0]
        metadata = f.read(meta_size)
        
        payload_start = 12 + meta_size
        print(f'  Payload magic OK, metadata size={meta_size}')
        
        # Parse the manifest protobuf
        manifest_fields = parse_protobuf_fields(metadata)
        
        # Field 1 in DeltaArchiveManifest is repeated PartitionUpdate
        for fn, val, wt in manifest_fields:
            if fn == 1 and wt == 2:  # PartitionUpdate
                # Parse the PartitionUpdate sub-message
                pu_fields = parse_protobuf_fields(val)
                pname = None
                psize = None
                operations = []
                
                for pu_fn, pu_val, pu_wt in pu_fields:
                    if pu_fn == 1 and pu_wt == 2:  # partition_name
                        pname = pu_val.decode('utf-8', errors='replace')
                    elif pu_fn == 2 and pu_wt == 0:  # size
                        psize = pu_val
                    elif pu_fn == 4 and pu_wt == 2:  # operations
                        # Parse InstallOperation
                        op_fields = parse_protobuf_fields(pu_val)
                        op_info = {}
                        for op_fn, op_val, op_wt in op_fields:
                            if op_fn == 1: op_type = op_val  # type
                            if op_fn == 2: op_doff = op_val   # data_offset
                            if op_fn == 3: op_dlen = op_val   # data_length
                            if op_fn == 4: op_doff2 = op_val  # dst_offset
                            if op_fn == 5: op_dlen2 = op_val  # dst_length
                        op_info = {'type': op_type, 'data_offset': op_doff, 'data_length': op_dlen,
                                  'dst_offset': op_doff2, 'dst_length': op_dlen2}
                        operations.append(op_info)
                
                if pname == target_name:
                    print(f'  Found partition: {pname}, size={psize}')
                    for i, op in enumerate(operations):
                        print(f'    Operation {i}: type={op["type"]}, data_offset={op.get("data_offset")}, data_length={op.get("data_length")}')
                    
                    # For REPLACE/REPLACE_BZ operations, extract the data
                    if operations:
                        op = operations[0]  # Usually just one replace op
                        data_off = op.get('data_offset', 0)
                        data_len = op.get('data_length', 0)
                        dst_len = op.get('dst_length', 0)
                        
                        # data_offset is relative to payload_start (end of manifest)
                        f.seek(payload_start + data_off)
                        data = f.read(data_len)
                        
                        # If REPLACE_BZ, it's bzip2 compressed
                        op_type = op.get('type', 0)
                        if op_type in (0, 1):  # REPLACE=0, REPLACE_BZ=1
                            actual_size = len(data)
                            print(f'  Extracted {actual_size} bytes')
                            return data
                        
                        return data
        return None

def extract_from_zip(zip_path, label, temp_dir):
    """Extract boot.img from an OTA zip's payload.bin."""
    print(f'\n=== {label} ===')
    print(f'Zip: {zip_path}')
    
    with zipfile.ZipFile(zip_path) as z:
        if 'payload.bin' not in z.namelist():
            print(f'  No payload.bin found in {zip_path}')
            return None
        
        info = z.getinfo('payload.bin')
        print(f'  payload.bin: {info.file_size} bytes')
        
        # Extract payload.bin to temp
        payload_tmp = os.path.join(temp_dir, f'payload-{label.lower()}.bin')
        with z.open('payload.bin') as src, open(payload_tmp, 'wb') as dst:
            shutil.copyfileobj(src, dst)
        
        # Extract boot partition
        boot_data = find_partition_in_payload(payload_tmp)
        
        if boot_data:
            boot_path = os.path.join(temp_dir, f'boot-{label.lower()}.img')
            with open(boot_path, 'wb') as f:
                f.write(boot_data)
            sha = hashlib.sha256(boot_data).hexdigest()
            print(f'  boot.img: {len(boot_data)} bytes, SHA256={sha}')
            return boot_path
        
        return None

# Create temp directories
temp_base = os.path.join(os.environ.get('TEMP', r'C:\Users\home\AppData\Local\Temp'), 'opencode', 'boot_analysis')
if os.path.exists(temp_base):
    shutil.rmtree(temp_base)
os.makedirs(temp_base)

artifacts = r'D:\AndroidProjects\porting\waterlily-i001d-reconstruction\artifacts'

official_zip = os.path.join(artifacts, 'Bliss-v19.6-I001D-OFFICIAL-gapps-20260616.zip')
rebuilt_zip = os.path.join(artifacts, 'Bliss-v19.6-I001D-UNOFFICIAL-vanilla-20260712.zip')

official_boot = extract_from_zip(official_zip, 'OFFICIAL', temp_base)
rebuilt_boot = extract_from_zip(rebuilt_zip, 'REBUILT', temp_base)

print(f'\nTemp extraction directory: {temp_base}')
print(f'Official boot: {official_boot}')
print(f'Rebuilt boot: {rebuilt_boot}')

os.system(f'explorer {temp_base}')
