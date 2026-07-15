package com.android.prodx.sdk;

interface IProdXProvider {
    String getProviderId();
    boolean isReady();
    int getHealth();
}
