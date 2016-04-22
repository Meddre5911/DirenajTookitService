package direnaj.functionalities.organizedBehaviour;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import direnaj.driver.DirenajMongoDriver;
import direnaj.driver.DirenajMongoDriverUtil;
import direnaj.driver.MongoCollectionFieldNames;
import direnaj.util.CosineSimilarityUtil;
import direnaj.util.DateTimeUtils;
import direnaj.util.PropertiesUtil;
import direnaj.util.TextUtils;

public class CosineSimilarity {

	private String originalRequestId;
	private List<CosineSimilarityRequestData> requestDataList;

	public CosineSimilarity(String requestId, boolean calculateGeneralSimilarity, boolean calculateHashTagSimilarity,
			boolean calculateHourBasisSimilarity, Date earliestTweetDate, Date latestTweetDate) {
		originalRequestId = requestId;
		requestDataList = new ArrayList<>(4);
		int hourBasisInterval = PropertiesUtil.getInstance()
				.getIntProperty("tweet.calculateSimilarity.hourBasisInterval", 2);
		// calculate general similarity
		if (calculateGeneralSimilarity) {
			CosineSimilarityRequestData requestData = new CosineSimilarityRequestData(
					TextUtils.generateUniqueId4Request(), originalRequestId);
			requestDataList.add(requestData);
			if (calculateHourBasisSimilarity) {
				prepareRequestData4HourlyBasisCalculation(earliestTweetDate, latestTweetDate, hourBasisInterval, false);
			}
		}
		// calculate hashtag basis similarity
		if (calculateHashTagSimilarity) {
			CosineSimilarityRequestData requestData = new CosineSimilarityRequestData(
					TextUtils.generateUniqueId4Request(), originalRequestId, true, null, null);
			requestDataList.add(requestData);
			if (calculateHourBasisSimilarity) {
				prepareRequestData4HourlyBasisCalculation(earliestTweetDate, latestTweetDate, hourBasisInterval, true);
			}
		}

	}

	private void prepareRequestData4HourlyBasisCalculation(Date earliestTweetDate, Date latestTweetDate,
			int hourBasisInterval, boolean isHashtagSpecificRequest) {
		if (earliestTweetDate != null && latestTweetDate != null && earliestTweetDate.before(latestTweetDate)) {
			Date lowerTime = earliestTweetDate;
			Date upperTime;
			do {
				// calculate upper time
				upperTime = DateTimeUtils.addHoursToDate(lowerTime, hourBasisInterval);
				CosineSimilarityRequestData requestData4TimeInterval = new CosineSimilarityRequestData(
						TextUtils.generateUniqueId4Request(), originalRequestId, isHashtagSpecificRequest, lowerTime,
						upperTime);
				requestDataList.add(requestData4TimeInterval);
				// assign new lower time
				lowerTime = upperTime;
			} while (upperTime.before(latestTweetDate));
		}
	}

	private void insertRequest2Mongo(CosineSimilarityRequestData requestData) {
		DBCollection orgBehaviourRequestedSimilarityCalculations = DirenajMongoDriver.getInstance()
				.getOrgBehaviourRequestedSimilarityCalculations();
		BasicDBObject document = new BasicDBObject();
		document.put("originalRequestId", originalRequestId);
		document.put("requestId", requestData.getRequestId());
		document.put("isHashtagRequest", requestData.isHashtagSpecificRequest());
		document.put("lowerTimeInterval", TextUtils.getNotNullValue(requestData.getLowerTime()));
		document.put("upperTimeInterval", TextUtils.getNotNullValue(requestData.getUpperTime()));
		document.put(MongoCollectionFieldNames.MONGO_TWEET_FOUND, false);
		orgBehaviourRequestedSimilarityCalculations.insert(document);
	}

