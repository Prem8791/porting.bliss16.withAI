package com.android.server.prodx;

import android.app.prodx.IProdXAuthority;
import android.app.prodx.IProdXGrantAdmin;
import android.app.prodx.ProdXMode;
import android.app.prodx.ProdXHealth;
import android.app.prodx.ProdXExecutionContext;
import android.app.prodx.ProdXRegistryGeneration;
import android.app.prodx.ProdXRegistrySnapshot;
import android.app.prodx.ProdXCapabilityDescriptor;
import android.app.prodx.ProdXPolicyDecision;
import android.app.prodx.ProdXCapabilityRequest;
import android.app.prodx.ProdXExecutionAuthorization;
import android.app.prodx.ProdXGrant;
import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Slog;
import com.android.server.SystemService;
import com.android.server.pm.UserManagerInternal;
import java.io.PrintWriter;
import java.util.List;

public class ProdXAuthorityService extends SystemService {
    private static final String TAG = "ProdXAuthority";
    private ProdXMode mMode = ProdXMode.DISABLED;
    private boolean mOperational = false;
    private final ProdXContextBuilder mContextBuilder;
    private final ProdXGrantStore mGrantStore;

    private final IProdXAuthority.Stub mBinder = new IProdXAuthority.Stub() {
        @Override public ProdXMode getMode() { return mMode; }
        @Override public ProdXHealth getHealth() {
            return new ProdXHealth(mOperational, mOperational ? "operational" : "not_ready");
        }
        @Override public ProdXExecutionContext deriveCallerContext(String purpose) {
            return mContextBuilder.derive(getCallingUid(), purpose);
        }
        @Override public ProdXRegistryGeneration getRegistryGeneration() {
            return new ProdXRegistryGeneration(0, System.currentTimeMillis());
        }
        @Override public ProdXRegistrySnapshot getRegistrySnapshot(long generationId) {
            return null;
        }
        @Override public boolean resolveCapability(ProdXCapabilityDescriptor descriptor) {
            return false;
        }
        @Override public ProdXPolicyDecision evaluatePolicy(ProdXExecutionContext context, ProdXCapabilityRequest request) {
            return new ProdXPolicyDecision(false, "policy_engine_not_initialized");
        }
        @Override public ProdXExecutionAuthorization mintAuthorization(ProdXPolicyDecision decision, byte[] proof) {
            return null;
        }
        @Override public void emergencyDisable() { mMode = ProdXMode.DISABLED; mOperational = false; }
        @Override public boolean isEmergencyDisabled() { return mMode == ProdXMode.DISABLED; }
        @Override public void setMode(ProdXMode mode) { mMode = mode; }
    };

    private final IProdXGrantAdmin.Stub mGrantAdminBinder = new IProdXGrantAdmin.Stub() {
        @Override public List<ProdXGrant> getGrants(int userId) {
            return java.util.Collections.emptyList();
        }
        @Override public ProdXGrant getGrant(String grantId) { return null; }
        @Override public boolean revokeGrant(String grantId) { return false; }
        @Override public boolean suspendGrant(String grantId) { return false; }
        @Override public List<ProdXGrant> getGrantsByPackage(String packageName, int userId) {
            return java.util.Collections.emptyList();
        }
    };

    public ProdXAuthorityService(Context context) {
        super(context);
        mContextBuilder = new ProdXContextBuilder(context);
        mGrantStore = new ProdXGrantStore();
    }

    @Override
    public void onStart() {
        publishBinderService("prodx_authority", mBinder);
        publishBinderService("prodx_grant_admin", mGrantAdminBinder);
        Slog.i(TAG, "ProdX Authority published (not ready)");
    }

    @Override
    public void onBootPhase(int phase) {
        Slog.i(TAG, "Boot phase: " + phase);
        if (phase == PHASE_LOCK_SETTINGS_READY) {
            ProdXKillSwitch.initialize(getContext());
            ProdXTamperEvidentEpoch.initialize();
        } else if (phase == PHASE_SYSTEM_SERVICES_READY) {
            mOperational = true;
        }
    }

    @Override
    public void onUserStarting(TargetUser user) {
        Slog.i(TAG, "User starting: " + user);
    }

    @Override
    public void onUserUnlocking(TargetUser user) {
        Slog.i(TAG, "User unlocking: " + user);
    }

    @Override
    public void onUserUnlocked(TargetUser user) {
        Slog.i(TAG, "User unlocked (CE): " + user);
    }

    @Override
    public void onUserStopping(TargetUser user) {
        Slog.i(TAG, "User stopping: " + user);
    }
}
