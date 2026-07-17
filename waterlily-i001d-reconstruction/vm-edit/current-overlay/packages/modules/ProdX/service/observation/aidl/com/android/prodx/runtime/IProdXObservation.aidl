package com.android.prodx.runtime;

import android.app.prodx.ProdXSubscriptionLease;

interface IProdXObservation {
    android.app.prodx.ProdXSubscriptionLease createLease(in String spec);
    boolean revokeLease(String leaseId);
    boolean registerSource(in android.os.IBinder source);
    boolean unregisterSource(String sourceId);
    int getHealth();

    boolean consumeObservation(in String leaseId, in byte[] observationData);
    boolean reportIncident(in String incidentJson);
    String[] getIncidentTimeline(long sinceMs);
    boolean acknowledgeIncident(String incidentId);
}
