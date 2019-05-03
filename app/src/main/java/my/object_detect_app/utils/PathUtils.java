package my.object_detect_app.utils;

import android.os.Environment;

import java.io.File;

/**
 * User: Lizhiguo
 */
public class PathUtils {


    /**
     * 获取手机拍照存储路径
     * @return
     */
    public static String getSDPath(){
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED);//判断sd卡是否存在
        if(sdCardExist)
        {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir.toString();
    }

}
