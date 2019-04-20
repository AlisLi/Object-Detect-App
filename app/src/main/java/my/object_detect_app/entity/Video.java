package my.object_detect_app.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 视频实体类
 * User: Lizhiguo
 */
public class Video implements Parcelable {

    private String thumbnailPath;   //缩略图路径
    private String videoPath;       //视频路径
    private long time;              //时间
    private String name;            //视频名称
    private String mimeType;        //视频类型

    public Video(String thumbnailPath, String videoPath, long time, String name, String mimeType) {
        this.thumbnailPath = thumbnailPath;
        this.videoPath = videoPath;
        this.time = time;
        this.name = name;
        this.mimeType = mimeType;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public String toString() {
        return "Video{" +
                "thumbnailPath='" + thumbnailPath + '\'' +
                ", videoPath='" + videoPath + '\'' +
                ", time=" + time +
                ", name='" + name + '\'' +
                ", mimeType='" + mimeType + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.thumbnailPath);
        parcel.writeString(this.videoPath);
        parcel.writeLong(this.time);
        parcel.writeString(this.name);
        parcel.writeString(this.mimeType);
    }

    protected Video(Parcel parcel){
        this.thumbnailPath = parcel.readString();
        this.videoPath = parcel.readString();
        this.time = parcel.readLong();
        this.name = parcel.readString();
        this.mimeType = parcel.readString();
    }

    public static final Creator<Video> CREATOR = new Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel parcel) {
            return new Video(parcel);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };

}