	private void updateRequestInMongo(CosineSimilarityRequestData requestData, boolean tweetFound) {
		DBCollection orgBehaviourRequestedSimilarityCalculations = DirenajMongoDriver.getInstance()
				.getOrgBehaviourRequestedSimilarityCalculations();
		BasicDBObject findQuery = new BasicDBObject();
		findQuery.put(MongoCollectionFieldNames.MONGO_REQUEST_ID, requestData.getRequestId());
		BasicDBObject updateQuery = new BasicDBObject();
		updateQuery.append("$set", new BasicDBObject().append(MongoCollectionFieldNames.MONGO_TWEET_FOUND, tweetFound));
		orgBehaviourRequestedSimilarityCalculations.update(findQuery, updateQuery, true, false);
	}

	public void calculateTweetSimilarities() {
		for (CosineSimilarityRequestData requestData : requestDataList) {
			// log request
			insertRequest2Mongo(requestData);
			// calculate similarity
			calculateTFValues(requestData);
			calculateIDFValues(requestData);
			calculateTFIDFValues(requestData);
			calculateSimilarities(requestData);
		}
	}

	@SuppressWarnings("unchecked")
	private void calculateSimilarities(CosineSimilarityRequestData requestData) {
		ArrayList<String> allTweetIds = (ArrayList<String>) DirenajMongoDriver.getInstance()
				.getOrgBehaviourTweetsShortInfo().findOne(requestData.getRequestIdObject())
				.get(MongoCollectionFieldNames.MONGO_ALL_TWEET_IDS);
		ArrayList<String> allTweetIdsClone = (ArrayList<String>) allTweetIds.clone();
		List<DBObject> tweetSimilarityWithOtherTweets = new ArrayList<>(
				DirenajMongoDriver.getInstance().getBulkInsertSize());
		for (String queryTweetId : allTweetIds) {
			BasicDBObject queryTweetTFIdfQueryObj = new BasicDBObject(MongoCollectionFieldNames.MONGO_REQUEST_ID,
					requestData.getRequestId()).append(MongoCollectionFieldNames.MONGO_TWEET_ID, queryTweetId);
			BasicDBObject tweetTfIdfValueObject = (BasicDBObject) DirenajMongoDriver.getInstance()
					.getOrgBehaviourProcessCosSimilarityTF_IDF().findOne(queryTweetTFIdfQueryObj);

			if (Boolean.valueOf(
					PropertiesUtil.getInstance().getProperty("tweet.calculateSimilarity.showTweetTexts", "false"))) {
				String tweetText = DirenajMongoDriverUtil.getTweetText4CosSimilarity(Long.valueOf(queryTweetId));
				queryTweetTFIdfQueryObj.put(MongoCollectionFieldNames.MONGO_TWEET_TEXT, tweetText);
			}
			ArrayList<String> queryTweetWords = (ArrayList<String>) tweetTfIdfValueObject
					.get(MongoCollectionFieldNames.MONGO_TWEET_WORDS);
			List<BasicDBObject> tfIdfList = (List<BasicDBObject>) tweetTfIdfValueObject
					.get(MongoCollectionFieldNames.MONGO_WORD_TF_IDF_LIST);
			Map<String, Double> tweetWordTfIdfMap = (Map<String, Double>) tweetTfIdfValueObject
					.get(MongoCollectionFieldNames.MONGO_WORD_TF_IDF_HASHMAP);
			double tweetVectorLength = CosineSimilarityUtil.calculateVectorLength(tfIdfList);
			Map<String, Double> similarityOfTweetWithOtherTweets = CosineSimilarityUtil
					.getEmptyMap4SimilarityDecisionTree();
			for (String tweetId : allTweetIdsClone) {
				Logger.getLogger(OrganizationDetector.class)
						.debug("Comparing TweetId : " + queryTweetId + " to TweetId : " + tweetId);
				BasicDBObject comparedTweetTFIdfValueObj = new BasicDBObject(MongoCollectionFieldNames.MONGO_REQUEST_ID,
						requestData.getRequestId()).append(MongoCollectionFieldNames.MONGO_TWEET_ID, tweetId);
				BasicDBObject queryTfIdfValues = (BasicDBObject) DirenajMongoDriver.getInstance()
						.getOrgBehaviourProcessCosSimilarityTF_IDF().findOne(comparedTweetTFIdfValueObj);
				List<BasicDBObject> comparedTfIdfList = (List<BasicDBObject>) queryTfIdfValues
						.get(MongoCollectionFieldNames.MONGO_WORD_TF_IDF_LIST);

				Map<String, Double> comparedTweetWordTfIdfMap = (Map<String, Double>) queryTfIdfValues
						.get(MongoCollectionFieldNames.MONGO_WORD_TF_IDF_HASHMAP);

				double comparedTweetVectorLength = CosineSimilarityUtil
						.calculateVectorLengthBasedOnComparedWordList(queryTweetWords, comparedTfIdfList);
				double dotProduct = CosineSimilarityUtil.calculateDotProduct(queryTweetWords, tweetWordTfIdfMap,
						comparedTweetWordTfIdfMap);
				CosineSimilarityUtil.findTweetSimilarityRange(similarityOfTweetWithOtherTweets, dotProduct,
						tweetVectorLength, comparedTweetVectorLength);
			}
			queryTweetTFIdfQueryObj.append(MongoCollectionFieldNames.MONGO_TWEET_SIMILARITY_WITH_OTHER_TWEETS,
					similarityOfTweetWithOtherTweets);
			tweetSimilarityWithOtherTweets.add(queryTweetTFIdfQueryObj);
			DirenajMongoDriverUtil.insertBulkData2CollectionIfNeeded(
					DirenajMongoDriver.getInstance().getOrgBehaviourProcessTweetSimilarity(),
					tweetSimilarityWithOtherTweets, false);

		}
		DirenajMongoDriverUtil.insertBulkData2CollectionIfNeeded(
				DirenajMongoDriver.getInstance().getOrgBehaviourProcessTweetSimilarity(),
				tweetSimilarityWithOtherTweets, true);

	}

