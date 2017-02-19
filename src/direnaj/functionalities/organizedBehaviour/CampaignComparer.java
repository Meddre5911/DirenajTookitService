package direnaj.functionalities.organizedBehaviour;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.mongodb.AggregationOptions;
import com.mongodb.BasicDBObject;
import com.mongodb.Cursor;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import direnaj.domain.ComparisonData;
import direnaj.driver.DirenajMongoDriver;
import direnaj.driver.DirenajMongoDriverUtil;
import direnaj.driver.MongoCollectionFieldNames;
import direnaj.twitter.twitter4j.Twitter4jUtil;
import direnaj.util.DateTimeUtils;
import direnaj.util.NumberUtils;
import direnaj.util.TextUtils;
import twitter4j.User;

public class CampaignComparer implements Runnable {

	private String actualCampaignId;
	private String actualHashtag;
	private String requestDefinition;
	private List<Entry<String, String>> comparisonCampaignHashtagInfo;
	private String generalComparisonRequestId;

	public CampaignComparer(String actualCampaignId, String actualHashtag,
			List<Entry<String, String>> comparisonCampaignHashtagInfo, String requestDefinition) throws Exception {
		this.generalComparisonRequestId = TextUtils.generateUniqueId4Request();
		this.actualCampaignId = actualCampaignId;
		this.actualHashtag = actualHashtag;
		this.comparisonCampaignHashtagInfo = comparisonCampaignHashtagInfo;
		this.requestDefinition = requestDefinition;
	}

	private void doComparison() {
		try {
			Logger.getLogger(CampaignComparer.class)
					.debug("Starts Comparison for requestId : " + generalComparisonRequestId);
			Logger.getLogger(CampaignComparer.class)
					.debug("Actual Comparison for CampaignId & Hashtag : " + actualCampaignId + " & " + actualHashtag);
			Gson gsonObject4Deserialization = Twitter4jUtil.getGsonObject4Deserialization();
			List<ComparisonData> allComparisons = new ArrayList<>();
			DBCollection orgBehaviourProcessInputData = DirenajMongoDriver.getInstance()
					.getOrgBehaviourProcessInputData();

			double distinctUserCount4ActualCampaign = getDistinctUserCount4CampaignAndHashtag(actualCampaignId,
					actualHashtag);

			StringBuilder comparedEntities = new StringBuilder();
			for (Entry<String, String> entrySet : comparisonCampaignHashtagInfo) {
				String comparedCampaignId = entrySet.getKey();
				String comparedHashtag = entrySet.getValue();
				if (actualCampaignId.equalsIgnoreCase(comparedCampaignId)) {
					continue;
				}
				comparedEntities.append("& " + comparedCampaignId + "-" + comparedHashtag + " ");

				ComparisonData comparisonData = new ComparisonData();

				try {
					initUsers2ProcessInputData(gsonObject4Deserialization, orgBehaviourProcessInputData,
							comparedCampaignId, comparedHashtag, comparisonData, true);
					initUsers2ProcessInputData(gsonObject4Deserialization, orgBehaviourProcessInputData,
							comparedCampaignId, comparedHashtag, comparisonData, false);

					// get comparison data
					double distinctUserCount4ComparedCampaign = getDistinctUserCount4CampaignAndHashtag(
							comparedCampaignId, comparedHashtag);
					double sameUserPercentageActualCampaign = NumberUtils
							.roundDouble(comparisonData.getSameUserCount() * 100d / distinctUserCount4ActualCampaign);
					double sameUserPercentageComparedCampaign = NumberUtils
							.roundDouble(comparisonData.getSameUserCount() * 100d / distinctUserCount4ComparedCampaign);
					comparisonData.setCampaign_id(comparedCampaignId);
					comparisonData.setHashtag(comparedHashtag);
					comparisonData.setSameUserPercentage4ActualCampaign(sameUserPercentageActualCampaign);
					comparisonData.setTotalComparedUserCount4ActualCampaign(distinctUserCount4ActualCampaign);
					comparisonData.setSameUserPercentage4ComparedCampaign(sameUserPercentageComparedCampaign);
					comparisonData.setTotalComparedUserCount4ComparedCampaign(distinctUserCount4ComparedCampaign);
					comparisonData.setUncommonUsersCount(
							(distinctUserCount4ActualCampaign - comparisonData.getSameUserCount())
									+ (distinctUserCount4ComparedCampaign - comparisonData.getSameUserCount()));

					allComparisons.add(comparisonData);
					Logger.getLogger(CampaignComparer.class)
							.debug("Calculation Ended for Comparison RequestId : " + comparisonData.getRequestId()
									+ "Compared CampaignId & Hashtag : " + comparedCampaignId + " & "
									+ comparedHashtag);

				} catch (Exception e) {
					Logger.getLogger(CampaignComparer.class).error(
							"Mean Variance are getting calculated for requestId : " + generalComparisonRequestId, e);
				}
			}
			// prepare json
			Gson gson = new Gson();
			String allComparisonsJson = gson.toJson(allComparisons);
			@SuppressWarnings("unchecked")
			List<DBObject> mongoDbObject4Comparisons = (List<DBObject>) JSON.parse(allComparisonsJson);
			// insert result
			BasicDBObject comparisonRecord = new BasicDBObject();
			comparisonRecord.put(MongoCollectionFieldNames.MONGO_REQUEST_ID, generalComparisonRequestId);
			comparisonRecord.put(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, actualCampaignId);
			comparisonRecord.put(MongoCollectionFieldNames.MONGO_COMPARISON_ACTUAL_HASHTAG, actualHashtag);
			comparisonRecord.put(MongoCollectionFieldNames.MONGO_COMPARISON_REQUEST_DEFINITION, requestDefinition);
			comparisonRecord.put(MongoCollectionFieldNames.MONGO_COMPARISON_COMPARED_ENTITIES,
					comparedEntities.substring(2));
			comparisonRecord.put(MongoCollectionFieldNames.MONGO_COMPARISON_RESULTS, mongoDbObject4Comparisons);
			DirenajMongoDriver.getInstance().getOrgBehaviourCampaignComparisons().insert(comparisonRecord);

			Logger.getLogger(CampaignComparer.class)
					.debug("Comparison is Finished. Actual Comparison for CampaignId & Hashtag : " + actualCampaignId
							+ " & " + actualHashtag);
		} catch (Exception e) {
			Logger.getLogger(CampaignComparer.class)
					.error("Campaign Comparison Exception for " + actualCampaignId + " & " + actualHashtag, e);
		}
	}

