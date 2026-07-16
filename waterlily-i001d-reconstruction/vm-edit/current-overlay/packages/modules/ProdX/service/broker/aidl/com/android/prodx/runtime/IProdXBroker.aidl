package com.android.prodx.runtime;

interface IProdXBroker {
    String submitTransaction(in android.app.prodx.ProdXCapabilityRequest request);
    void cancelTransaction(String transactionId);
    int getTransactionStatus(String transactionId);
}