	@SuppressWarnings("unchecked")
	private void calculateTFIDFValues(CosineSimilarityRequestData requestData) {
		// in tweetTfIdf Collect,on a record format is like
		// "requestId, tweetId, [word, tf*Idf, (tf*Idf)^2] dizi halinde
		List<DBObject> allTweetTFIdfValues = new ArrayList<>(DirenajMongoDriver.getInstance().getBulkInsertSize());
		List<String> allTweetIds = (List<String>) DirenajMongoDriver.getInstance().getOrgBehaviourTweetsShortInfo()
				.findOne(requestData.getRequestIdObject()).get(MongoCollectionFieldNames.MONGO_ALL_TWEET_IDS);
		for (String tweetId : allTweetIds) {
			List<String> tweetWords = new ArrayList<>(20);
			BasicDBObject tweetTFIdfValues = new BasicDBObject(MongoCollectionFieldNames.MONGO_REQUEST_ID,
					requestData.getRequestId()).append(MongoCollectionFieldNames.MONGO_TWEET_ID, tweetId);
			// get tf values for words in the tweet
			DBObject tfQueryObj = new BasicDBObject(MongoCollectionFieldNames.MONGO_REQUEST_ID,
					requestData.getRequestId()).append(MongoCollectionFieldNames.MONGO_TWEET_ID, tweetId);
			DBCursor wordTFValues = DirenajMongoDriver.getInstance().getOrgBehaviourCosSimilarityTF().find(tfQueryObj);
			List<DBObject> wordTfIdfValuesList = new ArrayList<>(20);
			HashMap<String, Double> wordTfIdfHashMap = new HashMap<>();
			try {
				while (wordTFValues.hasNext()) {
					// get word idf value
					DBObject wordTFObj = wordTFValues.next();
					String word = (String) wordTFObj.get(MongoCollectionFieldNames.MONGO_WORD);
					tweetWords.add(word);
					double wordTfValue = (double) wordTFObj.get(MongoCollectionFieldNames.MONGO_WORD_TF);
					DBObject idfQueryObj = new BasicDBObject(MongoCollectionFieldNames.MONGO_REQUEST_ID,
							requestData.getRequestId()).append(MongoCollectionFieldNames.MONGO_WORD, word);
					double wordIdfValue = (double) DirenajMongoDriver.getInstance()
							.getOrgBehaviourProcessCosSimilarityIDF().findOne(idfQueryObj)
							.get(MongoCollectionFieldNames.MONGO_WORD_IDF);
					// calculate tf*idf values
					double wordTfIdfValue = wordIdfValue * wordTfValue;
					DBObject tfIdfValues = new BasicDBObject(MongoCollectionFieldNames.MONGO_WORD, word)
							.append(MongoCollectionFieldNames.MONGO_WORD_TF_IDF_VALUE, wordTfIdfValue)
							.append(MongoCollectionFieldNames.MONGO_WORD_TF_IDF_VALUE_SQUARE,
									wordTfIdfValue * wordTfIdfValue);
					wordTfIdfHashMap.put(word, wordTfIdfValue);
					wordTfIdfValuesList.add(tfIdfValues);
				}
				tweetTFIdfValues.put(MongoCollectionFieldNames.MONGO_WORD_TF_IDF_LIST, wordTfIdfValuesList);
				tweetTFIdfValues.put(MongoCollectionFieldNames.MONGO_WORD_TF_IDF_HASHMAP, wordTfIdfHashMap);
				tweetTFIdfValues.put(MongoCollectionFieldNames.MONGO_TWEET_WORDS, tweetWords);
				allTweetTFIdfValues.add(tweetTFIdfValues);
				allTweetTFIdfValues = DirenajMongoDriverUtil.insertBulkData2CollectionIfNeeded(
						DirenajMongoDriver.getInstance().getOrgBehaviourProcessCosSimilarityTF_IDF(),
						allTweetTFIdfValues, false);
			} finally {
				wordTFValues.close();
			}
		}
		DirenajMongoDriverUtil.insertBulkData2CollectionIfNeeded(
				DirenajMongoDriver.getInstance().getOrgBehaviourProcessCosSimilarityTF_IDF(), allTweetTFIdfValues,
				true);
	}

