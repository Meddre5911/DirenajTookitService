package direnaj.functionalities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import direnaj.adapter.DirenajInvalidJSONException;
import direnaj.domain.User;
import direnaj.domain.UserAccountProperties;
import direnaj.driver.DirenajDriverUtils;
import direnaj.driver.DirenajDriverVersion2;
import direnaj.driver.DirenajMongoDriver;
import direnaj.driver.DirenajMongoDriverUtil;
import direnaj.driver.DirenajNeo4jDriver;
import direnaj.servlet.OrganizedBehaviourDetectionRequestType;
import direnaj.twitter.UserAccountPropertyAnalyser;
import direnaj.util.CollectionUtil;
import direnaj.util.DateTimeUtils;
import direnaj.util.ListUtils;
import direnaj.util.PropertiesUtil;
import direnaj.util.TextUtils;

public class OrganizationDetector implements Runnable {

	private String campaignId;
	private OrganizedBehaviourDetectionRequestType detectionRequestType;
	private DirenajDriverVersion2 direnajDriver;
	private DirenajMongoDriver direnajMongoDriver;
	private boolean disableGraphAnalysis;
	private String requestDefinition;
	private String requestId;
	private Integer topHashtagCount;
	private List<String> tracedHashtagList;
	private String tracedSingleHashtag;

