
package direnaj.functionalities.organizedBehaviour;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.Bytes;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import direnaj.adapter.DirenajInvalidJSONException;
import direnaj.domain.User;
import direnaj.domain.UserAccountProperties;
import direnaj.domain.UserTweets;
import direnaj.driver.DirenajDriverVersion2;
import direnaj.driver.DirenajMongoDriver;
import direnaj.driver.DirenajMongoDriverUtil;
import direnaj.driver.DirenajNeo4jDriver;
import direnaj.driver.MongoCollectionFieldNames;
import direnaj.servlet.OrganizedBehaviourDetectionRequestType;
import direnaj.twitter.UserAccountPropertyAnalyser;
import direnaj.twitter.twitter4j.Twitter4jUtil;
import direnaj.twitter.twitter4j.external.DrenajCampaignRecord;
import direnaj.util.CollectionUtil;
import direnaj.util.DateTimeUtils;
import direnaj.util.ListUtils;
import direnaj.util.PropertiesUtil;
import direnaj.util.TextUtils;
import twitter4j.HashtagEntity;
import twitter4j.Status;

public class OrganizationDetector implements Runnable {

	private String campaignId;
	private OrganizedBehaviourDetectionRequestType detectionRequestType;
	private DirenajDriverVersion2 direnajDriver;
	private DirenajMongoDriver direnajMongoDriver;
	private String requestDefinition;
	private String requestId;
	private Integer topHashtagCount;
	private List<String> tracedHashtagList;
	private String tracedSingleHashtag;
	private DBObject requestIdObj;
	private Date latestTweetDate;
	private Date earliestTweetDate;
	private Gson statusDeserializer;
	private ResumeBreakPoint resumeBreakPoint;
	private boolean isCleaningDone4ResumeProcess;
	private boolean calculateHashTagSimilarity;
	private boolean calculateGeneralSimilarity;
	private boolean bypassTweetCollection;
	private boolean bypassSimilarityCalculation;
	private boolean isExternalDateGiven;
	private ResumeBreakPoint workUntilBreakPoint;

	public OrganizationDetector(String campaignId, int topHashtagCount, String requestDefinition, String tracedHashtag,
			OrganizedBehaviourDetectionRequestType detectionRequestType, boolean disableGraphAnalysis,
			boolean calculateHashTagSimilarity, boolean calculateGeneralSimilarity, boolean bypassTweetCollection,
			String workUntilBreakPoint, boolean isExternalDateGiven, Date earliestDate, Date latestDate,
			boolean bypassSimilarityCalculation) {
		direnajDriver = new DirenajDriverVersion2();
		direnajMongoDriver = DirenajMongoDriver.getInstance();
		requestId = TextUtils.generateUniqueId4Request();
		requestIdObj = new BasicDBObject(MongoCollectionFieldNames.MONGO_REQUEST_ID, requestId);
		this.campaignId = campaignId;
		this.topHashtagCount = topHashtagCount;
		this.requestDefinition = requestDefinition;
		this.tracedHashtagList = new ArrayList<>();
		if (!TextUtils.isEmpty(tracedHashtag)) {
			tracedHashtag = tracedHashtag.toLowerCase(Locale.US);
			this.tracedHashtagList.add(tracedHashtag);
		}
		this.detectionRequestType = detectionRequestType;
		this.calculateHashTagSimilarity = calculateHashTagSimilarity;
		this.calculateGeneralSimilarity = calculateGeneralSimilarity;
		this.bypassTweetCollection = bypassTweetCollection;
		statusDeserializer = Twitter4jUtil.getGsonObject4Deserialization();
		isCleaningDone4ResumeProcess = false;
		this.isExternalDateGiven = isExternalDateGiven;
		this.earliestTweetDate = earliestDate;
		this.latestTweetDate = latestDate;
		this.bypassSimilarityCalculation = bypassSimilarityCalculation;
		insertRequest2Mongo();

		if (!TextUtils.isEmpty(workUntilBreakPoint)) {
			this.workUntilBreakPoint = ResumeBreakPoint.valueOf(workUntilBreakPoint);
		}
	}

