package com.lzl.rnplugin;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;

import com.lzl.rnplugin.compat.BuildCompat;
import com.lzl.rnplugin.compat.VMRuntimeCompat;
import com.lzl.rnplugin.util.RnPluginDirUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dalvik.system.DexClassLoader;

/**
 * Created by liuzhuolin on 17/8/30.
 */

public class RnPluginApk {
    private static final String TAG = "RnPluginApk";
    private RnPluginClassLoader mRnPluginClassLoader;
    private Resources mResources;
    private String mPluginPackageName;

    private String mApkPath;
    private String mPluginApplicationName;
    public RnPluginApk(Context context, String pluginPackageName, String pluginApplicationName, String apkPath) {
        try {
            copyNativeLibs(apkPath, RnPluginDirUtils.getNativeLibraryDir(context, pluginPackageName));
            //为了适配RN Soload
            copyNativeLibs(apkPath, RnPluginDirUtils.getRnExternalNativeLibDir(context));
        } catch (Exception e) {
            e.printStackTrace();
        }
        ClassLoader classLoader = RnPluginClassLoader.getBootClassLoader();
        mRnPluginClassLoader = new RnPluginClassLoader(context, apkPath, RnPluginDirUtils.getDexCachedDir(context, pluginPackageName),
                RnPluginDirUtils.getNativeLibraryDir(context, pluginPackageName), classLoader != null ? classLoader : context.getClassLoader());
        try {
            mResources = createResources(context, apkPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mPluginPackageName = pluginPackageName;
        mApkPath = apkPath;
        mPluginApplicationName = pluginApplicationName;
    }

    public String getPluginApplicationName() {
        return mPluginApplicationName;
    }

    public void setPluginApplicationName(String pluginApplicationName) {
        mPluginApplicationName = pluginApplicationName;
    }

    public Resources getResources() {
        return mResources;
    }

    public RnPluginClassLoader getClassLoader() {
        return mRnPluginClassLoader;
    }

    public String getApkPath() {
        return mApkPath;
    }

    private Resources createResources(Context context, String apkPath) throws Exception {
        AssetManager assetMag = AssetManager.class.newInstance();
        Reflect.on(assetMag).call("addAssetPath", apkPath);
        return new Resources(assetMag, context.getResources().getDisplayMetrics(), context.getResources().getConfiguration());

    }

    private void copyNativeLibs(String apkfile, String nativeLibraryDir) throws Exception {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(apkfile);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            Map<String, ZipEntry> libZipEntries = new HashMap<String, ZipEntry>();
            Map<String, Set<String>> soList = new HashMap<String, Set<String>>(1);
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.contains("../")) {
                    Log.d(TAG, "Path traversal attack prevented");
                    continue;
                }
                if (name.startsWith("lib/") && !entry.isDirectory()) {
                    libZipEntries.put(name, entry);
                    String soName = new File(name).getName();
                    Set<String> fs = soList.get(soName);
                    if (fs == null) {
                        fs = new TreeSet<String>();
                        soList.put(soName, fs);
                    }
                    fs.add(name);
                }
            }

            for (String soName : soList.keySet()) {
                Log.e(TAG, "try so =" + soName);
                Set<String> soPaths = soList.get(soName);
                String soPath = findSoPath(soPaths, soName);
                if (soPath != null) {
                    File file = new File(nativeLibraryDir, soName);
                    if (file.exists()) {
                        file.delete();
                    }
                    InputStream in = null;
                    FileOutputStream ou = null;
                    try {
                        in = zipFile.getInputStream(libZipEntries.get(soPath));
                        ou = new FileOutputStream(file);
                        byte[] buf = new byte[8192];
                        int read = 0;
                        while ((read = in.read(buf)) != -1) {
                            ou.write(buf, 0, read);
                        }
                        ou.flush();
                        ou.getFD().sync();
                    } catch (Exception e) {
                        if (file.exists()) {
                            file.delete();
                        }
                        throw e;
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (Exception e) {
                            }
                        }
                        if (ou != null) {
                            try {
                                ou.close();
                            } catch (Exception e) {
                            }
                        }
                    }
                }
            }
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (Exception e) {
                }
            }
        }
    }

    private String findSoPath(Set<String> soPaths, String soName) {
        if (soPaths != null && soPaths.size() > 0) {
            if (VMRuntimeCompat.is64Bit()) {
                //在宿主程序运行在64位进程中的时候，插件的so也只拷贝64位，否则会出现不支持的情况。
                String[] supported64BitAbis = BuildCompat.SUPPORTED_64_BIT_ABIS;
                Arrays.sort(supported64BitAbis);
                for (String soPath : soPaths) {
                    String abi = soPath.replaceFirst("lib/", "");
                    abi = abi.replace("/" + soName, "");

                    if (!TextUtils.isEmpty(abi) && Arrays.binarySearch(supported64BitAbis, abi) >= 0) {
                        return soPath;
                    }
                }
            } else {
                //在宿主程序运行在32位进程中的时候，插件的so也只拷贝64位，否则会出现不支持的情况。
                String[] supported32BitAbis = BuildCompat.SUPPORTED_32_BIT_ABIS;
                Arrays.sort(supported32BitAbis);
                for (String soPath : soPaths) {
                    String abi = soPath.replaceFirst("lib/", "");
                    abi = abi.replace("/" + soName, "");
                    if (!TextUtils.isEmpty(abi) && Arrays.binarySearch(supported32BitAbis, abi) >= 0) {
                        return soPath;
                    }
                }
            }
        }
        return null;
    }

}
