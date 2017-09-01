package com.lzl.rnplugin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liuzhuolin on 17/8/30.
 */

final public class RnPluginManager {

    private Map<String, RnPluginApk> mPluginApkMap = new HashMap<>();

    private final static RnPluginManager sIntance = new RnPluginManager();

    private RnPluginManager() {

    }

    public static RnPluginManager get() {
        return sIntance;
    }

    public void install(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo = pm.getPackageArchiveInfo(apkPath, 0);
        if (mPluginApkMap.get(packageInfo.packageName) == null) {
            RnPluginApk pluginApk = new RnPluginApk(context, packageInfo.packageName, packageInfo.applicationInfo.className, apkPath);
            mPluginApkMap.put(packageInfo.packageName, pluginApk);
        }
    }

    public void unInstall(Context context, String apkPath) {

    }

    public void startMainActivity(Context context, String packageName, String activityName, String apkPath) {
        RnPluginApk pluginApk = mPluginApkMap.get(packageName);
        if (pluginApk == null) {
            install(context, apkPath);
        }
        Intent intent = new Intent(context, RnPluginActivityProxy.class);
        intent.putExtra(RnPluginConstants.PLUGIN_PACKAGE_NAME, packageName);
        intent.putExtra(RnPluginConstants.PLUGIN_ACTIVITY_NAME, activityName);
        if (!(context instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    public void startPlugin(Activity activity, String packageName, String activityName, String apkPath) {
        Intent intent = new Intent(activity, PluginHelperActivity.class);
        intent.putExtra(RnPluginConstants.PLUGIN_PACKAGE_NAME, packageName);
        intent.putExtra(RnPluginConstants.PLUGIN_ACTIVITY_NAME, activityName);
        intent.putExtra(RnPluginConstants.PLUGIN_APK_PATH, apkPath);
        activity.startActivity(intent);
    }

    public RnPluginApk getPluginApk(String packageName) {
        return mPluginApkMap.get(packageName);
    }

}