	private void initUsers2ProcessInputData(Gson gsonObject4Deserialization, DBCollection orgBehaviourProcessInputData,
			String comparedCampaignId, String comparedHashtag, ComparisonData comparisonData,
			boolean search4CommonUsers) throws JSONException {

		double commonUserCount = 0d;
		String comparisonRequestId = TextUtils.generateUniqueId4Request();
		List<DBObject> allUsersInputData = new ArrayList<>(DirenajMongoDriver.getInstance().getBulkInsertSize());

		Cursor commonUsers = getUsers4ComparedCampaignsAndHashtags(comparedCampaignId, comparedHashtag,
				search4CommonUsers);
		try {
			Logger.getLogger(CampaignComparer.class).debug("Comparison RequestId : " + comparisonRequestId
					+ " - Compared CampaignId & Hashtag : " + comparedCampaignId + " & " + comparedHashtag
					+ " - Actual Comparison for CampaignId & Hashtag : " + actualCampaignId + " & " + actualHashtag);
			while (commonUsers.hasNext()) {
				commonUserCount++;
				DBObject next = commonUsers.next();
				Long userId = Long.valueOf(String.valueOf(next.get("_id")));

				DBObject userRetrievalQuery = new BasicDBObject();
				userRetrievalQuery.put("tweet.user.id", userId);
				userRetrievalQuery.put(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID,
						new BasicDBObject("$in", Arrays.asList(actualCampaignId, comparedCampaignId)));

				BasicDBObject projectionKey = new BasicDBObject();
				DBObject queryResult = DirenajMongoDriver.getInstance().getTweetsCollection()
						.findOne(userRetrievalQuery, projectionKey);

				JSONObject jsonObject = new JSONObject(queryResult.get("tweet").toString());
				User user = Twitter4jUtil.deserializeTwitter4jUserFromGson(gsonObject4Deserialization,
						jsonObject.get("user").toString());
				allUsersInputData.add(prepareUserInputData(user, comparisonRequestId));

				// insert preprocess input
				allUsersInputData = DirenajMongoDriverUtil
						.insertBulkData2CollectionIfNeeded(orgBehaviourProcessInputData, allUsersInputData, false);

			}
		} finally {
			commonUsers.close();
		}
		allUsersInputData = DirenajMongoDriverUtil.insertBulkData2CollectionIfNeeded(orgBehaviourProcessInputData,
				allUsersInputData, true);
		// calculate mean variance
		BasicDBObject requestIdObj = new BasicDBObject(MongoCollectionFieldNames.MONGO_REQUEST_ID, comparisonRequestId);
		StatisticCalculator statisticCalculator = new StatisticCalculator(comparisonRequestId, requestIdObj, null, null,
				null, false, null, null);
		statisticCalculator.calculateBasicUserMeanVariances(orgBehaviourProcessInputData);

		if (search4CommonUsers) {
			comparisonData.setRequestId(comparisonRequestId);
			comparisonData.setSameUserCount(commonUserCount);
		} else {
			comparisonData.setRequestId4UncommonUsers(comparisonRequestId);
		}
	}

