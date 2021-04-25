package com.jackco.avifencoder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.jackco.avifencoder.databinding.ActivityDemo2Binding;
import com.jackco.avifencoder.quality.Magick;

import org.devio.takephoto.wrap.TakeOnePhotoListener;
import org.devio.takephoto.wrap.TakePhotoUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import static com.darsh.multipleimageselect.helpers.Constants.REQUEST_CODE;

public class Demo2Activity extends AppCompatActivity {
    private static final int REQUEST_CODE_3 = 788;
    ActivityDemo2Binding binding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_demo2);
         binding = ActivityDemo2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initEvent();
        requestPermission();

    }


    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 先判断有没有权限
            if (Environment.isExternalStorageManager()) {
               // writeFile();
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE_3);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 先判断有没有权限
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
               // writeFile();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        } else {
           // writeFile();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
               // writeFile();
            } else {
                Toast.makeText(this,"存储权限获取失败",Toast.LENGTH_LONG).show();
            }
        }
    }


    private void initEvent() {
        q1 = binding.sbQ1.getProgress();
        q2 = binding.sbQ2.getProgress();
        binding.sbQ1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                compressAvif(path);
            }
        });

        binding.sbQ2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
               compressAvif(path);
            }
        });
    }
   volatile int q1,q2;

    String path;
    public void selectPic(View view) {
        TakePhotoUtil.startPickOneWitchDialog(this, new TakeOnePhotoListener() {
            @Override
            public void onSuccess(final String path) {
                Demo2Activity.this.path = path;
               /* Glide.with(Demo2Activity.this)
                        .load(path)
                        .into(binding.ivOriginal);*/
                File file = new File(path);
                final int quality = new Magick().getJPEGImageQuality(file);
                binding.ivOriginal.setImage(ImageSource.uri(Uri.fromFile(file)));
                binding.tvOriginal.setText(file.length()/1024+"kB,quality:"+quality+"\n"+path);
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

        Log.w("path","selected path:"+path);
        final File jpg = compressJpg(path);

        compressAvif(path);


    }

    private void compressAvif(final String path) {
        if(TextUtils.isEmpty(path)){
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Demo2Activity.this,"avif开始压缩",Toast.LENGTH_LONG).show();
            }
        });

        q1 = binding.sbQ1.getProgress();
        q2 = binding.sbQ2.getProgress();

        new Thread(new Runnable() {
            @Override
            public void run() {
                final long start = System.currentTimeMillis();

                String name = new File(path).getName();
                name = name.substring(0,name.lastIndexOf("."))+"-q1-"+q1+"-q2-"+q2;
                name = name+".avif";
                final File avif = new File(getExternalFilesDir("avif"),name);

                AvifEncoder.encodeFileTo(path,avif.getAbsolutePath(),q1,q2);




                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Demo2Activity.this,"avif压缩完成,耗时:"
                                +(System.currentTimeMillis() - start)+"ms",Toast.LENGTH_LONG).show();
                        //showDesc(path,jpg,avif);
                      /*  Glide.with(Demo2Activity.this)
                                .load(avif)
                                .override(w,h)
                                .into(binding.ivAvif);*/
                        binding.tvAvif.setText(avif.length()/1024+"kB, q1="+q1+",q2:"+q2+" cost:"
                                +(System.currentTimeMillis() - start)+"ms, wh:"+w+"x"+h+"\n"+avif.getAbsolutePath());
                        showAvifBig(avif);
                        uploadAvif(avif.getAbsolutePath());

                    }
                });
            }
        }).start();

    }

    private void uploadAvif(String absolutePath) {
        AliOssUtil.upload(absolutePath);
    }

    private void showAvifBig(File avif) {
        Glide.with(Demo2Activity.this)
                .asBitmap()
                .load(avif)
                .override(w,h)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        binding.ivAvif.setImage(ImageSource.bitmap(resource));
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
        //binding.tvDesc.setText(sb.toString());
    }

    int w,h;
    private File compressJpg(String path) {
        final long start = System.currentTimeMillis();

        Bitmap bitmap = BitmapFactory.decodeFile(path);
        w = bitmap.getWidth();
        h = bitmap.getHeight();
        String name = new File(path).getName();
        name = name.substring(0,name.lastIndexOf("."))+System.currentTimeMillis()+".jpg";
        final File file = new File(getExternalFilesDir("avif"),name);
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG,75,new FileOutputStream(file));
            final int quality = new Magick().getJPEGImageQuality(file);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    /*Glide.with(Demo2Activity.this)
                            .load(file)
                            .override(w,h)
                            .into(binding.ivJpg75);*/
                    binding.ivJpg75.setImage(ImageSource.uri(Uri.fromFile(file)));
                    binding.tvLibjpeg.setText(file.length()/1024+"kB cost"+(System.currentTimeMillis()-start)+"ms,qulity:"+quality+"\n"+file.getAbsolutePath());
                }
            });
            return file;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
