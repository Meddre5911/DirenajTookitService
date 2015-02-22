package semantic;

import java.util.ArrayList;
import java.util.List;

import labeler.NonAsciiLabeler;

import org.apache.commons.collections15.Bag;
import org.apache.commons.lang.StringUtils;

import search.SearchService;
import util.AppContextUtil;
import util.Util;

public class RelatedKeywordsFinder {
	
	private static SearchService searchService = AppContextUtil.getSearchService();
	private static SemanticService semanticService = AppContextUtil.getSemanticService();
	
	/**
	 * 
	 * @param query
	 * @throws Exception
	 */
	public static void process(Query query) throws Exception {
		
		System.out.println("Hello from query id=" + query.getId() + ", query=" + query.getQuery());
		
		updateStatus(query, Query.STATUS_PROCESSING);

		try {

			FreeBaseTopic freeBaseTopic = query.getFreeBaseTopic();

			FreeBaseClient.INSTANCE.enhanceTopic(freeBaseTopic);
			query.setDbpediaResource(semanticService.toDbpediaResource(freeBaseTopic));
			
			// TODO dbpedia-owl:wikiPageExternalLink
			
			/*
			 * http://live.dbpedia.org/page/Occupy_Wall_Street
			 * - Java için bak
			 * - Linked Data
			 * - AI
			 * - Steve Jobs
			 * - Iphone 
			 * 
			 * Bashka hangi alanlar kullanilabilir?
			 * 
			 * dbpedia-owl:knownFor	
			 * dbpedia-owl:wikiPageDisambiguates	
			 * dbpedia-owl:wikiPageRedirects
			 */
			
			
			if (query.getDbpediaResource() != null) {
				insertDbPediaRedirects(query);
				insertDbPediaCategories(query);
			}

			List<String> tagQueries = findTagQueries(query);
			for (String tagQuery : tagQueries) {
				findRelatedTags(query, tagQuery);
			}
			
			updateStatus(query, Query.STATUS_FINISHED);
			
		} catch (Exception e) {
			
			updateStatus(query, Query.STATUS_DIRTY);
			throw e;
		}
	
	}

	private static List<String> findTagQueries(Query query) {
		
		List<String> list = new ArrayList<String>();
		
		DbpediaResource dbpediaResource = query.getDbpediaResource();		
		if (dbpediaResource != null) {

			if (dbpediaResource.hasDisambiguation()) {
				
				/*
				 * TODO freebaseType'ın her bir kelimesi ile beraber ayrı ayrı findRelatedTags mi yapsak la?
				 * 
				 */
				
				/*
				 * TODO twitterSearch icin exlude edilecek kelimeleri de belirlesek?
				 *  1. Freebase suggest'teki aynı isimdeki diger type'lar? 
				 *  2. SELECT * FROM disambiguations_dbpedia where disambiguation = '<http://dbpedia.org/resource/Java_%28disambiguation%29>';
				 */
				
				String freeBaseType = query.getFreeBaseTopic().getType();
				String[] typeTokens = freeBaseType.split(Util.DELIMITER_SPACE_STR);
				for (String token : typeTokens) {
					list.add( StringUtils.join(new String[]{ query.getQuery(),token  }, Util.DELIMITER_SPACE) );	
				}
				
				boolean multiWord = typeTokens.length > 1;
				if (multiWord) {
					list.add( StringUtils.join(new String[]{ query.getQuery(),freeBaseType  }, Util.DELIMITER_SPACE) );
				}
				
				
			} else {
				list.add(query.getQuery());
			}

		} else {
			list.add(query.getQuery());
		}
		
		
		System.out.println("<tag_queries> for " + query.getQuery() + ", type=" + query.getFreeBaseTopic().getType());
		for (String string : list) {
			System.out.println(string);
		}
		System.out.println("</tag_queries>");
		
		return list;

	}

	/**
	 * 
	 * @param query
	 */
	private static void insertDbPediaCategories(Query query) {
		
		System.out.println("Hello from insertDbPediaCategories");
		
		insertList(query, query.getDbpediaResource().getResource(), 8, query.getDbpediaResource().getCategories());
	}

	/**
	 * 
	 * @param query
	 */
	private static void insertDbPediaRedirects(Query query) {
		
		System.out.println("Hello from insertDbPediaRedirects");
			
		insertBag(query, query.getDbpediaResource().getResource(), 5, query.getDbpediaResource().getRedirects() );
	}

	/**
	 * 
	 * @param searchService
	 * @param query
	 * @param newStatus
	 */
	private static void updateStatus(Query query, int newStatus) {
		query.setStatus(newStatus);
		searchService.updateKeywordOrder(query);
	}

	/**
	 * 
	 * @param searchService
	 * @param query
	 * @throws Exception
	 */
	private static void findRelatedTags(Query query, String tagQuery) throws Exception {
		
		RelatedTagsFinder[] relatedTagsFinders = {DeliciousClient.INSTANCE, FlickrClient.INSTANCE, YoutubeClient.INSTANCE, KwMapKeywordFinder.INSTANCE};

		for (RelatedTagsFinder relatedTagsFinder : relatedTagsFinders) {

			System.out.println("Hello from " + relatedTagsFinder.getClass().getName());
			
			Bag<String> tags = relatedTagsFinder.findCooccuringTags(tagQuery);

			query.addNewTaggingResult(new TagResourceQueryResult(relatedTagsFinder.id(), relatedTagsFinder.getClass().getName(), tagQuery, tags));
			
			insertBag(query, tagQuery, relatedTagsFinder.id(), tags);
		}
	}

	/**
	 * 
	 * @param query
	 * @param id
	 * @param tags
	 */
	private static void insertBag(Query query, String searchTxt, int id, Bag<String> tags) {
		
		for (String tag : tags.uniqueSet()) {
			insertIfEligible(query, searchTxt, tag, tags.getCount(tag),id);
		}
	}
	
	/**
	 * 
	 * @param query
	 * @param id
	 * @param list
	 */
	private static void insertList(Query query, String searchTxt, int id, List<String> list) {

		for (String elem : list) {
			insertIfEligible(query, searchTxt, elem, 0, id);
		}
	}

	/**
	 * 
	 * @param query
	 * @param tag
	 * @param count
	 * @param id
	 */
	private static void insertIfEligible(Query query, String searchTxt, String tag, int count, int id) {
		
		if(NonAsciiLabeler.isPureAscii(tag)) {
			searchService.insertRelatedTags(query, searchTxt, tag, count, id);
		} else {
			System.out.println("skipping: " + tag);
		}
	
	}
	
	public static void main(String[] args) throws Exception {
		
		while(true) {
			for (Query query : searchService.findWaitingKeywordOrders()) {
				process(query);
			}
			
			System.out.println("All keyword orders processed... Waiting for new ones..");
			Thread.sleep(10000);
			
		}
	}
}
