package my.object_detect_app.utils;

import android.graphics.Color;
import android.os.Handler;

import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

/**
 * User: Lizhiguo
 */
public class LoadingUtils {

    public static void duringDialog(ZLoadingDialog dialog, String dialogText, Z_TYPE type){
        dialog.setLoadingBuilder(type)//设置类型
                .setLoadingColor(Color.parseColor("#53406B"))//颜色
                .setHintText(dialogText)
                .setHintTextSize(16) // 设置字体大小 dp
                .setHintTextColor(Color.GRAY)  // 设置字体颜色
                .setCanceledOnTouchOutside(false)
                .show();
    }

    //设置n秒后，取消dialog显示
    public static void cancelSecondDialog(ZLoadingDialog dialog,int n){
        new Handler().postDelayed(new Runnable(){
            public void run() {
                dialog.cancel();
            }
        }, n);
    }

}
