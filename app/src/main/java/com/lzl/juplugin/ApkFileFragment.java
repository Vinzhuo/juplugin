package com.lzl.juplugin;

import android.Manifest;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lzl.rnplugin.RnPluginManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ApkFileFragment extends ListFragment {
    private ArrayAdapter<ApkFile> adapter;
    final Handler handler = new Handler();

    public ApkFileFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        adapter = new ArrayAdapter<ApkFile>(getActivity(), 0) {
            @Override
            public View getView(final int position, View convertView, final ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getActivity()).inflate(R.layout.apk_item, null);
                }
                ApkFile item = getItem(position);

                ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
                imageView.setImageDrawable(item.getIcon());
                TextView version = (TextView) convertView.findViewById(R.id.textView2);
                version.setText(item.getTitle());

                return convertView;
            }
        };

    }

    boolean isViewCreated = false;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isViewCreated = true;
        setListAdapter(adapter);
        setListShown(false);
        startLoad();
    }

    @Override
    public void onDestroyView() {
        isViewCreated = false;
        super.onDestroyView();
    }

    @Override
    public void setListShown(boolean shown) {
        if (isViewCreated) {
            super.setListShown(shown);
        }
    }

    private void startLoad() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            startLoadInner();
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0x1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0x1) {
            if (permissions != null && permissions.length > 0) {
                for (int i = 0; i < permissions.length; i++) {
                    String permisson = permissions[i];
                    int grantResult = grantResults[i];
                    if (Manifest.permission.READ_EXTERNAL_STORAGE.equals(permisson)) {
                        if (grantResult == PackageManager.PERMISSION_GRANTED) {
                            startLoadInner();
                        } else {
                            Toast.makeText(getActivity(), "没有授权，无法使用", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                for (String permisson : permissions) {

                }
            }
        }
    }

    private void startLoadInner() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                setListShown(true);
            }
        });
        if (!isViewCreated) {
            return;
        }
        new Thread("ApkScanner") {
            @Override
            public void run() {

                List<File> apks = new ArrayList<File>(10);

                File file = new File(Environment.getExternalStorageDirectory(), "juRnDownload");
                if (file.exists() && file.isDirectory()) {
                    File[] files1 = file.listFiles();
                    if (files1 != null) {
                        for (File apk : files1) {
                            if (apk.exists() && apk.getPath().toLowerCase().endsWith(".apk")) {
                                apks.add(apk);
                            }
                        }
                    }

                }
                PackageManager pm = getActivity().getPackageManager();
                for (final File apk : apks) {
                    try {
                        if (apk.exists() && apk.getPath().toLowerCase().endsWith(".apk")) {
                            final PackageInfo info = pm.getPackageArchiveInfo(apk.getPath(), PackageManager.GET_ACTIVITIES);
                            if (info != null && isViewCreated) {
                                try {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            adapter.add(new ApkFile(apk.getAbsolutePath(), getActivity().getPackageManager(), info));
                                        }
                                    });
                                } catch (Exception e) {
                                }
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }.start();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ApkFile item = adapter.getItem(position);
        RnPluginManager.get().startPlugin(getActivity(), item.getPackageName(), item.getMainActivityName(), item.getApkPath());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
