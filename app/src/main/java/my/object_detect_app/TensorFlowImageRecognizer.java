package my.object_detect_app;

import android.content.res.AssetManager;
import android.graphics.Bitmap;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.util.List;
import java.util.Vector;

import my.object_detect_app.entity.Recognition;
import my.object_detect_app.utils.ClassAttrProvider;

import static my.object_detect_app.Config.IMAGE_MEAN;
import static my.object_detect_app.Config.IMAGE_STD;
import static my.object_detect_app.Config.INPUT_NAME;
import static my.object_detect_app.Config.INPUT_SIZE;
import static my.object_detect_app.Config.MODEL_FILE;
import static my.object_detect_app.Config.OUTPUT_NAME;

/**
 * Tensorflow 分类器
 * User: Lizhiguo
 */
public class TensorFlowImageRecognizer {
    private int outputSize;
    private Vector<String> labels;
    private TensorFlowInferenceInterface inferenceInterface;

    private TensorFlowImageRecognizer() {
    }

    /**
     * Initializes ClassAttrProvider native TensorFlow session for classifying images.
     * @param assetManager The asset manager to be used to load assets.
     */
    public static TensorFlowImageRecognizer create(AssetManager assetManager) {
        TensorFlowImageRecognizer recognizer = new TensorFlowImageRecognizer();
        //获取label和label对应的颜色
        recognizer.labels = ClassAttrProvider.newInstance(assetManager).getLabels();
        // 调用训练好的model
        recognizer.inferenceInterface = new TensorFlowInferenceInterface(assetManager,
                "file:///android_asset/" + MODEL_FILE);
        //获取yolo最后一层输出的size
        recognizer.outputSize = YOLOClassifier.getInstance()
                .getOutputSizeByShape(recognizer.inferenceInterface.graphOperation(OUTPUT_NAME));
        return recognizer;
    }

    public List<Recognition> recognizeImage(final Bitmap bitmap) {
        return YOLOClassifier.getInstance().classifyImage(runTensorFlow(bitmap), labels);
    }

    public String getStatString() {
        return inferenceInterface.getStatString();
    }

    public void close() {
        inferenceInterface.close();
    }

    // 运行tensorFlow
    private float[] runTensorFlow(final Bitmap bitmap) {
        final float[] tfOutput = new float[outputSize];

        inferenceInterface.feed(INPUT_NAME, processBitmap(bitmap), 1, INPUT_SIZE, INPUT_SIZE, 3);   //3是输入节点的shape


        inferenceInterface.run(new String[]{OUTPUT_NAME});

        // 将输出放入tfOutput
        inferenceInterface.fetch(OUTPUT_NAME, tfOutput);

        return tfOutput;
    }

    /**
     * 归一化图像数据 从0~255 -> 0~1
     *
     * @param bitmap
     */
    private float[] processBitmap(final Bitmap bitmap) {
        int[] intValues = new int[INPUT_SIZE * INPUT_SIZE];
        float[] floatValues = new float[INPUT_SIZE * INPUT_SIZE * 3];   //*3是因为RGB有三个值
        //getPixels()函数把一张图片，从指定的偏移位置（offset），指定的位置（x,y）截取指定的宽高（width,height ），把所得图像的每个像素颜色转为int值，存入intValues。
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            floatValues[i * 3 + 0] = (((val >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD;
            floatValues[i * 3 + 1] = (((val >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD;
            floatValues[i * 3 + 2] = ((val & 0xFF) - IMAGE_MEAN) / IMAGE_STD;
        }
        return floatValues;
    }

}
