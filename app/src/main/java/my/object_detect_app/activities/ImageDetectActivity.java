package my.object_detect_app.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import my.object_detect_app.R;
import my.object_detect_app.TensorFlowImageRecognizer;
import my.object_detect_app.entity.Recognition;
import my.object_detect_app.utils.DialogUtils;
import my.object_detect_app.utils.ImageUtils;
import my.object_detect_app.utils.LoadingUtils;
import my.object_detect_app.utils.PathUtils;
import my.object_detect_app.utils.imageSelect.ImageSelector;
import my.object_detect_app.view.ImageFragment;
import my.object_detect_app.view.OverlayView;
import okhttp3.Call;
import okhttp3.Response;

import static my.object_detect_app.Config.DIALOG_DETECTING;
import static my.object_detect_app.Config.DIALOG_DOWNLOADED;
import static my.object_detect_app.Config.DIALOG_DOWNLOADING;
import static my.object_detect_app.Config.DIALOG_DOWNLOAD_ERROR;
import static my.object_detect_app.Config.IMAGE_HAS_DETECTED;
import static my.object_detect_app.Config.INPUT_SIZE;
import static my.object_detect_app.Config.LOGGING_TAG;
import static my.object_detect_app.Config.NET_URL;
import static my.object_detect_app.Config.PLEASE_CHOICE_IMAGE;
import static my.object_detect_app.utils.imageSelect.ImageUtils.getLocalBitmap;

/**
 * User: Lizhiguo
 */
public class ImageDetectActivity extends AppCompatActivity {
    private static final String TAG = "ImageDetectActivity";
    private static final int LOCAL_IMAGE_CHOICE_REQUEST_CODE = 0x0100;

    private Bitmap imageBitmap;     //原图像的bitmap
    private int imageWidth;         //原图像的宽
    private int imageHeight;        //原图像的高
    private Bitmap croppedBitmap;   // tensorflow 需要的图像尺寸
    private Matrix toCropTransform;  // 原图像转换为tensorflow图像的方式
    private boolean MAINTAIN_ASPECT = true; // 保持图像原比例

    private ArrayList<String> imagePaths;

    private long lastProcessingTimeMs;  // 检测时间
    private TensorFlowImageRecognizer recognizer;

    private ZLoadingDialog mDialog;

    private OverlayView overlayView;

    private boolean hasDetected;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detect);

        mDialog = new ZLoadingDialog(ImageDetectActivity.this);

        croppedBitmap = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Bitmap.Config.ARGB_8888);

        recognizer = TensorFlowImageRecognizer.create(getAssets());

    }

    @Override
    protected void onResume() {
        super.onResume();
        overlayView = findViewById(R.id.overlay_image);
    }

    public void netDetect(View view){
        if(hasDetected){
            // 提示已经处理过
            DialogUtils.basicMessage(this, IMAGE_HAS_DETECTED);
            return;
        }

        if (imagePaths == null || imagePaths.size() <= 0){
            // 图片未读取
            DialogUtils.basicMessage(this, PLEASE_CHOICE_IMAGE);
            return;
        }

        LoadingUtils.duringDialog(mDialog, DIALOG_DETECTING, Z_TYPE.SNAKE_CIRCLE);

        File file = new File(imagePaths.get(0));
        Log.i(TAG, "netDetect()");

        OkHttpUtils.post()//
                .addFile("file", file.getName(), file)//
                .addParams("algorithmName", "faster-rcnn")
                .addParams("net", "zf")
                .addParams("rotation", "0")
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

                        Log.d(TAG, "download parseNetworkResponse()");
                        Log.d(TAG, "response header  = " + response.header("Content-disposition"));

                        String fileName = response.header("Content-disposition").split(";")[1];

                        InputStream is = null;
                        byte[] buf = new byte[2048];
                        int len = 0;
                        FileOutputStream fos = null;
                        String resultImagePath = PathUtils.getSDPath() + "/DCIM/Camera/" + fileName;
                        try {
                            double current = 0;
                            double total = response.body().contentLength();

                            is = response.body().byteStream();
                            File file = new File(resultImagePath);
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
                        hasDetected = true;

                        // 将图片显示为结果图片
                        setImageFragment(resultImagePath);

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

    // 选择本地图片
    public void choicePhoto(View view){
        Intent intent = new Intent(this, ImageSelectorActivity.class);

        intent.putExtra(ImageSelector.IS_SINGLE, true);     //设置直选一张图片
        intent.putExtra(ImageSelector.MAX_SELECT_COUNT, 1);     //最大数量1张
        intent.putExtra(ImageSelector.IS_VIEW_IMAGE, true);     //可以放大预览图片
        intent.putExtra(ImageSelector.USE_CAMERA, false);       //不使用照相机

        startActivityForResult(intent, LOCAL_IMAGE_CHOICE_REQUEST_CODE);
    }

    // 生成本地检测结果
    public void getLocalDetectResult(View view){
        if(hasDetected){
            // 提示已经处理过
            DialogUtils.basicMessage(this, IMAGE_HAS_DETECTED);
            return;
        }

        if (imagePaths == null || imagePaths.size() <= 0){
            // 图片未读取
            DialogUtils.basicMessage(this, PLEASE_CHOICE_IMAGE);
            return;
        }

        hasDetected = true;

        //本地检测照片

        // 处理图像
        cropBitmap();

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
                    hasDetected = false;

                    // 获取传回的图片路径
                    imagePaths = data.getStringArrayListExtra(ImageSelector.SELECT_RESULT);

                    //将获取的图片显示到Fragment
                    setImageFragment(imagePaths.get(0));

                    //获取图片的Bitmap
                    imageBitmap = getLocalBitmap(imagePaths.get(0));

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
