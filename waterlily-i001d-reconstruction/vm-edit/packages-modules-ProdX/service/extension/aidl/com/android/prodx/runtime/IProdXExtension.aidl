package com.android.prodx.runtime;

interface IProdXExtension {
    byte[] validateCandidate(in byte[] sealedCandidate);
    void cancelValidation(String candidateId);
    int getHealth();
}
