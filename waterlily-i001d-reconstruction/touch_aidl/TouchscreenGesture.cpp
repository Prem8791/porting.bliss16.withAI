#define LOG_TAG "TouchscreenGestureService"

#include "TouchscreenGesture.h"

#include <android-base/logging.h>

#include <fstream>

namespace aidl::vendor::lineage::touch {

const std::map<int32_t, TouchscreenGesture::GestureInfo> TouchscreenGesture::kGestureInfoMap = {
        {0, {17, "Letter W", "/sys/devices/platform/goodix_ts.0/gesture/gesture_w"}},
        {1, {31, "Letter S", "/sys/devices/platform/goodix_ts.0/gesture/gesture_s"}},
        {2, {18, "Letter e", "/sys/devices/platform/goodix_ts.0/gesture/gesture_e"}},
        {3, {46, "Letter C", "/sys/devices/platform/goodix_ts.0/gesture/gesture_c"}},
        {4, {44, "Letter Z", "/sys/devices/platform/goodix_ts.0/gesture/gesture_z"}},
        {5, {47, "Letter V", "/sys/devices/platform/goodix_ts.0/gesture/gesture_v"}},
        {6, {103, "SwipeUp Gesture", "/sys/devices/platform/goodix_ts.0/gesture/swipeup"}},
};

ndk::ScopedAStatus TouchscreenGesture::getSupportedGestures(std::vector<Gesture>* aidl_return) {
    aidl_return->clear();
    for (const auto& [id, info] : kGestureInfoMap) {
        Gesture gesture;
        gesture.id = id;
        gesture.name = info.name;
        gesture.keycode = info.keycode;
        aidl_return->push_back(gesture);
    }
    return ndk::ScopedAStatus::ok();
}

ndk::ScopedAStatus TouchscreenGesture::setGestureEnabled(const Gesture& gesture, bool enabled) {
    const auto entry = kGestureInfoMap.find(gesture.id);
    if (entry == kGestureInfoMap.end()) {
        return ndk::ScopedAStatus::fromExceptionCode(EX_ILLEGAL_ARGUMENT);
    }

    std::ofstream file(entry->second.path);
    file << (enabled ? "1" : "0");
    LOG(DEBUG) << "Wrote file " << entry->second.path << " fail " << file.fail();
    return ndk::ScopedAStatus::ok();
}

}  // namespace aidl::vendor::lineage::touch
