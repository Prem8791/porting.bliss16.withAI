package com.android.server.prodx;

import android.util.Slog;
import java.util.UUID;

public class ProdXConfirmationChallenge {
    private static final String TAG = "ProdXChallenge";
    private final String mChallengeId;
    private final long mCreatedAt;

    public ProdXConfirmationChallenge() {
        mChallengeId = UUID.randomUUID().toString();
        mCreatedAt = System.currentTimeMillis();
        Slog.i(TAG, "Challenge created: " + mChallengeId);
    }

    public String getChallengeId() { return mChallengeId; }
    public long getCreatedAt() { return mCreatedAt; }
    public boolean isExpired() { return System.currentTimeMillis() - mCreatedAt > 30000; }
}
