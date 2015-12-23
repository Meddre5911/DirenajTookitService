package direnaj.util;

public class TextUtils {

	public static boolean isEmpty(String str) {
		if (str != null && !str.trim().equals("") && !str.trim().equals("null")) {
			return false;
		}
		return true;
	}

	public static String getNotNullValue(Object value) {
		if (value == null) {
			return "";
		}
		if (isEmpty(value.toString())) {
			return "";
		}
		return value.toString();
	}

	public static Long getLongValue(String value) {
		try {
			return Long.valueOf(value);
		} catch (Exception e) {
			return new Long(0);
		}
	}

	public static Integer getIntegerValue(String value) {
		try {
			return Integer.valueOf(value);
		} catch (Exception e) {
			return new Integer(0);
		}
	}

	public static Double getDoubleValue(String value) {
		try {
			return Double.valueOf(value);
		} catch (Exception e) {
			return new Double(0);
		}
	}
}
