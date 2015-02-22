package tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization.labeler;

import java.util.LinkedList;
import java.util.List;

import tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization.Token;

public class MultiLabeler implements Labeler {

	private List<Labeler> labelers;

	public MultiLabeler(List<Labeler> labelers) throws Exception {
		this.labelers = labelers;
		init();
	}

	public void init() throws Exception {
		for (Labeler labeler : labelers) {
			labeler.init();
		}
	}

	public List<Token> label(final List<Token> tokens) {

		List<Token> labeledTokens = new LinkedList<Token>();
		labeledTokens.addAll(tokens);

		for (Labeler labeler : labelers) {
			labeledTokens = labeler.label(labeledTokens);
		}
		return labeledTokens;
	}
}