	private double getDistinctUserCount4CampaignAndHashtag(String campaignId, String hashtag) {
		BasicDBObject userDistinctCountQuery4ActualCampaign = new BasicDBObject()
				.append(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, campaignId);
		if (!TextUtils.isEmpty(hashtag)) {
			userDistinctCountQuery4ActualCampaign.append("tweet.hashtagEntities.text",
					new BasicDBObject("$regex", "^" + hashtag + "$").append("$options", "i"));
		}
		double distinctUserCount = DirenajMongoDriver.getInstance().getTweetsCollection()
				.distinct("tweet.user.id", userDistinctCountQuery4ActualCampaign).size();
		return distinctUserCount;
	}

	private BasicDBObject prepareUserInputData(User user, String requestId) {
		Double friendFollowerRatio = new Double(0);
		double totalFriendFollowerCount = user.getFollowersCount() + user.getFriendsCount();
		if (totalFriendFollowerCount > 0) {
			friendFollowerRatio = NumberUtils.roundDouble((double) user.getFollowersCount() / totalFriendFollowerCount);
		}
		// get avarage day
		DateTime userCreationDate = new DateTime(user.getCreatedAt());
		DateTime now = new DateTime(DateTimeUtils.getLocalDate());
		double twitterDay = Days.daysBetween(userCreationDate, now).getDays();
		double dailyAvaregePostCount = NumberUtils.roundDouble(4, (double) user.getStatusesCount() / twitterDay);

		// first init user account properties
		BasicDBObject userInputData = new BasicDBObject();
		userInputData.put(MongoCollectionFieldNames.MONGO_REQUEST_ID, requestId);
		userInputData.put(MongoCollectionFieldNames.MONGO_USER_ID, String.valueOf(user.getId()));
		userInputData.put(MongoCollectionFieldNames.MONGO_USER_SCREEN_NAME, user.getScreenName());

		userInputData.put(MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT, (double) user.getFavouritesCount());
		userInputData.put(MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT, (double) user.getStatusesCount());

		userInputData.put(MongoCollectionFieldNames.MONGO_USER_FRIEND_FOLLOWER_RATIO, friendFollowerRatio);

		userInputData.put(MongoCollectionFieldNames.MONGO_USER_CREATION_DATE,
				DateTimeUtils.getUTCDateTimeStringInGenericFormat(user.getCreatedAt()));
		userInputData.put(MongoCollectionFieldNames.MONGO_USER_CREATION_DATE_IN_RATA_DIE,
				DateTimeUtils.getRataDieFormat4Date(user.getCreatedAt()));
		userInputData.put(MongoCollectionFieldNames.MONGO_USER_DAILY_AVARAGE_POST_COUNT, dailyAvaregePostCount);
		return userInputData;

	}

