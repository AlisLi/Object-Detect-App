package my.object_detect_app.activities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.media.Image;
import android.media.ImageReader;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import my.object_detect_app.R;
import my.object_detect_app.TensorFlowImageRecognizer;
import my.object_detect_app.entity.Recognition;
import my.object_detect_app.utils.ImageUtils;
import my.object_detect_app.view.OverlayView;
import my.object_detect_app.view.components.BorderedText;

import static my.object_detect_app.Config.INPUT_SIZE;
import static my.object_detect_app.Config.LOGGING_TAG;

/**
 * 分类器活动类
 * User: Lizhiguo
 */
public class ClassifierActivity extends TextToSpeechActivity implements ImageReader.OnImageAvailableListener {
    private boolean MAINTAIN_ASPECT = true;
    private float TEXT_SIZE_DIP = 10;               //设置字体大小为10px

    private TensorFlowImageRecognizer recognizer;
    private Integer sensorOrientation;
    private int previewWidth = 0;
    private int previewHeight = 0;
    private Bitmap croppedBitmap = null;
    private boolean computing = false;
    private Matrix frameToCropTransform;

    private OverlayView overlayView;
    private BorderedText borderedText;
    private long lastProcessingTimeMs;

    @Override
    protected void onPreviewSizeChosen(Size size, int rotation) {
        //  把Android系统中的非标准度量尺寸px转变为标准度量尺寸dp
        final float textSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        recognizer = TensorFlowImageRecognizer.create(getAssets());

        overlayView = (OverlayView) findViewById(R.id.overlay);
        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        final int screenOrientation = getWindowManager().getDefaultDisplay().getRotation();

        Log.i(LOGGING_TAG, String.format("Sensor orientation: %d, Screen orientation: %d",
                rotation, screenOrientation));

        sensorOrientation = rotation + screenOrientation;

        Log.i(LOGGING_TAG, String.format("Initializing at size %dx%d", previewWidth, previewHeight));

        croppedBitmap = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Bitmap.Config.ARGB_8888);
        // 将拍摄的图片转换为 tensorflow 输入的尺寸，保持比例不变
        frameToCropTransform = ImageUtils.getTransformationMatrix(previewWidth, previewHeight,
                INPUT_SIZE, INPUT_SIZE, sensorOrientation, MAINTAIN_ASPECT);
        frameToCropTransform.invert(new Matrix());

        addCallback((final Canvas canvas) -> renderAdditionalInformation(canvas));
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        Image image = null;

        try {
            image = reader.acquireLatestImage();
            //Log.i(LOGGING_TAG, "the Original image's height is" + image.getHeight() + ",and the Original image's width is" + image.getWidth());

            if (image == null) {
                return;
            }

            if (computing) {
                image.close();
                return;
            }

            computing = true;
            fillCroppedBitmap(image);
            image.close();
        } catch (final Exception ex) {
            if (image != null) {
                image.close();
            }
            Log.e(LOGGING_TAG, ex.getMessage());
        }

        runInBackground(() -> {
            final long startTime = SystemClock.uptimeMillis();
            final List<Recognition> results = recognizer.recognizeImage(croppedBitmap);
            lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
            overlayView.setResults(results);
            speak(results);
            requestRender();
            computing = false;
        });
    }

    private void fillCroppedBitmap(final Image image) {
        Bitmap rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
        //截取摄像头拍摄图片，起始位置：（0，0），宽高：（previewWidth，previewHeight）
        rgbFrameBitmap.setPixels(ImageUtils.convertYUVToARGB(image, previewWidth, previewHeight),
                0, previewWidth, 0, 0, previewWidth, previewHeight);
        new Canvas(croppedBitmap).drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recognizer != null) {
            recognizer.close();
        }
    }

    private void renderAdditionalInformation(final Canvas canvas) {
        final Vector<String> lines = new Vector();
        if (recognizer != null) {
            for (String line : recognizer.getStatString().split("\n")) {
                lines.add(line);
            }
        }
        lines.add("Frame: " + previewWidth + "x" + previewHeight);
        lines.add("Rotation: " + sensorOrientation);
        lines.add("Inference time: " + lastProcessingTimeMs + "ms");

        // 统计类别个数
        List<Recognition> results = this.overlayView.getResults();
        if (results != null){
            List<String> resultsString = new ArrayList<>();
            for(Recognition result : results){
                resultsString.add(result.getTitle());
            }
            Set uniqueSet = new HashSet(resultsString);

            for (Object temp : uniqueSet){
                lines.add(temp + ": " + Collections.frequency(resultsString, temp) + "个");
            }
        }

        borderedText.drawLines(canvas, 10, 10, lines);
    }
}
