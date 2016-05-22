package testPackage.generalTests;

import direnaj.util.TextUtils;

public class Deneme {

	public static void main(String[] args) {
		String str = "kjdfhk";

		str = Deneme.parseInvalidChars(str);
		System.out.println(str);

	}

	public static String parseInvalidChars(String str) {
		if (TextUtils.isEmpty(str) || !str.startsWith("$")) {
			return str;
		}
		if (str.startsWith("$")) {
			str = str.substring(1);
		}
		return parseInvalidChars(str);

	}

}
