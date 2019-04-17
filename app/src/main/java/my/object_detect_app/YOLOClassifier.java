package my.object_detect_app;

import org.apache.commons.math3.analysis.function.Sigmoid;
import org.tensorflow.Operation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Vector;

import my.object_detect_app.entity.BoundingBox;
import my.object_detect_app.entity.BoxPosition;
import my.object_detect_app.entity.Recognition;
import my.object_detect_app.utils.math.ArgMax;
import my.object_detect_app.utils.math.SoftMax;

/**
 * 实现YOLO v2分类器
 * User: Lizhiguo
 */
public class YOLOClassifier {
    private final static float OVERLAP_THRESHOLD = 0.5f;    //重叠阈
    private final static double anchors[] = {1.08,1.19,  3.42,4.41,  6.63,11.38,  9.42,5.11,  16.62,10.52};
    private final static int SIZE = 13;         // YOLO最后输出的feature map的cell的size
    private final static int MAX_RECOGNIZED_CLASSES = 13;   // 每个cell预测的最大类别数
    private final static float THRESHOLD = 0.3f;            //类别置信度阈值
    private final static int MAX_RESULTS = 15;
    private final static int NUMBER_OF_BOUNDING_BOX = 5;    //每个cell预测的bounding box的数量
    private static YOLOClassifier classifier;

    private YOLOClassifier() {}

    public static YOLOClassifier getInstance() {
        if (classifier == null) {
            classifier = new YOLOClassifier();
        }

        return  classifier;
    }

    /**
     * 获取输出的结果的size：将tensor shape (13 * 13 * 125)-> int
     *
     * @param operation tensorflow operation object
     * @return the number of classes
     */
    public int getOutputSizeByShape(final Operation operation) {
        return (int) (operation.output(0).shape().size(3) * Math.pow(SIZE,2));
    }

    /**
     * It classifies the object/objects on the image
     *
     * @param tensorFlowOutput output from the tensorflow, it is a 13x13x125 tensor
     * 125 = (numClass +  Tx, Ty, Tw, Th, To) * 5 - cause we have 5 boxes per each cell
     * @param labels a string vector with the labels
     * @return a list of recognition objects
     */
    public List<Recognition> classifyImage(final float[] tensorFlowOutput, final Vector<String> labels) {
        // 类别数量  20
        int numClass = (int) (tensorFlowOutput.length / (Math.pow(SIZE,2) * NUMBER_OF_BOUNDING_BOX) - 5);
        // 预测的BoundingBox
        BoundingBox[][][] boundingBoxPerCell = new BoundingBox[SIZE][SIZE][NUMBER_OF_BOUNDING_BOX];
        // 预测类别队列
        PriorityQueue<Recognition> priorityQueue = new PriorityQueue<>(MAX_RECOGNIZED_CLASSES, new RecognitionComparator());
        // 初始化偏差变量
        int offset = 0;
        for (int cy=0; cy<SIZE; cy++) {        // SIZE * SIZE cells
            for (int cx=0; cx<SIZE; cx++) {
                for (int b=0; b<NUMBER_OF_BOUNDING_BOX; b++) {   // 5 bounding boxes per each cell
                    boundingBoxPerCell[cx][cy][b] = getModel(tensorFlowOutput, cx, cy, b, numClass, offset);
                    calculateTopPredictions(boundingBoxPerCell[cx][cy][b], priorityQueue, labels);      //计算最高置信度的类别
                    offset = offset + numClass + 5;
                }
            }
        }

        return getRecognition(priorityQueue);
    }

    private BoundingBox getModel(final float[] tensorFlowOutput, int cx, int cy, int b, int numClass, int offset) {
        BoundingBox model = new BoundingBox();
        Sigmoid sigmoid = new Sigmoid();    //激活函数，将结果映射到0~1之间
        model.setX((cx + sigmoid.value(tensorFlowOutput[offset])) * 32);    //*32 是因为最后一层相比原图像缩小了32倍
        model.setY((cy + sigmoid.value(tensorFlowOutput[offset + 1])) * 32);
        model.setWidth(Math.exp(tensorFlowOutput[offset + 2]) * anchors[2 * b] * 32);
        model.setHeight(Math.exp(tensorFlowOutput[offset + 3]) * anchors[2 * b + 1] * 32);
        model.setConfidence(sigmoid.value(tensorFlowOutput[offset + 4]));       // 是否存在目标的置信度

        model.setClasses(new double[numClass]);

        for (int probIndex=0; probIndex<numClass; probIndex++) {
            model.getClasses()[probIndex] = tensorFlowOutput[probIndex + offset + 5];   //获取numClass个类别的置信度
        }

        return model;
    }

