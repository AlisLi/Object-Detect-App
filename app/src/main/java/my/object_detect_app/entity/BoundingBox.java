package my.object_detect_app.entity;

/**
 * Model：用于存储方框box的数据
 * User: Lizhiguo
 */
public class BoundingBox {
    private double x;               //centerX
    private double y;               //centerY
    private double width;           // 宽
    private double height;          // 高
    private double confidence;      // 置信度
    private double[] classes;       // 类别

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public double[] getClasses() {
        return classes;
    }

    public void setClasses(double[] classes) {
        this.classes = classes;
    }
}
