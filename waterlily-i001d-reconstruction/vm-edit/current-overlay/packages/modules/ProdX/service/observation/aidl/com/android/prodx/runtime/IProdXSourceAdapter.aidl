package com.android.prodx.runtime;

interface IProdXSourceAdapter {
    String getSourceId();
    String getSourceType();
    boolean isActive();

    byte[] getLatestEvent();
    byte[] getEventStream(long afterSequence);
}
