package com.android.server.prodx;

import android.app.prodx.IProdXGrantAdmin;
import android.app.prodx.ProdXGrant;
import android.content.Context;
import android.os.Binder;
import android.util.Slog;
import java.util.List;

public class ProdXGrantAdminService extends IProdXGrantAdmin.Stub {
    private static final String TAG = "ProdXGrantAdmin";
    private final Context mContext;
    private final ProdXGrantStore mGrantStore;

    public ProdXGrantAdminService(Context context, ProdXGrantStore grantStore) {
        mContext = context;
        mGrantStore = grantStore;
    }

    @Override
    public List<ProdXGrant> getGrants(int userId) {
        enforceAdminPermission();
        return mGrantStore.getGrantsForUser(userId);
    }

    @Override
    public ProdXGrant getGrant(String grantId) {
        enforceAdminPermission();
        return mGrantStore.getGrant(grantId);
    }

    @Override
    public boolean revokeGrant(String grantId) {
        enforceAdminPermission();
        return mGrantStore.revokeGrant(grantId);
    }

    @Override
    public boolean suspendGrant(String grantId) {
        enforceAdminPermission();
        return mGrantStore.revokeGrant(grantId);
    }

    @Override
    public List<ProdXGrant> getGrantsByPackage(String packageName, int userId) {
        enforceAdminPermission();
        return mGrantStore.getGrantsForUser(userId);
    }

    private void enforceAdminPermission() {
        mContext.enforceCallingPermission(
            android.Manifest.permission.MANAGE_USERS,  // placeholder; PRODX_ADMIN will replace this
            "ProdXGrantAdmin requires PRODX_ADMIN");
    }
}
