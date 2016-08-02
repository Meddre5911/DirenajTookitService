package testPackage.generalTests;

import java.math.BigDecimal;
import java.math.RoundingMode;

import direnaj.util.TextUtils;

public class Deneme {

	public static void main(String[] args) throws InterruptedException {
		Deneme d = new Deneme();
		synchronized (String.class) {
			d.wait();
		}

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
