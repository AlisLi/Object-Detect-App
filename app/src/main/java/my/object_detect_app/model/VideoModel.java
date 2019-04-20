package my.object_detect_app.model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import my.object_detect_app.entity.Video;
import my.object_detect_app.entity.VideoFolder;
import my.object_detect_app.utils.imageSelect.StringUtils;

/**
 * User: Lizhiguo
 */
public class VideoModel {

    /**
     * 从SD卡中扫描视频文件
     * @param context
     * @param dataCallback
     */
    public static void loadVideoForSDCard(final Context context, final DataCallback dataCallback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                //扫描视频
                Uri mVideoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                ContentResolver mContentResolver = context.getContentResolver();

                Cursor mCursor = mContentResolver.query(mVideoUri, new String[]{
                                MediaStore.Video.Thumbnails.DATA,      // 视频缩略图的文件路径
                                MediaStore.Video.Media.DATA,           // 视频文件路径
                                MediaStore.Video.Media.DISPLAY_NAME,   // 视频文件名
                                MediaStore.Video.Media.DATE_ADDED,      // 视频时间
                                MediaStore.Video.Media._ID,
                                MediaStore.Video.Media.MIME_TYPE},      // 视频类型
                        null,
                        null,
                        MediaStore.Video.Media.DATE_ADDED);

                ArrayList<Video> videos = new ArrayList<>();

                // 读取扫描到的视频
                if(mCursor != null){
                    while(mCursor.moveToNext()){
                        //获取视频缩略图的文件路径
                        String thumbnailPath = mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.Thumbnails.DATA));
                        //获取视频文件路径
                        String videoPath = mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.Media.DATA));
                        //获取视频文件名
                        String name = mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                        //获取视频时间
                        long time = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));
                        //获取视频类型
                        String mimeType = mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE));
                        //过滤未下载完成或者不存在的文件
                        if (!"downloading".equals(getExtensionName(videoPath)) && checkVideoExists(videoPath)) {
                            videos.add(new Video(thumbnailPath,videoPath,time,name,mimeType));
                        }
                    }
                    mCursor.close();
                }
                Collections.reverse(videos);
                dataCallback.onSuccess(splitFolder(videos));
            }
        }).start();

    }

    /**
     * Java文件操作 获取文件扩展名
     */
    public static String getExtensionName(String filename) {
        if (filename != null && filename.length() > 0) {
            int dot = filename.lastIndexOf('.');
            if (dot > -1 && dot < filename.length() - 1) {
                return filename.substring(dot + 1);
            }
        }
        return "";
    }

    private static boolean checkVideoExists(String filePath){
        return new File(filePath).exists();
    }

    /**
     * 把视频按文件夹拆分，第一个文件夹保存所有的视频
     *
     * @param videos
     * @return
     */
    private static ArrayList<VideoFolder> splitFolder(ArrayList<Video> videos) {
        ArrayList<VideoFolder> folders = new ArrayList<>();
        folders.add(new VideoFolder("全部视频", videos));

        if (videos != null && !videos.isEmpty()) {
            int size = videos.size();
            for (int i = 0; i < size; i++) {
                String videoPath = videos.get(i).getVideoPath();
                String name = getFolderName(videoPath);
                if (StringUtils.isNotEmptyString(name)) {
                    VideoFolder videoFolder = getFolder(name, folders);
                    videoFolder.addVideos(videos.get(i));
                }
            }
        }
        return folders;
    }

    /**
     * 根据视频路径，获取视频文件夹名称
     *
     * @param path
     * @return
     */
    private static String getFolderName(String path) {
        if (StringUtils.isNotEmptyString(path)) {
            String[] strings = path.split(File.separator);
            if (strings.length >= 2) {
                return strings[strings.length - 2];
            }
        }
        return "";
    }

    private static VideoFolder getFolder(String name, List<VideoFolder> folders) {
        if (!folders.isEmpty()) {
            int size = folders.size();
            for (int i = 0; i < size; i++) {
                VideoFolder folder = folders.get(i);
                if (name.equals(folder.getName())) {
                    return folder;
                }
            }
        }
        VideoFolder newFolder = new VideoFolder(name);
        folders.add(newFolder);
        return newFolder;
    }

    public interface DataCallback {
        void onSuccess(ArrayList<VideoFolder> folders);
    }

}
