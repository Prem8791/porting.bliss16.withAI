package com.android.server.prodx;

import android.app.prodx.ProdXCapabilityDescriptor;
import android.app.prodx.ProdXRegistryGeneration;
import android.app.prodx.ProdXRegistrySnapshot;
import android.util.Slog;
import java.util.Collections;

public class ProdXRegistry {
    private static final String TAG = "ProdXRegistry";
    private volatile long mGenerationId = 0;

    public ProdXRegistry() {
        Slog.i(TAG, "Registry initialized (empty)");
    }

    public ProdXRegistryGeneration getCurrentGeneration() {
        return new ProdXRegistryGeneration(mGenerationId, System.currentTimeMillis());
    }

    public boolean resolveCapability(ProdXCapabilityDescriptor descriptor) {
        return false;
    }

    public ProdXRegistrySnapshot getSnapshot(long generationId) {
        return null;
    }

    public void incrementGeneration() {
        mGenerationId++;
        Slog.i(TAG, "Generation incremented to " + mGenerationId);
    }
}
