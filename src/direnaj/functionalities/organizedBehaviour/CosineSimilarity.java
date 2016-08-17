package direnaj.functionalities.organizedBehaviour;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import direnaj.driver.DirenajMongoDriver;
import direnaj.driver.DirenajMongoDriverUtil;
import direnaj.driver.MongoCollectionFieldNames;
import direnaj.twitter.twitter4j.external.DrenajCampaignRecord;
import direnaj.util.CosineSimilarityUtil;
import direnaj.util.DateTimeUtils;
import direnaj.util.PropertiesUtil;
import direnaj.util.TextUtils;

public class CosineSimilarity {

	private String originalRequestId;
	private List<CosineSimilarityRequestData> requestDataList;

	public CosineSimilarity(String requestId, boolean calculateGeneralSimilarity, boolean calculateHashTagSimilarity,
			Date earliestTweetDate, Date latestTweetDate, String campaignId) throws Exception {
		requestDataList = new ArrayList<>();
		originalRequestId = requestId;
		// first assume as resume process
		check4ExistingCosSimilarityCalculationRequests();

		Boolean calculateOnlyTopTrendDate = PropertiesUtil.getInstance()
				.getBooleanProperty("cosSimilarity.calculateOnlyTopTrendDate", true);
		if (calculateOnlyTopTrendDate) {
			DrenajCampaignRecord drenajCampaignRecord = DirenajMongoDriverUtil.getCampaign(campaignId);
			earliestTweetDate = drenajCampaignRecord.getMinCampaignDate();
			latestTweetDate = drenajCampaignRecord.getMaxCampaignDate();
		}
		// if this is not a resume process
		if (requestDataList == null || requestDataList.size() <= 0) {
			int hourBasisInterval = PropertiesUtil.getInstance()
					.getIntProperty("tweet.calculateSimilarity.hourBasisInterval", 2);
			// calculate general hour basis similarity
			if (calculateGeneralSimilarity) {
				prepareRequestData4HourlyBasisCalculation(earliestTweetDate, latestTweetDate, hourBasisInterval, false);
			}
			// calculate hashtag hour basis similarity
			if (calculateHashTagSimilarity) {
				prepareRequestData4HourlyBasisCalculation(earliestTweetDate, latestTweetDate, hourBasisInterval, true);
			}
		}
	}

