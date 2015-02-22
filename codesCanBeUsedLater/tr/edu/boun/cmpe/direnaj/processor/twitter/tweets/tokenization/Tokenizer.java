package tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization;

import java.util.ArrayList;
import java.util.List;

import tr.edu.boun.cmpe.direnaj.processor.utils.GeneralUtil;

public final class Tokenizer {

	public static List<Token> tokenize(String text) {
		
		String[] textArr = preProcess(text).split("\\s+");
		
		List<Token> tokens = new ArrayList<Token>(textArr.length);
		
		for (int i = 0; i < textArr.length; i++) {
			if ( !textArr[i].equals(" ") && !textArr[i].equals("")) {
				tokens.add(new Token(textArr[i], Label.UNKNOWN)); 
			}
		}
		return tokens;
	}
	
	/**
	 * 
	 * @param text
	 * @return
	 */
	private static String preProcess(String str) {
		
		return str;
		// return str.replaceAll("[\\?,\\,]", " ");
		
		/*
		for (String string : WordLabeler.extractNonWords(str)) {
			
			boolean hasLabeler = string.equals("@") || string.equals("#");
			
			if (!hasLabeler) {
				str = str.replace(string, " ");
			}
		}

		return str = str.trim();
		*/
	}

	/**
	 * Preventing construction
	 */
	private Tokenizer(){
		throw new AssertionError();
	}
	
	public static void main(String[] args) throws Exception {
		String s = "RT @openidretweeter: RT: [hal] OiX Publish Trust Framework????OpenID?VIVO???.  #irisc2011: [hal] OiX Publish Trust Framework????OpenI... ...";
		List<Token> tokens = GeneralUtil.toLabeledTokens(s);
		
		for (Token token : tokens) {
			System.out.println(token);
		}
	}
}