	public OrganizationDetector(String campaignId, int topHashtagCount, String requestDefinition, String tracedHashtag,
			OrganizedBehaviourDetectionRequestType detectionRequestType, boolean disableGraphAnalysis) {
		direnajDriver = new DirenajDriverVersion2();
		direnajMongoDriver = DirenajMongoDriver.getInstance();
		requestId = generateUniqueId4Request();
		this.campaignId = campaignId;
		this.topHashtagCount = topHashtagCount;
		this.requestDefinition = requestDefinition;
		this.tracedHashtagList = new Vector<>();
		if (!TextUtils.isEmpty(tracedHashtag)) {
			this.tracedHashtagList.add(tracedHashtag);
		}
		this.detectionRequestType = detectionRequestType;
		this.disableGraphAnalysis = disableGraphAnalysis;
		insertRequest2Mongo();
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
		// + "CREATE (n)-[:CalculateCentrality]->(x)-[r1:" + newRelationName +
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
			Map<String, Double> hashtagCounts = direnajDriver.getHashtagCounts(campaignId);
			// get hashtag users
			LinkedHashMap<String, Double> topHashtagCounts = CollectionUtil.discardOtherElementsOfMap(hashtagCounts,
					topHashtagCount);
			Logger.getLogger(OrganizationDetector.class).debug("Top Hashtags Descending");
			for (Entry<String, Double> hashtag : topHashtagCounts.entrySet()) {
				Logger.getLogger(OrganizationDetector.class).debug(hashtag.getKey() + " - " + hashtag.getValue());
			}
			Set<String> topHashtags = topHashtagCounts.keySet();
			for (String topHashTag : topHashtags) {
				tracedHashtagList.add(topHashTag);
			}
			// update found hashtags
			updateRequestInMongo();
			getMetricsOfUsersOfHashTag();
		} catch (Exception e) {
			Logger.getLogger(OrganizationDetector.class).error("Error in detectOrganizedBehaviourInHashtags", e);
		}

	}

	public void getMetricsOfUsersOfHashTag() throws DirenajInvalidJSONException, Exception {
		for (String tracedHashtag : tracedHashtagList) {
			tracedSingleHashtag = tracedHashtag;
			direnajDriver.saveHashtagUsers2Mongo(campaignId, tracedHashtag, requestId);
			// FIXME 20150604
			saveAllUserTweets();
			startUserAnalysis();

		}
		calculateTweetSimilarities();
		changeRequestStatusInMongo(true);
		removePreProcessUsers();
	}

	private void calculateTweetSimilarities() {

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

	private User analyzePreProcessUser(DBObject preProcessUser) throws Exception {
		// get collection
		DBCollection tweetsCollection = direnajMongoDriver.getTweetsCollection();
		// parse user
		User domainUser = DirenajMongoDriverUtil.parsePreProcessUsers(preProcessUser);
		// get tweets of users in an interval of two weeks
		BasicDBObject tweetsRetrievalQuery = new BasicDBObject("tweet.user.id_str", domainUser.getUserId()).append(
				"tweet.created_at",
				new BasicDBObject("$gt", DateTimeUtils.subtractWeeksFromDate(domainUser.getCampaignTweetPostDate(), 2))
						.append("$lt", DateTimeUtils.addWeeksToDate(domainUser.getCampaignTweetPostDate(), 2)));

		DBCursor tweetsOfUser = tweetsCollection.find(tweetsRetrievalQuery);
		try {
			while (tweetsOfUser.hasNext()) {
				JSONObject tweetData = new JSONObject(tweetsOfUser.next().toString());
				JSONObject tweet = DirenajDriverUtils.getTweet(tweetData);
				String tweetId = DirenajDriverUtils.getTweetId(tweet);
				JSONObject entities = DirenajDriverUtils.getEntities(tweet);
				String tweetPostSource = DirenajDriverUtils.getSource(tweet);
				String tweetText = DirenajDriverUtils.getSingleTweetText(tweetData);
				int usedHashtagCount = DirenajDriverUtils.getHashTags(entities).length();
				List<String> urlStrings = DirenajDriverUtils.getUrlStrings(entities);
				int mentionedUserCount = DirenajDriverUtils.getUserMentions(entities).length();
				// get user
				domainUser.incrementPostCount();
				// spam link olayina girersek, url string'leri kullanacagiz
				// domainUser.addUrlsToUser(urlStrings);
				domainUser.addValue2CountOfUsedUrls(urlStrings.size());
				domainUser.addValue2CountOfHashtags((double) usedHashtagCount);
				domainUser.addValue2CountOfMentionedUsers((double) mentionedUserCount);
				domainUser.incrementPostDeviceCount(tweetPostSource);
				domainUser.getAllTweetIds().add(tweetId);
				if (tweetText.contains(tracedSingleHashtag)) {
					domainUser.getHashtagTweetIds().add(tweetId);
				}
			}
		} catch (JSONException e) {
			Logger.getLogger(OrganizationDetector.class.getSimpleName()).error("analyzePreProcessUser method error", e);
			tweetsOfUser.close();
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

	private void calculateClosenessCentrality(List<String> userIds) {
		String subgraphEdgeLabel = createSubgraphByAddingEdges(userIds);
		HashMap<String, Double> userClosenessCentralities = calculateInNeo4J(userIds, subgraphEdgeLabel);
		bulkUpdateMongo4ClosenessCentrality(userClosenessCentralities);
		// clearNeo4jSubGraph(subgraphEdgeLabel);
	}

	private void changeRequestStatusInMongo(boolean requestStatus) {
		DBCollection organizedBehaviorCollection = direnajMongoDriver.getOrgBehaviorRequestCollection();
		BasicDBObject findQuery = new BasicDBObject();
		findQuery.put("_id", requestId);
		BasicDBObject updateQuery = new BasicDBObject();
		updateQuery.append("$set", new BasicDBObject().append("processCompleted", requestStatus)
				.append("statusChangeTime", DateTimeUtils.getLocalDate()));
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

	private String generateUniqueId4Request() {
		// get current time
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmssSS");
		Date now = DateTimeUtils.getLocalDate();
		String strDate = sdfDate.format(now);
		return strDate;
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
		document.put("campaignId", campaignId);
		document.put("topHashtagCount", topHashtagCount);
		document.put("tracedHashtag", tracedHashtagList);
		document.put("processCompleted", Boolean.FALSE);
		document.put("similartyCalculationCompleted", Boolean.FALSE);
		document.put("statusChangeTime", DateTimeUtils.getLocalDate());
		organizedBehaviorCollection.insert(document);
	}

	private void saveAllUserTweets() {

	}

	private List<User> saveOrganizedBehaviourInputData(List<User> domainUsers) {
		List<DBObject> allUserInputData = new Vector<>();
		List<DBObject> userTweetIdsData = new Vector<>();
		for (User user : domainUsers) {
			// first init user account properties
			UserAccountProperties accountProperties = user.getAccountProperties();
			BasicDBObject userInputData = new BasicDBObject();
			userInputData.put("requestId", requestId);
			userInputData.put("userId", user.getUserId());
			userInputData.put("userScreenName", user.getUserScreenName());
			userInputData.put("closenessCentrality", new Double(0));

			userInputData.put("friendFollowerRatio", accountProperties.getFriendFollowerRatio());
			userInputData.put("urlRatio", accountProperties.getUrlRatio());
			userInputData.put("hashtagRatio", accountProperties.getHashtagRatio());
			userInputData.put("mentionRatio", accountProperties.getMentionRatio());
			userInputData.put("postTwitterDeviceRatio", accountProperties.getTwitterPostRatio());
			userInputData.put("postMobileDeviceRatio", accountProperties.getMobilePostRatio());
			userInputData.put("postThirdPartyDeviceRatio", accountProperties.getThirdPartyPostRatio());

			userInputData.put("isProtected", user.isProtected());
			userInputData.put("isVerified", user.isVerified());
			userInputData.put("creationDate", user.getCreationDate().toString());
			allUserInputData.add(userInputData);
			// init tweet ids for user
			BasicDBObject userTweetIdData = new BasicDBObject();
			userTweetIdData.put("requestId", requestId);
			userTweetIdData.put("userId", user.getUserId());
			userTweetIdData.put("tweetType", "ALL");
			userTweetIdData.put("allTweetIds",user.getAllTweetIds());
			userTweetIdsData.add(userTweetIdData);
			BasicDBObject userHashtagTweetIdData = new BasicDBObject();
			userHashtagTweetIdData.put("requestId", requestId);
			userHashtagTweetIdData.put("userId", user.getUserId());
			userHashtagTweetIdData.put("tweetType", "HASHTAG");
			userHashtagTweetIdData.put("allTweetIds",user.getHashtagTweetIds());
			userTweetIdsData.add(userHashtagTweetIdData);
		}
		direnajMongoDriver.getOrgBehaviourProcessInputData().insert(allUserInputData);
		direnajMongoDriver.getOrgBehaviourUserTweetIds().insert(userTweetIdsData);
		return new Vector<User>();
	}

	private void startUserAnalysis() throws Exception {
		List<User> domainUsers = new Vector<User>();
		DBObject requestIdObj = new BasicDBObject("requestId", requestId);
		// get total user count for detection
		DBCollection orgBehaviorPreProcessUsers = direnajMongoDriver.getOrgBehaviorPreProcessUsers();
		Long preprocessUserCounts = direnajMongoDriver.executeCountQuery(orgBehaviorPreProcessUsers, requestIdObj);
		List<String> userIds = new Vector<>();
		DBCursor preProcessUsers = orgBehaviorPreProcessUsers.find(requestIdObj);
		try {
			int i = 0;
			while (preProcessUsers.hasNext()) {
				i++;
				DBObject preProcessUser = preProcessUsers.next();
				User domainUser = analyzePreProcessUser(preProcessUser);
				// do hashtag / mention / url & twitter device ratio
				UserAccountPropertyAnalyser.getInstance().calculateUserAccountProperties(domainUser);
				domainUsers.add(domainUser);
				userIds.add(domainUser.getUserId());
				if ((i == preprocessUserCounts)
						|| domainUsers.size() > DirenajMongoDriver.getInstance().getBulkInsertSize()) {
					domainUsers = saveOrganizedBehaviourInputData(domainUsers);
				}
			}
		} finally {
			preProcessUsers.close();
		}
		if (!disableGraphAnalysis) {
			calculateClosenessCentrality(userIds);
		}
	}

	/**
	 * En son kullanilacak. Unutma !!!
	 * 
	 * @param orgBehaviorPreProcessUsers
	 */
	private void removePreProcessUsers() {
		DBObject requestIdObj = new BasicDBObject("requestId", requestId);
		DirenajMongoDriver.getInstance().getOrgBehaviorPreProcessUsers().remove(requestIdObj);
	}

	private void updateRequestInMongo() {
		DBCollection organizedBehaviorCollection = direnajMongoDriver.getOrgBehaviorRequestCollection();
		BasicDBObject findQuery = new BasicDBObject();
		findQuery.put("_id", requestId);
		BasicDBObject updateQuery = new BasicDBObject();
		updateQuery.append("$set", new BasicDBObject().append("tracedHashtag", tracedHashtagList));
		organizedBehaviorCollection.update(findQuery, updateQuery);
	}

}
