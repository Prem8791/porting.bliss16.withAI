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
    private static final String PERMISSION_AUTHORITY = "android.permission.PRODX_AUTHORITY";
    private static final String PERMISSION_BROKER = "android.permission.PRODX_BROKER";
    private static final String PERMISSION_ADMIN = "android.permission.PRODX_ADMIN";
    private ProdXMode mMode = ProdXMode.DISABLED;
    private boolean mOperational = false;
    private final ProdXContextBuilder mContextBuilder;
    private final ProdXGrantStore mGrantStore;

    private final IProdXAuthority.Stub mBinder = new IProdXAuthority.Stub() {
        @Override public ProdXMode getMode() {
            enforcePermission(PERMISSION_AUTHORITY, "get ProdX mode");
            return mMode;
        }
        @Override public ProdXHealth getHealth() {
            enforcePermission(PERMISSION_AUTHORITY, "get ProdX health");
            return new ProdXHealth(mOperational, mOperational ? "operational" : "not_ready");
        }
        @Override public ProdXExecutionContext deriveCallerContext(String purpose) {
            enforcePermission(PERMISSION_AUTHORITY, "derive ProdX caller context");
            return mContextBuilder.derive(getCallingUid(), purpose);
        }
        @Override public ProdXRegistryGeneration getRegistryGeneration() {
            enforcePermission(PERMISSION_AUTHORITY, "get ProdX registry generation");
            return new ProdXRegistryGeneration(0, System.currentTimeMillis());
        }
        @Override public ProdXRegistrySnapshot getRegistrySnapshot(long generationId) {
            enforcePermission(PERMISSION_AUTHORITY, "get ProdX registry snapshot");
            return null;
        }
        @Override public boolean resolveCapability(ProdXCapabilityDescriptor descriptor) {
            enforcePermission(PERMISSION_AUTHORITY, "resolve ProdX capability");
            return false;
        }
        @Override public ProdXPolicyDecision evaluatePolicy(ProdXExecutionContext context, ProdXCapabilityRequest request) {
            enforcePermission(PERMISSION_AUTHORITY, "evaluate ProdX policy");
            return new ProdXPolicyDecision(false, "policy_engine_not_initialized");
        }
        @Override public ProdXExecutionAuthorization mintAuthorization(ProdXPolicyDecision decision, byte[] proof) {
            enforcePermission(PERMISSION_BROKER, "mint ProdX authorization");
            return null;
        }
        @Override public void emergencyDisable() {
            enforcePermission(PERMISSION_ADMIN, "emergency-disable ProdX");
            mMode = ProdXMode.DISABLED;
            mOperational = false;
        }
        @Override public boolean isEmergencyDisabled() {
            enforcePermission(PERMISSION_AUTHORITY, "read ProdX emergency state");
            return mMode == ProdXMode.DISABLED;
        }
        @Override public void setMode(ProdXMode mode) {
            enforcePermission(PERMISSION_ADMIN, "set ProdX mode");
            mMode = mode;
        }
    };

    private final IProdXGrantAdmin.Stub mGrantAdminBinder = new IProdXGrantAdmin.Stub() {
        @Override public List<ProdXGrant> getGrants(int userId) {
            enforcePermission(PERMISSION_ADMIN, "list ProdX grants");
            return java.util.Collections.emptyList();
        }
        @Override public ProdXGrant getGrant(String grantId) {
            enforcePermission(PERMISSION_ADMIN, "get ProdX grant");
            return null;
        }
        @Override public boolean revokeGrant(String grantId) {
            enforcePermission(PERMISSION_ADMIN, "revoke ProdX grant");
            return false;
        }
        @Override public boolean suspendGrant(String grantId) {
            enforcePermission(PERMISSION_ADMIN, "suspend ProdX grant");
            return false;
        }
        @Override public List<ProdXGrant> getGrantsByPackage(String packageName, int userId) {
            enforcePermission(PERMISSION_ADMIN, "list package ProdX grants");
            return java.util.Collections.emptyList();
        }
    };

    private void enforcePermission(String permission, String operation) {
        getContext().enforceCallingOrSelfPermission(
                permission, "Permission required to " + operation);
    }

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
