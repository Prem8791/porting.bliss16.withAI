"""Extract boot.img from OTA payload by scanning for ANDROID! magic."""
import struct, zipfile, hashlib

def extract_boot(zip_path, label, out_dir):
    import os
    with zipfile.ZipFile(zip_path) as z:
        payload_data = z.read('payload.bin')
    
    # Find ANDROID! magic in payload
    target = b'ANDROID!'
    pos = 0
    found = None
    while True:
        pos = payload_data.find(target, pos)
        if pos < 0:
            break
        # Validate: check page size at offset 36
        ps = struct.unpack('<I', payload_data[pos+36:pos+40])[0]
        if ps in (4096, 2048, 1024):
            ks = struct.unpack('<I', payload_data[pos+8:pos+12])[0]
            rs = struct.unpack('<I', payload_data[pos+16:pos+20])[0]
            cmdline = payload_data[pos+64:pos+576].rstrip(b'\x00').decode('ascii', errors='replace')
            has_devices = 'androidboot.boot_devices' in cmdline
            print(f'{label}: ANDROID! at payload offset {pos} (0x{pos:x})')
            print(f'  Kernel sz={ks}, Ramdisk sz={rs}, Page sz={ps}')
            print(f'  boot_devices: {has_devices}')
            print(f'  Cmdline: {cmdline[:120]}...')
            
            # Extract full image (size might be padded to page boundary)
            if ks > 0 and rs > 0:
                # Total size = header(ps) + kernel(padded) + ramdisk(padded)
                k_pad = (ps - (ks % ps)) % ps
                r_pad = (ps - (rs % ps)) % ps
                total = ps + ks + k_pad + rs + r_pad
                boot_data = payload_data[pos:pos+total]
                sha = hashlib.sha256(boot_data).hexdigest()
                print(f'  Extracted: {len(boot_data)} bytes, SHA256={sha}')
                found = boot_data
                break
        pos += 1
    
    if found:
        out_path = os.path.join(out_dir, f'boot-{label.lower()}.img')
        with open(out_path, 'wb') as f:
            f.write(found)
        return out_path
    return None

import os, shutil
temp_base = os.path.join(os.environ.get('TEMP', r'C:\Users\home\AppData\Local\Temp'), 'opencode', 'boot_analysis_fresh')
if os.path.exists(temp_base):
    shutil.rmtree(temp_base)
os.makedirs(temp_base)

artifacts = r'D:\AndroidProjects\porting\waterlily-i001d-reconstruction\artifacts'
official_zip = os.path.join(artifacts, 'Bliss-v19.6-I001D-OFFICIAL-gapps-20260616.zip')
rebuilt_zip = os.path.join(artifacts, 'Bliss-v19.6-I001D-UNOFFICIAL-vanilla-20260712.zip')

official = extract_boot(official_zip, 'OFFICIAL', temp_base)
rebuilt = extract_boot(rebuilt_zip, 'REBUILT', temp_base)

print(f'\nExtracted to: {temp_base}')
print(f'Official: {official}')
print(f'Rebuilt: {rebuilt}')
