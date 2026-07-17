package com.home.hiddenapiprobe;

import android.app.Activity;
import android.app.ActivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ProbeActivity extends Activity {
    private static final String TAG = "HiddenApiProbe";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        runProbe();
        finish();
    }

    private static void runProbe() {
        Object iAtm = null;
        probeMethod("ActivityTaskManager.getService", "android.app.ActivityTaskManager", "getService", new Class<?>[0], null);
        try {
            Class<?> atm = Class.forName("android.app.ActivityTaskManager");
            Method getService = atm.getDeclaredMethod("getService");
            getService.setAccessible(true);
            iAtm = getService.invoke(null);
            Log.i(TAG, "INVOKE OK ActivityTaskManager.getService -> " + className(iAtm));
        } catch (Throwable t) {
            Log.i(TAG, "INVOKE FAIL ActivityTaskManager.getService -> " + shortFailure(t));
        }

        Object binder = null;
        try {
            Class<?> sm = Class.forName("android.os.ServiceManager");
            Method getService = sm.getDeclaredMethod("getService", String.class);
            getService.setAccessible(true);
            binder = getService.invoke(null, "activity_task");
            Log.i(TAG, "INVOKE OK ServiceManager.getService -> " + className(binder));
        } catch (Throwable t) {
            Log.i(TAG, "INVOKE FAIL ServiceManager.getService -> " + shortFailure(t));
        }

        try {
            Class<?> stub = Class.forName("android.app.IActivityTaskManager$Stub");
            Method asInterface = stub.getDeclaredMethod("asInterface", IBinder.class);
            asInterface.setAccessible(true);
            Object fromBinder = asInterface.invoke(null, binder);
            Log.i(TAG, "INVOKE OK IActivityTaskManager.Stub.asInterface -> " + className(fromBinder));
            if (iAtm == null) iAtm = fromBinder;
        } catch (Throwable t) {
            Log.i(TAG, "INVOKE FAIL IActivityTaskManager.Stub.asInterface -> " + shortFailure(t));
        }

        probeMethod("ActivityTaskManager.getInstance", "android.app.ActivityTaskManager", "getInstance", new Class<?>[0], null);
        probeIAtm(iAtm, "getRecentTasks", int.class, int.class, int.class);
        probeIAtm(iAtm, "registerTaskStackListener", classForName("android.app.ITaskStackListener"));
        probeIAtm(iAtm, "unregisterTaskStackListener", classForName("android.app.ITaskStackListener"));
        probeIAtm(iAtm, "removeTask", int.class);
        probeIAtm(iAtm, "removeAllVisibleRecentTasks");
        probeIAtm(iAtm, "startActivityFromRecents", int.class, String.class);
        probeIAtm(iAtm, "getTaskSnapshot", int.class, boolean.class);

        probeField("TaskInfo.taskId", "android.app.TaskInfo", "taskId");
        probeField("TaskInfo.baseIntent", "android.app.TaskInfo", "baseIntent");
        probeField("TaskInfo.userId", "android.app.TaskInfo", "userId");
        probeField("TaskInfo.taskDescription", "android.app.TaskInfo", "taskDescription");
        probeMethod("TaskDescription.getLabel", "android.app.ActivityManager$TaskDescription", "getLabel", new Class<?>[0], null);
        probeMethod("TaskSnapshot.getHardwareBuffer", "android.window.TaskSnapshot", "getHardwareBuffer", new Class<?>[0], null);
        probeMethod("TaskSnapshot.getSnapshot", "android.window.TaskSnapshot", "getSnapshot", new Class<?>[0], null);
        probeMethod("TaskSnapshot.getOrientation", "android.window.TaskSnapshot", "getOrientation", new Class<?>[0], null);

        try {
            ActivityManager am = (ActivityManager) null;
            Method m = Class.forName("android.app.ActivityManager").getDeclaredMethod("forceStopPackage", String.class);
            m.setAccessible(true);
            Log.i(TAG, "LOOKUP OK ActivityManager.forceStopPackage -> " + m);
        } catch (Throwable t) {
            Log.i(TAG, "LOOKUP FAIL ActivityManager.forceStopPackage -> " + shortFailure(t));
        }
    }

    private static void probeIAtm(Object iAtm, String name, Class<?>... args) {
        if (iAtm == null) {
            Log.i(TAG, "LOOKUP SKIP IActivityTaskManager." + name + " -> no service proxy");
            return;
        }
        try {
            Method m = iAtm.getClass().getDeclaredMethod(name, args);
            m.setAccessible(true);
            Log.i(TAG, "LOOKUP OK IActivityTaskManager." + name + " -> " + m);
        } catch (Throwable t) {
            Log.i(TAG, "LOOKUP FAIL IActivityTaskManager." + name + " -> " + shortFailure(t));
        }
    }

    private static void probeMethod(String label, String className, String methodName, Class<?>[] args, Object receiver) {
        try {
            Class<?> c = Class.forName(className);
            Method m = c.getDeclaredMethod(methodName, args);
            m.setAccessible(true);
            Log.i(TAG, "LOOKUP OK " + label + " -> " + m);
        } catch (Throwable t) {
            Log.i(TAG, "LOOKUP FAIL " + label + " -> " + shortFailure(t));
        }
    }

    private static void probeField(String label, String className, String fieldName) {
        try {
            Class<?> c = Class.forName(className);
            Field f = c.getField(fieldName);
            f.setAccessible(true);
            Log.i(TAG, "LOOKUP OK " + label + " -> " + f);
        } catch (Throwable t) {
            Log.i(TAG, "LOOKUP FAIL " + label + " -> " + shortFailure(t));
        }
    }

    private static Class<?> classForName(String className) {
        try {
            return Class.forName(className);
        } catch (Throwable t) {
            Log.i(TAG, "CLASS FAIL " + className + " -> " + shortFailure(t));
            return Object.class;
        }
    }

    private static String className(Object o) {
        return o == null ? "null" : o.getClass().getName();
    }

    private static String shortFailure(Throwable t) {
        Throwable cause = t.getCause() == null ? t : t.getCause();
        String message = cause.getMessage();
        return cause.getClass().getSimpleName() + (message == null ? "" : ": " + message);
    }
}
