package testPackage.generalTests;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import direnaj.util.TextUtils;

public class Deneme {

	public static void main(String[] args) throws InterruptedException, ParseException {

		
//		System.out.println("lkjdsl : " + "lkjdsl".matches("[^a-zA-Z0-9]*[a-zA-Z0-9]+[^a-zA-Z0-9]*"));
//		System.out.println("   : " + "   ".matches("[^a-zA-Z0-9]*[a-zA-Z0-9]+[^a-zA-Z0-9]*"));
//		System.out.println("k998j : " + "k998j".matches("[^a-zA-Z0-9]*[a-zA-Z0-9]+[^a-zA-Z0-9]*"));
//		System.out.println("#4yiu : " + "#4yiu ".matches("[^a-zA-Z0-9]*[a-zA-Z0-9]+[^a-zA-Z0-9]*"));
//		System.out.println("???___ : " + "???___".matches("[^a-zA-Z0-9]*[a-zA-Z0-9]+[^a-zA-Z0-9]*"));
		
		
		String retweetInfo = "7557.0-%83.74194061927884";
		String[] split = retweetInfo.split("-");
		double retweetCount = Double.valueOf(split[0]);
		double retweetPercentage = Double.valueOf(split[1].substring(1));
		
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
