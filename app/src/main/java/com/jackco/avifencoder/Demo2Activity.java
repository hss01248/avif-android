package com.jackco.avifencoder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.jackco.avifencoder.databinding.ActivityDemo2Binding;

import org.devio.takephoto.wrap.TakeOnePhotoListener;
import org.devio.takephoto.wrap.TakePhotoUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class Demo2Activity extends AppCompatActivity {
    ActivityDemo2Binding binding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_demo2);
         binding = ActivityDemo2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
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


    private void doCompress(final String path) {
        Glide.with(Demo2Activity.this)
                .load(path)
                .into(binding.ivOriginal);
        Log.w("path","selected path:"+path);
        final File jpg = compressJpg(path);
        String name = new File(path).getName();
        name = name.substring(0,name.lastIndexOf("."))+".avif";
      final File avif = new File(getExternalFilesDir("avif"),name);

        AvifEncoder.encodeFileTo(path,avif.getAbsolutePath(),40);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showDesc(path,jpg,avif);
                Glide.with(Demo2Activity.this)
                        .load(avif)
                        .into(binding.ivAvif);

            }
        });

    }

    private void showDesc(String path, File jpg, File avif) {
        StringBuilder sb = new StringBuilder();
        sb.append("avif:")
                .append(avif.length()/1024)
                .append("kB") .append("\n")
                .append(avif.getAbsolutePath())
                .append("\n").append("original:")
                .append(new File(path).length()/1024)
                .append("kB") .append("\n")
                .append(path)
        .append("\n")
        .append("jpg:") .append(jpg.length()/1024)
                .append("kB") .append("\n")
                .append(jpg.getAbsolutePath())
                .append("\n")
               ;
        binding.tvDesc.setText(sb.toString());
    }

    private File compressJpg(String path) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        String name = new File(path).getName();
        name = name.substring(0,name.lastIndexOf("."))+System.currentTimeMillis()+".jpg";
        final File file = new File(getExternalFilesDir("avif"),name);
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG,75,new FileOutputStream(file));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Glide.with(Demo2Activity.this)
                            .load(file)
                            .into(binding.ivJpg75);
                }
            });
            return file;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
