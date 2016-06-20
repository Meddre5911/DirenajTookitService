package testPackage.generalTests;

import java.math.BigDecimal;
import java.math.RoundingMode;

import direnaj.util.TextUtils;

public class Deneme {

	public static void main(String[] args) {
		double d = 1.4423076923076923d;
		
		BigDecimal bd = new BigDecimal(d);
	    bd = bd.setScale(3, RoundingMode.HALF_UP);
		System.out.println(bd.doubleValue());

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
