package tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization.labeler;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.List;

import tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization.Label;
import tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization.Token;

public class NonAsciiLabeler implements Labeler {

	public void init() throws Exception {
	}

	public List<Token> label(List<Token> tokens) {

		List<Token> labeledTokens = new ArrayList<Token>();

		for (Token token : tokens) {

			if (!isPureAscii(token.getValue())) {
				token.setLabel(Label.NON_ASCII);
			}

			labeledTokens.add(token);
		}
		return labeledTokens;
	}

	public static boolean isPureAscii(String v) {
		byte bytearray[] = v.getBytes();
		CharsetDecoder d = Charset.forName("US-ASCII").newDecoder();
		try {
			CharBuffer r = d.decode(ByteBuffer.wrap(bytearray));
			r.toString();
		} catch (CharacterCodingException e) {
			return false;
		}
		return true;
	}

	private static boolean containsSpecialChars(String str) {
		return WordLabeler.NON_WORDS.matcher(str).find();
	}

	public static void main(String[] args) {

		String[] str = { "burak", "burak)", "34324", "burak2323",
				"burak_celebi" };

		for (String string : str) {
			System.out.println(string + ": " + containsSpecialChars(string));
		}
	}
}
