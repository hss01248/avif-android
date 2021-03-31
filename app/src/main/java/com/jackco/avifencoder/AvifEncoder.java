package com.jackco.avifencoder;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import it.sephiroth.android.library.exif2.ExifTag;

public class AvifEncoder {

    public static Context context;

    public static void init(Application application){
        context = application;
    }


    public static void  encodeFileTo(String input,String output,int quality){
        try {
            Bitmap bitmap = decodeBitmap(input);
            String rawPath = writeRaw(bitmap);
            int qua1 = 63-quality;
            int  qua2 = qua1 + 10;

            if(qua2 > 63) qua2 = 63;
            if(qua1 == 0) qua2 = 0;
            boolean success =  execAvifEncoder(rawPath,output,bitmap.getWidth(),bitmap.getHeight(),8,qua1,qua2,10);
            if(success){
                copyExif(input,output);
            }
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }




    }

    //使用exiftool
    private static void copyExif(String input, String output) throws IOException {
        it.sephiroth.android.library.exif2.ExifInterface exifInterface = new it.sephiroth.android.library.exif2.ExifInterface();
        exifInterface.readExif(input, it.sephiroth.android.library.exif2.ExifInterface.Options.OPTION_ALL);
        exifInterface.writeExif(output);

        it.sephiroth.android.library.exif2.ExifInterface exifInterface2 = new it.sephiroth.android.library.exif2.ExifInterface();
        exifInterface2.readExif(output, it.sephiroth.android.library.exif2.ExifInterface.Options.OPTION_ALL);
        List<ExifTag> allTags = exifInterface2.getAllTags();
        for (ExifTag allTag : allTags) {
            Log.d("exif",allTag.toString());
        }


    }

    private static boolean execAvifEncoder(String rawPath, String output,int width,int height,int threads,int qua1,int qua2,int speed) {
        Log.e("Log","execAvifEncoder start ");
        String ev = context.getApplicationInfo().nativeLibraryDir;
        String[] envp = {"LD_LIBRARY_PATH=" + ev};

        String name = context.getApplicationInfo().nativeLibraryDir + "/libavif_example1.so "
                +  rawPath + " " + output
                + " " + width + " " + height +" " + threads + " " + qua1 +  " " + qua2 +  " " + speed;
//EXECUTING: /data/app/com.jackco.avifencoder-pfiGIymaFugjRSeVPy2yOg==/lib/arm64/libavif_example1.so
// /data/user/0/com.jackco.avifencoder/decoded.raw /data/user/0/com.jackco.avifencoder/b.avif 1080 2340 8 50 60 10

        String res = null;
        try {
            res = execCmd(name, envp);

            new File(rawPath).delete();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static String execCmd(String cmd, String[] envp) throws java.io.IOException {
        Log.i("EXECUTING", cmd.replace(".avif ",".avif\nparams: "));

        Process proc = null;
        proc = Runtime.getRuntime().exec(cmd, envp);
        java.io.InputStream is = proc.getInputStream();
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        String val = "";
        if (s.hasNext()) {
            val = s.next();
            Log.i("Result",val);
        }
        else {
            val = "";
        }
        return val;
    }

    private static File getCacheDir(){
        File cacheDir = new File(context.getCacheDir(),"avifRawTmp");
        if(!cacheDir.exists()){
            cacheDir.mkdirs();
        }
        return cacheDir;
    }

    private static String writeRaw(Bitmap bitmap_f) throws IOException {
        SimpleDateFormat formatter = new SimpleDateFormat("_dd_MM_yyyy_HH_mm_ss");
        Date date = new Date();
        File cacheDir =getCacheDir();
        File file = new File(cacheDir,formatter.format(date)+".raw");
        OutputStream fo = new FileOutputStream(file);

        int size = bitmap_f.getRowBytes() * bitmap_f.getHeight();

        byte[] byteArray;

        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        bitmap_f.copyPixelsToBuffer(byteBuffer);

        byteArray = byteBuffer.array();

        fo.write(byteArray);

        return file.getAbsolutePath();
    }

    private static Bitmap decodeBitmap(String input) {
        if(TextUtils.isEmpty(input)){
            return null;
        }
        File file = new File(input);
        Bitmap bitmap = null;
        int rotation = 0;
        ExifInterface exif = null;
        if(file.exists()){
            bitmap =  BitmapFactory.decodeFile(input);
            try {
                exif = new ExifInterface(input);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else {
            try {
                bitmap =  BitmapFactory.decodeStream(context.getContentResolver().openInputStream(Uri.parse(input)));
                exif = new ExifInterface(context.getContentResolver().openInputStream(Uri.parse(input)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(bitmap != null && exif != null){
           int ori =  exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,0);
           if(ori == ExifInterface.ORIENTATION_ROTATE_90){
               rotation = 90;
           }else if(ori == ExifInterface.ORIENTATION_ROTATE_180){
               rotation = 180;
           }else if(ori == ExifInterface.ORIENTATION_ROTATE_270){
                rotation = 270;
            }else {
               rotation = 0;
           }
           if(rotation != 0){
               //旋转图片
               Log.w("avif","旋转图片:"+rotation);
               int width = bitmap.getWidth();
               int height = bitmap.getHeight();
               Matrix matrix = new Matrix();
               matrix.setRotate(rotation);
               // 围绕原地进行旋转
               Bitmap newBM = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
               if (!newBM.equals(bitmap)) {
                   bitmap = newBM;
               }
           }
        }
        return bitmap;
    }
}
