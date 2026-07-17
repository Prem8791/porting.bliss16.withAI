package com.android.prodx.sdk;

interface IProdXProvider {
    String getProviderId();
    boolean isReady();
    int getHealth();

    byte[] executeCapability(String capabilityId, byte[] input);
    boolean cancelOperation(String operationId);
    byte[] getCapabilityManifest();
}
