package com.lzl.rnplugin;

import android.content.Context;

import dalvik.system.DexClassLoader;

/**
 * Created by liuzhuolin on 17/8/30.
 */

public class RnPluginClassLoader extends DexClassLoader {

    private ClassLoader mHostClassLoader;

    public RnPluginClassLoader(Context context, String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
        mHostClassLoader = context.getClassLoader();
    }

    public static ClassLoader getBootClassLoader() {
        Context context = null;
        try {
            Reflect reflect = Reflect.on("android.app.ActivityThread");
            if (null != reflect) {
                Reflect currentActivityThread = reflect.call("currentActivityThread");
                if (null != currentActivityThread) {
                    Reflect mSystemContext = currentActivityThread.call("getSystemContext");
                    if (null != mSystemContext) {
                        context = mSystemContext.get();
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return context == null ? null : context.getClassLoader();
    }

    @Override
    public String findLibrary(final String libraryName) {
        String fileName = System.mapLibraryName(libraryName);
        // Log.i(LOG_TAG,libraryName);
        // Log.i(LOG_TAG,fileName);

        return super.findLibrary(libraryName);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // Log.i(LOG_TAG,name);

        try {
            return super.findClass(name);
        } catch (Throwable t) {
            Reflect classLoader = Reflect.on(mHostClassLoader);
            Reflect clz = null;
            try {
                clz = classLoader.call("findClass", name);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return clz.get();
        }
    }
}
