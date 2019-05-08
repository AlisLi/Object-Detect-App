package my.object_detect_app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import java.util.LinkedList;
import java.util.List;

import my.object_detect_app.Config;
import my.object_detect_app.entity.BoxPosition;
import my.object_detect_app.entity.Recognition;
import my.object_detect_app.utils.ClassAttrProvider;

/**
 * 显示自定义视图
 * User: Lizhiguo
 */
public class OverlayView extends View {
    public static final String TAG = "OverlayView";
    private final Paint paint;
    private final List<DrawCallback> callbacks = new LinkedList();
    private List<Recognition> results;
    private List<Integer> colors;
    private float resultsViewHeight;

    private int rotation = 0;

    public OverlayView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                15, getResources().getDisplayMetrics()));
        resultsViewHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                10, getResources().getDisplayMetrics());
        Log.i(TAG, "resultsViewHeight3 = " + resultsViewHeight);
        colors = ClassAttrProvider.newInstance(context.getAssets()).getColors();
    }

    public void addCallback(final DrawCallback callback) {
        callbacks.add(callback);
    }

    @Override
    public synchronized void onDraw(final Canvas canvas) {
        for (final DrawCallback callback : callbacks) {
            callback.drawCallback(canvas);
        }

        // 绘制预测box和类别以及置信度值
        if (results != null) {
            for (int i = 0; i < results.size(); i++) {
                RectF box = reCalcSize(results.get(i).getLocation());
                String title = results.get(i).getTitle() + ":"
                        + String.format("%.2f", results.get(i).getConfidence());
                paint.setColor(colors.get(results.get(i).getId()));
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(box, paint);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawText(title, box.left, box.top, paint);
            }
        }
    }

    public void setResults(final List<Recognition> results) {
        this.results = results;
        postInvalidate();
    }

    public List<Recognition> getResults(){
        return this.results;
    }

    /**
     * Interface defining the callback for client classes.
     */
    public interface DrawCallback {
        void drawCallback(final Canvas canvas);
    }

    /**
     * 因为预览的长宽和预测图象的长宽不一致，所以要进行相应的微调
     * @param rect
     * @return
     */
    private RectF reCalcSize(BoxPosition rect) {
        Log.i(TAG, "resultsViewHeight2 = " + resultsViewHeight);
        int padding = 5;
        float overlayViewHeight = this.getHeight() - resultsViewHeight;
        float sizeMultiplier = Math.min((float) this.getWidth() / (float) Config.INPUT_SIZE,
                overlayViewHeight / (float) Config.INPUT_SIZE);

        float offsetX = (this.getWidth() - Config.INPUT_SIZE * sizeMultiplier) / 2;
        float offsetY = (overlayViewHeight - Config.INPUT_SIZE * sizeMultiplier) / 2 + resultsViewHeight;

        float left = Math.max(padding,sizeMultiplier * rect.getLeft() + offsetX);
        float top = Math.max(offsetY + padding, sizeMultiplier * rect.getTop() + offsetY);
        Log.i(TAG, "offsetY = " + offsetY);
        Log.i(TAG, "sizeMultiplier = " + sizeMultiplier);

        float right = Math.min(rect.getRight() * sizeMultiplier, this.getWidth() - padding);
        float bottom = Math.min(rect.getBottom() * sizeMultiplier + offsetY, this.getHeight() - padding);

        return new RectF(left, top, right, bottom);
    }

    public void setResultsViewHeight(int resultsViewHeight){
        this.resultsViewHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                resultsViewHeight, getResources().getDisplayMetrics());
        Log.i(TAG, "resultsViewHeight1 = " + this.resultsViewHeight);
    }

    public void setRotation(){
        this.rotation = rotation;
    }


}
