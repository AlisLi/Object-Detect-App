package my.object_detect_app.entity;

import java.util.ArrayList;

import my.object_detect_app.utils.imageSelect.StringUtils;

/**
 * User: Lizhiguo
 */
public class ImageFolder extends Folder {
    private ArrayList<Image> images;


    public ImageFolder(String name) {
        super(name);
    }

    public ImageFolder(String name, ArrayList<Image> images) {
        super(name);
        this.images = images;
    }

    public ArrayList<Image> getImages() {
        return images;
    }

    public void setImages(ArrayList<Image> images) {
        this.images = images;
    }

    public void addImage(Image image) {
        if (image != null && StringUtils.isNotEmptyString(image.getPath())) {
            if (images == null) {
                images = new ArrayList<>();
            }
            images.add(image);
        }
    }

    @Override
    public String toString() {
        return "ImageFolder{" +
                "images=" + images +
                ", useCamera=" + useCamera +
                ", name='" + name + '\'' +
                '}';
    }
}
