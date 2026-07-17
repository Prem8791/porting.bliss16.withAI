package com.android.systemui.shared.recents;

import android.graphics.Bitmap;
import android.graphics.Insets;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import com.android.internal.util.ScreenshotRequest;

interface ISystemUiProxy {
    oneway void startScreenPinning(int taskId) = 1;
    oneway void onOverviewShown(boolean fromHome) = 6;
    oneway void onStatusBarMotionEvent(in MotionEvent event) = 9;
    oneway void onAssistantProgress(float progress) = 12;
    oneway void onAssistantGestureCompletion(float velocity) = 18;
    oneway void startAssistant(in Bundle bundle) = 13;
    oneway void notifyAccessibilityButtonClicked(int displayId) = 15;
    oneway void notifyAccessibilityButtonLongClicked() = 16;
    oneway void stopScreenPinning() = 17;
    oneway void notifyPrioritizedRotation(int rotation) = 25;
    oneway void expandNotificationPanel() = 29;
    oneway void onBackPressed() = 44;
    oneway void setHomeRotationEnabled(boolean enabled) = 45;
    oneway void notifyTaskbarStatus(boolean visible, boolean stashed) = 47;
    oneway void notifyTaskbarAutohideSuspend(boolean suspend) = 48;
    oneway void onImeSwitcherPressed() = 49;
    oneway void toggleNotificationPanel() = 50;
    oneway void takeScreenshot(in ScreenshotRequest request) = 51;
}