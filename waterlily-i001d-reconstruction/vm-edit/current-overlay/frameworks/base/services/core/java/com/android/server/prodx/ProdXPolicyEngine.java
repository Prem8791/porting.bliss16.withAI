package com.android.server.prodx;

import android.app.prodx.ProdXPolicyDecision;
import android.app.prodx.ProdXExecutionContext;
import android.app.prodx.ProdXCapabilityRequest;
import android.app.prodx.ProdXCapabilityDescriptor;
import android.util.Slog;
import java.util.List;

public class ProdXPolicyEngine {
    private static final String TAG = "ProdXPolicyEngine";
    private static final int POLICY_EPOCH = 0;

    private final ProdXGrantStore mGrantStore;
    private final ProdXRegistry mRegistry;

    public ProdXPolicyEngine(ProdXGrantStore grantStore, ProdXRegistry registry) {
        mGrantStore = grantStore;
        mRegistry = registry;
        Slog.i(TAG, "Policy engine initialized (shadow-only)");
    }

    public ProdXPolicyDecision evaluate(ProdXExecutionContext context, ProdXCapabilityRequest request) {
        ProdXCapabilityDescriptor descriptor = request.getDescriptor();
        int userId = context.getUserId();
        String packageName = context.getPackageName();

        if (ProdXKillSwitch.isDisabled()) {
            return deny("kill_switch_active");
        }

        if (!mRegistry.resolveCapability(descriptor)) {
            return deny("capability_not_resolved");
        }

        if (!mGrantStore.hasActiveGrant(packageName, descriptor.getCapabilityId(), userId)) {
            return deny("no_active_grant");
        }

        return new ProdXPolicyDecision(true, "allowed_shadow");
    }

    public int getCurrentPolicyEpoch() {
        return POLICY_EPOCH;
    }

    private static ProdXPolicyDecision deny(String reason) {
        return new ProdXPolicyDecision(false, reason);
    }
}
