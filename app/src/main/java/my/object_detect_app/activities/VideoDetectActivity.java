package my.object_detect_app.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import my.object_detect_app.R;
import my.object_detect_app.utils.MediaHelper;
import my.object_detect_app.utils.imageSelect.ImageSelector;
import my.object_detect_app.view.videoPlayer.VideoPlayer;

/**
 * User: Lizhiguo
 */
public class VideoDetectActivity extends AppCompatActivity {
    private static final int LOCAL_VIDEO_CHOICE_REQUEST_CODE = 0x0000;

    private ArrayList<String> videoPaths;

    private ArrayList<Bitmap> videoBitmaps;

    private VideoPlayer mVideoPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   //屏幕保持不暗不关闭

        setContentView(R.layout.activity_video_detect);

        mVideoPlayer = findViewById(R.id.video_player);


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

                    videoBitmaps = getBitmapsFromVideo(videoPaths.get(0));

                    //将获取的视频显示到Fragment
                    setVideoFragment();


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
