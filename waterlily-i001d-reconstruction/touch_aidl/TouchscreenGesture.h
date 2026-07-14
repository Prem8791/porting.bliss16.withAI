#pragma once

#include <aidl/vendor/lineage/touch/BnTouchscreenGesture.h>

#include <map>
#include <string>

namespace aidl::vendor::lineage::touch {

class TouchscreenGesture : public BnTouchscreenGesture {
  public:
    struct GestureInfo {
        int32_t keycode;
        std::string name;
        std::string path;
    };

    ndk::ScopedAStatus getSupportedGestures(std::vector<Gesture>* aidl_return) override;
    ndk::ScopedAStatus setGestureEnabled(const Gesture& gesture, bool enabled) override;

  private:
    static const std::map<int32_t, GestureInfo> kGestureInfoMap;
};

}  // namespace aidl::vendor::lineage::touch
