package my.object_detect_app.entity;

/**
 * 文件夹实体类
 * User: Lizhiguo
 */
public class Folder {
    protected boolean useCamera; // 是否可以调用相机拍照。只有“全部”文件夹才可以拍照
    protected String name;

    public Folder(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUseCamera() {
        return useCamera;
    }

    public void setUseCamera(boolean useCamera) {
        this.useCamera = useCamera;
    }

}
