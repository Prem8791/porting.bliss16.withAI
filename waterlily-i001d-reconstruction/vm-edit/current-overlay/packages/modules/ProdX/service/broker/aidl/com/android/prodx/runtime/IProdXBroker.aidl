package com.android.prodx.runtime;

import android.app.prodx.ProdXCapabilityRequest;

interface IProdXBroker {
    String submitTransaction(in ProdXCapabilityRequest request);
    void cancelTransaction(String transactionId);
    int getTransactionStatus(String transactionId);
    byte[] getTransactionResult(String transactionId);
    String[] queryTransactions(int maxResults);
    String getTransactionPhase(String transactionId);
    long getTransactionTimestamp(String transactionId);
    boolean hasTransaction(String transactionId);
}
