"""Debug the protobuf parsing around the boot partition entry."""
import struct, zipfile, hashlib

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

# Load rebuilt payload
artifacts = r'D:\AndroidProjects\porting\waterlily-i001d-reconstruction\artifacts'
zip_path = artifacts + '\\Bliss-v19.6-I001D-UNOFFICIAL-vanilla-20260712.zip'
with zipfile.ZipFile(zip_path) as z:
    payload_data = z.read('payload.bin')

version = struct.unpack('>Q', payload_data[4:12])[0]
manifest_size = struct.unpack('>Q', payload_data[12:20])[0]
manifest = payload_data[20:20+manifest_size]
payload_start = 20 + manifest_size

print(f'Manifest size: {manifest_size}')
print(f'Payload start: {payload_start}')

# Find the boot partition entry in the manifest
# boot string is at manifest offset 25 (after 2 zero bytes + fields)
boot_idx = manifest.find(b'boot')
print(f'\nBoot string at manifest offset {boot_idx}')
print(f'Bytes before boot ({boot_idx-20}:{boot_idx}): {manifest[boot_idx-20:boot_idx].hex()}')
print(f'Bytes 0-{boot_idx}: {manifest[:boot_idx].hex()}')

# Try to parse protobuf from offset 2 (after leading zeros)
print('\n=== Parsing protobuf from offset 2 ===')
offset = 2
while offset < min(manifest_size, 100):
    key, offset = decode_varint(manifest, offset)
    fn = key >> 3
    wt = key & 0x7
    print(f'  offset={offset-1 if offset > 0 else 0}, key=0x{key:x}, field={fn}, wire_type={wt}', end='')
    
    if wt == 0:  # varint
        val, offset = decode_varint(manifest, offset)
        print(f', varint={val}')
    elif wt == 2:  # length-delimited
        length, offset = decode_varint(manifest, offset)
        # Peek at content
        peek = manifest[offset:offset+min(length, 40)]
        if all(32 <= b < 127 for b in peek) or b'\x00' in peek:
            text = peek.decode('utf-8', errors='replace')
            print(f', len={length}, text={text[:40]}')
        else:
            print(f', len={length}, hex={peek.hex()[:40]}')
        offset += length
    elif wt == 1:
        print(f', 64-bit')
        offset += 8
    elif wt == 5:
        print(f', 32-bit')
        offset += 4
    else:
        print(f', unknown wt={wt}')
        break
