package my.object_detect_app.utils.imageSelect;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * 图片工具类
 * User: Lizhiguo
 */
public class ImageUtils {

    /**
     * 获取本地图片
     * @param url
     * @return
     */
    public static Bitmap getLocalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
