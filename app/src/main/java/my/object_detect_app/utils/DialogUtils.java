package my.object_detect_app.utils;

import androidx.appcompat.app.AppCompatActivity;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * User: Lizhiguo
 */
public class DialogUtils {

    public static void basicMessage(AppCompatActivity activity, String message){
        new SweetAlertDialog(activity)
                .setTitleText(message)
                .show();
    }

}
