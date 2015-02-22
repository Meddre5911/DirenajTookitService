package tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization.labeler;

import java.util.ArrayList;
import java.util.List;

import tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization.Label;
import tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization.Token;
import tr.edu.boun.cmpe.direnaj.processor.utils.GeneralUtil;



public class HashTagLabeler implements Labeler {

	public void init() throws Exception {
	}

	public List<Token> label(List<Token> tokens) {

		List<Token> labeledTokens = new ArrayList<Token>();
		
		for (Token token : tokens) {
			
			List<String> hashtags = GeneralUtil.extractor.extractHashtags(token.getValue());
			
			if (hashtags.size() > 0) {
				token.setLabel(Label.HASHTAG);
				token.setValue(hashtags.get(0));
			}
			
			/*
			if(token.getValue().startsWith("#")) {
				token.setLabel(Label.HASHTAG);
			}
			*/
			
			labeledTokens.add(token);
		}
		return labeledTokens;
	}

}
