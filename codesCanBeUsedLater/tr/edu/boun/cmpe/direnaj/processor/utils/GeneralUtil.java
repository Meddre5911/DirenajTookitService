package tr.edu.boun.cmpe.direnaj.processor.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.language.Soundex;
import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.bag.TreeBag;
import org.apache.commons.lang.StringUtils;

import com.twitter.Extractor;

import tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization.Token;
import tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization.Tokenizer;
import tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization.labeler.HashTagLabeler;
import tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization.labeler.Labeler;
import tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization.labeler.MentionLabeler;
import tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization.labeler.MultiLabeler;
import tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization.labeler.NonAsciiLabeler;
import tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization.labeler.StopwordLabeler;
import tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization.labeler.UrlLabeler;
import tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization.labeler.WordLabeler;

/**
 * FIXME Comment'lenen Tag s�n�flar�n�n Direnaj'daki yeri analiz edilip, ona
 * g�re kod yazilacak
 */
public final class GeneralUtil {

	public static final String TWITTER_SIGN_HASHTAG = "#",
			TWITTER_SIGN_MENTION = "@";

	public static Extractor extractor = new Extractor();

	public static final char DELIMITER_COMMA = ',', DELIMITER_SPACE = ' ';

	private static final char QUOTATION_MARK = '\'';

	private static final String DELIMITER_OR = " OR ";
	public static final String DELIMITER_SPACE_STR = String
			.valueOf(DELIMITER_SPACE);

	public static final String DELIMITER_UNDERSCORE = "_";

	public static final String DELIMITER_DASH = "-";

	private static final String QUOT_COMMA_QUOT = "\',\'";

	public static final String LANG_ISO_639_1_ENGLISH = "en";

	/**
	 * 
	 * @param word
	 * @return
	 */
	public static String asHashtag(String word) {
		return TWITTER_SIGN_HASHTAG.concat(word
				.replace(DELIMITER_SPACE_STR, "").replace("-", ""));
	}

	/**
	 * 
	 * @param str
	 * @return
	 */
	public static String lowerCase(String str) {
		return StringUtils.lowerCase(str, Locale.ENGLISH);
	}

	public static List<Token> toLabeledTokens(String str) throws Exception {

		List<Token> tweetTokens = Tokenizer.tokenize(str);

		MultiLabeler multilabeler = new MultiLabeler(
				Arrays.asList(new Labeler[] { new StopwordLabeler(),
						new HashTagLabeler(), new MentionLabeler(),
						new UrlLabeler(), new WordLabeler(),
						new NonAsciiLabeler() }));

		tweetTokens = multilabeler.label(tweetTokens);

		/*
		 * for (Token token : tweetTokens) { if
		 * (token.getLabel()==Label.UNKNOWN) { List<String> extractedWords =
		 * WordLabeler.extractWords(token.getValue()); if
		 * (extractedWords.size()>1) {
		 * 
		 * } } }
		 */
		return tweetTokens;
	}

	/**
	 * 
	 * @param tokens
	 * @return
	 */

	/*
	 * public static String toCSV(Bag<Token> tokens) { return
	 * toSeparatedValue(tokens, DELIMITER_COMMA); }
	 */

	public static String toSpaceSeparated(Bag<Token> tokens) {
		return toSeparatedValue(tokens, DELIMITER_SPACE);
	}

	/**
	 * 
	 * @param tokens
	 * @return
	 */
	private static String toSeparatedValue(Bag<Token> tokens, char delimeter) {

		if (tokens == null || tokens.size() == 0) {
			return null;
		}

		StringBuilder builder = new StringBuilder();

		for (Token token : tokens) {
			builder.append(token.getValue()).append(delimeter);
		}

		return exceptLastChar(builder);
	}

	/**
	 * 
	 * @param stringSet
	 * @return
	 */
	public static String joinWithSpace(Collection<?> collection) {
		return StringUtils.join(collection, String.valueOf(DELIMITER_SPACE));
	}

	/**
	 * 
	 * @param stringSet
	 * @return
	 */
	public static String joinWithOr(Collection<?> collection) {
		return StringUtils.join(collection, DELIMITER_OR);
	}

	/**
	 * 
	 * @param stringSet
	 * @return
	 */
	public static String joinWithComma(Collection<?> collection) {
		return StringUtils.join(collection, DELIMITER_COMMA);
	}

