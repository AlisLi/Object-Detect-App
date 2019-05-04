package my.object_detect_app.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import my.object_detect_app.R;
import my.object_detect_app.utils.LoadingUtils;
import my.object_detect_app.utils.MediaHelper;
import my.object_detect_app.utils.PathUtils;
import my.object_detect_app.utils.imageSelect.ImageSelector;
import my.object_detect_app.view.videoPlayer.VideoPlayer;
import okhttp3.Call;
import okhttp3.Response;

import static my.object_detect_app.Config.DIALOG_DETECTING;
import static my.object_detect_app.Config.DIALOG_DOWNLOADED;
import static my.object_detect_app.Config.DIALOG_DOWNLOADING;
import static my.object_detect_app.Config.DIALOG_DOWNLOAD_ERROR;
import static my.object_detect_app.Config.NET_URL;

/**
 * User: Lizhiguo
 */
public class VideoDetectActivity extends AppCompatActivity {
    private static final String TAG = "VideoDetectActivity";
    private static final int LOCAL_VIDEO_CHOICE_REQUEST_CODE = 0x0000;

    private ArrayList<String> videoPaths;

    private ArrayList<Bitmap> videoBitmaps;

    private VideoPlayer mVideoPlayer;

    private ZLoadingDialog mDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   //屏幕保持不暗不关闭

        setContentView(R.layout.activity_video_detect);

        mVideoPlayer = findViewById(R.id.video_player);
        mDialog = new ZLoadingDialog(VideoDetectActivity.this);


    }

    @Override
    protected void onPause() {
        super.onPause();
        if(MediaHelper.getInstance().isPlaying()){
            mVideoPlayer.mediaController.play();
        }
    }

    public void choiceLocalVideo(View view){
        Intent intent = new Intent(this, VideoSelectorActivity.class);

        intent.putExtra(ImageSelector.IS_SINGLE, true);     //设置直选一张图片
        intent.putExtra(ImageSelector.MAX_SELECT_COUNT, 1);     //最大数量1张
        intent.putExtra(ImageSelector.IS_VIEW_IMAGE, true);     //可以放大预览图片
        intent.putExtra(ImageSelector.USE_CAMERA, false);       //不使用照相机

        startActivityForResult(intent, LOCAL_VIDEO_CHOICE_REQUEST_CODE);
    }

    public void getDetectResult(View view){
        // 检测时，如果视频播放，则暂停
        if(MediaHelper.getInstance().isPlaying()){
            mVideoPlayer.mediaController.play();
        }

        LoadingUtils.duringDialog(mDialog, DIALOG_DETECTING, Z_TYPE.SNAKE_CIRCLE);

        File file = new File(videoPaths.get(0));
        Log.i(TAG, "netDetect()");

        OkHttpUtils.post()//
                .addFile("file", file.getName(), file)//
                .addParams("algorithmName", "faster-rcnn")
                .addParams("net", "zf")
                .url(NET_URL + "algorithm/upload")
                .build()//
                .connTimeOut(200000000)
                .readTimeOut(200000000)
                .writeTimeOut(200000000)
                .execute(new Callback() {
                    @Override
                    public Object parseNetworkResponse(Response response, int id) throws Exception {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mDialog.cancel();
                                LoadingUtils.duringDialog(mDialog, DIALOG_DOWNLOADING, Z_TYPE.INFECTION_BALL);
                            }
                        });
                        Log.i(TAG, "response.headers() ： " + response.headers().toString());
                        String fileName = response.header("Content-disposition").split(";")[1];

                        InputStream is = null;
                        byte[] buf = new byte[2048];
                        int len = 0;
                        FileOutputStream fos = null;
                        String resultVideoPath = PathUtils.getSDPath() + "/DCIM/Camera/" + fileName;
                        try {
                            double current = 0;
                            double total = response.body().contentLength();

                            is = response.body().byteStream();
                            File file = new File(resultVideoPath);
                            Log.d(TAG,"file path is : " + PathUtils.getSDPath());

                            fos = new FileOutputStream(file);
                            while ((len = is.read(buf)) != -1) {
                                current += len;
                                fos.write(buf, 0, len);
                                Log.i(TAG, "download current------>" + current);
                            }

                            fos.flush();

                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (is != null) is.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                if (fos != null) fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mDialog.cancel();
                                LoadingUtils.duringDialog(mDialog, DIALOG_DOWNLOADED, Z_TYPE.LEAF_ROTATE);
                                LoadingUtils.cancelSecondDialog(mDialog, 1000);
                            }
                        });

                        //将视频显示为处理过后的视频
                        // 设置是一个新的视频
                        mVideoPlayer.mediaController.setIsNewVideo(true);
                        // 获取视频路径
                        mVideoPlayer.setVideoPath(resultVideoPath);
                        // 设置视频操作可见
                        mVideoPlayer.setVisibility(View.VISIBLE);
                        //设置为初始化状态
                        mVideoPlayer.initViewDisplay();

                        return null;
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mDialog.cancel();
                                LoadingUtils.duringDialog(mDialog, DIALOG_DOWNLOAD_ERROR, Z_TYPE.LEAF_ROTATE);
                                LoadingUtils.cancelSecondDialog(mDialog, 1000);
                            }
                        });
                        Log.d(TAG, "download error()");
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Object response, int id) {
                        Log.d(TAG, "download onResponse()");
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case LOCAL_VIDEO_CHOICE_REQUEST_CODE:
                if(resultCode == RESULT_OK){
                    // 获取传回的视频路径
                    videoPaths = data.getStringArrayListExtra(ImageSelector.SELECT_RESULT);

                    // 设置是一个新的视频
                    mVideoPlayer.mediaController.setIsNewVideo(true);
                    // 获取视频路径
                    mVideoPlayer.setVideoPath(videoPaths.get(0));
                    // 设置视频操作可见
                    mVideoPlayer.setVisibility(View.VISIBLE);
                    //设置为初始化状态
                    mVideoPlayer.initViewDisplay();


                }
                break;
        }
    }

    //将获取的视频显示到Fragment
    private void setVideoFragment(){

    }

    // 从Video中提取Bitmap
    public ArrayList<Bitmap> getBitmapsFromVideo(String videoPath) {
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoPath);
        // 取得视频的长度(单位为毫秒)
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        // 取得视频的长度(单位为秒)
        int seconds = Integer.valueOf(time) / 1000;
        // 得到每一秒时刻的bitmap比如第一秒,第二秒
        for (int i = 1; i <= seconds; i++) {
            Bitmap bitmap = retriever.getFrameAtTime(i * 1000 * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            bitmaps.add(bitmap);
        }
        return bitmaps;

    }

    public void replay(View view){
        mVideoPlayer.mediaController.replay();
    }

    public void play(View view){
        mVideoPlayer.mediaController.play();
    }
}
