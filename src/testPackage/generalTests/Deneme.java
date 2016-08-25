package testPackage.generalTests;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;

import direnaj.util.TextUtils;

public class Deneme {

	public static void main(String[] args) throws InterruptedException, ParseException {

		String str = "aa";
		str.substring(0, 500);
		

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
