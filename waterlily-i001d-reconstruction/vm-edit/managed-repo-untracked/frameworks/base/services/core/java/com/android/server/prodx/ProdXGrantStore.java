package com.android.server.prodx;

import android.app.prodx.ProdXGrant;
import android.util.ArrayMap;
import java.util.ArrayList;
import java.util.List;

public class ProdXGrantStore {
    private final ArrayMap<String, ProdXGrant> mGrants = new ArrayMap<>();

    public ProdXGrantStore() {}

    public List<ProdXGrant> getGrantsForUser(int userId) {
        List<ProdXGrant> result = new ArrayList<>();
        for (ProdXGrant grant : mGrants.values()) {
            if (grant.getUserId() == userId) result.add(grant);
        }
        return result;
    }

    public ProdXGrant getGrant(String grantId) {
        return mGrants.get(grantId);
    }

    public boolean revokeGrant(String grantId) {
        return mGrants.remove(grantId) != null;
    }

    public boolean addGrant(ProdXGrant grant) {
        if (mGrants.containsKey(grant.getGrantId())) return false;
        mGrants.put(grant.getGrantId(), grant);
        return true;
    }
}
