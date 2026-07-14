#pragma once

#include <aidl/vendor/lineage/touch/BnGloveMode.h>

namespace aidl::vendor::lineage::touch {

class GloveMode : public BnGloveMode {
  public:
    ndk::ScopedAStatus getEnabled(bool* aidl_return) override;
    ndk::ScopedAStatus setEnabled(bool enabled) override;
};

}  // namespace aidl::vendor::lineage::touch
