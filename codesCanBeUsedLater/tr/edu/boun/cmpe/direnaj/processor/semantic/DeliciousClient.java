package semantic;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import labeler.StopwordLabeler;

import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.bag.TreeBag;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import search.Conf;
import util.JsonUtil;
import util.LogUtil;
import util.Util;

public class DeliciousClient implements RelatedTagsFinder{
	
	public static void main(String[] args) throws Exception {
		
		Conf.LOG_ENRICHMENT = false;
		
		// String[] queries = {"Mitt Romney", "Romney", "Newt Gingrich", "Gingrich", "Rick Santorum", "Santorum"};
		
		String[] queries = { 
				// "rock climbing routes in Turkey", "value added services", "community informatics", "near field communication", "mobile finance" 
				"synchronization entropy energy", "synchronization entropy", "entropy energy", "nonlinear synchronization", "nonlinear dynamics", "control of chaos", "chaos theory", "nonlinear systems", "mythology" /* "synchronization entropy energy", "anzu", "anzud", "self"*/
				//"jon wayne and the pain"
		};
				/*
				"textmining information retrieval", "arab spring", "jsf primefaces", "oracle coherence cache", "gsm mobile telecommunication",
				"photography", "travis music band"};
				*/
				
				//"bipolar teens", "vitamin D deficiency", "qigong", "join syntax in SQL", "FTP files from IFS file system on System i"};
				
				//"gesture recognition", "pastafarianism", "diablo3", "star wars", "wheel of time", "ethics in academia"};
				
				// "fpga", "reconfigurable computing", "monica bellucci", "basketball", "matt damon"};
		
		// "social media", ""
		
		for (String q : queries) {
			
			System.out.println("query: " + q);
			
			Bag<String> tags = INSTANCE.findCooccuringTags(q);

			Bag<String> findFirstElems = Util.findFirstElems(tags, Conf.MAX_TOP_COOCCURING_TAGS, Arrays.asList(unifiedTag(q).split("\\s+")));
			
			for (String string : findFirstElems.uniqueSet()) {
				System.out.println(string + ": " + findFirstElems.getCount(string));
			}
			
			/*
			for (String tag : tags) {
				System.out.print(tag + " ");
			}
			
			List<String> terms = Arrays.asList(q.split("\\s+"));
			
			for (String tag : Util.findTopElems(tags, Math.max(terms.size()-1, 3), terms).uniqueSet() ) {
				System.out.println(tag + "," + tags.getCount(tag));
			}
			*/
		}
		
			
	}
	
	public static final DeliciousClient INSTANCE = new DeliciousClient();
	private DeliciousClient() {}
	
	public int id() {
		return 1;
	}
	
	private static final int MAX_DELICIOUS_LINK = 40;
	
	public static final List<String> TAGS_BLACK_LIST = Arrays.asList(new String[]{
			"via:packrati.us", "packrati.us", "packrati", "system:unfiled", "ifttt", "trunkly", "porn", "xxx", "gay", "lesbian", "imported", "via ", "via:", "for:", "bookmarks bar", "unsorted bookmarks", "bookmarks", "GoogleBookmarks",
			"from ", "favorited", "favorites", "porn", "freeporn", "porno", "anal", "pussy", "fuck", "hardcore", "tits", "ass", "teen", "youporn", "nude", "publicinvasion", "public invasion",
			"nuke.news", "&amp;", 
			"toread" // new
	});
	/**
	 *  numbers?
	 */
	
