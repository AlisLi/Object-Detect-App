package my.object_detect_app.entity;

import java.util.ArrayList;

import my.object_detect_app.utils.imageSelect.StringUtils;

/**
 * User: Lizhiguo
 */
public class VideoFolder extends Folder {
    private ArrayList<Video> videos;

    public VideoFolder(String name) {
        super(name);
    }

    public VideoFolder(String name, ArrayList<Video> videos) {
        super(name);
        this.videos = videos;
    }

    public ArrayList<Video> getVideos() {
        return videos;
    }

    public void setVideos(ArrayList<Video> videos) {
        this.videos = videos;
    }

    public void addVideos(Video video){
        if (video != null && StringUtils.isNotEmptyString(video.getVideoPath())) {
            if (videos == null) {
                videos = new ArrayList<>();
            }
            videos.add(video);
        }
    }

    @Override
    public String toString() {
        return "VideoFolder{" +
                "videos=" + videos +
                ", useCamera=" + useCamera +
                ", name='" + name + '\'' +
                '}';
    }
}
