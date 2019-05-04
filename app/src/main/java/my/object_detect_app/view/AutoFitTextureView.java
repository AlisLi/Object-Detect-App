package my.object_detect_app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

/**
 *
 * User: Lizhiguo
 */
public class AutoFitTextureView extends TextureView{
    private static final String TAG = "AutoFitTextureView";
    private int ratioWidth = 0;
    private int ratioHeight = 0;
    private int rotation = 0;

    public AutoFitTextureView(final Context context) {
        this(context, null);
    }

    public AutoFitTextureView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoFitTextureView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setAspectRatio(final int width, final int height, final int rotation) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        this.ratioWidth = width;
        this.ratioHeight = height;
        this.rotation = rotation;
        requestLayout();
    }

    //重新测量宽高比，以适应不同分辨率的手机
    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = MeasureSpec.getSize(heightMeasureSpec);
        Log.i(TAG, "width = " + width);
        Log.i(TAG, "height = " + height);
//        if (0 == ratioWidth || 0 == ratioHeight) {
//            setMeasuredDimension(width, height);
//        } else {
//            if (width < height * ratioWidth / ratioHeight) {
//                setMeasuredDimension(width, width * ratioHeight / ratioWidth);
//            } else {
//                setMeasuredDimension(height * ratioWidth / ratioHeight, height);
//            }
//        }
        if (0 == ratioWidth || 0 == ratioHeight) {
            setMeasuredDimension(width, height);
        } else {
            // 不论横着，竖着，width总是最大值
            if (rotation==0){
                // 横着拍摄
                setMeasuredDimension(width, width * ratioHeight / ratioWidth);
            }else if(rotation == 90){
                // 竖着拍摄
                setMeasuredDimension(width, width * ratioWidth / ratioHeight);
            }else if(rotation == -1){
                if (width < height * ratioWidth / ratioHeight) {
                    setMeasuredDimension(width, width * ratioHeight / ratioWidth);
                } else {
                    setMeasuredDimension(height * ratioWidth / ratioHeight, height);
                }
            }
        }
    }
}
