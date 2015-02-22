package direnaj.functionalities.nlp;

public class WordStringUtils {
	public static String generateMultiWordString(String s) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			if ( s.charAt(i) >= 'A' && s.charAt(i) <= 'Z') {
				result.append(" " + s.charAt(i));
				i++;
				if (i < s.length()) {
					result.append(s.charAt(i));
				}
			} else {
				result.append(s.charAt(i));
			}
		}
		
		String resultStr = result.toString();
		
		return resultStr.trim();
	}
	
	public static String normalizeTweetWords(String t) {
		t = t.replaceAll(" u ", " you ")
			.replaceAll(" 4 ", " for ")
			.replaceAll(" 4u ", " for you ")
			.replaceAll(" 2day ", " today ")
			.replaceAll(" lol ", " laughing out loud ")
			.replaceAll(" Lol ", " laughing out loud ")
			.replaceAll(" LOL ", " laughing out loud ")
			.replaceAll(" rly ", " really ")
			.replaceAll(" g00d ", " good ")
			.replaceAll(" b ", " be ")
			.replaceAll(" nutz", " nuts")
			.replaceAll(" morn ", " morning ")
			.replaceAll(" wrking ", " working ")
			.replaceAll(" 2 ", " to ")
			.replaceAll(" da ", " the ")
			.replaceAll(" goin ", " going ")
			.replaceAll(" bac ", " back ")
			.replaceAll(" cali", " California")
			.replaceAll(" msg ", " message ")
			.replaceAll(" yr ", " year ")
			.replaceAll(" yrs ", " years ")
			.replaceAll(" bday ", " birthday ")
			.replaceAll(" mom ", " mother ")
			.replaceAll(" dad ", " father ")
			.replaceAll(" sis ", " sister ")
			.replaceAll(" bro ", " brother ")
			;
		
		
		return t;
	}


	public static String removeStopwords(String t) {
	
		t = t.replaceAll("[();\"'.!?,]", "");
		
		// extracted from http://www.ranks.nl/tools/stopwords.html
		t = t.replaceAll(" a ", " ")
			.replaceAll(" the ", " ")
			.replaceAll(" The ", " ")
			.replaceAll(" and ", " ")
			.replaceAll(" And ", " ")
			.replaceAll(" AND ", " ")
			.replaceAll(" or ", " ")
			.replaceAll(" Or ", " ")
			.replaceAll(" OR ", " ")
			.replaceAll(" in ", " ")
			.replaceAll(" In ",  " ")
			.replaceAll(" IN ", " ")
			.replaceAll(" of ", " ")
			.replaceAll(" Of ", " ")
			.replaceAll(" OF ", " ")
			.replaceAll(" to ", " ")
			.replaceAll(" To ", " ")
			.replaceAll(" TO ", " ")
			.replaceAll(" that ", " ")
			.replaceAll(" That ", " ")
			.replaceAll(" THAT ", " ")
			.replaceAll(" it ", " ")
			.replaceAll(" It ", " ")
			.replaceAll(" IT ", " ")
			.replaceAll(" i ", " ")
			.replaceAll(" I ", " ")
			.replaceAll(" you ", " ")
			.replaceAll(" You ", " ")
			.replaceAll(" YOU ", " ")
			.replaceAll(" about ", " ")
			.replaceAll(" About ", " ")
			.replaceAll(" an ", " ")
			.replaceAll(" An ", " ")
			.replaceAll(" AN ", " ")
			.replaceAll(" are ", " ")
			.replaceAll(" Are ", " ")
			.replaceAll(" ARE ", " ")
			.replaceAll(" should ", " ")
			.replaceAll(" Should ", " ")
			.replaceAll(" as ", " ")
			.replaceAll(" As ", " ")
			.replaceAll(" As ", " ")
			.replaceAll(" at ", " ")
			.replaceAll(" At ", " ")
			.replaceAll(" AT ", " ")
			.replaceAll(" be ", " ")
			.replaceAll(" Be ", " ")
			.replaceAll(" by ", " ")
			.replaceAll(" By ", " ")
			.replaceAll(" BY ", " ")
			.replaceAll(" bye ", " ")
			.replaceAll(" Bye ", " ")
			.replaceAll(" for ", " ")
			.replaceAll(" For ", " ")
			.replaceAll(" FOR ", " ")
			.replaceAll(" from ", " ")
			.replaceAll(" From ", " ")
			.replaceAll(" FROM ", " ")
			.replaceAll(" how ", " ")
			.replaceAll(" How ", " ")
			.replaceAll(" HOW ", " ")
			.replaceAll(" is ", " ")
			.replaceAll(" Is ", " ")
			.replaceAll(" IS ", " ")
			.replaceAll(" on ", " ")
			.replaceAll(" On ", " ")
			.replaceAll(" ON ", " ")
			.replaceAll(" this ", " ")
			.replaceAll(" This ", " ")
			.replaceAll("was ", " ")
			.replaceAll("Was ", " ")
			.replaceAll("WAS ", " ")
			.replaceAll("what ", " ")
			.replaceAll("What ", " ")
			.replaceAll("WHAT ", " ")
			.replaceAll("when ", " ")
			.replaceAll("When ", " ")
			.replaceAll("where ", " ")
			.replaceAll("Where ", " ")
			.replaceAll("WHERE ", " ")
			.replaceAll("who ", " ")
			.replaceAll("Who ", " ")
			.replaceAll("WHO ", " ")
			.replaceAll("will ", " ")
			.replaceAll("Will ", " ")
			.replaceAll("WILL ", " ")
			.replaceAll("into ", " ")
			.replaceAll("Into ", " ")
			.replaceAll("INTO ", " ")
			.replaceAll("which ", " ")
			.replaceAll("Which ", " ")
			.replaceAll("with ", " ")
			.replaceAll("With ", " ")
			.replaceAll("WITH ", " ")
			;
		return t;
	}
	
}