	private Cursor getUsers4ComparedCampaignsAndHashtags(String comparedCampaignId, String comparedHashtag,
			boolean search4CommonUsers) {

		BasicDBObject matchQuery4Initial = getInitialMatchQuery(comparedCampaignId, comparedHashtag);

		DBObject initialMatchElement = (DBObject) new BasicDBObject("$match", matchQuery4Initial);
		// prepare group element
		DBObject groupElement = (DBObject) new BasicDBObject("$group", new BasicDBObject("_id", "$tweet.user.id")
				.append("grouping", new BasicDBObject("$addToSet", "$campaign_id")));

		// prepare match element after grouping
		BasicDBObject matchQuery4Group = null;
		if (search4CommonUsers) {
			BasicDBObject query4FirstCampaign = new BasicDBObject("grouping", actualCampaignId);
			BasicDBObject query4SecondCampaign = new BasicDBObject("grouping", comparedCampaignId);
			matchQuery4Group = new BasicDBObject("$and", Arrays.asList(query4FirstCampaign, query4SecondCampaign));
		} else {
			matchQuery4Group = new BasicDBObject("grouping", new BasicDBObject("$size", 1));
		}
		DBObject matchElementAfterGroup = new BasicDBObject("$match", matchQuery4Group);

		// prepare project element
		DBObject projectionElement = new BasicDBObject("$project", new BasicDBObject("_id", 1));
		List<DBObject> wholeAggregationQuery = Arrays.asList(initialMatchElement, groupElement, matchElementAfterGroup,
				projectionElement);

		Logger.getLogger(CampaignComparer.class)
				.debug("Aggregation Query for Common Users : " + wholeAggregationQuery.toString());

		AggregationOptions aggregationOptions = AggregationOptions.builder().batchSize(50)
				.outputMode(AggregationOptions.OutputMode.CURSOR).build();

		Cursor commonUsers = DirenajMongoDriver.getInstance().getTweetsCollection().aggregate(wholeAggregationQuery,
				aggregationOptions);

		return commonUsers;
	}

	private BasicDBObject getInitialMatchQuery(String comparedCampaignId, String comparedHashtag) {
		BasicDBObject matchQuery4Initial = new BasicDBObject();

		if (!TextUtils.isEmpty(actualHashtag) && actualHashtag.equalsIgnoreCase(comparedHashtag)) {
			BasicDBObject hashtagQuery = new BasicDBObject().append("tweet.hashtagEntities.text",
					new BasicDBObject("$regex", "^" + actualHashtag + "$").append("$options", "i"));
			BasicDBObject campaignIdQuery = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID,
					new BasicDBObject("$in", Arrays.asList(actualCampaignId, comparedCampaignId)));
			matchQuery4Initial.append("$and", Arrays.asList(hashtagQuery, campaignIdQuery));
		} else {
			BasicDBObject initialMatch4ActualCampaign = prepareInitialMatchQuery(actualCampaignId, actualHashtag);
			BasicDBObject initialMatch4ComparedCampaign = prepareInitialMatchQuery(comparedCampaignId, comparedHashtag);
			matchQuery4Initial.append("$or", Arrays.asList(initialMatch4ActualCampaign, initialMatch4ComparedCampaign));
		}
		return matchQuery4Initial;
	}

	private BasicDBObject prepareInitialMatchQuery(String campaignId, String hashtag) {
		// prepare first match element
		BasicDBObject initialMatchQuery = new BasicDBObject();
		BasicDBObject hashtagQuery = null;
		List<BasicDBObject> andQuery = null;
		// first campaign id
		BasicDBObject campaignIdQuery = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, campaignId);
		if (!TextUtils.isEmpty(hashtag)) {
			// check for hashtag
			hashtagQuery = new BasicDBObject().append("tweet.hashtagEntities.text",
					new BasicDBObject("$regex", "^" + hashtag + "$").append("$options", "i"));
			andQuery = Arrays.asList(hashtagQuery, campaignIdQuery);
		} else {
			andQuery = Arrays.asList(campaignIdQuery);
		}
		initialMatchQuery.append("$and", andQuery);
		return initialMatchQuery;
	}

	@Override
	public void run() {
		doComparison();
	}

}
