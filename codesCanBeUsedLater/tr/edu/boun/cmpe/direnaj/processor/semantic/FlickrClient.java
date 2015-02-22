package semantic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;

import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.bag.TreeBag;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import search.Conf;
import util.JsonUtil;
import util.Util;

public class FlickrClient implements RelatedTagsFinder {
	
	public static final FlickrClient INSTANCE = new FlickrClient();
	private FlickrClient() {}
	
	public int id() {
		return 3;
	}
	
	@Override
	public int maxItems() {
		return MAX_PHOTOS;
	}
	
	private static final    int MAX_PHOTOS = 20;
	private static final String API_KEY    = "0f35bb4699c885c5f4f185a2cfb76d78";
	
	/**
	 * 
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public Bag<String> findCooccuringTags(String query) throws Exception {
		
		Bag<String> tagBag = new TreeBag<String>();

		String[] photos = findPhotos(query, MAX_PHOTOS);
        
        for (String photoId : photos) {
        	//System.out.println("photoId=" + photoId); // http://www.flickr.com/services/api/explore/flickr.photos.getInfo
        	DeliciousClient.addArrElemsToSet(findTags(photoId), tagBag);
		}
        return tagBag;
	}

	/**
	 * 
	 * @param tag
	 * @param count
	 * @return
	 * @throws IOException
	 */
	private static String[] findPhotos(String tag, int count) throws IOException {
		
		String url = "http://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=" + API_KEY + "&tags="+tag.replaceAll("\\s+", "+")+"&per_page="+count+"&page=1&format=json&nojsoncallback=1";
		System.out.println(url);
		
		URL flickr = new URL(url);
		BufferedReader in = new BufferedReader(new InputStreamReader(flickr.openStream()));
		
		JSONObject photos = (JSONObject) ((JSONObject) JSONValue.parse(in)).get("photos");
		JSONArray photoArr = JsonUtil.getJsonArray("photo", photos); // (JSONArray) photos.get("photo");
		
		String[] result = new String[photoArr.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = (String)(((JSONObject)photoArr.get(i)).get("id"));
		}
		
		return result;
	}

	/**
	 * 
	 * @param id
	 * @throws IOException
	 */
	private static String[] findTags(String photoId) throws IOException {

		URL flickr = new URL("http://api.flickr.com/services/rest/?method=flickr.tags.getListPhoto&api_key=" + API_KEY + "&photo_id="+photoId+"&format=json&nojsoncallback=1");
		BufferedReader in = new BufferedReader(new InputStreamReader(flickr.openStream()));
		
		JSONObject photo = (JSONObject) ((JSONObject) JSONValue.parse(in)).get("photo");
		JSONObject tagsObj = JsonUtil.getJsonObject("tags", photo); //(JSONObject) photo.get("tags");
		JSONArray tags = JsonUtil.getJsonArray("tag", tagsObj); // (JSONArray) tagsObj.get("tag");
		
		String[] result = new String[tags.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = (String)(((JSONObject)tags.get(i)).get("raw"));
		}
		
		return result;
	}
	
	public static void main(String[] args) throws Exception {
		
		// String[] justinBieber = {"baby", "bieber", "blog", "chuck", "deine", "emma", "erfahrung", "gaga", "hilary", "justin", "katy", "lady", "lautner", "megan", "music", "norris", "perry", "porn", "programming", "roberts", "software", "swank", "taylor", "twitter", "video", "vs", "xxx"};
		
		/*
		String[] linkeddata = {"archives","authorities","automaticallyaddedvia:packrati.us","books","cataloging","coding","community","conferences","data","database","datacite","description","digital_collections","dish2012","eac","ead","ebook","find","framework","future","geospatial","geospatial","linkeddata","rdf","geosparql","scovo","maps","visualization","semweb","google","googlerefine","gui","hack","hacking","has:via","howto","ifttt","ipad","ipadapps","javascript","kb11","labs","lams","libraries","library","libraryhack","librsrock","linkeddata","linked_data","lodlam","mads","mashups","mesh","metadata","mlascarides","ndsa","new","york","nypl","ontologies","ontology","open","openculture","opendata","openvisualization","owl","peerreview","rdf","rdf","semanticweb","linkeddata","foaf","via:zite","reference","refine","research","semantic","semanticweb","semweb","skos","socialnetworking","standards","technology","tools","tutorial","tutorials","twitter_friends","type:article","usability","ux-design","visualization","w3c","web","web2.0","wiki","_wiki"};
		
		for (String tag : linkeddata) {
			Bag<String> tags = INSTANCE.findCooccuringTags("linkeddata " + tag);
			
			System.out.println(tag + (tags.size()>0 ? ": yes" : ": no") );
		}
		*/
		
		String q = "miles davis";
		
		Bag<String> tags = INSTANCE.findCooccuringTags(q);
		
		tags =  Util.findFirstElems(tags, Conf.MAX_TOP_COOCCURING_TAGS, Arrays.asList(q.split("\\s+")));
		
		for (String string : tags.uniqueSet()) {
			System.out.println(string + ": " + tags.getCount(string));
		}
		
		/*
		for (String tag : tags.uniqueSet() ) {
			System.out.println(tag + "," + tags.getCount(tag));
		}
		*/
		
		/*
		for (String tag : Util.findTopElems(tags, 5).uniqueSet() ) {
			System.out.println(tag + ": " + tags.getCount(tag));
		}
		 */
	}

}
