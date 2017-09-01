package com.lzl.rnplugin;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Created by liuzhuolin on 17/8/30.
 */

public class RnPluginActivityProxy extends Activity {
    private RnPluginApk mRnPluginApk;
    private Instrumentation mInstrument = new Instrumentation();
    private Activity mPluginActivity;
    private Application mpluginApplication;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String packageName = getIntent().getStringExtra(RnPluginConstants.PLUGIN_PACKAGE_NAME);
        mRnPluginApk = RnPluginManager.get().getPluginApk(packageName);
        String activityClass = getIntent().getStringExtra(RnPluginConstants.PLUGIN_ACTIVITY_NAME);
        try {
            Class activitCls = mRnPluginApk.getClassLoader().loadClass(activityClass);
            mPluginActivity = (Activity) activitCls.newInstance();
            String pluginApplication = mRnPluginApk.getPluginApplicationName();
            if (!TextUtils.isEmpty(pluginApplication)) {
                Class applicationCls = mRnPluginApk.getClassLoader().loadClass(pluginApplication);
                mpluginApplication = (Application) applicationCls.newInstance();
                Reflect pluginApp = Reflect.on(mpluginApplication);
                //为啥适配RN assetPath
                addPluginRnAssetPath(mRnPluginApk.getApkPath());
                pluginApp.call("attach", getApplicationContext());
                mpluginApplication.onCreate();
                Reflect.on(mPluginActivity).set("mApplication", mpluginApplication);
            }
            fillPluginActivity(mPluginActivity);
            mInstrument.callActivityOnCreate(mPluginActivity, savedInstanceState);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fillPluginActivity(Activity plugin) {
        Reflect pluginPoxy = Reflect.on(this);
        Reflect pluginRef = Reflect.on(plugin);
        dispatchProxyToPlugin(pluginPoxy, pluginRef, this, plugin);
    }

    @Override
    public Resources getResources() {
        if (mRnPluginApk == null) {
            return super.getResources();
        }
        return mRnPluginApk.getResources();
    }

    @Override
    public ClassLoader getClassLoader() {
        return mRnPluginApk.getClassLoader();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mInstrument.callActivityOnResume(mPluginActivity);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mInstrument.callActivityOnPause(mPluginActivity);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mInstrument.callActivityOnDestroy(mPluginActivity);
        System.exit(0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mInstrument.callActivityOnStart(mPluginActivity);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mInstrument.callActivityOnPause(mPluginActivity);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void addPluginRnAssetPath(String apkPath) {
        Reflect asset = Reflect.on(getApplication().getAssets());
        try {
            asset.call("addAssetPath", apkPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dispatchProxyToPlugin(Reflect proxyRef, Reflect pluginRef, Activity proxy, Activity plugin) {
        try {
            Instrumentation instrumentation = proxyRef.get("mInstrumentation");
            if (Build.VERSION.SDK_INT < 11) {
                pluginRef.call(
                        // 方法名
                        "attach",
                        // Context context
                        proxy,
                        // ActivityThread aThread
                        proxyRef.get("mMainThread"),
                        // Instrumentation instr
                        instrumentation,
                        // IBinder token
                        proxyRef.get("mToken"),
                        // int ident
                        proxyRef.get("mEmbeddedID") == null ? 0 : proxyRef.get("mEmbeddedID"),
                        // Application application
                        plugin.getApplication() != null ? plugin.getApplication() : proxy.getApplication(),
                        // Intent intent
                        proxy.getIntent(),
                        // ActivityInfo info
                        proxyRef.get("mActivityInfo"),
                        // CharSequence title
                        proxy.getTitle(),
                        // Activity parent
                        proxy.getParent(),
                        // String id
                        proxyRef.get("mEmbeddedID"),
                        // NonConfigurationInstances
                        // lastNonConfigurationInstances
                        proxy.getLastNonConfigurationInstance(),
                        // HashMap<String,Object>
                        // lastNonConfigurationChildInstances
                        proxyRef.get("mLastNonConfigurationChildInstances"),
                        // Configuration config
                        proxyRef.get("mCurrentConfig"));
            } else if (Build.VERSION.SDK_INT >= 11 && Build.VERSION.SDK_INT < 21) {
                pluginRef.call(
                        // 方法名
                        "attach",
                        // Context context
                        proxy,
                        // ActivityThread aThread
                        proxyRef.get("mMainThread"),
                        // Instrumentation instr
                        instrumentation,
                        // IBinder token
                        proxyRef.get("mToken"),
                        // int ident
                        proxyRef.get("mEmbeddedID") == null ? 0 : proxyRef.get("mEmbeddedID"),
                        // Application application
                        plugin.getApplication() != null ? plugin.getApplication() : proxy.getApplication(),
                        // Intent intent
                        proxy.getIntent(),
                        // ActivityInfo info
                        proxyRef.get("mActivityInfo"),
                        // CharSequence title
                        proxy.getTitle(),
                        // Activity parent
                        proxy.getParent(),
                        // String id
                        proxyRef.get("mEmbeddedID"),
                        // NonConfigurationInstances
                        // lastNonConfigurationInstances
                        proxy.getLastNonConfigurationInstance(),
                        // Configuration config
                        proxyRef.get("mCurrentConfig"));
            } else if (Build.VERSION.SDK_INT == 21) {
                pluginRef.call(
                        // 方法名
                        "attach",
                        // Context context
                        proxy,
                        // ActivityThread aThread
                        proxyRef.get("mMainThread"),
                        // Instrumentation instr
                        instrumentation,
                        // IBinder token
                        proxyRef.get("mToken"),
                        // int ident
                        proxyRef.get("mEmbeddedID") == null ? 0 : proxyRef.get("mEmbeddedID"),
                        // Application application
                        plugin.getApplication() != null ? plugin.getApplication() : proxy.getApplication(),
                        // Intent intent
                        proxy.getIntent(),
                        // ActivityInfo info
                        proxyRef.get("mActivityInfo"),
                        // CharSequence title
                        proxy.getTitle(),
                        // Activity parent
                        proxy.getParent(),
                        // String id
                        proxyRef.get("mEmbeddedID"),
                        // NonConfigurationInstances
                        // lastNonConfigurationInstances
                        proxy.getLastNonConfigurationInstance(),
                        // Configuration config
                        proxyRef.get("mCurrentConfig"),
                        // IVoiceInteractor voiceInteractor
                        proxyRef.get("mVoiceInteractor"));
            } else if (Build.VERSION.SDK_INT > 21) {
                pluginRef.call(
                        // 方法名
                        "attach",
                        // Context context
                        proxy,
                        // ActivityThread aThread
                        proxyRef.get("mMainThread"),
                        // Instrumentation instr
                        instrumentation,
                        // IBinder token
                        proxyRef.get("mToken"),
                        // int ident
                        proxyRef.get("mEmbeddedID") == null ? 0 : proxyRef.get("mEmbeddedID"),
                        // Application application
                        plugin.getApplication() != null ? plugin.getApplication() : proxy.getApplication(),
                        // Intent intent
                        proxy.getIntent(),
                        // ActivityInfo info
                        proxyRef.get("mActivityInfo"),
                        // CharSequence title
                        proxy.getTitle(),
                        // Activity parent
                        proxy.getParent(),
                        // String id
                        proxyRef.get("mEmbeddedID"),
                        // NonConfigurationInstances
                        // lastNonConfigurationInstances
                        proxy.getLastNonConfigurationInstance(),
                        // Configuration config
                        proxyRef.get("mCurrentConfig"),
                        // String referrer
                        proxyRef.get("mReferrer"),
                        // IVoiceInteractor voiceInteractor
                        proxyRef.get("mVoiceInteractor"));
            } else {
                // TODO
            }
            pluginRef.set("mWindow", proxy.getWindow());
            plugin.getWindow().setCallback(plugin);
            Reflect.on(proxy.getBaseContext()).call("setOuterContext", plugin);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
