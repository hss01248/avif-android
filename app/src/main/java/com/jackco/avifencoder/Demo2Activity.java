package com.jackco.avifencoder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.devio.takephoto.wrap.TakeOnePhotoListener;
import org.devio.takephoto.wrap.TakePhotoUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class Demo2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo2);
    }

    public void selectPic(View view) {
        TakePhotoUtil.startPickOneWitchDialog(this, new TakeOnePhotoListener() {
            @Override
            public void onSuccess(final String path) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        doCompress(path);
                    }
                }).start();

            }

            @Override
            public void onFail(String path, String msg) {

            }

            @Override
            public void onCancel() {

            }
        });
    }


    private void doCompress(String path) {
        Log.w("path","selected path:"+path);
        compressJpg(path);
        String name = new File(path).getName();
        name = name.substring(0,name.lastIndexOf("."))+".avif";
      File file = new File(getExternalFilesDir("avif"),name);

        AvifEncoder.encodeFileTo(path,file.getAbsolutePath(),40);
    }

    private void compressJpg(String path) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        String name = new File(path).getName();
        name = name.substring(0,name.lastIndexOf("."))+System.currentTimeMillis()+".jpg";
        File file = new File(getExternalFilesDir("avif"),name);
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG,75,new FileOutputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
