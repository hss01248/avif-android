package com.jackco.avifencoder;

import android.app.Application;

import org.devio.takephoto.wrap.TakePhotoUtil;


public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AvifEncoder.init(this);
        AliOssUtil.init(this);


    }
}
