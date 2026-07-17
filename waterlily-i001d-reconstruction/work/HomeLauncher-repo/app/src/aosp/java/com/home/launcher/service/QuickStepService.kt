package com.home.launcher.service

import android.app.Service
import android.content.Intent
import android.graphics.Rect
import android.graphics.Region
import android.os.Bundle
import android.os.IBinder
import android.view.MotionEvent
import android.view.SurfaceControl
import android.util.Log
import com.android.systemui.shared.recents.IOverviewProxy
import com.android.systemui.shared.recents.ISystemUiProxy

class QuickStepService : Service() {
    override fun onBind(intent: Intent): IBinder {
        Log.i(TAG, "QuickStep service bound: $intent")
        return binder
    }

    private val binder = object : IOverviewProxy.Stub() {
        override fun onActiveNavBarRegionChanges(activeRegion: Region?) {}
        override fun onInitialize(params: Bundle?) {}
        override fun onOverviewToggle() {}
        override fun onOverviewShown(triggeredFromAltTab: Boolean) {}
        override fun onOverviewHidden(triggeredFromAltTab: Boolean, triggeredFromHomeKey: Boolean) {}
        override fun onAssistantAvailable(available: Boolean, longPressHomeEnabled: Boolean) {}
        override fun onAssistantVisibilityChanged(visibility: Float) {}
        override fun onSystemUiStateChanged(stateFlags: Int) {}
        override fun onRotationProposal(rotation: Int, isValid: Boolean) {}
        override fun disable(displayId: Int, state1: Int, state2: Int, animate: Boolean) {}
        override fun onSystemBarAttributesChanged(displayId: Int, behavior: Int) {}
        override fun onScreenTurnedOn() {}
        override fun onNavButtonsDarkIntensityChanged(darkIntensity: Float) {}
        override fun onScreenTurningOn() {}
        override fun onScreenTurningOff() {}
        override fun enterStageSplitFromRunningApp(leftOrTop: Boolean) {}
        override fun onNavigationBarSurface(surface: SurfaceControl?) {}
        override fun onTaskbarToggled() {}
    }

    companion object {
        private const val TAG = "QuickStepService"
    }
}