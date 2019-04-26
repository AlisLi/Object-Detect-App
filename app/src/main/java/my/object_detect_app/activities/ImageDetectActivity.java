package my.object_detect_app.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import my.object_detect_app.R;
import my.object_detect_app.TensorFlowImageRecognizer;
import my.object_detect_app.entity.Recognition;
import my.object_detect_app.utils.ImageUtils;
import my.object_detect_app.utils.imageSelect.ImageSelector;
import my.object_detect_app.view.ImageFragment;
import my.object_detect_app.view.OverlayView;

import static my.object_detect_app.Config.INPUT_SIZE;
import static my.object_detect_app.Config.LOGGING_TAG;
import static my.object_detect_app.utils.imageSelect.ImageUtils.getLocalBitmap;

/**
 * User: Lizhiguo
 */
public class ImageDetectActivity extends AppCompatActivity {
    private static final int LOCAL_IMAGE_CHOICE_REQUEST_CODE = 0x0100;

    private static boolean FLAG_IS_LOCAL_IMAGE = true;

    private Bitmap imageBitmap;     //原图像的bitmap
    private int imageWidth;         //原图像的宽
    private int imageHeight;        //原图像的高
    private Bitmap croppedBitmap;   // tensorflow 需要的图像尺寸
    private Matrix toCropTransform;  // 原图像转换为tensorflow图像的方式
    private boolean MAINTAIN_ASPECT = true; // 保持图像原比例

    private ArrayList<String> imagePaths;

    private long lastProcessingTimeMs;  // 检测时间
    private TensorFlowImageRecognizer recognizer;

    private OverlayView overlayView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detect);

        croppedBitmap = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Bitmap.Config.ARGB_8888);

        recognizer = TensorFlowImageRecognizer.create(getAssets());

    }

    @Override
    protected void onResume() {
        super.onResume();
        overlayView = findViewById(R.id.overlay_image);
    }

    // 选择本地图片
    public void choicePhoto(View view){
        Intent intent = new Intent(this, ImageSelectorActivity.class);

        intent.putExtra(ImageSelector.IS_SINGLE, true);     //设置直选一张图片
        intent.putExtra(ImageSelector.MAX_SELECT_COUNT, 1);     //最大数量1张
        intent.putExtra(ImageSelector.IS_VIEW_IMAGE, true);     //可以放大预览图片
        intent.putExtra(ImageSelector.USE_CAMERA, false);       //不使用照相机

        startActivityForResult(intent, LOCAL_IMAGE_CHOICE_REQUEST_CODE);
    }

    // 生成检测结果
    public void getLocalDetectResult(View view){
        if(FLAG_IS_LOCAL_IMAGE){
            /**
             * 本地检测照片
              */

            // 处理图像
            cropBitmap();

            //
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final long startTime = SystemClock.uptimeMillis();
                    final List<Recognition> results = recognizer.recognizeImage(croppedBitmap);
                    Log.i(LOGGING_TAG, "detect image size : " + results.size());
                    lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            overlayView.setResults(results);
                            requestRender();
                        }
                    });
                }
            }).start();

        }else{
            // 检测视频
        }
    }

    public void requestRender() {
        final OverlayView overlay = (OverlayView) findViewById(R.id.overlay_image);
        if (overlay != null) {
            overlay.postInvalidate();
        }
    }


    private void cropBitmap() {
        // 获取原图像的宽和高
         imageWidth = imageBitmap.getWidth();
         imageHeight = imageBitmap.getHeight();

        toCropTransform = ImageUtils.getTransformationMatrix(imageWidth, imageHeight,
                INPUT_SIZE, INPUT_SIZE, 0, MAINTAIN_ASPECT);

        new Canvas(croppedBitmap).drawBitmap(imageBitmap, toCropTransform, null);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            // 选择图片
            case LOCAL_IMAGE_CHOICE_REQUEST_CODE:
                if(resultCode == RESULT_OK){
                    // 获取传回的图片路径
                    imagePaths = data.getStringArrayListExtra(ImageSelector.SELECT_RESULT);

                    //将获取的图片显示到Fragment
                    setImageFragment(imagePaths.get(0));

                    //获取图片的Bitmap
                    imageBitmap = getLocalBitmap(imagePaths.get(0));

                    //更改Flag
                    FLAG_IS_LOCAL_IMAGE = true;

                }
                break;
             //
        }
    }

    /**
     * Fragment替换为ImageFragment
     */
    public void setImageFragment(String imagePath){

        ImageFragment imageFragment = new ImageFragment();
        Bundle bundle = new Bundle();
        bundle.putString("imagePath", imagePath);
        imageFragment.setArguments(bundle);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.photo_container, imageFragment)
                .commit();
    }

}
