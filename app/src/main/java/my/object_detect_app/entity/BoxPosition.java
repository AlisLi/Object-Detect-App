package my.object_detect_app.entity;

/**
 * Model：用于保存方框的位置
 * User: Lizhiguo
 */

public class BoxPosition {
    private float left;     // 方框左上角  左边坐标
    private float top;      // 方框左上角  上边坐标
    private float right;    // 方框右下角 右边坐标
    private float bottom;   // 方框右下角 下边坐标
    private float width;    // 方框的宽度
    private float height;   // 方框的高度

    public BoxPosition(float left, float top, float width, float height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;

        init();
    }

    public BoxPosition(BoxPosition boxPosition) {
        this.left = boxPosition.left;
        this.top = boxPosition.top;
        this.width = boxPosition.width;
        this.height = boxPosition.height;

        init();
    }

    public void init() {
        float tmpLeft = this.left;
        float tmpTop = this.top;
        float tmpRight = this.left + this.width;
        float tmpBottom = this.top + this.height;

        this.left = Math.min(tmpLeft, tmpRight); // 确保左边始终小于右边
        this.top = Math.min(tmpTop, tmpBottom);  // 确保上边始终小于下边
        this.right = Math.max(tmpLeft, tmpRight);
        this.bottom = Math.max(tmpTop, tmpBottom);
    }

    public float getLeft() {
        return left;
    }

    public float getTop() {
        return top;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getRight() {
        return right;
    }

    public float getBottom() {
        return bottom;
    }

    @Override
    public String toString() {
        return "BoxPosition{" +
                "left=" + left +
                ", top=" + top +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
