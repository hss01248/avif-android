package com.jackco.avifencoder;

import android.app.Application;
import android.os.StrictMode;

import com.hss01248.avif.AvifEncoder;


public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AvifEncoder.init(this);
      /*  StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());*/

    }
}
