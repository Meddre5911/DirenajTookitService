package direnaj.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
				return (o2.getValue()).compareTo(o1.getValue());
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

	public static <T> void incrementKeyValueInMap(Map<T, Double> map, T key) {
		if (map.containsKey(key)) {
			Double count = map.get(key) + 1;
			map.put(key, count);
		} else {
			map.put(key, new Double(1));
		}
	}

	public static <T> void calculatePercentage(Map<T, Double> map, double totalNumber) {
		for (Entry<T, Double> entry : map.entrySet()) {
			Double value = entry.getValue();
			Double percentage = NumberUtils.roundDouble(2, (value * 100d) / totalNumber);
			map.put(entry.getKey(), percentage);
		}
	}

	public static Map<String, Double> sortByComparator4DateKey(Map<String, Double> unsortMap) {
		// Convert Map to List
		List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(unsortMap.entrySet());
		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
				try {
					Date o1Date = DateTimeUtils.getDate("yyyyMM", o1.getKey());
					Date o2Date = DateTimeUtils.getDate("yyyyMM", o2.getKey());
					return (o1Date).compareTo(o2Date);
				} catch (Exception e) {
					e.printStackTrace();
					return 0;
				}
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

	public static Map<Double, Double> sortByComparator4Key(Map<Double, Double> unsortMap) {
		// Convert Map to List
		List<Map.Entry<Double, Double>> list = new LinkedList<Map.Entry<Double, Double>>(unsortMap.entrySet());
		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<Double, Double>>() {
			public int compare(Map.Entry<Double, Double> o1, Map.Entry<Double, Double> o2) {
				try {
					return o1.getKey().compareTo(o2.getKey());
				} catch (Exception e) {
					e.printStackTrace();
					return 0;
				}
			}
		});
		// Convert sorted map back to a Map
		Map<Double, Double> sortedMap = new LinkedHashMap<Double, Double>();
		for (Iterator<Map.Entry<Double, Double>> it = list.iterator(); it.hasNext();) {
			Map.Entry<Double, Double> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

	public static LinkedHashMap<String, Double> discardOtherElementsOfMap(Map<String, Double> map,
			int discardElementsAfter) {
		LinkedHashMap<String, Double> newMap = new LinkedHashMap<>();
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

	public static void findGenericRange(List<String> rangeList, Map<String, Double> map, double value) {
		for (String range : rangeList) {
			if (range.contains("-")) {
				String[] ranges = range.split("-");
				double lowerRange = Integer.valueOf(ranges[0]);
				double upperRange;
				if ("...".equals(ranges[1])) {
					upperRange = 9999999;
				} else {
					upperRange = Integer.valueOf(ranges[1]);
				}
				if (value <= upperRange && value >= lowerRange) {
					incrementKeyValueInMap(map, range);
				}
			} else {
				if(Double.valueOf(range).equals(value))
				incrementKeyValueInMap(map, range);
			}
		}

	}

}
