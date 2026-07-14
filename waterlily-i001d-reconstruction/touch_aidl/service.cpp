#define LOG_TAG "vendor.lineage.touch-service.asus_msmnile"

#include "GloveMode.h"
#include "TouchscreenGesture.h"

#include <android-base/logging.h>
#include <android/binder_manager.h>
#include <android/binder_process.h>

using aidl::vendor::lineage::touch::GloveMode;
using aidl::vendor::lineage::touch::TouchscreenGesture;

int main() {
    ABinderProcess_setThreadPoolMaxThreadCount(0);

    std::shared_ptr<GloveMode> gloveMode = ndk::SharedRefBase::make<GloveMode>();
    std::shared_ptr<TouchscreenGesture> touchscreenGesture =
            ndk::SharedRefBase::make<TouchscreenGesture>();

    const std::string gloveModeInstance = std::string(GloveMode::descriptor) + "/default";
    binder_status_t status =
            AServiceManager_addService(gloveMode->asBinder().get(), gloveModeInstance.c_str());
    CHECK_EQ(status, STATUS_OK) << "Failed to add service " << gloveModeInstance << " " << status;

    const std::string touchscreenGestureInstance =
            std::string(TouchscreenGesture::descriptor) + "/default";
    status = AServiceManager_addService(touchscreenGesture->asBinder().get(),
                                        touchscreenGestureInstance.c_str());
    CHECK_EQ(status, STATUS_OK) << "Failed to add service " << touchscreenGestureInstance << " "
                                << status;

    ABinderProcess_joinThreadPool();
    return EXIT_FAILURE;
}
