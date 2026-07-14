#define LOG_TAG "GloveModeService"

#include "GloveMode.h"

#include <fstream>
#include <string>

namespace {

constexpr const char* kGloveModePath = "/proc/driver/glove";

}  // namespace

namespace aidl::vendor::lineage::touch {

ndk::ScopedAStatus GloveMode::getEnabled(bool* aidl_return) {
    std::ifstream file(kGloveModePath);
    std::string line;
    while (getline(file, line)) {
        if (line == "Glove Mode: On") {
            *aidl_return = true;
            return ndk::ScopedAStatus::ok();
        }
    }

    *aidl_return = false;
    return ndk::ScopedAStatus::ok();
}

ndk::ScopedAStatus GloveMode::setEnabled(bool enabled) {
    std::ofstream file(kGloveModePath);
    file << (enabled ? "1" : "0");
    return ndk::ScopedAStatus::ok();
}

}  // namespace aidl::vendor::lineage::touch
