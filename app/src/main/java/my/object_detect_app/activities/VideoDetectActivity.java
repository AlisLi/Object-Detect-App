package my.object_detect_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import my.object_detect_app.R;
import my.object_detect_app.utils.imageSelect.ImageSelector;

/**
 * User: Lizhiguo
 */
public class VideoDetectActivity extends AppCompatActivity {
    private static final int LOCAL_VIDEO_CHOICE_REQUEST_CODE = 0x0000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_detect);
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

                break;

        }
    }
}
