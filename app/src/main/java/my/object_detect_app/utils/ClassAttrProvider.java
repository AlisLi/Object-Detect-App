package my.object_detect_app.utils;

import android.content.res.AssetManager;
import android.graphics.Color;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import my.object_detect_app.Config;

/**
 * 从assets 文件下读取class的名字
 * 以及每一个类方框的color
 * User: Lizhiguo
 */
public class ClassAttrProvider {
    private final Vector<String> labels = new Vector();     //标签队列
    private final Vector<Integer> colors = new Vector();    //颜色队列
    private static ClassAttrProvider instance;

    private ClassAttrProvider(final AssetManager assetManager) {
        init(assetManager);
    }

    public static ClassAttrProvider newInstance(final AssetManager assetManager) {
        if (instance == null) {
            instance = new ClassAttrProvider(assetManager);
        }

        return instance;
    }

    /**
     * 从assets中读取label的数据
     * @param assetManager
     */
    private void init(final AssetManager assetManager) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(assetManager.open(Config.LABEL_FILE)))) {
            String line;
            while ((line = br.readLine()) != null) {
                labels.add(line);
                colors.add(convertClassNameToColor(line));
            }
        } catch (IOException ex) {
            throw new RuntimeException("Problem reading label file!", ex);
        }
    }

    /**
     * 将label名字转换为颜色
     * @param className
     * @return
     */
    private int convertClassNameToColor(String className) {
        byte[] rgb = new byte[3];
        byte[] name = className.getBytes();

        for (int i=0; i<name.length; i++) {
            rgb[i%3] += name[i];
        }

        // Hue saturation
        for (int i=0; i<rgb.length; i++) {
            if (rgb[i] < 120) {
                rgb[i] += 120;
            }
        }

        return Color.rgb(rgb[0], rgb[1], rgb[2]);
    }

    public Vector<String> getLabels() {
        return labels;
    }

    public Vector<Integer> getColors() {
        return colors;
    }
}
