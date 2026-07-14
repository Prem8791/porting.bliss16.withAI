"""Parse OTA payload protobuf manifest using google.protobuf.
Dynamically compile the update_metadata proto."""
import struct, zipfile, hashlib, os, shutil, sys

# First, try to use protobuf to decode the manifest
from google.protobuf import descriptor_pb2, descriptor, descriptor_pool, message_factory, symbol_database

# Compile the update_metadata proto definition
UPDATE_METADATA_PROTO = '''
syntax = "proto2";

message InstallOperation {
  enum Type {
    REPLACE = 0;
    REPLACE_BZ = 1;
    SOURCE_COPY = 2;
    REPLACE_XZ = 3;
    ZERO = 4;
    DISCARD = 5;
    BROTLI_BSDIFF = 6;
    PUFFDIFF = 7;
    REPLACE_ZSTD = 8;
  }
  required Type type = 1;
  optional uint64 data_offset = 2;
  optional uint64 data_length = 3;
  optional uint64 dst_offset = 4;
  optional uint64 dst_length = 5;
  optional bytes src_sha256 = 6;
  optional uint64 src_offset = 7;
  optional uint64 src_length = 8;
}

message PartitionUpdate {
  required string partition_name = 1;
  optional uint64 size = 2;
  optional bytes hash = 3;
  repeated InstallOperation operations = 4;
  optional bytes partition_uuid = 15;
}

message DeltaArchiveManifest {
  optional uint32 minor_version = 11;
  optional uint32 max_timestamp = 13;
  repeated PartitionUpdate partitions = 1;
  optional uint64 block_size = 3;
  optional bytes metadata_signature = 19;
  optional uint64 metadata_size = 21;
}
'''

# Create file descriptor set from proto text
from google.protobuf import descriptor_pb2
from google.protobuf import descriptor_pool as _descriptor_pool
from google.protobuf import symbol_database as _symbol_database
from google.protobuf import descriptor as _descriptor

# Use compiler
import tempfile
proto_path = os.path.join(tempfile.gettempdir(), 'update_metadata.proto')
with open(proto_path, 'w') as f:
    f.write(UPDATE_METADATA_PROTO)

# Compile with protoc if available, otherwise use pure Python
try:
    import subprocess
    # Check if protoc is available
    result = subprocess.run(['protoc', '--version'], capture_output=True, text=True)
    if result.returncode == 0:
        desc_out = os.path.join(tempfile.gettempdir(), 'metadata.desc')
        subprocess.run(['protoc', '-I' + tempfile.gettempdir(), 
                       '--descriptor_set_out=' + desc_out,
                       os.path.join(tempfile.gettempdir(), 'update_metadata.proto')],
                      capture_output=True)
        # Load descriptor
        with open(desc_out, 'rb') as f:
            desc_set = descriptor_pb2.FileDescriptorSet()
            desc_set.ParseFromString(f.read())
        
        pool = _descriptor_pool.Default()
        for fd in desc_set.file:
            pool.Add(fd)
        
        desc = pool.FindMessageTypeByName('DeltaArchiveManifest')
        msg_factory = message_factory.MessageFactory(pool)
        proto_msg = msg_factory.GetPrototype(desc)
        print('protoc available, compiled proto successfully')
    else:
        raise FileNotFoundError('protoc not found')
except Exception as e:
    print(f'protoc approach failed: {e}')
    # Fall back to manual protobuf parsing
    print('Using manual protobuf parsing')

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

def parse_manifest_fields(data):
    offset = 0
    partitions = []
    minor_version = None
    
    while offset < len(data):
        key, offset = decode_varint(data, offset)
        fn = key >> 3
        wt = key & 0x7
        
        if wt == 0:  # varint
            val, offset = decode_varint(data, offset)
            if fn == 11:
                minor_version = val
                print(f'  minor_version={val}')
        elif wt == 2:  # length-delimited
            length, offset = decode_varint(data, offset)
            val = data[offset:offset+length]
            offset += length
            if fn == 1:  # PartitionUpdate
                partitions.append(('partition', val))
                print(f'  Partition update: {len(val)} bytes')
            elif fn == 3:  # block_size
                print(f'  block_size: {struct.unpack("<Q", val[:8])[0] if len(val) >= 8 else "?"}')
            elif fn == 19:  # metadata_signature
                print(f'  metadata_signature: {len(val)} bytes')
        elif wt == 5:  # 32-bit
            val = struct.unpack('<I', data[offset:offset+4])[0]
            offset += 4
            print(f'  Field {fn} (32-bit): {val}')
        elif wt == 1:  # 64-bit
            val = struct.unpack('<Q', data[offset:offset+8])[0]
            offset += 8
            print(f'  Field {fn} (64-bit): {val}')
    
    return partitions, minor_version

# Test parsing
path = r'D:\AndroidProjects\porting\waterlily-i001d-reconstruction\artifacts\Bliss-v19.6-I001D-UNOFFICIAL-vanilla-20260712.zip'
with zipfile.ZipFile(path) as z:
    payload_data = z.read('payload.bin')

magic = payload_data[0:4]
version = struct.unpack('>Q', payload_data[4:12])[0]
manifest_size = struct.unpack('>Q', payload_data[12:20])[0]
manifest = payload_data[20:20+manifest_size]

print(f'Payload version: {version}')
print(f'Manifest size: {manifest_size}')
print(f'Manifest first 20 bytes: {manifest[:20].hex()}')

# Check if first byte 0x00 is actually a protobuf tag or padding
# Try skipping leading zeros
for skip in range(0, 10):
    test = manifest[skip:]
    if test[0] != 0:
        print(f'First non-zero byte at offset {skip}: 0x{test[0]:02x}')
        # Decode this as a protobuf tag
        try:
            key, _ = decode_varint(test, 0)
            fn = key >> 3
            wt = key & 0x7
            print(f'  As protobuf tag: field={fn}, wire_type={wt}')
        except:
            pass
        break

# The issue is leading zeros. In proto3, zero-value fields are not serialized.
# But a leading 0x00 byte should not appear.
# Let me check: maybe the manifest uses groups which start with a different tag
# Groups have wire_type 3 (start group) and 4 (end group)
# But looking at bytes: 00 00 01 0b 
# Could these be 2-byte or 4-byte fields?
for byte_order in ['<', '>']:
    if len(manifest) >= 4:
        val32 = struct.unpack(byte_order + 'I', manifest[:4])[0]
        if 0 < val32 < 1000000:
            print(f'First 4 bytes as {byte_order} uint32: {val32}')

# Try: maybe the manifest has a 4-byte header for the number of partitions
num_partitions = struct.unpack('<I', manifest[:4])[0]
print(f'First 4 bytes as LE partition count: {num_partitions}')
num_partitions = struct.unpack('>I', manifest[:4])[0]
print(f'First 4 bytes as BE partition count: {num_partitions}')

# Let's just manually search for 'boot' in the payload and track around it
# Find the boot partition data by looking for the payload operations
# For REPLACE operations, data is at payload_start + data_offset
# First, parse all partition names from the manifest
print('\nSearching for partition names in manifest...')
idx = 0
while idx < len(manifest):
    # Look for known partition names
    for pname in [b'boot', b'system', b'vendor', b'vbmeta', b'dtbo', b'product']:
        pos = manifest.find(pname, idx)
        if pos >= 0 and pos < idx + 10:
            continue
        if pos >= 0:
            print(f'  Found [{pname.decode()}] at manifest offset {pos}')
            # Show context
            ctx = manifest[pos-5:pos+30]
            print(f'    Context: {ctx.hex()}')
            idx = pos + 1
    idx += 1
