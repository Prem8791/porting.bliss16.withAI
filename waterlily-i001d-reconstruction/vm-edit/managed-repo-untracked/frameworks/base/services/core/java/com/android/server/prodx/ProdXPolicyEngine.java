package com.android.server.prodx;

import android.app.prodx.ProdXPolicyDecision;
import android.app.prodx.ProdXExecutionContext;
import android.app.prodx.ProdXCapabilityRequest;
import android.util.Slog;

public class ProdXPolicyEngine {
    private static final String TAG = "ProdXPolicyEngine";

    public ProdXPolicyEngine() {
        Slog.i(TAG, "Policy engine initialized (shadow-only)");
    }

    public ProdXPolicyDecision evaluate(ProdXExecutionContext context, ProdXCapabilityRequest request) {
        return new ProdXPolicyDecision(false, "not_implemented");
    }
}