	public Bag<String> findCooccuringTags(String query) throws Exception {

		Bag<String> tagBag = new TreeBag<String>();

		query =  Util.lowerCase(query);
		String queryWithoutStopwords = StopwordLabeler.excludeStopWords(query);
		
		if (queryWithoutStopwords != null ) {
			query = queryWithoutStopwords;
		}
		
		String url = "http://feeds.delicious.com/v2/json/tag/" + query.replaceAll("\\s+", "+") + "?count=" + MAX_DELICIOUS_LINK ;
		
		if (Conf.LOG_ENRICHMENT) {
			LogUtil.logger.info(url);
		} else {
			System.out.println(url);
		}
		
		URL delicious = new URL(url);
		BufferedReader in = new BufferedReader(new InputStreamReader(delicious.openStream()));
		
		JSONArray array=(JSONArray)JSONValue.parse(in);
		
		for (Object object : array) {

			JSONObject deliciousEntry = (JSONObject)object;
			
			JSONArray currentTagList = (JSONArray)deliciousEntry.get("t");
			
			String[] tagArr = JsonUtil.toStringArr(currentTagList);
			addArrElemsToSet( tagArr, tagBag );
			
			StringBuilder sb = new StringBuilder();
			sb.append("linkTitle=[").append(deliciousEntry.get("d").toString()).append("], ").append("link=[").append(deliciousEntry.get("u").toString()).append("], tags=").append(Arrays.toString(tagArr));
			
			if (Conf.LOG_ENRICHMENT) {
				LogUtil.logger.info(sb.toString());
			} else {
				System.out.println(sb.toString());
			}
		}
		
		return tagBag;
	}
	
	/**
	 * 
	 * @param arr
	 * @param tagSet
	 */
	public static void addArrElemsToSet(String[] tagArr, Bag<String> tagBag) {
		
		for (String tag : tagArr) {
			
			tag = unifiedTag(tag);
			
			if ( isEligible(tag)) {
				tagBag.add(tag);
			}
		}
	}

	public static boolean isEligible(String tag) {
		return 
			   !Util.startsWith(tag, TAGS_BLACK_LIST) 
			&& !StopwordLabeler.findStopWords().contains(tag) 
			&& tag.length()!=1
			;
	}

	/**
	 * 
	 * @param tag
	 * @return
	 */
	public static String unifiedTag(String tag) {
		
		tag = Util.lowerCase(tag);
		
		if (tag.startsWith(Util.TWITTER_SIGN_HASHTAG)){
			tag = tag.substring(1);
		}
		
		// StringBuilder sb = new StringBuilder(tag);
		
		tag = tag
			  .replace(Util.DELIMITER_UNDERSCORE, Util.DELIMITER_SPACE_STR)
			  .replace(Util.DELIMITER_DASH, Util.DELIMITER_SPACE_STR)
		;
		
		// System.out.println( sb.append(" > ").append(tag).toString() );
		
		return tag;
	}

	@Override
	public int maxItems() {
		return MAX_DELICIOUS_LINK;
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	/*
	public static void main(String[] args) throws Exception {
		
		String query = "occupy wall street";
		Bag<String> cooccuringTagsSet = findCooccuringTags(query);
		// System.out.println( Util.toStringDelimitedWithSpace(cooccuringTagsSet.uniqueSet()) );
		
		List<String> queryTerms = Arrays.asList(query.split("\\s+"));
		
		for (String string : Util.findTopElems(cooccuringTagsSet, Math.max(queryTerms.size()-1,3), queryTerms).uniqueSet() ) {
			System.out.println(string + ": " + cooccuringTagsSet.getCount(string));
		}
		
		
		// StringBuilder b = new StringBuilder();
		// for (String string : cooccuringTagsSet) {
		// 	b.append(string);
		// 	b.append(' ');
		// }
		// System.out.println(b.toString());
	}
	
	
	public static void main_(String[] args) throws Exception {
		System.out.println(buildQueryWithTopRelatedWords("linkeddata", 3));
		// System.out.println(findRelatedWords("linkeddata"));
	}
	
	private static String buildQueryWithTopRelatedWords(String query, int minOccurrence) throws Exception {
		
		Set<String> tagSet = Util.findTopElems(DeliciousClient.findCooccuringTags(query),minOccurrence).uniqueSet();
		return Util.joinWithOr(tagSet);
	}
	
	private static String findRelatedWords(String query) throws Exception {
		//return "!log annotation api apps article berners-lee bookmarking cloud contextelicitation data database datasets datos abiertos dbpedia dictionary freebase future libraries library linguistics linked-data linked_data linkedata linkeddata lod metadata nlp ontology open_access openaccess opendata opensource owl rdf semantic semantic-web semantic_web semanticweb semweb talis technology thesaurus toread video visualization web web2.0 web30 wiki wikipedia wikimedia wikidata mediawiki deliverables wikidata wikimania wikimedia wikipedia wordnet xarxa_educativa";
		return Util.joinWithSpace( DeliciousClient.findCooccuringTags(query).uniqueSet() );
	}
	*/
}