	/**
	 * 
	 * @param stringSet
	 * @return
	 */
	public static String joinWithCommaForSqlIn(Collection<?> collection) {
		return new StringBuilder().append(QUOTATION_MARK)
				.append(StringUtils.join(collection, QUOT_COMMA_QUOT))
				.append(QUOTATION_MARK).toString();
	}

	/**
	 * @deprecated Unused method
	 * 
	 * @param stringSet
	 * @param delimiter
	 * @return
	 */
	public static String toStringDelimited_(Set<String> stringSet,
			String delimiter) {

		StringBuilder builder = new StringBuilder();

		for (String string : stringSet) {
			builder.append(string).append(delimiter);
		}
		return exceptLastNChar(builder, delimiter.length());
	}

	/**
	 * 
	 * @param builder
	 * @return
	 */
	private static String exceptLastChar(StringBuilder builder) {
		return exceptLastNChar(builder, 1);
	}

	/**
	 * 
	 * @param stringBuilder
	 * @return
	 */
	private static String exceptLastNChar(StringBuilder stringBuilder,
			int nChars) {
		return stringBuilder.toString().substring(0,
				stringBuilder.length() - nChars);
	}

	/**
	 * 
	 * @param bag
	 * @param minOccurrence
	 * @return
	 */
	public static Bag<String> findTopElems(Bag<String> bag, int minOccurrence) {
		return findTopElems(bag, minOccurrence, null);
	}

	/**
	 * 
	 * @param bag
	 * @param occurrence
	 * @return
	 */
	public static Bag<String> findTopElems(Bag<String> bag, int minOccurrence,
			List<String> blackList) {

		Bag<String> topBag = new TreeBag<String>();

		int count = 0;
		for (String string : bag.uniqueSet()) {

			count = bag.getCount(string);

			if (!topBag.contains(string) && count >= minOccurrence) {

				if (blackList != null && blackList.contains(string)) {
					continue;
				}
				topBag.add(string, count);
			}
		}
		return topBag;
	}

	// private static Bag<String> findFirstElems(Bag<String> bag, int firstN) {
	// return findFirstElems(bag, firstN, null);
	// }
	//
	// /**
	// *
	// * @param bag
	// * @param firstN
	// * @param stopwords
	// * @return
	// */
	// public static Bag<String> findFirstElems(Bag<String> bag, int firstN,
	// List<String> blackList) {
	//
	// Bag<String> copyOfBag = null;
	//
	// if (blackList==null){
	// copyOfBag = bag;
	// } else {
	// copyOfBag = new TreeBag<String>(bag);
	//
	// for (String blacklisted : blackList) {
	// copyOfBag.remove(blacklisted);
	// }
	// }
	// List<Tag> sortedTagList = toSortedTagList(copyOfBag);
	// Bag<String> topTagBag = toTagBag(sortedTagList.subList(0,
	// Math.min(sortedTagList.size(), firstN)));
	// return topTagBag;
	// }
	//
	// private static List<Tag> toSortedTagList(Bag<String> tagBag) {
	//
	// List<Tag> sortedList = new ArrayList<Tag>(tagBag.uniqueSet().size());
	//
	// for (String tag : tagBag.uniqueSet()) {
	// sortedList.add(new Tag(tag, tagBag.getCount(tag)));
	// }
	//
	// Collections.sort(sortedList);
	// return sortedList;
	// }
	//
	// private static Bag<String> toTagBag(List<Tag> tagList) {
	// Bag<String> tagBag = new TreeBag<String>();
	// for (Tag tag : tagList) {
	// tagBag.add(tag.getTag(), tag.getWeight());
	// }
	// return tagBag;
	// }

	/**
	 * 
	 * @param str
	 * @return
	 */
	public static String escapePercentage(String str) {
		return str == null ? str : str.replaceAll("%", " ");
	}

	/**
	 * 
	 * @param strDelimeted
	 * @param delimeter
	 * @return
	 */
	private static Bag<String> toBag(String strDelimeted, String delimeter) {

		Bag<String> bag = new TreeBag<String>();

		for (String topTag : strDelimeted.split(delimeter)) {
			bag.add(topTag);
		}

		return bag;
	}

	/**
	 * 
	 * @param strDelimeted
	 * @return
	 */
	public static Bag<String> toBagFromSpaceDelimeted(String strDelimeted) {
		return toBag(strDelimeted, DELIMITER_SPACE_STR);
	}

