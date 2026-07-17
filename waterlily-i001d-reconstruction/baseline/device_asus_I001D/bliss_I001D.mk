#
# Copyright (C) 2022 The LineageOS Project
#
# SPDX-License-Identifier: Apache-2.0
#

# Inherit from those products. Most specific first.
$(call inherit-product, $(SRC_TARGET_DIR)/product/core_64_bit.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/full_base_telephony.mk)

# Inherit some common Bliss stuff.
$(call inherit-product, vendor/bliss/config/common_full_phone.mk)

# Inherit from I001D device
$(call inherit-product, device/asus/I001D/device.mk)

# Bootanimation
TARGET_BOOT_ANIMATION_RES := 1080

# UDFPS Animations
EXTRA_UDFPS_ANIMATIONS := true

PRODUCT_DEVICE := I001D
PRODUCT_NAME := bliss_I001D
PRODUCT_BRAND := asus
PRODUCT_MODEL := ASUS_I001D
PRODUCT_MANUFACTURER := asus

PRODUCT_GMS_CLIENTID_BASE := android-asus

PRODUCT_BUILD_PROP_OVERRIDES += \
    PRIVATE_BUILD_DESC="WW_I001D-user 11 RKQ1.200710.002 18.0210.2201.215-0 release-keys"

# Build info
PRODUCT_BUILD_PROP_OVERRIDES += \
    TARGET_DEVICE=ZS660KL \
    PRODUCT_NAME=WW_I001D
    BUILD_FINGERPRINT := asus/WW_I001D/ASUS_I001_1:11/RKQ1.200710.002/18.0210.2201.215-0:user/release-keys
