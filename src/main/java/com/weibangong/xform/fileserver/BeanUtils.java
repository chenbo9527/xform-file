package com.weibangong.xform.fileserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BeanUtils {

    public static Object filter(Object obj, Set<String> exclude, Map<String, String> rename) {
        if (obj instanceof List) {
            return filterList((List) obj, exclude, rename);
        } else if (obj instanceof Map) {
            return filterMap((Map) obj, exclude, rename);
        } else {
            return null;
        }
    }

    public static List filterList(List obj, Set<String> exclude, Map<String, String> rename) {
        if (obj == null || obj.size() == 0) {
            return null;
        }
        List list = new ArrayList();
        for (int i = 0; i < obj.size(); i++) {
            if (obj.get(i) instanceof List) {
                list.add(filterList((List) obj.get(i), exclude, rename));
            } else if (obj.get(i) instanceof Map) {
                list.add(filterMap((Map) obj.get(i), exclude, rename));
            } else {
                list.add(obj.get(i));
            }
        }
        return list;
    }

    public static Map filterMap(Map map, Set<String> exclude, Map<String, String> rename) {
        if (map == null || map.keySet().size() == 0) {
            return null;
        }
        Object[] keys = map.keySet().toArray();
        for (int i = 0; i < keys.length; i++) {
            if (exclude.contains(keys[i].toString()) || map.get(keys[i]) == null) {
                map.remove(keys[i]);
                continue;
            }
            if (map.get(keys[i]) instanceof Map) {
                map.put(keys[i], filterMap((Map) map.get(keys[i]), exclude, rename));
            } else if (map.get(keys[i]) instanceof List) {
                map.put(keys[i], filterList((List) map.get(keys[i]), exclude, rename));
            }
            if (rename.containsKey(keys[i])) {
                Object value = map.get(keys[i]);
                map.remove(keys[i]);
                map.put(rename.get(keys[i]), value);
            }
        }
        return map;
    }

}