	@SuppressWarnings("unchecked")
	private void calculateIDFValues(CosineSimilarityRequestData requestData) {
		List<DBObject> wordIdfValues = new ArrayList<>(DirenajMongoDriver.getInstance().getBulkInsertSize());
		double totalTweetCount = (double) DirenajMongoDriver.getInstance().getOrgBehaviourTweetsShortInfo()
				.findOne(requestData.getRequestIdObject()).get(MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT);
		List<String> distinctWords = DirenajMongoDriver.getInstance().getOrgBehaviourCosSimilarityTF()
				.distinct(MongoCollectionFieldNames.MONGO_WORD, requestData.getRequestIdObject());
		for (String word : distinctWords) {
			BasicDBObject wordCountQueryObj = new BasicDBObject(MongoCollectionFieldNames.MONGO_REQUEST_ID,
					requestData.getRequestId()).append(MongoCollectionFieldNames.MONGO_WORD, word);
			long wordCount = DirenajMongoDriver.getInstance().getOrgBehaviourCosSimilarityTF().count(wordCountQueryObj);
			double idfValue = 1d + Math.log(totalTweetCount / (double) wordCount);
			wordCountQueryObj.append(MongoCollectionFieldNames.MONGO_WORD_IDF, idfValue);
			wordIdfValues.add(wordCountQueryObj);
			wordIdfValues = DirenajMongoDriverUtil.insertBulkData2CollectionIfNeeded(
					DirenajMongoDriver.getInstance().getOrgBehaviourProcessCosSimilarityIDF(), wordIdfValues, false);
		}
		DirenajMongoDriverUtil.insertBulkData2CollectionIfNeeded(
				DirenajMongoDriver.getInstance().getOrgBehaviourProcessCosSimilarityIDF(), wordIdfValues, true);

	}

