package testPackage.generalTests;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import direnaj.util.TextUtils;

public class Deneme {

	public static void main(String[] args) throws InterruptedException, ParseException {
		// define limits
		List<String> limits = new ArrayList<>();
		// limits between 0 - 1000
		int previous = 0;
		for (int i = 100; i <= 1000; i = i + 100) {
			limits.add(previous + "-" + i);
			previous = i + 1;
		}
		// limits between 1000 - 10000
		previous = 1001;
		for (int i = 2000; i <= 10000; i = i + 1000) {
			limits.add(previous + "-" + i);
			previous = i + 1;
		}
		limits.add("10000-50000");
		limits.add("50001-100000");
		// limits between 100.000 - 1.000.000
		previous = 100001;
		for (int i = 200000; i <= 1000000; i = i + 100000) {
			limits.add(previous + "-" + i);
			previous = i + 1;
		}
		limits.add("1000001-...");
		
		for(String limit : limits){
			System.out.println(limit);
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
