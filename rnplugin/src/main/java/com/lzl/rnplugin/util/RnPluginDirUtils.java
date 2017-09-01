package com.lzl.rnplugin.util;

import android.content.Context;

import java.io.File;

/**
 * Created by liuzhuolin on 17/8/31.
 */

final public class RnPluginDirUtils {

    private static File sBaseDir = null;

    private static void init(Context context) {
        if (sBaseDir == null) {
            sBaseDir = new File(context.getCacheDir().getParentFile(), "JuPlugin");
            enforceDirExists(sBaseDir);
        }
    }

    public static String getDexCachedDir(Context context, String pluginPackageName) {
        return enforceDirExists(new File(makePluginBaseDir(context, pluginPackageName), "dex-cache"));
    }

    public static String getNativeLibraryDir(Context context, String pluginPackageName) {
        return enforceDirExists(new File(makePluginBaseDir(context, pluginPackageName), "lib"));
    }

    public static String getRnExternalNativeLibDir(Context context) {
        return enforceDirExists(new File(context.getApplicationInfo().dataDir + File.separatorChar + "lib-main"));
    }

    private static String enforceDirExists(File file) {
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getPath();
    }

    private static String makePluginBaseDir(Context context, String pluginInfoPackageName) {
        init(context);
        return enforceDirExists(new File(sBaseDir, pluginInfoPackageName));
    }
}
