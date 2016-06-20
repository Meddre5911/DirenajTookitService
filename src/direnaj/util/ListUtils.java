package direnaj.util;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {
    //
    //	public static <T> LinkedHashMap<T, Integer> sortHashMapByValuesD(HashMap<T,String> passedMap) {
    //		List<T> mapKeys = new ArrayList(passedMap.keySet());
    //		List<Integer> mapValues = new ArrayList(passedMap.values());
    //		Collections.sort(mapValues);
    //		Collections.sort(mapKeys);
    //
    //		LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
    //
    //		Iterator valueIt = mapValues.iterator();
    //		while (valueIt.hasNext()) {
    //			Object val = valueIt.next();
    //			Iterator keyIt = mapKeys.iterator();
    //
    //			while (keyIt.hasNext()) {
    //				Object key = keyIt.next();
    //				String comp1 = passedMap.get(key).toString();
    //				String comp2 = val.toString();
    //
    //				if (comp1.equals(comp2)) {
    //					sortedMap.put((String) key, (Integer) val);
    //					break;
    //				}
    //
    //			}
    //
    //		}
    //		return sortedMap;
    //	}

    public static List<String> getListOfStrings(String... values) {
        List<String> list = new ArrayList<>();
        for (String value : values) {
            list.add(value);
        }
        return list;
    }
    
    public static <T> List<T> getListOfObjects(@SuppressWarnings("unchecked") T... values) {
        List<T> list = new ArrayList<>();
        for (T value : values) {
            list.add(value);
        }
        return list;
    }
}
