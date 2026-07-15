package com.android.prodx.runtime;

interface IProdXObservation {
    android.app.prodx.ProdXSubscriptionLease createLease(in String spec);
    boolean revokeLease(String leaseId);
    boolean registerSource(in android.os.IBinder source);
    boolean unregisterSource(String sourceId);
    int getHealth();
}
