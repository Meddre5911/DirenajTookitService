package testPackage.generalTests;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;

import direnaj.util.TextUtils;

public class Deneme {

	public static void main(String[] args) throws InterruptedException, ParseException {

		double value = 0.6505190311418685d;
		DecimalFormat df = new DecimalFormat("0.0000");
		String formate = df.format(value);
		double finalValue = (Double) df.parse(formate);
		System.out.println("Final Value : " + finalValue);

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
