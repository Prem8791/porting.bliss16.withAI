package com.android.prodx.runtime;

interface IProdXAudit {
    boolean reserve(in byte[] transactionHash, int riskLevel);
    boolean appendPhase(in String reservationId, int phase, in byte[] phaseData);
    boolean appendOutcome(in String reservationId, in byte[] outcomeData);
    boolean cancelReservation(String reservationId);
    int getHealth();
    List<android.app.prodx.ProdXAuditRecord> queryHistory(int userId, long sinceTimestamp);
}