    private void calculateTopPredictions(final BoundingBox boundingBox, final PriorityQueue<Recognition> predictionQueue,
                                         final Vector<String> labels) {
        for (int i=0; i<boundingBox.getClasses().length; i++) {
            ArgMax.Result argMax = new ArgMax(new SoftMax(boundingBox.getClasses()).getValue()).getResult();
            double confidenceInClass = argMax.getMaxValue() * boundingBox.getConfidence();      //计算类别置信度

            /**
             * 当类别置信度大于0.3
             * 绘制方框，创建辨别类实例
              */
            if (confidenceInClass > THRESHOLD) {
                predictionQueue.add(new Recognition(argMax.getIndex(), labels.get(argMax.getIndex()), (float) confidenceInClass,
                        new BoxPosition((float) (boundingBox.getX() - boundingBox.getWidth() / 2),
                                (float) (boundingBox.getY() - boundingBox.getHeight() / 2),
                                (float) boundingBox.getWidth(),
                                (float) boundingBox.getHeight())));
            }
        }
    }

    private List<Recognition> getRecognition(final PriorityQueue<Recognition> priorityQueue) {
        List<Recognition> recognitions = new ArrayList();

        if (priorityQueue.size() > 0) {
            // 获取最好的识别结果
            Recognition bestRecognition = priorityQueue.poll();
            recognitions.add(bestRecognition);

            // 最多预测MAX_RESULTS个结果，至少重叠阈值应该大于OVERLAP_THRESHOLD
            for (int i = 0; i < Math.min(priorityQueue.size(), MAX_RESULTS); ++i) {
                Recognition recognition = priorityQueue.poll();
                boolean overlaps = false;
                // 只要后面的方框和前面的任意一个方框overlaps > 0.5,则overlaps=true
                for (Recognition previousRecognition : recognitions) {
                    overlaps = overlaps || (getIntersectionProportion(previousRecognition.getLocation(),
                            recognition.getLocation()) > OVERLAP_THRESHOLD);
                }

                if (!overlaps) {
                    recognitions.add(recognition);
                }
            }
        }

        return recognitions;
    }

    private float getIntersectionProportion(BoxPosition primaryShape, BoxPosition secondaryShape) {
        if (overlaps(primaryShape, secondaryShape)) {
            // 计算两个方框的重叠度
            float intersectionSurface = Math.max(0, Math.min(primaryShape.getRight(), secondaryShape.getRight()) - Math.max(primaryShape.getLeft(), secondaryShape.getLeft())) *
                    Math.max(0, Math.min(primaryShape.getBottom(), secondaryShape.getBottom()) - Math.max(primaryShape.getTop(), secondaryShape.getTop()));

            float surfacePrimary = Math.abs(primaryShape.getRight() - primaryShape.getLeft()) * Math.abs(primaryShape.getBottom() - primaryShape.getTop());

            return intersectionSurface / surfacePrimary;
        }

        return 0f;

    }

    // 判断两个方框是否有重叠
    private boolean overlaps(BoxPosition primary, BoxPosition secondary) {
        return primary.getLeft() < secondary.getRight() && primary.getRight() > secondary.getLeft()
                && primary.getTop() < secondary.getBottom() && primary.getBottom() > secondary.getTop();
    }

    // 将置信度最高的类别放到队列前面
    private class RecognitionComparator implements Comparator<Recognition> {
        @Override
        public int compare(final Recognition recognition1, final Recognition recognition2) {
            return Float.compare(recognition2.getConfidence(), recognition1.getConfidence());
        }
    }

}
