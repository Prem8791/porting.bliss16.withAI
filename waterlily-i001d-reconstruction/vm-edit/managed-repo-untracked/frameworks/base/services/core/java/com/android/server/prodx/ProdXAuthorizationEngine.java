package com.android.server.prodx;

import android.app.prodx.ProdXExecutionAuthorization;
import android.app.prodx.ProdXPolicyDecision;
import android.util.Slog;
import java.security.KeyStore;

public class ProdXAuthorizationEngine {
    private static final String TAG = "ProdXAuthorizationEngine";
    private KeyStore mKeyStore;

    public ProdXAuthorizationEngine() {
        try {
            mKeyStore = KeyStore.getInstance("AndroidKeyStore");
            mKeyStore.load(null);
            Slog.i(TAG, "Authorization engine initialized");
        } catch (Exception e) {
            Slog.e(TAG, "Failed to initialize AndroidKeyStore", e);
        }
    }

    public ProdXExecutionAuthorization mintAuthorization(ProdXPolicyDecision decision, byte[] proof) {
        return null;
    }

    public boolean verifyAuthorization(byte[] token) {
        return false;
    }
}
