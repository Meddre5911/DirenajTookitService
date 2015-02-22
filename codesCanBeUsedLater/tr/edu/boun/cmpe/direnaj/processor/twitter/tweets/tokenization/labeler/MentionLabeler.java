package tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization.labeler;

import java.util.ArrayList;
import java.util.List;

import tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization.Label;
import tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization.Token;
import tr.edu.boun.cmpe.direnaj.processor.utils.GeneralUtil;

public class MentionLabeler implements Labeler {

	public void init() throws Exception {
	}

	public List<Token> label(List<Token> tokens) {

		List<Token> labeledTokens = new ArrayList<Token>();

		for (Token token : tokens) {

			List<String> mentions = GeneralUtil.extractor
					.extractMentionedScreennames(token.getValue());

			if (mentions.size() > 0) {
				token.setLabel(Label.MENTION);
				token.setValue(mentions.get(0));
			}

			/*
			 * if(token.getValue().startsWith("@")) {
			 * token.setLabel(Label.MENTION); }
			 */

			labeledTokens.add(token);
		}
		return labeledTokens;
	}

}
