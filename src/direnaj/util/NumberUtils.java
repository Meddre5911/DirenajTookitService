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

	public static double roundDouble(int decimalDigit, double number, double maxNumber) {
		try {
			BigDecimal bd = new BigDecimal(number);
			bd = bd.setScale(decimalDigit, RoundingMode.HALF_UP);
			double value = bd.doubleValue();
			if (value > maxNumber) {
				return maxNumber;
			}
			return value;
		} catch (Exception e) {
			return 0d;
		}
	}

	public static double roundDouble(double number) {
		return roundDouble(2, number);
	}
	
	public static void main(String[] args) {
		double d = 0.9d;
		System.out.println(NumberUtils.roundDouble(1,d));
	}
}
