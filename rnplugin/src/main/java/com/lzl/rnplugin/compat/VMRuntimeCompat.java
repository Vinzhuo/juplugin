package com.lzl.rnplugin.compat;

import android.os.Build;
import android.util.Log;

import com.lzl.rnplugin.Reflect;

/**
 * Created by liuzhuolin on 17/8/31.
 */

public class VMRuntimeCompat {
    private static final String TAG = VMRuntimeCompat.class.getSimpleName();

    public final static boolean is64Bit() {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                return false;
            }
            Reflect vmRuntime = Reflect.on("dalvik.system.VMRuntime");
            Reflect vmRuntimeObj = vmRuntime.call("getRuntime");
            Reflect is64Bit = vmRuntimeObj.call("is64Bit");
            Object result = is64Bit.get();
            if (result instanceof Boolean) {
                return ((Boolean) result);
            }
        } catch (Throwable e) {
            Log.w(TAG, "is64Bit", e);
        }
        return false;
    }
}
