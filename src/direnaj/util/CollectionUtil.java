package direnaj.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

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

    public static Map<String, Double> sortByComparator(Map<String, Double> unsortMap) {
        // Convert Map to List
        List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(unsortMap.entrySet());
        // Sort list with comparator, to compare the Map values
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });
        // Convert sorted map back to a Map
        Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
        for (Iterator<Map.Entry<String, Double>> it = list.iterator(); it.hasNext();) {
            Map.Entry<String, Double> entry = it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    public static void incrementKeyValueInMap(TreeMap<String, Double> map, String key) {
        if (map.containsKey(key)) {
            Double count = map.get(key) + 1;
            map.put(key, count);
        } else {
            map.put(key, new Double(1));
        }
    }

    public static TreeMap<String, Double> discardOtherElementsOfMap(Map<String, Double> map, int discardElementsAfter) {
        TreeMap<String, Double> newMap = new TreeMap<>();
        Set<String> keySet = map.keySet();
        int counter = 0;
        for (String key : keySet) {
            newMap.put(key, map.get(key));
            counter++;
            if (counter == discardElementsAfter) {
                break;
            }
        }
        map.clear();
        return newMap;
    }

}
