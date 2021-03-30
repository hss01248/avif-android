package com.jackco.avifencoder;

import android.app.Application;


public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AvifEncoder.init(this);

    }
}
