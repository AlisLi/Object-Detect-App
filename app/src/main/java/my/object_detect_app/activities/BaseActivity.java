package my.object_detect_app.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import static my.object_detect_app.Config.LOGGING_TAG;

/**
 * User: Lizhiguo
 */
public class BaseActivity extends AppCompatActivity {

    private Handler handler;
    private HandlerThread handlerThread;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

        //创建名为inference的线程
        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    @Override
    public synchronized void onPause() {
        if (!isFinishing()) {
            finish();
        }
        //停止线程
        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (final InterruptedException ex) {
            Log.e(LOGGING_TAG, "Exception: " + ex.getMessage());
        }

        super.onPause();
    }

    protected synchronized void runInBackground(final Runnable runnable) {
        if (handler != null) {
            handler.post(runnable);
        }
    }


}
