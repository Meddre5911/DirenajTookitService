package tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization.labeler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tr.edu.boun.cmpe.direnaj.processor.ProcessorApiConfigurator;
import tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization.Label;
import tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization.Token;
import tr.edu.boun.cmpe.direnaj.processor.utils.GeneralUtil;

public class StopwordLabeler implements Labeler {

	private static Set<String> STOPWORDS = null;

	public StopwordLabeler() {
	}

	public void init() throws Exception {
		STOPWORDS = findStopWords();
	}

	/**
	 * 
	 * @param str
	 * @param toBeExcluded
	 * @return
	 */
	public static String excludeStopWords(String str) {
		return GeneralUtil.excludeEach(str, findStopWords());
	}

	public static Set<String> findStopWords() {
		if (STOPWORDS == null) {
			STOPWORDS = new HashSet<String>();
			STOPWORDS.addAll(ProcessorApiConfigurator
					.getPropertyWithDelimeterAsStringList(
							ProcessorApiConfigurator.STOPWORDS,
							GeneralUtil.DELIMITER_SPACE_STR));
		}

		return STOPWORDS;
	}

	public List<Token> label(List<Token> tokens) {
		List<Token> labeledTokens = new ArrayList<Token>();
		for (Token token : tokens) {
			if (STOPWORDS.contains(GeneralUtil.lowerCase(token.getValue()))) {
				token.setLabel(Label.STOP_WORD);
			}
			labeledTokens.add(token);
		}
		return labeledTokens;
	}

	public static void main(String[] args) {

		String[] queries = { "fine dining" };

		for (String q : queries) {
			System.out.println(q + " -> " + excludeStopWords(q));
		}
	}

	/*
	 * private static final String TWITTER_STOPWORDS =
	 * "fun nice love 1 2 3 4 5 6 7 8 9 0 check look brief live getting " +
	 * "tomorrow awesome soon details guy guys oh people morning night coming that's hours hour rt lol via"
	 * ;
	 * 
	 * private static final String DEFAULT_STOPWORDS =
	 * "a about add ago after all also an and another any are as at be " +
	 * "because been before being between big both but by came can come " +
	 * "could did do does due each else end far few for from get got had " +
	 * "has have he her here him himself his how if in into is it its " +
	 * "just let lie like low make many me might more most much must " +
	 * "my never no nor not now of off old on only or other our out over " +
	 * "per pre put re said same see she should since so some still such " +
	 * "take than that the their them then there these they this those " +
	 * "through to too under up use very via want was way we well were " +
	 * "what when where which while who will with would yes yet you your i " +
	 * TWITTER_STOPWORDS;
	 */
}
