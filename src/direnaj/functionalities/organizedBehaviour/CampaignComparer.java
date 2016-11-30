package direnaj.functionalities.organizedBehaviour;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Days;
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

	@SuppressWarnings("unused")
	private static final String sameHashtagComparisonType = "sameHashtag";
	private static final String givenHashtagComparisonType = "givenHashtag";

	private String actualCampaignId;
	private String actualHashtag;
	private String requestDefinition;
	private Map<String, String> comparisonCampaignHashtagInfo;
	private String comparisonType;
	private String generalComparisonRequestId;

	public CampaignComparer(String actualCampaignId, String actualHashtag,
			Map<String, String> comparisonCampaignHashtagInfo, String comparisonType, String requestDefinition)
					throws Exception {
		this.generalComparisonRequestId = TextUtils.generateUniqueId4Request();
		this.actualCampaignId = actualCampaignId;
		this.actualHashtag = actualHashtag;
		this.comparisonCampaignHashtagInfo = comparisonCampaignHashtagInfo;
		this.comparisonType = comparisonType;
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
			// get campaign distinct user count
			DBObject campaignStatisticQuery = new BasicDBObject();
			campaignStatisticQuery.put(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, actualCampaignId);

			BasicDBObject userDisnctCountQuery = new BasicDBObject()
					.append("tweet.hashtagEntities.text",
							new BasicDBObject("$regex", actualHashtag).append("$options", "i"))
					.append(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, actualCampaignId);

			double distinctUserCount = DirenajMongoDriver.getInstance().getTweetsCollection()
					.distinct("tweet.user.id", userDisnctCountQuery).size();

			StringBuilder comparedEntities = new StringBuilder();
			for (Entry<String, String> entrySet : comparisonCampaignHashtagInfo.entrySet()) {
				String comparedCampaignId = entrySet.getKey();
				String comparedHashtag = actualHashtag;
				if (givenHashtagComparisonType.equals(comparisonType)) {
					comparedHashtag = entrySet.getValue();
				}
				String comparisonRequestId = TextUtils.generateUniqueId4Request();
				comparedEntities.append("& " + comparedCampaignId + "-" + comparedHashtag + " ");

				Logger.getLogger(CampaignComparer.class)
						.debug("Comparison RequestId : " + comparisonRequestId + " - Compared CampaignId & Hashtag : "
								+ comparedCampaignId + " & " + comparedHashtag
								+ " - Actual Comparison for CampaignId & Hashtag : " + actualCampaignId + " & "
								+ actualHashtag);

				List<DBObject> allUsersInputData = new ArrayList<>(
						DirenajMongoDriver.getInstance().getBulkInsertSize());
				Cursor commonUsers = getCommonUsersWithSameHashtagInDifferentCampaigns(comparedCampaignId,
						comparedHashtag);
				try {
					double commonUserCount = 0d;
					while (commonUsers.hasNext()) {
						commonUserCount++;
						DBObject next = commonUsers.next();
						Long userId = Long.valueOf(String.valueOf(next.get("_id")));

						DBObject userRetrievalQuery = new BasicDBObject();
						userRetrievalQuery.put("tweet.user.id", userId);
						userRetrievalQuery.put(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, actualCampaignId);

						BasicDBObject projectionKey = new BasicDBObject();
						DBObject queryResult = DirenajMongoDriver.getInstance().getTweetsCollection()
								.findOne(userRetrievalQuery, projectionKey);

						JSONObject jsonObject = new JSONObject(queryResult.get("tweet").toString());
						User user = Twitter4jUtil.deserializeTwitter4jUserFromGson(gsonObject4Deserialization,
								jsonObject.get("user").toString());
						allUsersInputData.add(prepareUserInputData(user, comparisonRequestId));

						// insert preprocess input
						allUsersInputData = DirenajMongoDriverUtil.insertBulkData2CollectionIfNeeded(
								orgBehaviourProcessInputData, allUsersInputData, false);

					}
					allUsersInputData = DirenajMongoDriverUtil
							.insertBulkData2CollectionIfNeeded(orgBehaviourProcessInputData, allUsersInputData, true);
					// calculate mean variance
					BasicDBObject requestIdObj = new BasicDBObject(MongoCollectionFieldNames.MONGO_REQUEST_ID,
							comparisonRequestId);
					StatisticCalculator statisticCalculator = new StatisticCalculator(comparisonRequestId, requestIdObj,
							null, null, null, false);
					statisticCalculator.calculateBasicUserMeanVariances(orgBehaviourProcessInputData);
					// get comparison data
					double sameUserPercentage = NumberUtils.roundDouble(commonUserCount * 100d / distinctUserCount);
					ComparisonData comparisonData = new ComparisonData(comparedCampaignId, comparedHashtag,
							sameUserPercentage, comparisonRequestId, commonUserCount, distinctUserCount);
					allComparisons.add(comparisonData);
					Logger.getLogger(CampaignComparer.class)
							.debug("Calculation Ended for Comparison RequestId : " + comparisonRequestId
									+ "Compared CampaignId & Hashtag : " + comparedCampaignId + " & "
									+ comparedHashtag);

				} catch (Exception e) {
					Logger.getLogger(CampaignComparer.class).error(
							"Mean Variance are getting calculated for requestId : " + generalComparisonRequestId, e);
				} finally {
					commonUsers.close();
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

	private Cursor getCommonUsersWithSameHashtagInDifferentCampaigns(String comparedCampaignId,
			String comparedHashtag) {

		BasicDBObject matchQuery4Initial = getInitialMatchQuery(comparedCampaignId, comparedHashtag);

		DBObject initialMatchElement = (DBObject) new BasicDBObject("$match", matchQuery4Initial);
		// prepare group element
		DBObject groupElement = (DBObject) new BasicDBObject("$group", new BasicDBObject("_id", "$tweet.user.id")
				.append("grouping", new BasicDBObject("$addToSet", "$campaign_id")));
		// prepare match element after grouping
		BasicDBObject query4FirstCampaign = new BasicDBObject("grouping", actualCampaignId);
		BasicDBObject query4SecondCampaign = new BasicDBObject("grouping", comparedCampaignId);
		BasicDBObject matchQuery4Group = new BasicDBObject("$and",
				Arrays.asList(query4FirstCampaign, query4SecondCampaign));
		DBObject matchElementAfterGroup = new BasicDBObject("$match", matchQuery4Group);
		// prepare project element
		DBObject projectionElement = new BasicDBObject("$project", new BasicDBObject("_id", 1));
		List<DBObject> wholeAggregationQuery = Arrays.asList(initialMatchElement, groupElement, matchElementAfterGroup,
				projectionElement);

		AggregationOptions aggregationOptions = AggregationOptions.builder().batchSize(50)
				.outputMode(AggregationOptions.OutputMode.CURSOR).build();

		Cursor commonUsers = DirenajMongoDriver.getInstance().getTweetsCollection().aggregate(wholeAggregationQuery,
				aggregationOptions);

		return commonUsers;
	}

	private BasicDBObject getInitialMatchQuery(String comparedCampaignId, String comparedHashtag) {
		BasicDBObject matchQuery4Initial = new BasicDBObject();

		if (actualHashtag.equalsIgnoreCase(comparedHashtag)) {
			BasicDBObject hashtagQuery = new BasicDBObject().append("tweet.hashtagEntities.text",
					new BasicDBObject("$regex", actualHashtag).append("$options", "i"));
			BasicDBObject campaignIdQuery = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID,
					new BasicDBObject("$in", Arrays.asList(actualCampaignId, comparedCampaignId)));
			matchQuery4Initial.append("$and", Arrays.asList(hashtagQuery, campaignIdQuery));
		} else {
			// prepare first match element
			BasicDBObject hashtagQuery = new BasicDBObject().append("tweet.hashtagEntities.text",
					new BasicDBObject("$regex", actualHashtag).append("$options", "i"));
			BasicDBObject campaignIdQuery = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID,
					actualCampaignId);
			matchQuery4Initial.append("$and", Arrays.asList(hashtagQuery, campaignIdQuery));

			BasicDBObject comparedHashtagQuery = new BasicDBObject().append("tweet.hashtagEntities.text",
					new BasicDBObject("$regex", comparedHashtag).append("$options", "i"));
			BasicDBObject comparedCampaignIdQuery = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID,
					comparedCampaignId);
			BasicDBObject comparedMatchQuery4Initial = new BasicDBObject();
			comparedMatchQuery4Initial.append("$and", Arrays.asList(comparedHashtagQuery, comparedCampaignIdQuery));

			matchQuery4Initial.append("$or", Arrays.asList(matchQuery4Initial, comparedMatchQuery4Initial));
		}
		return matchQuery4Initial;
	}

	@Override
	public void run() {
		doComparison();
	}

}
