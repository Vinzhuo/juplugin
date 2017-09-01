package com.lzl.rnplugin;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

//切换进程Activity 不能继承AppCompatActivity
/*
     android.content.res.Resources$NotFoundException: Resource ID #0x7f090000
 */
public class PluginHelperActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plugin_help);
        Intent intent = getIntent();
        String packageName = intent.getStringExtra(RnPluginConstants.PLUGIN_PACKAGE_NAME);
        String activityName = intent.getStringExtra(RnPluginConstants.PLUGIN_ACTIVITY_NAME);
        String apkPath = intent.getStringExtra(RnPluginConstants.PLUGIN_APK_PATH);
        RnPluginManager.get().startMainActivity(this, packageName, activityName, apkPath);
        finish();
    }
}
