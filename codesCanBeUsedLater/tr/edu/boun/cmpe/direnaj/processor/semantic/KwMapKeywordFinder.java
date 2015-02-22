package semantic;

import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.bag.TreeBag;
import org.apache.commons.lang.StringUtils;

import util.Util;

public class KwMapKeywordFinder implements RelatedTagsFinder {
	
	public static final KwMapKeywordFinder INSTANCE = new KwMapKeywordFinder();
	private KwMapKeywordFinder() {}
	
	private static final String KW_MAP_URL = "http://www.kwmap.net/x.html";
	
	public Bag<String> findCooccuringTags(String query) throws Exception {
		
		Bag<String> keywords = new TreeBag<String>();
		
		String url = KW_MAP_URL.replace("x",  query.replace(' ', '-'));
		System.out.println(url);
		
		String src = Util.fetchSource(url);
		
		String map = src.substring(src.indexOf("kw_map=")+9, src.indexOf(";function min(")-2);
		//String map = StringUtils.substringBetween(src, "kw_map=", ";function min(");
		
		// System.out.println(map);
		
		String[] arrays = map.split("\\],\\[");
		
		for (String arr : arrays) {
			
			arr = arr.substring(1,arr.length()-1);
			// System.out.println(arr);
			
			String[] kwArray = arr.split("\",\"");
			
			for (String kw : kwArray) {
				if (!StringUtils.isBlank(kw)) {
					keywords.add(kw);
				}
			}
		}
		
		return keywords;
	}
	 
	public int id() {
		return 4;
	}

	@Override
	public int maxItems() {
		return 1;
	}
	
	public static void main(String[] args) throws Exception  {
		
		Bag<String> keywords = INSTANCE.findCooccuringTags("artificial intelligence programming");
		
		for (String kw : keywords) {
			System.out.println(kw);
		}
		
		// System.out.println(fetchSource(KW_MAP_URL.replace("x", "jazz")));
	}
}
