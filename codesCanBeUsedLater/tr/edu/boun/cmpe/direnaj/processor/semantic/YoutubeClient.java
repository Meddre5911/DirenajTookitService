package semantic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
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

public class YoutubeClient implements RelatedTagsFinder {
	
	private static final int MAX_RESULTS = 50;
	private static final int MIN_COOCC = 3;
	
	public static final YoutubeClient INSTANCE = new YoutubeClient();
	private YoutubeClient() {}
	
	/**
	 * 
	 * @param query
	 * @return
	 * @throws Exception
	 */
	private static Bag<String> findTopCooccuringTags(Bag<String> cooccuringTags, List<String> queryTerms) throws Exception {
		return Util.findFirstElems(cooccuringTags, Conf.MAX_TOP_COOCCURING_TAGS, queryTerms);
	}
	
	public Bag<String> findCooccuringTags(String query) throws Exception {
		
		Bag<String> tagBag = new TreeBag<String>();
		
		query =  Util.lowerCase(query);
		query = StopwordLabeler.excludeStopWords(query);

		JSONObject root = (JSONObject)JSONValue.parse(buildReader(query, MAX_RESULTS));
		
		JSONObject  feeds = JsonUtil.getJsonObject("feed", root);
		JSONArray entries = JsonUtil.getJsonArray("entry", feeds);

		StringBuilder sb = new StringBuilder();
		
		if (entries!=null) {
			
			for (int i = 0; i < entries.size(); i++) {
				
				JSONObject entry = (JSONObject)entries.get(i);
				
				String videoTitle = String.valueOf( ((JSONObject)entry.get("title")).get("$t") );
				String videoLink = null;
				
				JSONArray links = JsonUtil.getJsonArray("link", entry);		
				for (int j = 0; j < links.size(); j++) {
					JSONObject link = (JSONObject)links.get(j);
					
					if ( link.get("rel").equals("alternate") ) {
						videoLink = String.valueOf( link.get("href") );
						break;
					}
				}
					
				sb.append("videoTitle=[").append(videoTitle).append("], ").append("videoLink=[").append(videoLink).append("], tags=[");
				
				JSONArray categories = JsonUtil.getJsonArray("category", entry);
				
				for (int j = 0; j < categories.size(); j++) {
					
					JSONObject category = (JSONObject)categories.get(j);
					
					if (category.containsKey("scheme")){
						if (category.get("scheme").equals("http://gdata.youtube.com/schemas/2007/keywords.cat")) {
							
							String unifiedTag = DeliciousClient.unifiedTag((String)category.get("term"));
							
							if (DeliciousClient.isEligible(unifiedTag)) {
								
								sb.append("\"") .append(unifiedTag).append("\", ");
								tagBag.add(unifiedTag);
							}
							//tagBag.add((String)category.get("term"));
						}
					}
				}
				sb.append("]\n");
			}
		}
		
		if (Conf.LOG_ENRICHMENT) {
			LogUtil.logger.info(sb.toString());
		} else {
			System.out.println(sb.toString());
		}
		
        return tagBag;
	}

	/**
	 * 
	 * @param query
	 * @param maxResults
	 * @return
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	private Reader buildReader (String query, int maxResults) throws MalformedURLException, IOException {
		
		String url = "http://gdata.youtube.com/feeds/api/videos?category=" + query.replace(" ", "/") + "&max-results=" + maxResults + "&alt=json&lr=en&orderby=relevance";
		if (Conf.LOG_ENRICHMENT) {
			LogUtil.logger.info(url);
		} else {
			System.out.println(url);
		}
		return new BufferedReader(new InputStreamReader(new URL(url).openStream()));
	}
	
	/**
	 * 
	 */
	public int id() {
		return 2;
	}
	
	@Override
	public int maxItems() {
		return MAX_RESULTS;
	}

	public static void main(String[] args) throws Exception {
		
		Conf.LOG_ENRICHMENT = false;
		
		String[] queries = {"jon wayne and the pain"};

		for (String q : queries) {
			
			Bag<String> tags = INSTANCE.findCooccuringTags(q);
			
			tags = findTopCooccuringTags(tags, Arrays.asList(q.split("\\s+")) );
			
			for (String string : tags.uniqueSet()) {
				System.out.println(string + ": " + tags.getCount(string));
			}
			
			System.out.println("-------------------------------------");
		}
		
		/*
		for (String tag : tags.uniqueSet() ) {
			System.out.println(tag + "," + tags.getCount(tag));
		}
		*/
		
		/*
		for (String tag : Util.findTopElems(tags, MIN_COOCC).uniqueSet() ) {
			System.out.println(tag + ": " + tags.getCount(tag));
		}
		*/
		
	}

	
}
