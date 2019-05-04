package my.object_detect_app.view;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import my.object_detect_app.R;
import my.object_detect_app.utils.comparator.CompareSizesByArea;
import my.object_detect_app.view.components.ErrorDialog;

import static my.object_detect_app.Config.LOGGING_TAG;

/**
 * Camera connection fragment
 * User: Lizhiguo
 */
public class CameraConnectionFragment extends Fragment {
    private static final Size DESIRED_PREVIEW_SIZE = new Size(1080, 1080);    //默认预览窗口大小

    private static final int MINIMUM_PREVIEW_SIZE = 320; //视频窗口最小尺寸

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();    //将视频角度转换为JPEG图像方向

    static{
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private static final String FRAGMENT_DIALOG = "dialog";

    private final Semaphore cameraOpenCloseLock = new Semaphore(1);

    private OnImageAvailableListener imageListener;     //当视频帧有用时，接收视频帧

    private ConnectionListener cameraConnectionListener;

    private String cameraId;

    private AutoFitTextureView textureView;

    private CameraCaptureSession captureSession;    //向相机设备发送获取图像的请求.

    private CameraDevice cameraDevice;  //安卓设备上的单个相机的抽象表示

    private Integer sensorOrientation;      //摄像机传感器测得的显示屏旋转的度数。

    private Size previewSize;       //预览窗口显示的大小

    private HandlerThread backgroundThread;     //运行任务的线程，不应该被UI阻断

    private Handler backgroundHandler;  //运行任务的handler

    private ImageReader previewReader;  //捕获视频帧为图片

    private CaptureRequest.Builder previewRequestBuilder;

    private CaptureRequest previewRequest;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.camera_connection_fragment, container, false);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        textureView = (AutoFitTextureView) view.findViewById(R.id.texture);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();

        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // ClassAttrProvider camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (textureView.isAvailable()) {
            openCamera(textureView.getWidth(), textureView.getHeight());
        } else {
            textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(final SurfaceTexture texture, final int width, final int height) {
                    openCamera(width, height);
                }

                /**
                 *
                 * @param texture
                 * @param width
                 * @param height
                 */
                @Override
                public void onSurfaceTextureSizeChanged(final SurfaceTexture texture, final int width, final int height) {
                    configureTransform(width, height);
                }

                @Override
                public boolean onSurfaceTextureDestroyed(final SurfaceTexture texture) {
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(final SurfaceTexture texture) {
                }
            });
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    /**
     * 开启后台线程
     */
    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("ImageListener");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    public void addConnectionListener(final ConnectionListener cameraConnectionListener) {
        this.cameraConnectionListener = cameraConnectionListener;
    }

    public void addImageAvailableListener(final OnImageAvailableListener imageListener) {
        this.imageListener = imageListener;
    }

    /**
     * 设置摄像机参数
     * 打开摄像机
     */
    private void openCamera(final int width, final int height) {
        setUpCameraOutputs();
        configureTransform(width, height);
        final Activity activity = getActivity();


        final CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            if (activity.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                /**
                 * 打开相机预览
                 * CameraDevice.StateCallback()：每有一帧画面，都会回调一次此方法
                 */
                manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                    /**
                     * 当相机打开成功之后会回调此方法
                     * @param cameraDevice
                     */
                    @Override
                    public void onOpened(final CameraDevice cameraDevice) {
                        // 一般在此进行获取一个全局的CameraDevice实例，开启相机预览等操作
                        cameraOpenCloseLock.release();
                        CameraConnectionFragment.this.cameraDevice = cameraDevice;  //获取CameraDevice实例
                        createCameraPreviewSession();   //创建相机预览会话
                    }

                    /**
                     * 相机设备失去连接(不能继续使用)时回调此方法，同时当打开相机失败时也会调用此方法而不会调用onOpened()
                     * @param cameraDevice
                     */
                    @Override
                    public void onDisconnected(final CameraDevice cameraDevice) {
                        //释放开关锁，关闭相机
                        cameraOpenCloseLock.release();
                        cameraDevice.close();
                        CameraConnectionFragment.this.cameraDevice = null;
                    }

                    /**
                     * 相机发生错误时调用此方法
                     * @param cameraDevice
                     * @param error
                     */
                    @Override
                    public void onError(final CameraDevice cameraDevice, final int error) {
                        cameraOpenCloseLock.release();
                        cameraDevice.close();
                        CameraConnectionFragment.this.cameraDevice = null;
                        final Activity activity = getActivity();
                        if (null != activity) {
                            activity.finish();
                        }
                    }
                }, backgroundHandler);
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            }
        } catch (final CameraAccessException ex) {
            Log.e(LOGGING_TAG, "Exception: " + ex.getMessage());
        } catch (final InterruptedException ex) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", ex);
        }
    }

    /**
     * 配置相机的参数
     */
    private void setUpCameraOutputs() {
        final CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);   //获取CameraManager
        try {
            for (final String cameraId : manager.getCameraIdList()) {
                //获取相机设备列表
                final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                // 不使用前置摄像头
                final Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                // 获取StreamConfigurationMap，它是管理摄像头支持的所有输出格式和尺寸
                final StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                if (map == null) {
                    continue;
                }
                //获取手机传感器方向
                sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

                //根据TextureView的尺寸设置预览尺寸
                previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class));

                /**
                 * Android中Configuration类专门用于描述手机设备上的配置信息,这些配置信息既包括用户特定的配置项,也包括系统的动态设备配置。
                 *
                 * 得到系统屏幕的方向,该属性将会返回ORIENTATION_LANDSCAPE(横向
                 * 屏幕),ORIENTATION_PORTRAIT(竖向屏幕),ORIENTATION_SQUARE(方
                 * 形屏幕)三个属性值之一
                 */
                final int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    textureView.setAspectRatio(previewSize.getWidth(), previewSize.getHeight(), -1);
                } else {
                    textureView.setAspectRatio(previewSize.getHeight(), previewSize.getWidth(), -1);
                }

                this.cameraId = cameraId;
            }
        } catch (final CameraAccessException ex) {
            Log.e(LOGGING_TAG, "Exception: " + ex.getMessage());
        } catch (final NullPointerException ex) {
            ErrorDialog.newInstance(getString(R.string.camera_error))
                    .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            throw new RuntimeException(getString(R.string.camera_error));
        }

        // 相机捕获的图片的大小确定后，需要对捕获图片做裁剪等预操作。这将回调到ClassifierActivity中。
        cameraConnectionListener.onPreviewSizeChosen(previewSize, sensorOrientation);
    }

    /**
     * 选择一个合适的分辨率
     * 如果相机分辨率和设置的预览分辨率一致，直接返回
     * 如果不相同，则选择最小的并且不低于最小size的分辨率
     * 如果没有则选择choices[0]
     */
    private static Size chooseOptimalSize(final Size[] choices) {
        final int minSize = Math.max(Math.min(DESIRED_PREVIEW_SIZE.getWidth(),
                DESIRED_PREVIEW_SIZE.getHeight()), MINIMUM_PREVIEW_SIZE);

        // Collect the supported resolutions that are at least as big as the preview Surface
        final List<Size> bigEnough = new ArrayList();
        for (final Size option : choices) {
            if (option.equals(DESIRED_PREVIEW_SIZE)) {
                return DESIRED_PREVIEW_SIZE;
            }

            if (option.getHeight() >= minSize && option.getWidth() >= minSize) {
                Log.i(LOGGING_TAG, "option.getHeight() : " + option.getHeight() + ", option.getWidth() : " + option.getWidth());
                bigEnough.add(option);
            }
        }

        // 返回分辨率面积最小的一个，否则返回choices[0]
        return (bigEnough.size() > 0) ? Collections.min(bigEnough, new CompareSizesByArea()) : choices[0];
        //return (bigEnough.size() > 4) ? choices[3] : Collections.min(bigEnough, new CompareSizesByArea());
    }

    /**
     * 对图像进行变换处理
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(final int viewWidth, final int viewHeight) {
        final Activity activity = getActivity();
        if (null == textureView || null == previewSize || null == activity) {
            return;
        }
        //获取屏幕旋转的方向
        final int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        final Matrix matrix = new Matrix();
        final RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        final RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
        final float centerX = viewRect.centerX();
        final float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            final float scale =
                    Math.max(
                            (float) viewHeight / previewSize.getHeight(),
                            (float) viewWidth / previewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        textureView.setTransform(matrix);
    }

    /**
     * 为相机预览创建新的createCameraPreviewSession
     */
    private void createCameraPreviewSession() {
        try {
            final SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;

            // 设置Surface分辨率大小
            texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());

            // 获取Surface实例，使用SurfaceView来显示预览画面
            final Surface surface = new Surface(texture);

            // 设置了一个具有输出Surface的CaptureRequest.Builder。设置捕获请求为预览，这里还有拍照啊，录像等
            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);


            Log.i(LOGGING_TAG, String.format("Opening camera preview: "
                    + previewSize.getWidth() + "x" + previewSize.getHeight()));

            /**
             * ImageReader类允许应用程序直接访问呈现表面的图像数据
             * format：图像的格式
             * maxImages：用户想要读图像的最大数量
             */
            previewReader = ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(),
                    ImageFormat.YUV_420_888, 2);

            previewReader.setOnImageAvailableListener(imageListener, backgroundHandler);
            //这里一定分别add两个surface，一个Textureview的，一个ImageReader的，如果没add，会造成没摄像头预览，或者没有ImageReader的那个回调！！
            previewRequestBuilder.addTarget(surface);
            previewRequestBuilder.addTarget(previewReader.getSurface());

            // 为预览创建CameraCaptureSession
            cameraDevice.createCaptureSession(Arrays.asList(surface, previewReader.getSurface()),
                    getCaptureSessionStateCallback(), null);
        } catch (final CameraAccessException ex) {
            Log.e(LOGGING_TAG, "Exception: " + ex.getMessage());
        }
    }

    private CameraCaptureSession.StateCallback getCaptureSessionStateCallback() {
        return new CameraCaptureSession.StateCallback() {

            @Override
            public void onConfigured(final CameraCaptureSession cameraCaptureSession) {
                // 相机关闭
                if (null == cameraDevice) {
                    return;
                }

                // 会话准备好后，展示展示预览
                captureSession = cameraCaptureSession;
                try {
                    // 自动对焦
                    previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                    // 需要的时候自动打开闪关灯
                    previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                            CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

                    // 给摄像头发起请求预览和图片
                    previewRequest = previewRequestBuilder.build();
                    captureSession.setRepeatingRequest(previewRequest, null, backgroundHandler);
                } catch (final CameraAccessException ex) {
                    Log.e(LOGGING_TAG, "Exception: " + ex.getMessage());
                }
            }

            @Override
            public void onConfigureFailed(final CameraCaptureSession cameraCaptureSession) {
                showToast("Failed");
            }
        };
    }

    private void showToast(final String text) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> Toast.makeText(activity, text, Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * 关闭相机
     */
    private void closeCamera() {
        try {
            cameraOpenCloseLock.acquire();
            if (null != captureSession) {
                captureSession.close();
                captureSession = null;
            }
            if (null != cameraDevice) {
                cameraDevice.close();
                cameraDevice = null;
            }
            if (null != previewReader) {
                previewReader.close();
                previewReader = null;
            }
        } catch (final InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            cameraOpenCloseLock.release();
        }
    }

    /**
     * 停止后台线程
     */
    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (final InterruptedException ex) {
            Log.e(LOGGING_TAG, "Exception: " + ex.getMessage());
        }
    }

    /**
     * 一旦视频窗口尺寸确定，回调给活动，初始化数据
     */
    public interface ConnectionListener {
        void onPreviewSizeChosen(Size size, int cameraRotation);
    }


}
