package my.object_detect_app.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Build;
import android.os.Bundle;
import android.util.Size;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import my.object_detect_app.R;
import my.object_detect_app.view.CameraConnectionFragment;
import my.object_detect_app.view.OverlayView;

/**
 * Camera activity class.
 * User: Lizhiguo
 */
public abstract class CameraActivity extends BaseActivity implements OnImageAvailableListener {
    private static final int PERMISSIONS_REQUEST = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   //屏幕保持不暗不关闭

        setContentView(R.layout.activity_camera);

        if(hasPermission()){
            setFragment();
        }else {
            requestPermission();
        }
    }



    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions,
                                           final int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    setFragment();
                } else {
                    requestPermission();
                }
            }
        }
    }


    protected void setFragment() {
        CameraConnectionFragment cameraConnectionFragment = new CameraConnectionFragment();
        cameraConnectionFragment.addConnectionListener((final Size size, final int rotation) ->
                CameraActivity.this.onPreviewSizeChosen(size, rotation));
        cameraConnectionFragment.addImageAvailableListener(this);

        /**
         * 使用另一个Fragment替换当前的
         */
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, cameraConnectionFragment)
                .commit();
    }

    /**
     * 要求获取相机和存储权限
     */
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
                    || shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(CameraActivity.this,
                        "Camera AND storage permission are required for this demo", Toast.LENGTH_LONG).show();
            }
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST);
        }
    }

    /**
     * 检查是否有权限
     * @return
     */
    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    public void addCallback(final OverlayView.DrawCallback callback) {
        final OverlayView overlay = (OverlayView) findViewById(R.id.overlay);
        if (overlay != null) {
            overlay.addCallback(callback);
        }
    }

    public void requestRender() {
        final OverlayView overlay = (OverlayView) findViewById(R.id.overlay);
        if (overlay != null) {
            overlay.postInvalidate();
        }
    }

    protected abstract void onPreviewSizeChosen(final Size size, final int rotation);

}
