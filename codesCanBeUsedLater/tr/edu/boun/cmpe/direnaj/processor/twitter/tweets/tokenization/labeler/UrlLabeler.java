package tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization.labeler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization.Label;
import tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization.Token;
import tr.edu.boun.cmpe.direnaj.processor.utils.GeneralUtil;

public class UrlLabeler implements Labeler {

	private static final String URL_REGEX = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
	private static Pattern pattern;

	public void init() throws Exception {
		pattern = Pattern.compile(URL_REGEX, Pattern.CASE_INSENSITIVE);
	}

	public List<Token> label(List<Token> tokens) {

		List<Token> labeledTokens = new ArrayList<Token>();

		for (Token token : tokens) {

			List<String> urls = GeneralUtil.extractor.extractURLs(token
					.getValue());

			if (urls.size() > 0) {
				token.setLabel(Label.URL);
				token.setValue(urls.get(0));
			}

			/*
			 * boolean isUrl = pattern.matcher(token.getValue()).matches();
			 * if(isUrl) { token.setLabel(Label.URL); }
			 */

			labeledTokens.add(token);
		}
		return labeledTokens;
	}

}
