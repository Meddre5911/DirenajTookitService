package testPackage.generalTests;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import direnaj.util.TextUtils;

public class Deneme {

	public static void main(String[] args) throws InterruptedException, ParseException {

		List<String> allTweetIds = new ArrayList<>();
		allTweetIds.add("0");
		allTweetIds.add("1");
		allTweetIds.add("2");
		allTweetIds.add("3");
		allTweetIds.add("4");
		allTweetIds.add("5");
		allTweetIds.add("6");
		allTweetIds.add("7");
		allTweetIds.add("8");
		
		
		int oneThreadTweetSize2Compare = allTweetIds.size()/4;
		List<String> subList = allTweetIds.subList(0, oneThreadTweetSize2Compare);
		List<String> subList2 = allTweetIds.subList(oneThreadTweetSize2Compare, 2*oneThreadTweetSize2Compare);
		List<String> subList3 = allTweetIds.subList( 2*oneThreadTweetSize2Compare,  3*oneThreadTweetSize2Compare);
		List<String> subList4 = allTweetIds.subList( 3*oneThreadTweetSize2Compare,  allTweetIds.size());
		
		
		System.out.println(subList);
		System.out.println(subList2);
		System.out.println(subList3);
		System.out.println(subList4);
		
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