	private void check4ExistingCosSimilarityCalculationRequests() throws Exception {
		DBCollection orgBehaviourRequestedSimilarityCalculations = DirenajMongoDriver.getInstance()
				.getOrgBehaviourRequestedSimilarityCalculations();
		BasicDBObject queryObj = new BasicDBObject();
		queryObj.put("originalRequestId", originalRequestId);
		DBCursor requestedSimilarityCalculations = orgBehaviourRequestedSimilarityCalculations.find(queryObj)
				.addOption(Bytes.QUERYOPTION_NOTIMEOUT);
		while (requestedSimilarityCalculations.hasNext()) {
			DBObject similarityCalculationRequest = requestedSimilarityCalculations.next();
			CosineSimilarityRequestData cosineSimilarityRequestData = new CosineSimilarityRequestData(originalRequestId,
					similarityCalculationRequest);
			requestDataList.add(cosineSimilarityRequestData);
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
		document.put(MongoCollectionFieldNames.MONGO_RESUME_BREAKPOINT, "");
		orgBehaviourRequestedSimilarityCalculations.insert(document);
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

	private void updateRequestInMongo(CosineSimilarityRequestData requestData, boolean tweetFound) {
		DBCollection orgBehaviourRequestedSimilarityCalculations = DirenajMongoDriver.getInstance()
				.getOrgBehaviourRequestedSimilarityCalculations();
		BasicDBObject findQuery = new BasicDBObject();
		findQuery.put(MongoCollectionFieldNames.MONGO_REQUEST_ID, requestData.getRequestId());
		BasicDBObject updateQuery = new BasicDBObject();
		updateQuery.append("$set", new BasicDBObject().append(MongoCollectionFieldNames.MONGO_TWEET_FOUND, tweetFound));
		orgBehaviourRequestedSimilarityCalculations.update(findQuery, updateQuery, true, false);
	}

	private void updateRequestInMongoByColumnName(CosineSimilarityRequestData requestData, String columnName,
			Object updateValue) {
		DBCollection orgBehaviourRequestedSimilarityCalculations = DirenajMongoDriver.getInstance()
				.getOrgBehaviourRequestedSimilarityCalculations();
		BasicDBObject findQuery = new BasicDBObject();
		findQuery.put(MongoCollectionFieldNames.MONGO_REQUEST_ID, requestData.getRequestId());
		BasicDBObject updateQuery = new BasicDBObject();
		updateQuery.append("$set", new BasicDBObject().append(columnName, updateValue));
		orgBehaviourRequestedSimilarityCalculations.update(findQuery, updateQuery, true, false);
	}

	public void calculateTweetSimilarities() {
		// first insert requests
		for (CosineSimilarityRequestData requestData : requestDataList) {
			if (requestData.getResumeBreakPoint() == null) {
				// log request
				Logger.getLogger(CosineSimilarity.class)
						.debug("Request Data is getting inserted. \n" + requestData.toString());
				insertRequest2Mongo(requestData);
			}
		}
		// then start calculations
		for (CosineSimilarityRequestData requestData : requestDataList) {
			DirenajMongoDriverUtil.cleanData4ResumeProcess(requestData);
			// calculate similarity
			// calculate TF values
			if (ResumeBreakPoint.shouldProcessCurrentBreakPoint(ResumeBreakPoint.TF_CALCULATION_COMPLETED,
					requestData.getResumeBreakPoint())) {
				Logger.getLogger(CosineSimilarity.class)
						.debug("TF Values are getting calculated for requestId : " + requestData.getRequestId());
				calculateTFValues(requestData);
				updateRequestInMongoByColumnName(requestData, MongoCollectionFieldNames.MONGO_RESUME_BREAKPOINT,
						ResumeBreakPoint.TF_CALCULATION_COMPLETED.name());
			}
			if (ResumeBreakPoint.shouldProcessCurrentBreakPoint(ResumeBreakPoint.IDF_CALCULATION_COMPLETED,
					requestData.getResumeBreakPoint())) {
				// calculate IDF values
				Logger.getLogger(CosineSimilarity.class)
						.debug("IDF Values are getting calculated for requestId : " + requestData.getRequestId());
				calculateIDFValues(requestData);
				updateRequestInMongoByColumnName(requestData, MongoCollectionFieldNames.MONGO_RESUME_BREAKPOINT,
						ResumeBreakPoint.IDF_CALCULATION_COMPLETED.name());
			}
			if (ResumeBreakPoint.shouldProcessCurrentBreakPoint(ResumeBreakPoint.TF_IDF_CALCULATION_COMPLETED,
					requestData.getResumeBreakPoint())) {
				// calculate TF IDF values
				Logger.getLogger(CosineSimilarity.class)
						.debug("TF_IDF Values are getting calculated for requestId : " + requestData.getRequestId());
				calculateTFIDFValues(requestData);
				updateRequestInMongoByColumnName(requestData, MongoCollectionFieldNames.MONGO_RESUME_BREAKPOINT,
						ResumeBreakPoint.TF_IDF_CALCULATION_COMPLETED.name());
			}
			if (ResumeBreakPoint.shouldProcessCurrentBreakPoint(ResumeBreakPoint.SIMILARTY_CALCULATED,
					requestData.getResumeBreakPoint())) {
				// calculate Similarities
				Logger.getLogger(CosineSimilarity.class)
						.debug("Similarity is getting calculated for requestId : " + requestData.getRequestId());
				calculateSimilarities(requestData);
				updateRequestInMongoByColumnName(requestData, MongoCollectionFieldNames.MONGO_RESUME_BREAKPOINT,
						ResumeBreakPoint.SIMILARTY_CALCULATED.name());
			}
			Logger.getLogger(CosineSimilarity.class)
					.debug("Cosine Similarity Calculation is DONE for requestId : " + requestData.getRequestId());
		}
	}

	@SuppressWarnings("unchecked")
	private void calculateSimilarities(CosineSimilarityRequestData requestData) {
		// FIXME 20160804 - cursor memory leak fix is reverted
		// AggregationOptions aggregationOptions =
		// AggregationOptions.builder().batchSize(50)
		// .outputMode(AggregationOptions.OutputMode.CURSOR).build();
		// Cursor allTweetIds =
		// DirenajMongoDriver.getInstance().getOrgBehaviourTweetsOfRequest().aggregate(
		// Arrays.asList(
		// (DBObject) new BasicDBObject("$match",
		// requestData.getQuery4OrgBehaviourTweetsOfRequestCollection()),
		// (DBObject) new BasicDBObject("$group",
		// new BasicDBObject("_id", "$" +
		// MongoCollectionFieldNames.MONGO_TWEET_ID))),
		// aggregationOptions);
		//
		// List<DBObject> tweetSimilarityWithOtherTweets = new ArrayList<>(
		// DirenajMongoDriver.getInstance().getBulkInsertSize());
		// while (allTweetIds.hasNext()) {
		// String queryTweetId = (String) allTweetIds.next().get("_id");

		DBObject requestTweetsShortInfoObject = DirenajMongoDriver.getInstance().getOrgBehaviourTweetsShortInfo()
				.findOne(requestData.getRequestIdObject());
		double allTweetCount = (double) requestTweetsShortInfoObject
				.get(MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT);
		ArrayList<String> allTweetIds = (ArrayList<String>) requestTweetsShortInfoObject
				.get(MongoCollectionFieldNames.MONGO_ALL_TWEET_IDS);
		ArrayList<String> allTweetIdsClone = (ArrayList<String>) allTweetIds.clone();
		List<DBObject> tweetSimilarityWithOtherTweets = new ArrayList<>(
				DirenajMongoDriver.getInstance().getBulkInsertSize());

		Map<String, Double> similarityComparisonOfAllTweets = CosineSimilarityUtil.getEmptyMap4SimilarityDecisionTree();

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
			// get cursor for clone
			for (String tweetId : allTweetIdsClone) {
				// FIXME Burada 2 retweet tweet'i hiç karşılaştırmayacak şekilde
				// bir logic koyalım
				if (!queryTweetId.equals(tweetId)) {
					Logger.getLogger(CosineSimilarity.class)
							.trace("Comparing TweetId : " + queryTweetId + " to TweetId : " + tweetId);
					BasicDBObject comparedTweetTFIdfValueObj = new BasicDBObject(
							MongoCollectionFieldNames.MONGO_REQUEST_ID, requestData.getRequestId())
									.append(MongoCollectionFieldNames.MONGO_TWEET_ID, tweetId);
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
					Logger.getLogger(CosineSimilarity.class).trace("Dot Product is : " + dotProduct
							+ " - comparedTweetVectorLength : " + comparedTweetVectorLength);
					CosineSimilarityUtil.findTweetSimilarityRange(similarityOfTweetWithOtherTweets, dotProduct,
							tweetVectorLength, comparedTweetVectorLength);
				}
			}
			queryTweetTFIdfQueryObj.append(MongoCollectionFieldNames.MONGO_TWEET_SIMILARITY_WITH_OTHER_TWEETS,
					similarityOfTweetWithOtherTweets);
			CosineSimilarityUtil.addSimilarities2General(similarityComparisonOfAllTweets,
					similarityOfTweetWithOtherTweets, allTweetCount);
			tweetSimilarityWithOtherTweets.add(queryTweetTFIdfQueryObj);
			tweetSimilarityWithOtherTweets = DirenajMongoDriverUtil.insertBulkData2CollectionIfNeeded(
					DirenajMongoDriver.getInstance().getOrgBehaviourProcessTweetSimilarity(),
					tweetSimilarityWithOtherTweets, false);

		}
		DirenajMongoDriverUtil.insertBulkData2CollectionIfNeeded(
				DirenajMongoDriver.getInstance().getOrgBehaviourProcessTweetSimilarity(),
				tweetSimilarityWithOtherTweets, true);

		CosineSimilarityUtil.calculateAvarage(similarityComparisonOfAllTweets, allTweetCount);

		updateRequestInMongoByColumnName(requestData, MongoCollectionFieldNames.NON_SIMILAR,
				similarityComparisonOfAllTweets.get(MongoCollectionFieldNames.NON_SIMILAR));
		updateRequestInMongoByColumnName(requestData, MongoCollectionFieldNames.SLIGHTLY_SIMILAR,
				similarityComparisonOfAllTweets.get(MongoCollectionFieldNames.SLIGHTLY_SIMILAR));
		updateRequestInMongoByColumnName(requestData, MongoCollectionFieldNames.SIMILAR,
				similarityComparisonOfAllTweets.get(MongoCollectionFieldNames.SIMILAR));
		updateRequestInMongoByColumnName(requestData, MongoCollectionFieldNames.VERY_SIMILAR,
				similarityComparisonOfAllTweets.get(MongoCollectionFieldNames.VERY_SIMILAR));
		updateRequestInMongoByColumnName(requestData, MongoCollectionFieldNames.MOST_SIMILAR,
				similarityComparisonOfAllTweets.get(MongoCollectionFieldNames.MOST_SIMILAR));

	}

	@SuppressWarnings("unchecked")
	private void calculateTFIDFValues(CosineSimilarityRequestData requestData) {
		double totalTweetCount = (double) DirenajMongoDriver.getInstance().getOrgBehaviourTweetsShortInfo()
				.findOne(requestData.getRequestIdObject()).get(MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT);

		// in tweetTfIdf Collect,on a record format is like
		// "requestId, tweetId, [word, tf*Idf, (tf*Idf)^2] dizi halinde
		List<DBObject> allTweetTFIdfValues = new ArrayList<>(DirenajMongoDriver.getInstance().getBulkInsertSize());

		// FIXME 20160804 - cursor memory leak fix is reverted
		// AggregationOptions aggregationOptions = AggregationOptions.builder()
		// .batchSize(DirenajMongoDriver.getInstance().getBulkInsertSize())
		// .outputMode(AggregationOptions.OutputMode.CURSOR).build();
		// // [{$match: {requestId :
		// // "20160522230016135400bc398-24f1-41df-888f-cfa6ff13c8b8"}},
		// {$group:
		// // {_id : "$word"}}
		//
		// Cursor allTweetIds =
		// DirenajMongoDriver.getInstance().getOrgBehaviourTweetsOfRequest().aggregate(
		// Arrays.asList(
		// (DBObject) new BasicDBObject("$match",
		// requestData.getQuery4OrgBehaviourTweetsOfRequestCollection()),
		// (DBObject) new BasicDBObject("$group",
		// new BasicDBObject("_id", "$" +
		// MongoCollectionFieldNames.MONGO_TWEET_ID))),
		// aggregationOptions);
		// while (allTweetIds.hasNext()) {
		// String tweetId = (String) allTweetIds.next().get("_id");

		Logger.getLogger(CosineSimilarity.class).debug("calculateTFIDFValues. allTweetIds size : " + totalTweetCount);

		// FIXME 20160522 Collection dan baska bir yontem bul
		List<String> allTweetIds = (List<String>) DirenajMongoDriver.getInstance().getOrgBehaviourTweetsShortInfo()
				.findOne(requestData.getRequestIdObject()).get(MongoCollectionFieldNames.MONGO_ALL_TWEET_IDS);
		Logger.getLogger(CosineSimilarity.class)
				.debug("calculateTFIDFValues. allTweetIds size : " + allTweetIds.size());
		for (String tweetId : allTweetIds) {

			List<String> tweetWords = new ArrayList<>(20);
			BasicDBObject tweetTFIdfValues = new BasicDBObject(MongoCollectionFieldNames.MONGO_REQUEST_ID,
					requestData.getRequestId()).append(MongoCollectionFieldNames.MONGO_TWEET_ID, tweetId);
			// get tf values for words in the tweet
			DBObject tfQueryObj = new BasicDBObject(MongoCollectionFieldNames.MONGO_REQUEST_ID,
					requestData.getRequestId()).append(MongoCollectionFieldNames.MONGO_TWEET_ID, tweetId);
			DBCursor wordTFValues = DirenajMongoDriver.getInstance().getOrgBehaviourCosSimilarityTF().find(tfQueryObj)
					.addOption(Bytes.QUERYOPTION_NOTIMEOUT);
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

		// FIXME Possible Memory Leak Fix is reverted
		// AggregationOptions aggregationOptions = AggregationOptions.builder()
		// .batchSize(DirenajMongoDriver.getInstance().getBulkInsertSize())
		// .outputMode(AggregationOptions.OutputMode.CURSOR).build();
		// [{$match: {requestId :
		// "20160522230016135400bc398-24f1-41df-888f-cfa6ff13c8b8"}}, {$group:
		// {_id : "$word"}}
		//
		// Cursor distinctWords =
		// DirenajMongoDriver.getInstance().getOrgBehaviourCosSimilarityTF().aggregate(
		// Arrays.asList(
		// (DBObject) new BasicDBObject("$match",
		// new BasicDBObject(MongoCollectionFieldNames.MONGO_REQUEST_ID,
		// requestData.getRequestId())),
		// (DBObject) new BasicDBObject("$group",
		// new BasicDBObject("_id", "$" +
		// MongoCollectionFieldNames.MONGO_WORD))),
		// aggregationOptions);
		// while (distinctWords.hasNext()) {
		// String word = (String) distinctWords.next().get("_id");

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
		Set<String> allUserIds = new HashSet<>(DirenajMongoDriver.getInstance().getBulkInsertSize());
		// get tweets first
		DBCursor tweetsOfRequest = DirenajMongoDriver.getInstance().getOrgBehaviourTweetsOfRequest()
				.find(requestData.getQuery4OrgBehaviourTweetsOfRequestCollection())
				.addOption(Bytes.QUERYOPTION_NOTIMEOUT);
		try {
			double totalTweetCount = 0;
			while (tweetsOfRequest.hasNext()) {
				totalTweetCount++;
				DBObject userTweetObj = tweetsOfRequest.next();
				// add tweet id to list
				String tweetId = TextUtils.getNotNullValue(userTweetObj.get(MongoCollectionFieldNames.MONGO_TWEET_ID));
				String userId = TextUtils.getNotNullValue(userTweetObj.get(MongoCollectionFieldNames.MONGO_USER_ID));
				allTweetIds.add(tweetId);
				allUserIds.add(userId);
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
			int distinctUserIdCount = allUserIds.size();
			DirenajMongoDriver.getInstance().getOrgBehaviourTweetsShortInfo()
					.insert(new BasicDBObject(MongoCollectionFieldNames.MONGO_REQUEST_ID, requestData.getRequestId())
							.append(MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT, totalTweetCount)
							.append(MongoCollectionFieldNames.MONGO_DISTINCT_USER_COUNT, distinctUserIdCount)
							.append(MongoCollectionFieldNames.MONGO_ALL_TWEET_IDS, allTweetIds)
							.append(MongoCollectionFieldNames.MONGO_ALL_USER_IDS, allUserIds));

			// update request
			updateRequestInMongoByColumnName(requestData, MongoCollectionFieldNames.MONGO_DISTINCT_USER_COUNT,
					distinctUserIdCount);
			updateRequestInMongoByColumnName(requestData, MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT,
					totalTweetCount);
		} finally {
			tweetsOfRequest.close();
		}
	}

}
