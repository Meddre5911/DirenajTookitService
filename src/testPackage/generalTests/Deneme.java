package testPackage.generalTests;

import java.math.BigDecimal;
import java.math.RoundingMode;

import direnaj.util.TextUtils;

public class Deneme {

	public static void main(String[] args) throws InterruptedException {
		
		Double d1 = new Double(4);
		Double d2 = new Double(3);
		System.out.println(d1+d2);

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