	@SuppressWarnings("unchecked")
	public OrganizationDetector(String requestId) {
		// init objects
		direnajDriver = new DirenajDriverVersion2();
		direnajMongoDriver = DirenajMongoDriver.getInstance();
		statusDeserializer = Twitter4jUtil.getGsonObject4Deserialization();
		this.requestId = requestId;
		requestIdObj = new BasicDBObject(MongoCollectionFieldNames.MONGO_REQUEST_ID, requestId);
		// get request object from mongo
		BasicDBObject findQuery = new BasicDBObject();
		findQuery.put("_id", requestId);
		DBObject requestObj = direnajMongoDriver.getOrgBehaviorRequestCollection().findOne(findQuery);
		// retrive features of request object
		this.campaignId = (String) requestObj.get("campaignId");
		this.topHashtagCount = (int) Double.parseDouble(requestObj.get("topHashtagCount").toString());
		this.requestDefinition = (String) requestObj.get("requestDefinition");
		this.tracedHashtagList = (List<String>) requestObj.get("tracedHashtag");
		this.detectionRequestType = OrganizedBehaviourDetectionRequestType
				.valueOf((String) requestObj.get("requestType"));
		this.calculateGeneralSimilarity = (boolean) requestObj
				.get(MongoCollectionFieldNames.MONGO_GENARAL_SIMILARITY_CALCULATION);
		this.calculateHashTagSimilarity = (boolean) requestObj
				.get(MongoCollectionFieldNames.MONGO_HASHTAG_SIMILARITY_CALCULATION);
		this.bypassSimilarityCalculation = (boolean) requestObj
				.get(MongoCollectionFieldNames.MONGO_BYPASS_SIMILARITY_CALCULATION);
		this.isExternalDateGiven = (boolean) requestObj.get(MongoCollectionFieldNames.MONGO_IS_EXTERNAL_DATE_GIVEN);

		this.earliestTweetDate = DateTimeUtils
				.cast2Date(requestObj.get(MongoCollectionFieldNames.MONGO_EARLIEST_TWEET_TIME));
		this.latestTweetDate = DateTimeUtils
				.cast2Date(requestObj.get(MongoCollectionFieldNames.MONGO_LATEST_TWEET_TIME));

		Object retrievedResumeBreakPoint = requestObj.get(MongoCollectionFieldNames.MONGO_RESUME_BREAKPOINT);
		if (retrievedResumeBreakPoint != null) {
			resumeBreakPoint = ResumeBreakPoint.valueOf(retrievedResumeBreakPoint.toString());
		}
		isCleaningDone4ResumeProcess = false;
		updateRequestInMongoByColumnName(MongoCollectionFieldNames.MONGO_RESUME_PROCESS, false);
	}

	public HashMap<String, Double> calculateInNeo4J(List<String> userIds, String subgraphEdgeLabel) {
		HashMap<String, Double> userClosenessCentralities = new HashMap<>();
		if (!TextUtils.isEmpty(subgraphEdgeLabel)) {
			for (String userId : userIds) {
				String closenessCalculateQuery = "START centralityNode=node:node_auto_index(calculationEdge = {calculationEdge}) " //
						+ "WITH centralityNode " //
						+ "MATCH p= (centralityNode)-[r]-(:User) " //
						+ "WITH nodes(p) as nodes " //
						+ "MATCH (a), (b) WHERE a<>b and a.id_str = {userId} and  b in nodes and b:User " //
						+ "WITH length(shortestPath((a)<-[:" + subgraphEdgeLabel + "]-(b))) AS dist " //
						+ "RETURN DISTINCT sum(1.0/dist) AS closenessCentrality"; //

				String queryParams = "{}";
				try {
					JSONObject queryParamsJson = new JSONObject();
					queryParamsJson.put("userId", userId);
					queryParamsJson.put("calculationEdge", subgraphEdgeLabel);
					queryParams = queryParamsJson.toString();
				} catch (JSONException e) {
					Logger.getLogger(OrganizationDetector.class)
							.error("Error in OrganizationDetector calculateInNeo4J.", e);
				}

				Map<String, Object> cypherResult = DirenajNeo4jDriver.getInstance().executeSingleResultCypher(
						closenessCalculateQuery, queryParams, ListUtils.getListOfStrings("closenessCentrality"));
				double closenessCentrality = 0d;
				if (cypherResult.containsKey("closenessCentrality")) {
					closenessCentrality = Double.valueOf(cypherResult.get("closenessCentrality").toString());
				}
				userClosenessCentralities.put(userId, closenessCentrality);
			}
		}
		return userClosenessCentralities;
	}

