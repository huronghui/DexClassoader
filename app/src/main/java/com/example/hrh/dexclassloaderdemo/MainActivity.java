package com.example.hrh.dexclassloaderdemo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.DexClassLoader;

public class MainActivity extends AppCompatActivity {

    private static final String OPT_DIR = "opt";
    private static String TAG = "loadDexClasses";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        loadDexClassses();
    }

    private void loadDexClassses() {
        if(Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            Log.v("loadDexClassses", "LoadDexClasses is only available for ICS or up");
        }

        String paths[] = null;
        try {
            paths = getAssets().list("plugins");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(paths == null) {
            Log.v("loadlDexClasses", "There was no " + paths);
            return;
        }

        Log.v("loadDexClasses", "Dex Preparing to loadDexClasses!");
        for (String file : paths) {
            //接下来完善
            LoadPluginDir(paths);
        }
    }

    private void LoadPluginDir(String[] paths) {
        Intent intent = new Intent();
        intent.setPackage("com.example.hrh.apkbeloaded");
        PackageManager pm = this.getPackageManager();
        final List<ResolveInfo> plugins = pm.queryIntentActivities(intent,0);
        if(plugins.size() <= 0){
            Log.i(TAG, "resolve info size is:" + plugins.size());
            return;
        }
        ResolveInfo resolveInfo = plugins.get(0);
        ActivityInfo activityInfo = resolveInfo.activityInfo;

        String div = System.getProperty("path.separator");
        String packageName = activityInfo.packageName;
        String dexPath = activityInfo.applicationInfo.sourceDir;
        //目标类所在的apk或者jar的路径，class loader会通过这个路径来加载目标类文件
        String dexOutputDir = this.getApplicationInfo().dataDir;
        //由于dex文件是包含在apk或者jar文件中的,所以在加载class之前就需要先将dex文件解压出来，dexOutputDir为解压路径
        String libPath = activityInfo.applicationInfo.nativeLibraryDir;
        //目标类可能使用的c或者c++的库文件的存放路径

        Log.i(TAG, "div:" + div + "   " +
                "packageName:" + packageName + "   " +
                "dexPath:" + dexPath + "   " +
                "dexOutputDir:" + dexOutputDir + "   " +
                "libPath:" + libPath);

        DexClassLoader dcLoader = new DexClassLoader(dexPath,dexOutputDir,libPath,this.getClass().getClassLoader());
        try {
            Class<?> clazz = dcLoader.loadClass(packageName + ".Registry");
            Log.i(TAG, clazz.getName());
            Method[] methods = clazz.getDeclaredMethods();
            for(Method method : methods) {
                Log.i(TAG, method.getName());
            }

            Method invokeMethod = clazz.getDeclaredMethod("invoke", String.class);
            invokeMethod.invoke(clazz.newInstance(), "哈哈  成功啦");
//            Object obj = clazz.newInstance();
//            Class[] param = new Class[1];
//            param[0] = String.class;
//            Method action = clazz.getMethod("invoke", param);
//            action.invoke(obj, "test this function");
        } catch (ClassNotFoundException e) {
            Log.i(TAG, "ClassNotFoundException");
        } catch (InstantiationException e) {
            Log.i(TAG, "InstantiationException");
        } catch (IllegalAccessException e) {
            Log.i(TAG, "IllegalAccessException");
        } catch (NoSuchMethodException e) {
            Log.i(TAG, "NoSuchMethodException");
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "IllegalArgumentException");
        } catch (InvocationTargetException e) {
            Log.i(TAG, "InvocationTargetException");
        }


    }

    public void copyAssetsApkToFile(Context context, String src, String des) {
        try {
            InputStream is = context.getAssets().open(src);
            FileOutputStream fos = new FileOutputStream(new File(des));
            byte[] buffer = new byte[1024];
            while (true) {
                int len = is.read(buffer);
                if (len == -1) {
                    break;
                }
                fos.write(buffer, 0, len);
            }
            is.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
