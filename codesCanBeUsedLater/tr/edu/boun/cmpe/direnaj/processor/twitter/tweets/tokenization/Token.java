package tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization;

import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.bag.TreeBag;

/**
 * FIXME Search sinifi Burak'ýn kodunda DB ile ilgili olduðu için alýnmadý. Hata
 * veren methodlarda yapýlmak istenen Business Logic'e bakýp, ona göre Direnaj'a
 * uyarlayacaðýz
 * 
 * O yuzden su an icin Search sinifi ile ilgili olan yerler comment'lendi
 */
public class Token implements Comparable<Token> {

	private static final Bag<Token> EMPTY_TOKEN_BAG = new TreeBag<Token>();

	private String value;
	private Label label;

	public Token() {
		super();
	}

	public Token(String value, Label label) {
		setValue(value);
		setLabel(label);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Label getLabel() {
		return label;
	}

	public void setLabel(Label label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return value + " (" + label + ")";
	}

	public int compareTo(Token otherToken) {

		boolean sameValue = value.equals(otherToken.getValue());
		boolean sameLabel = label == otherToken.getLabel();

		if (!sameLabel) {
			return -1;
		} else if (!sameValue) {
			return 1;
		} else {
			return 0;
		}
	}

	// /**
	// *
	// * @param search
	// * @param token
	// * @return
	// */
	// public boolean isRelatedWith (Search search) {
	//
	// /*
	// if (Label.URL==label) {
	//
	// return handleIsRelatedWith(search); //TODO: url black list? http, htm,
	// html, www, etc.
	//
	// } else {
	// */
	// return search.getQueryTerms().contains(value) ||
	// search.getTopCooccuringTags().contains(value) ||
	// Util.asHashtag(search.getQuery()).substring(1).equals(value); //TODO:
	// query terms
	// // }
	// }
	//
	// /**
	// *
	// * @param search
	// * @return
	// */
	// private boolean handleIsRelatedWith (Search search) {
	//
	// for (String queryTerm : search.getQueryTerms()) {
	// if (value.contains(queryTerm)) {
	// return true;
	// }
	// }
	//
	// for (String tag : search.getCooccuringTags()) {
	// if (value.contains(tag)) {
	// return true;
	// }
	// }
	//
	// return false;
	// }
	//
	// /**
	// *
	// * @param tokens
	// * @param search
	// * @return
	// */
	// public static boolean isRelatedWith (Collection<Token> tokens, Search
	// search) {
	//
	// for (Token token : tokens) {
	// if ( token.isRelatedWith(search) ) {
	// return true;
	// }
	// }
	//
	// return false;
	// }

	/**
	 * 
	 * @param tokensSpaceSep
	 * @param label
	 * @return
	 */
	public static Bag<Token> toTokens(String tokensSpaceSep, Label label) {

		if (tokensSpaceSep == null) {
			return EMPTY_TOKEN_BAG;
		}

		return toTokens(tokensSpaceSep.split(" "), label);
	}

	/**
	 * 
	 * @param tokenArr
	 * @param label
	 * @return
	 */
	private static Bag<Token> toTokens(String[] tokenArr, Label label) {

		Bag<Token> tokenBag = new TreeBag<Token>();

		for (String token : tokenArr) {
			tokenBag.add(new Token(token, label));
		}

		return tokenBag;
	}
}
