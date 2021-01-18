package com.zhangqie.videocache;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.VideoView;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import androidx.appcompat.app.AppCompatActivity;
/***
 * https://github.com/danikula/AndroidVideoCache
 *
 */

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 1;
    private File videoFile;
    String url = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";
    VideoView videoView;
    String path = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"; // 下载地址
    private String fileName;
    private File file;
    private String dirName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoView = findViewById(R.id.videoView);
        final String cameraPath=
                Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DCIM+File.separator+"Camera"+File.separator;
        /*initVideo();*/
      /*  //检查版本是否大于M
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
                Log.i("aaa", "if");
            } else {
                Log.i("aaa", "权限已申请");
                initVideo();
            }
        }*/
      findViewById(R.id.tx).setOnClickListener(new View.OnClickListener() {

          @Override
          public void onClick(View v) {

               /* init();
              ArrayList<String> list = new ArrayList<>();
              list.add("https://plusjrmallstatic.bj.bcebos.com/wfe/mgm/1607336390816855.mp4");
              saveVideoToFile(list);
              saveFile2Album(videoFile,true);
              Log.i("aaa",""+videoFile);*/

              // 创建文件夹，在存储卡下
             /* dirName = Environment.getExternalStorageDirectory().getPath()+ "/tuiyouqian/";
              file = new File(Environment.getExternalStorageDirectory(), "tuiyouqian");*/
              dirName = cameraPath;

              // 下载后的文件名
              fileName = dirName + System.currentTimeMillis()+".mp4";
              Log.i("aaa","文件名字    "+fileName);
               file = new File(fileName);
              if (file.exists()) {
                  // 如果已经存在, 就不下载了, 去播放
                  /*startVideo(fileName);*/
                  new Thread(new Runnable() {
                      @Override
                      public void run() {
                          DOWNLOAD();
                      }
                  }).start();
                  Log.i("aaa","已经下载");
              } else {
                  new Thread(new Runnable() {
                      @Override
                      public void run() {
                          DOWNLOAD();
                      }
                  }).start();
              }
              Log.i("aaa","file路径"+file.getPath().toString());
          }
      });
    }
    // 下载具体操作
    private void DOWNLOAD() {
        try {
            Log.i("aaa","开始下载");
            URL url = new URL(path);
            // 打开连接
            URLConnection conn = url.openConnection();
            // 打开输入流
            InputStream is = conn.getInputStream();
            // 创建字节流
            byte[] bs = new byte[1024];
            int len;
            OutputStream os = new FileOutputStream(fileName);
            // 写数据
            while ((len = is.read(bs)) != -1) {
                os.write(bs, 0, len);
            }
            // 完成后关闭流
            os.close();
            is.close();
            Log.i("aaa","下载完成");
            saveFile2Album(file,true);
            updateUI(this);
            /*updateVideo(Environment.getExternalStorageDirectory().getPath()+ "/推有钱视频");*/
            /*getImageContentValues(MainActivity.this,file,10000);*/
            // 其次把文件插入到系统图库

//            insertIntoMediaStore(MainActivity.this,true,file,123123);
//            scanFile(MainActivity.this,file.getPath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//
public void updateUI(final Context context) {
    ((MainActivity) context).runOnUiThread(new Runnable() {
        @Override
        public void run() {
            //此时已在主线程中，可以更新UI了
            Toast.makeText(context, "下载完成", Toast.LENGTH_SHORT).show();
        }
    });

}
    private void saveFile2Album(File file, boolean video) throws IOException {
        if (file == null){
            Log.i("aaa","空的");
            return;
        }
        Log.i("aaa","不空----"+file.getPath());
        ContentResolver contentResolver = this.getContentResolver();
        ContentValues values = new ContentValues();
        values.put("title", fileName);
        values.put("_display_name", fileName);
        values.put("datetaken", System.currentTimeMillis());
        values.put("date_modified", System.currentTimeMillis());
        values.put("date_added", System.currentTimeMillis());
        values.put("_data", fileName+"");
        values.put("_size", fileName.length());
        Uri uri;
        if (video) {
            values.put("mime_type", "video/mp4");
            values.put("duration", "60000");
            uri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            Log.i("aaa","values     ---"+values);
            Log.i("aaa","file     ---"+file);
            Log.i("aaa","走道了video里面了     ---"+uri);
        } else {
            values.put("mime_type", "image/jpeg");
            uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));

        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(file.getPath()))));

        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(dirName))));
        ContentValues a = new ContentValues();
        values.put(MediaStore.Images.Media.DATA, "file://"+fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "video/mp4");
        Uri r = MainActivity.this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, a);        // 最后通知图库更新
        Log.i("aaa","fileName---"+fileName);
        MainActivity.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + fileName)));

    }
}
