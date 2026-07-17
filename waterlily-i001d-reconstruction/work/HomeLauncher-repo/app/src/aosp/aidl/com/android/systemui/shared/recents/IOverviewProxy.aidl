package com.android.systemui.shared.recents;

import android.graphics.Rect;
import android.graphics.Region;
import android.os.Bundle;
import android.view.SurfaceControl;
import com.android.systemui.shared.recents.ISystemUiProxy;

oneway interface IOverviewProxy {
    void onActiveNavBarRegionChanges(in Region activeRegion) = 11;
    void onInitialize(in Bundle params) = 12;
    void onOverviewToggle() = 6;
    void onOverviewShown(boolean triggeredFromAltTab) = 7;
    void onOverviewHidden(boolean triggeredFromAltTab, boolean triggeredFromHomeKey) = 8;
    void onAssistantAvailable(boolean available, boolean longPressHomeEnabled) = 13;
    void onAssistantVisibilityChanged(float visibility) = 14;
    void onSystemUiStateChanged(int stateFlags) = 16;
    void onRotationProposal(int rotation, boolean isValid) = 18;
    void disable(int displayId, int state1, int state2, boolean animate) = 19;
    void onSystemBarAttributesChanged(int displayId, int behavior) = 20;
    void onScreenTurnedOn() = 21;
    void onNavButtonsDarkIntensityChanged(float darkIntensity) = 22;
    void onScreenTurningOn() = 23;
    void onScreenTurningOff() = 24;
    void enterStageSplitFromRunningApp(boolean leftOrTop) = 25;
    void onNavigationBarSurface(in SurfaceControl surface) = 26;
    void onTaskbarToggled() = 27;
}