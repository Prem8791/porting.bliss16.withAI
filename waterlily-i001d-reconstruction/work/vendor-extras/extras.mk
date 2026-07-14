# Bliss Extra Packages
PRODUCT_PACKAGES += \
    AboutBliss

# Neuron's bundled Vulkan backend aborts on the Adreno 640 in ASUS I001D.
ifneq ($(TARGET_PRODUCT),bliss_I001D)
PRODUCT_PACKAGES += \
    Neuron
endif
