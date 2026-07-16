package com.android.prodx.runtime;

import android.app.prodx.ProdXAuditRecord;

interface IProdXAudit {
    String reserve(in byte[] transactionHash, int riskLevel);
    boolean appendPhase(in String reservationId, int phase, in byte[] phaseData);
    boolean appendOutcome(in String reservationId, in byte[] outcomeData);
    boolean cancelReservation(in String reservationId);
    int getHealth();
    List<ProdXAuditRecord> queryHistory(int userId, long sinceTimestamp);
}