	private void calculateTFValues(CosineSimilarityRequestData requestData) {
		List<DBObject> tweetTfValues = new ArrayList<>(DirenajMongoDriver.getInstance().getBulkInsertSize());
		List<String> allTweetIds = new ArrayList<>(DirenajMongoDriver.getInstance().getBulkInsertSize());
		// get tweets first
		DBCursor tweetsOfRequest = DirenajMongoDriver.getInstance().getOrgBehaviourTweetsOfRequest()
				.find(requestData.getQuery4OrgBehaviourTweetsOfRequestCollection());
		try {
			double totalTweetCount = 0;
			while (tweetsOfRequest.hasNext()) {
				totalTweetCount++;
				DBObject userTweetObj = tweetsOfRequest.next();
				// add tweet id to list
				String tweetId = TextUtils.getNotNullValue(userTweetObj.get(MongoCollectionFieldNames.MONGO_TWEET_ID));
				allTweetIds.add(tweetId);
				// get tweet text
				String tweetText = TextUtils
						.getNotNullValue(userTweetObj.get(MongoCollectionFieldNames.MONGO_TWEET_TEXT));
				String[] tweetWords = tweetText.split(" ");
				HashMap<String, Double> wordCounts = new HashMap<>();
				double tweetWordCount = (double) tweetWords.length;
				// count tweet words
				for (String word : tweetWords) {
					if (!TextUtils.isEmpty(word)) {
						word = DirenajMongoDriverUtil.getSuitableColumnName(word.toLowerCase(Locale.US));
						double wordCount = 0d;
						if (wordCounts.containsKey(word)) {
							wordCount = wordCounts.get(word);
						}
						wordCounts.put(word, ++wordCount);
					}
				}
				// normalize tweet word counts
				for (Entry<String, Double> wordCount : wordCounts.entrySet()) {
					DBObject tweetWordTfValue = new BasicDBObject(MongoCollectionFieldNames.MONGO_REQUEST_ID,
							requestData.getRequestId());
					tweetWordTfValue.put(MongoCollectionFieldNames.MONGO_TWEET_ID, tweetId);
					tweetWordTfValue.put(MongoCollectionFieldNames.MONGO_WORD, wordCount.getKey());
					tweetWordTfValue.put(MongoCollectionFieldNames.MONGO_WORD_TF,
							(wordCount.getValue() / tweetWordCount));
					tweetTfValues.add(tweetWordTfValue);
				}
				if (totalTweetCount % DirenajMongoDriver.getInstance().getBulkInsertSize() == 0) {
					tweetTfValues = DirenajMongoDriverUtil.insertBulkData2CollectionIfNeeded(
							DirenajMongoDriver.getInstance().getOrgBehaviourCosSimilarityTF(), tweetTfValues, false);
				}
			}
			if (totalTweetCount > 0d) {
				updateRequestInMongo(requestData, true);
			}
			DirenajMongoDriverUtil.insertBulkData2CollectionIfNeeded(
					DirenajMongoDriver.getInstance().getOrgBehaviourCosSimilarityTF(), tweetTfValues, true);
			DirenajMongoDriver.getInstance().getOrgBehaviourTweetsShortInfo()
					.insert(new BasicDBObject(MongoCollectionFieldNames.MONGO_REQUEST_ID, requestData.getRequestId())
							.append(MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT, totalTweetCount)
							.append(MongoCollectionFieldNames.MONGO_ALL_TWEET_IDS, allTweetIds));
		} finally {
			tweetsOfRequest.close();
		}
	}

}
