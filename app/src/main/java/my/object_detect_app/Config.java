package my.object_detect_app;

/**
 * 配置接口
 * User: Lizhiguo
 */
public interface Config {
    int INPUT_SIZE = 416;   // The input size. A square image of inputSize x inputSize is assumed.
    int IMAGE_MEAN = 128;   // The assumed mean of the image values.
    float IMAGE_STD = 128.0f;   // The assumed std of the image values.
    String MODEL_FILE = "tiny-yolo-voc-graph.pb";   // The filepath of the model GraphDef protocol buffer.
    String LABEL_FILE = "tiny-yolo-voc-labels.txt"; // The filepath of label file for classes.
    String INPUT_NAME = "input";    // The label of the image input node.
    String OUTPUT_NAME = "output"; // The label of the output node.

    String LOGGING_TAG = "Object-Detect-App";

    String NET_URL = "http://10.10.97.16:8080/ObjectDetectService/";
//    String NET_URL = "http://192.168.43.58:8080/ObjectDetectService/";

    // dialog text

    // 下载
    String DIALOG_DETECTING = "检测中。。。";
    String DIALOG_DOWNLOADING = "下载中。。。";
    String DIALOG_DOWNLOADED = "下载完成";
    String DIALOG_DOWNLOAD_ERROR = "下载失败";

    // Message
    String VIDEO_HAS_DETECTED = "视频已检测，请重新选择！";
    String IMAGE_HAS_DETECTED = "图片已检测，请重新选择！";
    String PLEASE_CHOICE_VIDEO = "请先选择视频！";
    String PLEASE_CHOICE_IMAGE = "请先选择照片！";

}
