package direnaj.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumberUtils {

	public static double roundDouble(int decimalDigit, double number) {
		try {
			BigDecimal bd = new BigDecimal(number);
			bd = bd.setScale(decimalDigit, RoundingMode.HALF_UP);
			return bd.doubleValue();
		} catch (Exception e) {
			return 0d;
		}
	}

	public static double roundDouble(double number) {
		return roundDouble(2, number);
	}
}
