package my.object_detect_app.utils;

import java.util.HashSet;
import java.util.List;

/**
 * User: Lizhiguo
 */
public class CommonUtils {

    public static List removeDuplicate(List list) {
        HashSet h = new HashSet(list);
        list.clear();
        list.addAll(h);
        return list;
    }

}
