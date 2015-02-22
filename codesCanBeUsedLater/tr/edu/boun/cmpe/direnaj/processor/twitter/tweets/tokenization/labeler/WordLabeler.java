package tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization.labeler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization.Label;
import tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization.Token;

public class WordLabeler implements Labeler {

	static final Pattern NON_WORDS = Pattern.compile("\\W");

	private static List<String> extractNonWords(String text) {
		if (text == null) {
			return null;
		}
		return extractList(NON_WORDS, text);
	}

	private static List<String> extractList(Pattern pattern, String text) {
		List<String> extracted = new ArrayList<String>();
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			extracted.add(matcher.group());
		}
		return extracted;
	}

	private static List<String> extractWords(String str) {

		List<String> words = new ArrayList<String>();

		for (String string : extractNonWords(str)) {
			str = str.replace(string, " ");
		}

		str = str.trim();

		String[] splitted = str.split("\\s+");

		for (String word : splitted) {
			if (!word.equals("")) {
				words.add(word);
			}
		}

		return words;
	}

	private static String trim(String str) {
		final String special = "[\\),\\(,\\?,\\.,\\:,\\+,\\-,%,/,\\[,\\],\"]*";
		return str.replaceAll(special + "$", "").replaceAll("^" + special, "");
	}

	public void init() throws Exception {
	}

	public List<Token> label(List<Token> tokens) {

		List<Token> labeledTokens = new ArrayList<Token>();

		// String wordCandidate;
		for (Token token : tokens) {

			if (token.getLabel() == Label.UNKNOWN) {

				// wordCandidate = token.getValue();

				List<String> extractedWords = extractWords(token.getValue());

				if (extractedWords.size() == 0) {

					labeledTokens.add(token);

				} else {

					if (extractedWords.size() > 1) {
						;
					}

					// labeledTokens.remove(token);
					for (String word : extractedWords) {
						labeledTokens.add(new Token(word, Label.WORD));
					}

					/*
					 * if (extractedWord.size()>1) {
					 * System.out.println("Error extracting words from " +
					 * token.getValue()); } else {
					 * 
					 * token.setValue(extractedWord.get(0));
					 * token.setLabel(Label.WORD); }
					 */
				}

			} else {
				labeledTokens.add(token);
			}

		}
		return labeledTokens;
	}

}
