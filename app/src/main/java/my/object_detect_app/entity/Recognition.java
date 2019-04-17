package my.object_detect_app.entity;

/**
 * 方框分类识别结果描述类
 * User: Lizhiguo
 */
public final class Recognition {
    /**
     * 唯一的标识符.
     */
    private final Integer id;
    private final String title;
    private final Float confidence;
    private BoxPosition location;

    public Recognition(final Integer id, final String title,
                       final Float confidence, final BoxPosition location) {
        this.id = id;
        this.title = title;
        this.confidence = confidence;
        this.location = location;
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Float getConfidence() {
        return confidence;
    }

    public BoxPosition getLocation() {
        return new BoxPosition(location);
    }

    public void setLocation(BoxPosition location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "Recognition{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", confidence=" + confidence +
                ", location=" + location +
                '}';
    }
}