	/**
	 * 
	 * @param urlAddress
	 * @return
	 * @throws IOException
	 */
	public static String fetchSource(String urlAddress) throws IOException {

		InputStream is = new URL(urlAddress).openConnection().getInputStream();
		BufferedReader in = new BufferedReader(new InputStreamReader(is));

		StringBuilder sb = new StringBuilder();

		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			sb.append(inputLine);
		}
		in.close();

		return sb.toString();
	}

	/**
	 * Preventing construction
	 */
	private GeneralUtil() {
		throw new AssertionError();
	}

	public static boolean startsWith(String tag, List<String> tagPreBlackList) {

		for (String blackList : tagPreBlackList) {
			if (tag.startsWith(blackList)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param str
	 * @param toBeExcluded
	 * @return
	 */
	public static String excludeEach(String str, Set<String> toBeExcludedSet) {

		StringBuilder sb = new StringBuilder();

		String[] splitted = str.split(DELIMITER_SPACE_STR);

		for (String token : splitted) {
			if (!toBeExcludedSet.contains(token)) {
				sb.append(token).append(DELIMITER_SPACE_STR);
			}
		}

		if (sb.length() > 1) {
			return exceptLastChar(sb);
		} else {
			LogUtil.logger.info(
					"Hey, nothing left for string={} after excludeEach", str);
			return null;
		}

		/*
		 * TreeSet<String> strSet = new
		 * TreeSet<String>(Arrays.asList(str.split(DELIMITER_SPACE_STR)));
		 * 
		 * strSet.removeAll(toBeExcluded);
		 * 
		 * return joinWithSpace(strSet);
		 */
	}

	public static boolean isSearchable(String str) {
		return !StrUtil.isNumeric(str) && StringUtils.isAlphanumericSpace(str);
	}

	/**
	 * 
	 * @param str
	 * @return
	 */
	public static String makeSearchable(String str) {

		String input = new String(str);

		String search = null;

		str = StrUtil.removeNonAlphanumeric(str);
		if (!str.equals("")) {

			StringBuilder sb = new StringBuilder();
			String[] tokens = str.split("\\s+");

			if (tokens.length == 1) {

				if (isSearchable(str)) {
					search = str;
				} else {
					search = null;
				}

			} else {

				for (String token : tokens) {
					if (isSearchable(token)) {
						sb.append(token).append(' ');
					}
				}
				search = sb.toString().equals("") ? null : sb.toString();
			}

		}

		if (search == null) {
			LogUtil.logger.info("skipping tag: {}", str);
		}

		LogUtil.logger.info("makeSearchable({})={}", input, search);

		return search;

	}

	public static void main1(String[] args) throws EncoderException {

		String s1 = "#occupyatlanta Joe Rogan - Banks & Occupy Wall Street http://t.co/0YY3Da7k"
				.trim();
		String s2 = "Joe Rogan - Banks & Occupy Wall Street http://t.co/0YY3Da7k"
				.trim();

		System.out.println(StringUtils.getLevenshteinDistance(s1, s2));

		Soundex soundex = new Soundex();

		System.out.println(soundex.difference(s1, s2));
	}

	public static void main2(String[] args) {

		System.out.println(makeSearchable("burak"));
		System.out.println(makeSearchable("burak873483"));
		System.out.println(makeSearchable("3434burak"));
		System.out.println(makeSearchable("1burak2burak"));
		System.out.println(makeSearchable("burak çelebi ğüşiöç"));
		System.out.println(makeSearchable("occupy ğüşiçöÇÇÇ"));
		System.out.println(makeSearchable("occupy wall street"));
		System.out.println(makeSearchable("occupy wall ü~~street"));
		System.out.println(makeSearchable("doctor dru & adana twins"));
		System.out.println(makeSearchable("burak 453 burak"));
		System.out.println(makeSearchable("to-read-some"));

		System.out.println("-------------------------");

		System.out.println(makeSearchable("!!"));
		System.out.println(makeSearchable("..."));
		System.out.println(makeSearchable("?"));
		System.out.println(makeSearchable("1930-1980"));
		System.out.println(makeSearchable("80~"));
		System.out.println(makeSearchable("34324"));
		System.out.println(makeSearchable("232.32"));
		System.out.println(makeSearchable("232,232"));
		System.out.println(makeSearchable("%70"));
	}

}