	public String createSubgraphByAddingEdges(List<String> userIds) {
		String newRelationName = "FOLLOWS_" + requestId;
		String cypherCreateNode = "CREATE (n:ClosenessCentralityCalculator { calculationEdge : '" + newRelationName
				+ "' })";
		DirenajNeo4jDriver.getInstance().executeNoResultCypher(cypherCreateNode, "{}");

		int hopCount = PropertiesUtil.getInstance().getIntProperty("graphDb.closenessCentrality.calculation.hopNode",
				2);
		// collect all userIds
		String collectionRepresentation4UserIds = "";
		JSONArray array = new JSONArray();
		for (String userId : userIds) {
			collectionRepresentation4UserIds = ",'" + userId + "'";
			array.put(userId);
		}
		// delete comma in the beginning
		collectionRepresentation4UserIds = collectionRepresentation4UserIds.substring(1);
		// execute cypher query
		// String cypherQuery = "START begin=node:node_auto_index('id_str:(" +
		// collectionRepresentation4UserIds + ")') " //
		// + "WITH begin " //
		// + "MATCH p = (begin:User)-[r:FOLLOWS*.." + hopCount + "]-(end:User) "
		// //
		// + "WITH distinct nodes(p) as nodes " //
		// + "MATCH
		// (x)-[:FOLLOWS]->(y),(n:ClosenessCentralityCalculator{calculationEdge:
		// '" + newRelationName + "' }) " //
		// + "WHERE x in nodes and y in nodes " //
		// + "CREATE (n)-[:CalculateCentrality]->(x)-[r1:" + newRelationName +Y
		// "]->(y)<-[:CalculateCentrality]-(n)";

		String cypherQuery = "MATCH p = (begin:User)-[r:FOLLOWS*.." + hopCount + "]-(end:User) "
				+ "WHERE begin.id_str IN {id_str} " //
				+ "WITH distinct nodes(p) as nodes " //
				+ "MATCH (n:ClosenessCentralityCalculator),(z:User) " //
				+ "WHERE  n.calculationEdge = {calculationEdge} and z in nodes " //
				+ "CREATE UNIQUE (n)-[:CalculateCentrality]->(z) " //
				+ "WITH nodes " //
				+ "MATCH (x)-[:FOLLOWS]->(y) " //
				+ "WHERE x in nodes and y in nodes " //
				+ "CREATE UNIQUE (x)-[r1:" + newRelationName + "]->(y)";

		String params = "";
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("id_str", array);
			jsonObject.put("calculationEdge", newRelationName);
			params = jsonObject.toString();
		} catch (JSONException e) {
			Logger.getLogger(OrganizationDetector.class)
					.error("Error in OrganizationDetector createSubgraphByAddingEdges.", e);
		}
		Logger.getLogger(OrganizationDetector.class).debug("Params :" + params);
		DirenajNeo4jDriver.getInstance().executeNoResultCypher(cypherQuery, params);
		return newRelationName;
	}

	public void detectOrganizedBehaviourInHashtags() {
		try {
			DirenajMongoDriverUtil.cleanData4ResumeProcess(requestId, requestIdObj, resumeBreakPoint,
					bypassSimilarityCalculation);
			isCleaningDone4ResumeProcess = true;
			if (ResumeBreakPoint.shouldProcessCurrentBreakPoint(ResumeBreakPoint.INIT, resumeBreakPoint,
					workUntilBreakPoint)) {
				Map<String, Double> hashtagCounts = direnajDriver.getHashtagCounts(campaignId);
				// get hashtag users
				LinkedHashMap<String, Double> topHashtagCounts = CollectionUtil.discardOtherElementsOfMap(hashtagCounts,
						topHashtagCount);
				Logger.getLogger(OrganizationDetector.class).debug("Top Hashtags Descending");
				for (Entry<String, Double> hashtag : topHashtagCounts.entrySet()) {
					Logger.getLogger(OrganizationDetector.class).debug(hashtag.getKey() + " - " + hashtag.getValue());
					tracedHashtagList.add(hashtag.getKey());
				}
			}
			getMetricsOfUsersOfHashTag();
		} catch (Exception e) {
			Logger.getLogger(OrganizationDetector.class).error("Error in detectOrganizedBehaviourInHashtags", e);
		}
	}

	public void getMetricsOfUsersOfHashTag() throws DirenajInvalidJSONException, Exception {
		try {
			// update found hashtags
			updateRequestInMongo();
			if (!isCleaningDone4ResumeProcess) {
				DirenajMongoDriverUtil.cleanData4ResumeProcess(requestId, requestIdObj, resumeBreakPoint,
						bypassSimilarityCalculation);
			}
			for (String tracedHashtag : tracedHashtagList) {
				Logger.getLogger(OrganizationDetector.class.getSimpleName())
						.debug("Analysis For Hashtag : " + tracedHashtag);
				tracedSingleHashtag = tracedHashtag;
				if (ResumeBreakPoint.shouldProcessCurrentBreakPoint(ResumeBreakPoint.TWEET_COLLECTION_COMPLETED,
						resumeBreakPoint, workUntilBreakPoint)) {
					direnajDriver.saveHashtagUsers2Mongo(campaignId, tracedHashtag, requestId);
					collectTweetsOfAllUsers(requestId);
				}
				if (ResumeBreakPoint.shouldProcessCurrentBreakPoint(ResumeBreakPoint.USER_ANALYZE_COMPLETED,
						resumeBreakPoint, workUntilBreakPoint)) {
					saveData4UserAnalysis();
				}
			}
			calculateTweetSimilarities();
			if (ResumeBreakPoint.shouldProcessCurrentBreakPoint(ResumeBreakPoint.STATISCTIC_CALCULATED,
					resumeBreakPoint, workUntilBreakPoint)) {
				calculateStatistics();
			}
			if (workUntilBreakPoint != null) {
				updateRequestInMongoByColumnName(MongoCollectionFieldNames.MONGO_RESUME_PROCESS, Boolean.TRUE);
			} else {
				changeRequestStatusInMongo(true);
			}
			cleanTempData();
			Logger.getLogger(OrganizationDetector.class.getSimpleName())
					.debug("Hashtag Analysis is Finished for requestId : " + requestId);
		} catch (Exception e) {
			updateRequestInMongoByColumnName(MongoCollectionFieldNames.MONGO_RESUME_PROCESS, Boolean.TRUE);
			throw e;
		}
	}

	private void cleanTempData() {
		removePreProcessUsers();
		DirenajMongoDriver.getInstance().getOrgBehaviourTweetsOfRequest().remove(requestIdObj);
	}

	private void calculateStatistics() throws Exception {
		BasicDBObject query4CosSimilarityRequest = new BasicDBObject(
				MongoCollectionFieldNames.MONGO_COS_SIM_REQ_ORG_REQUEST_ID, requestId);
		query4CosSimilarityRequest.put(MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT, new BasicDBObject("$gt", 5));
		StatisticCalculator statisticCalculator = new StatisticCalculator(requestId, requestIdObj,
				query4CosSimilarityRequest, tracedSingleHashtag, campaignId, bypassSimilarityCalculation,
				resumeBreakPoint, workUntilBreakPoint);
		statisticCalculator.calculateStatistics();
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void calculateTweetSimilarities() throws Exception {
		Logger.getLogger(OrganizationDetector.class.getSimpleName()).debug("Calculate General Similarity : "
				+ calculateGeneralSimilarity + " - Calculate Hashtag Similarity : " + calculateHashTagSimilarity);
		// calculate similarity
		new CosineSimilarity(requestId, calculateGeneralSimilarity, calculateHashTagSimilarity, earliestTweetDate,
				latestTweetDate, campaignId, isExternalDateGiven, bypassSimilarityCalculation)
						.calculateTweetSimilarities();
	}

	public void collectTweetsOfAllUsers(String requestId) {
		if (!bypassTweetCollection) {
			// get initial objects
			DBCollection orgBehaviorPreProcessUsers = direnajMongoDriver.getOrgBehaviorPreProcessUsers();
			// get pre process users
			DBCursor preProcessUsers = orgBehaviorPreProcessUsers.find(requestIdObj)
					.addOption(Bytes.QUERYOPTION_NOTIMEOUT);
			Boolean calculateOnlyTopTrendDate = PropertiesUtil.getInstance()
					.getBooleanProperty("cosSimilarity.calculateOnlyTopTrendDate", true);
			DrenajCampaignRecord drenajCampaignRecord = DirenajMongoDriverUtil.getCampaign(campaignId);

			int maxUserCount = PropertiesUtil.getInstance().getIntProperty("toolkit.tweetCollection.maxUserCount",
					5000);
			int userCount = 0;
			try {
				while (preProcessUsers.hasNext() && userCount <= maxUserCount) {
					userCount++;
					Logger.getLogger(OrganizationDetector.class.getSimpleName())
							.debug("Processed User Count : " + userCount);
					DBObject preProcessUser = preProcessUsers.next();
					try {
						User user = DirenajMongoDriverUtil.parsePreProcessUsers(preProcessUser);
						Twitter4jUtil.saveTweetsOfUser(user, calculateOnlyTopTrendDate,
								drenajCampaignRecord.getMinCampaignDate(), drenajCampaignRecord.getMaxCampaignDate(),
								campaignId);
					} catch (Exception e) {
						Logger.getLogger(OrganizationDetector.class.getSimpleName())
								.error("Twitter4jUtil-collectTweetsOfAllUsers", e);
					}
				}
			} finally {
				preProcessUsers.close();
			}
		}
		updateRequestInMongoByColumnName(MongoCollectionFieldNames.MONGO_RESUME_BREAKPOINT,
				ResumeBreakPoint.TWEET_COLLECTION_COMPLETED.name());
	}

	@Override
	public void run() {
		try {
			switch (detectionRequestType) {
			case CheckHashtagsInCampaign:
				detectOrganizedBehaviourInHashtags();
				break;
			case CheckSingleHashtagInCampaign:
				getMetricsOfUsersOfHashTag();
				break;
			}
		} catch (Exception e) {
			Logger.getLogger(OrganizationDetector.class).error("Error in OrganizationDetector run", e);
		}

	}

	public User analyzePreProcessUser(DBObject preProcessUser) throws Exception {
		// get collection
		// parse user
		User domainUser = DirenajMongoDriverUtil.parsePreProcessUsers(preProcessUser);
		// get tweets of users in an interval of two weeks

		Integer tweetDuration = PropertiesUtil.getInstance().getIntProperty("tweet.checkInterval.inWeeks", 1);
		BasicDBObject tweetsRetrievalQuery = new BasicDBObject("user.id", Long.valueOf(domainUser.getUserId())).append(
				"createdAt",
				new BasicDBObject("$gt",
						DateTimeUtils.subtractWeeksFromDate(domainUser.getCampaignTweetPostDate(), tweetDuration))
								.append("$lt", DateTimeUtils.addWeeksToDate(domainUser.getCampaignTweetPostDate(),
										tweetDuration)));

		Logger.getLogger(OrganizationDetector.class)
				.debug("Tweets Retrieval Query : " + tweetsRetrievalQuery.toString());

		// BasicDBObject tweetsRetrievalQuery = new BasicDBObject("id",
		// Long.valueOf(633531082739216384l));
		BasicDBObject keys = new BasicDBObject("_id", false);

		DBCursor tweetsOfUser = direnajMongoDriver.getOrgBehaviourUserTweets().find(tweetsRetrievalQuery, keys)
				.addOption(Bytes.QUERYOPTION_NOTIMEOUT);
		if (tweetsOfUser == null || !tweetsOfUser.hasNext()) {
			domainUser = null;
		} else {
			List<DBObject> allUserTweets = new ArrayList<>(DirenajMongoDriver.getInstance().getBulkInsertSize());
			try {
				while (tweetsOfUser.hasNext()) {
					DBObject tweetObj = tweetsOfUser.next();
					String string = tweetObj.toString();
					// FIXME tweet'leri system.out'a yazmak istediğinde aç
					// System.out.println(string + ",");

					Status twitter4jStatus = Twitter4jUtil.deserializeTwitter4jStatusFromGson(statusDeserializer,
							string);

					if ((twitter4jStatus.getExtendedMediaEntities() != null
							&& twitter4jStatus.getExtendedMediaEntities().length > 0)
							|| (twitter4jStatus.getMediaEntities() != null
									&& twitter4jStatus.getMediaEntities().length > 0)) {
						domainUser.incrementCountOfMediaPosts();
					}
					domainUser.addValue2CountOfUsedUrls((double) twitter4jStatus.getURLEntities().length);
					domainUser.addValue2CountOfHashtags((double) twitter4jStatus.getHashtagEntities().length);
					domainUser.addValue2CountOfMentionedUsers((double) twitter4jStatus.getUserMentionEntities().length);
					domainUser.incrementPostDeviceCount(twitter4jStatus.getSource());
					domainUser.incrementPostCount();
					domainUser.setFavoriteCount(twitter4jStatus.getUser().getFavouritesCount());
					domainUser.setWholeStatusesCount(twitter4jStatus.getUser().getStatusesCount());
					// get user tweet data
					UserTweets userTweet = new UserTweets();
					if (twitter4jStatus.getHashtagEntities() != null) {
						HashtagEntity[] hashtagEntities = twitter4jStatus.getHashtagEntities();
						for (HashtagEntity hashtagEntity : hashtagEntities) {
							if (hashtagEntity.getText().equalsIgnoreCase(tracedSingleHashtag)) {
								userTweet.setHashtagTweet(true);
								domainUser.incrementHashtagPostCount();
								break;
							}
						}
					}
					// check earliest tweet date
					if (!isExternalDateGiven) {
						if (earliestTweetDate == null) {
							earliestTweetDate = twitter4jStatus.getCreatedAt();
						} else if (twitter4jStatus.getCreatedAt() != null
								&& twitter4jStatus.getCreatedAt().compareTo(earliestTweetDate) < 0) {
							earliestTweetDate = twitter4jStatus.getCreatedAt();
						}
						// check latest tweet date
						if (latestTweetDate == null) {
							latestTweetDate = twitter4jStatus.getCreatedAt();
						} else if (twitter4jStatus.getCreatedAt() != null
								&& twitter4jStatus.getCreatedAt().compareTo(latestTweetDate) > 0) {
							latestTweetDate = twitter4jStatus.getCreatedAt();
						}
					}
					userTweet.setTweetText(twitter4jStatus.getText());
					userTweet.setTweetId(String.valueOf(twitter4jStatus.getId()));
					userTweet.setTweetCreationDate(DateTimeUtils.getRataDieFormat4Date(twitter4jStatus.getCreatedAt()));

					// get user tweets
					BasicDBObject userTweetData = new BasicDBObject();
					userTweetData.put(MongoCollectionFieldNames.MONGO_REQUEST_ID, requestId);
					userTweetData.put(MongoCollectionFieldNames.MONGO_USER_ID, Long.valueOf(domainUser.getUserId()));
					userTweetData.put(MongoCollectionFieldNames.MONGO_TWEET_ID, userTweet.getTweetId());
					userTweetData.put(MongoCollectionFieldNames.MONGO_TWEET_TEXT, userTweet.getTweetText());
					userTweetData.put(MongoCollectionFieldNames.MONGO_IS_HASHTAG_TWEET, userTweet.isHashtagTweet());
					userTweetData.put(MongoCollectionFieldNames.MONGO_TWEET_CREATION_DATE,
							userTweet.getTweetCreationDate());
					userTweetData.put(MongoCollectionFieldNames.MONGO_ACTUAL_TWEET_OBJECT, tweetObj);
					allUserTweets.add(userTweetData);
					// insert user tweets
					allUserTweets = DirenajMongoDriverUtil.insertBulkData2CollectionIfNeeded(
							DirenajMongoDriver.getInstance().getOrgBehaviourTweetsOfRequest(), allUserTweets, false);

				}
				DirenajMongoDriverUtil.insertBulkData2CollectionIfNeeded(
						DirenajMongoDriver.getInstance().getOrgBehaviourTweetsOfRequest(), allUserTweets, true);
			} catch (Exception e) {
				Logger.getLogger(OrganizationDetector.class.getSimpleName()).error("analyzePreProcessUser method error",
						e);
			} finally {
				tweetsOfUser.close();

			}
		}
		return domainUser;
	}

	private void bulkUpdateMongo4ClosenessCentrality(HashMap<String, Double> userClosenessCentralities) {
		DBCollection processInputDataCollection = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData();
		BulkWriteOperation bulkWriteOperation = processInputDataCollection.initializeUnorderedBulkOperation();

		for (Map.Entry<String, Double> entry : userClosenessCentralities.entrySet()) {
			bulkWriteOperation.find(new BasicDBObject("requestId", requestId).append("userId", entry.getKey()))
					.updateOne(new BasicDBObject("$set", new BasicDBObject("closenessCentrality", entry.getValue())));
		}
		bulkWriteOperation.execute();
	}

	@SuppressWarnings("unused")
	private void calculateClosenessCentrality(List<String> userIds) {
		String subgraphEdgeLabel = createSubgraphByAddingEdges(userIds);
		HashMap<String, Double> userClosenessCentralities = calculateInNeo4J(userIds, subgraphEdgeLabel);
		bulkUpdateMongo4ClosenessCentrality(userClosenessCentralities);
		// clearNeo4jSubGraph(subgraphEdgeLabel);
	}

	public void changeRequestStatusInMongo(boolean requestStatus) {
		DBCollection organizedBehaviorCollection = direnajMongoDriver.getOrgBehaviorRequestCollection();
		BasicDBObject findQuery = new BasicDBObject();
		findQuery.put("_id", requestId);

		BasicDBObject updateQuery = new BasicDBObject();
		updateQuery.append("$set", new BasicDBObject().append("processCompleted", requestStatus));
		organizedBehaviorCollection.update(findQuery, updateQuery);

		updateQuery = new BasicDBObject();
		updateQuery.append("$set", new BasicDBObject().append("statusChangeTime", DateTimeUtils.getLocalDate()));
		organizedBehaviorCollection.update(findQuery, updateQuery);

		updateQuery = new BasicDBObject();
		updateQuery.append("$set", new BasicDBObject().append(MongoCollectionFieldNames.MONGO_EARLIEST_TWEET_TIME,
				TextUtils.getNotNullValue(earliestTweetDate)));
		organizedBehaviorCollection.update(findQuery, updateQuery);

		updateQuery = new BasicDBObject();
		updateQuery.append("$set", new BasicDBObject().append(MongoCollectionFieldNames.MONGO_LATEST_TWEET_TIME,
				TextUtils.getNotNullValue(latestTweetDate)));
		organizedBehaviorCollection.update(findQuery, updateQuery);

		updateQuery = new BasicDBObject();
		updateQuery.append("$set",
				new BasicDBObject().append(MongoCollectionFieldNames.MONGO_RESUME_PROCESS, Boolean.FALSE));
		organizedBehaviorCollection.update(findQuery, updateQuery);

		updateQuery = new BasicDBObject();
		updateQuery.append("$set", new BasicDBObject().append(MongoCollectionFieldNames.MONGO_RESUME_BREAKPOINT,
				ResumeBreakPoint.FINISHED.name()));
		organizedBehaviorCollection.update(findQuery, updateQuery);
	}

	private void clearNeo4jSubGraph(String subgraphEdgeLabel) {
		if (!TextUtils.isEmpty(subgraphEdgeLabel)) {
			String deleteRelationshipCypher = "MATCH (u:User)-[r:" + subgraphEdgeLabel + "]-(t:User) DELETE r";
			DirenajNeo4jDriver.getInstance().executeNoResultCypher(deleteRelationshipCypher, "{}");
			// delete Centrality Calculator Node and its relations
			String params = "";
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("calculationEdge", subgraphEdgeLabel);
				params = jsonObject.toString();
			} catch (JSONException e) {
				Logger.getLogger(OrganizationDetector.class).error("Error in OrganizationDetector clearNeo4jSubGraph.",
						e);
			}
			// delete relationships
			String deleteCentralitycalculatorRelationShips = "MATCH (n:ClosenessCentralityCalculator)-[r]-(u:User) "
					+ "WHERE  n.calculationEdge = {calculationEdge} DELETE r";
			DirenajNeo4jDriver.getInstance().executeNoResultCypher(deleteCentralitycalculatorRelationShips, params);
			// delete node
			String deleteCentralitycalculatorNode = "MATCH (n:ClosenessCentralityCalculator) "
					+ "WHERE  n.calculationEdge = {calculationEdge} DELETE n";
			DirenajNeo4jDriver.getInstance().executeNoResultCypher(deleteCentralitycalculatorNode, params);
		}
	}

	/**
	 * @param campaignId
	 * @param limit
	 * @return
	 * 
	 * @deprecated
	 */
	private Long getQueryLimit(String campaignId, String limit) {
		Long originalLimit;
		if (TextUtils.isEmpty(limit)) {
			DBObject campaignCountQuery = new BasicDBObject("campaign_id", campaignId);
			// get total tweet count
			originalLimit = direnajMongoDriver.executeCountQuery(direnajMongoDriver.getTweetsCollection(),
					campaignCountQuery);
		} else {
			originalLimit = Long.valueOf(limit);
		}
		return originalLimit;
	}

	private void insertRequest2Mongo() {
		DBCollection organizedBehaviorCollection = direnajMongoDriver.getOrgBehaviorRequestCollection();
		BasicDBObject document = new BasicDBObject();
		document.put("_id", requestId);
		document.put("requestType", detectionRequestType.name());
		document.put("requestDefinition", requestDefinition);
		document.put(MongoCollectionFieldNames.MONGO_REQUEST_CAMPAIGN_ID, campaignId);
		document.put("topHashtagCount", topHashtagCount);
		document.put("tracedHashtag", tracedHashtagList);
		document.put("processCompleted", Boolean.FALSE);
		document.put("similartyCalculationCompleted", Boolean.FALSE);
		document.put("statusChangeTime", DateTimeUtils.getLocalDate());
		document.put(MongoCollectionFieldNames.MONGO_EARLIEST_TWEET_TIME, "");
		document.put(MongoCollectionFieldNames.MONGO_LATEST_TWEET_TIME, "");
		document.put(MongoCollectionFieldNames.MONGO_RESUME_BREAKPOINT, null);
		document.put(MongoCollectionFieldNames.MONGO_RESUME_PROCESS, false);
		document.put(MongoCollectionFieldNames.MONGO_GENARAL_SIMILARITY_CALCULATION, calculateGeneralSimilarity);
		document.put(MongoCollectionFieldNames.MONGO_HASHTAG_SIMILARITY_CALCULATION, calculateHashTagSimilarity);
		document.put(MongoCollectionFieldNames.MONGO_BYPASS_TWEET_COLLECTION, bypassTweetCollection);
		document.put(MongoCollectionFieldNames.MONGO_BYPASS_SIMILARITY_CALCULATION, bypassSimilarityCalculation);
		document.put(MongoCollectionFieldNames.MONGO_IS_EXTERNAL_DATE_GIVEN, isExternalDateGiven);
		document.put(MongoCollectionFieldNames.MONGO_EARLIEST_TWEET_TIME, earliestTweetDate);
		document.put(MongoCollectionFieldNames.MONGO_LATEST_TWEET_TIME, latestTweetDate);
		organizedBehaviorCollection.insert(document);
	}

	private List<User> saveOrganizedBehaviourInputData(List<User> domainUsers) {
		List<DBObject> allUserInputData = new ArrayList<>(DirenajMongoDriver.getInstance().getBulkInsertSize());
		for (User user : domainUsers) {
			// first init user account properties
			UserAccountProperties accountProperties = user.getAccountProperties();
			BasicDBObject userInputData = new BasicDBObject();
			userInputData.put(MongoCollectionFieldNames.MONGO_REQUEST_ID, requestId);
			userInputData.put(MongoCollectionFieldNames.MONGO_USER_ID, user.getUserId());
			userInputData.put(MongoCollectionFieldNames.MONGO_USER_SCREEN_NAME, user.getUserScreenName());
			userInputData.put(MongoCollectionFieldNames.MONGO_USER_CLOSENESS_CENTRALITY, new Double(0));

			userInputData.put(MongoCollectionFieldNames.MONGO_USER_HASHTAG_POST_COUNT, user.getHashtagPostCount());
			userInputData.put(MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT, user.getFavoriteCount());
			userInputData.put(MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT, user.getWholeStatusesCount());

			userInputData.put(MongoCollectionFieldNames.MONGO_USER_FRIEND_FOLLOWER_RATIO,
					accountProperties.getFriendFollowerRatio());
			userInputData.put(MongoCollectionFieldNames.MONGO_URL_RATIO, accountProperties.getUrlRatio());
			userInputData.put(MongoCollectionFieldNames.MONGO_HASHTAG_RATIO, accountProperties.getHashtagRatio());
			userInputData.put(MongoCollectionFieldNames.MONGO_MENTION_RATIO, accountProperties.getMentionRatio());
			userInputData.put(MongoCollectionFieldNames.MONGO_MEDIA_RATIO, accountProperties.getMediaPostRatio());
			userInputData.put(MongoCollectionFieldNames.MONGO_USER_POST_TWITTER_DEVICE_RATIO,
					accountProperties.getTwitterPostRatio());
			userInputData.put(MongoCollectionFieldNames.MONGO_USER_POST_MOBILE_DEVICE_RATIO,
					accountProperties.getMobilePostRatio());
			userInputData.put(MongoCollectionFieldNames.MONGO_USER_THIRD_PARTY_DEVICE_RATIO,
					accountProperties.getThirdPartyPostRatio());

			userInputData.put(MongoCollectionFieldNames.MONGO_USER_PROTECTED, user.isProtected());
			userInputData.put(MongoCollectionFieldNames.MONGO_USER_VERIFIED, user.isVerified());
			userInputData.put(MongoCollectionFieldNames.MONGO_USER_CREATION_DATE,
					DateTimeUtils.getUTCDateTimeStringInGenericFormat(user.getCreationDate()));
			userInputData.put(MongoCollectionFieldNames.MONGO_USER_CREATION_DATE_IN_RATA_DIE,
					DateTimeUtils.getRataDieFormat4Date(user.getCreationDate()));
			userInputData.put(MongoCollectionFieldNames.MONGO_USER_DAILY_AVARAGE_POST_COUNT,
					accountProperties.getAvarageDailyPostCount());

			allUserInputData.add(userInputData);
		}
		if (allUserInputData != null && allUserInputData.size() > 0) {
			direnajMongoDriver.getOrgBehaviourProcessInputData().insert(allUserInputData);
		}
		return new ArrayList<User>();
	}

	public void saveData4UserAnalysis() throws Exception {
		List<User> domainUsers = new ArrayList<User>();
		// get total user count for detection
		DBCollection orgBehaviorPreProcessUsers = direnajMongoDriver.getOrgBehaviorPreProcessUsers();
		int preprocessUserCounts = (int) direnajMongoDriver.executeCountQuery(orgBehaviorPreProcessUsers, requestIdObj);
		DBCursor preProcessUsers = orgBehaviorPreProcessUsers.find(requestIdObj).addOption(Bytes.QUERYOPTION_NOTIMEOUT);
		try {
			int i = 0;
			while (preProcessUsers.hasNext()) {
				i++;
				DBObject preProcessUser = preProcessUsers.next();
				User domainUser = analyzePreProcessUser(preProcessUser);
				if (domainUser == null) {
					continue;
				}
				// do hashtag / mention / url & twitter device ratio
				UserAccountPropertyAnalyser.getInstance().calculateUserAccountProperties(domainUser);
				domainUsers.add(domainUser);
				if ((i == preprocessUserCounts)
						|| domainUsers.size() > DirenajMongoDriver.getInstance().getBulkInsertSize()) {
					domainUsers = saveOrganizedBehaviourInputData(domainUsers);
				}
			}
			saveOrganizedBehaviourInputData(domainUsers);
		} finally {
			preProcessUsers.close();
		}
		updateRequestInMongoByColumnName(MongoCollectionFieldNames.MONGO_RESUME_BREAKPOINT,
				ResumeBreakPoint.USER_ANALYZE_COMPLETED.name());
	}

	/**
	 * En son kullanilacak. Unutma !!!
	 * 
	 * @param orgBehaviorPreProcessUsers
	 */
	public void removePreProcessUsers() {
		DBObject requestIdObj = new BasicDBObject("requestId", requestId);
		DirenajMongoDriver.getInstance().getOrgBehaviorPreProcessUsers().remove(requestIdObj);
	}

	private void updateRequestInMongo() {
		Logger.getLogger(OrganizationDetector.class)
				.debug("updateRequestInMongo - do Upsert for requestId : " + requestId);
		DBCollection organizedBehaviorCollection = direnajMongoDriver.getOrgBehaviorRequestCollection();
		BasicDBObject findQuery = new BasicDBObject();
		findQuery.put("_id", requestId);
		BasicDBObject updateQuery = new BasicDBObject();
		updateQuery.append("$set", new BasicDBObject().append("tracedHashtag", tracedHashtagList));
		organizedBehaviorCollection.update(findQuery, updateQuery, true, false);
	}

	private void updateRequestInMongoByColumnName(String columnName, Object updateValue) {
		Logger.getLogger(OrganizationDetector.class)
				.debug("updateRequestInMongo4ResumeProcess - do Upsert for requestId : " + requestId);
		DBCollection organizedBehaviorCollection = direnajMongoDriver.getOrgBehaviorRequestCollection();
		BasicDBObject findQuery = new BasicDBObject();
		findQuery.put("_id", requestId);
		BasicDBObject updateQuery = new BasicDBObject();
		updateQuery.append("$set", new BasicDBObject().append(columnName, updateValue));
		organizedBehaviorCollection.update(findQuery, updateQuery, true, false);
	}

}
