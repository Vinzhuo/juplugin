package com.lzl.juplugin;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

/**
 * Created by liuzhuolin on 17/8/31.
 */

public class ApkFile {
    private Drawable icon;
    private CharSequence title;
    private String mApkPath;
    private PackageInfo mPackageInfo;
    private String mMainActivityName;

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public CharSequence getTitle() {
        return title;
    }

    public void setTitle(CharSequence title) {
        this.title = title;
    }

    public String getApkPath() {
        return mApkPath;
    }

    public void setApkPath(String apkPath) {
        mApkPath = apkPath;
    }

    public String getPackageName() {
        return mPackageInfo.packageName;
    }


    public String getMainActivityName() {
        return mMainActivityName;
    }

    public ApkFile(String apkPath, PackageManager pm, PackageInfo info) {
        mApkPath = apkPath;
        mPackageInfo = info;
        try {
            icon = pm.getApplicationIcon(info.applicationInfo);
        } catch (Exception e) {
            icon = pm.getDefaultActivityIcon();
        }
        title = pm.getApplicationLabel(info.applicationInfo);
        generateMainActivityName(mPackageInfo);
    }

    private void generateMainActivityName(PackageInfo packageInfo) {
        ActivityInfo [] activityInfos = packageInfo.activities;
        for (ActivityInfo info : activityInfos) {
            if (info.name.endsWith("PluginActivity") || info.name.endsWith("MainActivity")) {
                mMainActivityName = info.name;
                break;
            }
        }
    }
}
