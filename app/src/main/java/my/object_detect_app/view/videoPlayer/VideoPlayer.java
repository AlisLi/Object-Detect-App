package my.object_detect_app.view.videoPlayer;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.RelativeLayout;

import my.object_detect_app.R;
import my.object_detect_app.utils.MediaHelper;

/**
 * User: Lizhiguo
 */
public class VideoPlayer extends RelativeLayout {
    private static final String  TAG = "VideoPlayer";

    private String videoPath;
    public TextureView videoView;
    public VideoMediaController mediaController;

    public MediaPlayer mPlayer;
    private Surface mSurface;

    public boolean hasPlay;//是否播放了

    public VideoPlayer(Context context) {
        this(context, null);
    }

    public VideoPlayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    //初始化布局
    private void initView() {
        View view = View.inflate(getContext(), R.layout.video_player, this);

        videoView = view.findViewById(R.id.video_view);
        mediaController = view.findViewById(R.id.mediaController);

        initViewDisplay();
        //把VideoPlayer对象传递给VideoMediaController
        mediaController.setVideoPlayer(this);

        //进行TextureView控件创建的监听
        videoView.setSurfaceTextureListener(surfaceTextureListener);
    }

    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {

        //创建完成  TextureView才可以进行视频画面的显示
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.i(TAG,"onSurfaceTextureAvailable");
            mSurface = new Surface(surface);//连接对象（MediaPlayer和TextureView）
            play(videoPath);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            Log.i(TAG,"onSurfaceTextureSizeChanged");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            Log.i(TAG,"onSurfaceTextureDestroyed");
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            Log.i(TAG,"onSurfaceTextureUpdated");
            if(mediaController.getIsNewVideo()) {
                play(videoPath);
                mediaController.setIsNewVideo(false);
            }
        }
    };

    //视频播放（视频的初始化）
    private void play(String url){
        try {
            mPlayer = MediaHelper.getInstance();
            mPlayer.reset();
            mPlayer.setDataSource(url);
            //让MediaPlayer和TextureView进行视频画面的结合
            mPlayer.setSurface(mSurface);
            //设置监听
            mPlayer.setOnBufferingUpdateListener(onBufferingUpdateListener);
            mPlayer.setOnCompletionListener(onCompletionListener);
            mPlayer.setOnErrorListener(onErrorListener);
            mPlayer.setOnPreparedListener(onPreparedListener);
            mPlayer.setScreenOnWhilePlaying(true);//在视频播放的时候保持屏幕的高亮
            //异步准备
            mPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //准备完成监听
    private MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            Log.i(TAG, "onPreparedListener");
            //隐藏视频加载进度条
            mediaController.setPbLoadingVisiable(View.GONE);
            //进行视频的播放
            MediaHelper.play();
            hasPlay = true;
            //隐藏标题
            mediaController.delayHideTitle();
            //设置视频的总时长
            mediaController.setDuration(mPlayer.getDuration());
            //更新播放的时间和进度
            mediaController.updatePlayTimeAndProgress();
        }
    };

    //错误监听
    private MediaPlayer.OnErrorListener onErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            return true;
        }
    };

    //完成监听
    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            //视频播放完成
            mediaController.showPlayFinishView();
        }
    };

    //缓冲的监听
    private MediaPlayer.OnBufferingUpdateListener onBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
//            Log.i(TAG,"percent:"+percent);
            mediaController.updateSeekBarSecondProgress(percent);
        }
    };


    //初始化控件的显示状态
    public void initViewDisplay() {
        videoView.setVisibility(View.GONE);
        mediaController.initViewDisplay();
    }

    //设置视频播放界面的显示
    public void setVideoViewVisiable(int visible) {
        videoView.setVisibility(View.VISIBLE);
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }


}

