package direnaj.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

public class CollectionUtil {

    public static <T> ArrayList<Map.Entry<T, BigDecimal>> sortValues(Hashtable<T, BigDecimal> t, boolean isDescending) {
        // Transfer as List and sort it
        ArrayList<Entry<T, BigDecimal>> l = new ArrayList<Entry<T, BigDecimal>>(t.entrySet());
        if (isDescending) {

            Collections.sort(l, new Comparator<Map.Entry<T, BigDecimal>>() {
                public int compare(Map.Entry<T, BigDecimal> o1, Map.Entry<T, BigDecimal> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }
            });
        } else {
            Collections.sort(l, new Comparator<Map.Entry<T, BigDecimal>>() {
                public int compare(Map.Entry<T, BigDecimal> o1, Map.Entry<T, BigDecimal> o2) {
                    return o1.getValue().compareTo(o2.getValue());
                }
            });
        }
        return l;
    }

    public static <T> ArrayList<Map.Entry<T, Integer>> sortCounts(Hashtable<T, Integer> t) {
        // Transfer as List and sort it
        ArrayList<Entry<T, Integer>> l = new ArrayList<Entry<T, Integer>>(t.entrySet());
        Collections.sort(l, new Comparator<Map.Entry<T, Integer>>() {
            public int compare(Map.Entry<T, Integer> o1, Map.Entry<T, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        return l;
    }

}
