package direnaj.functionalities.organizedBehaviour;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;

import direnaj.driver.DirenajMongoDriver;
import direnaj.driver.DirenajMongoDriverUtil;
import direnaj.driver.MongoCollectionFieldNames;
import direnaj.twitter.twitter4j.Twitter4jUtil;
import direnaj.util.DateTimeUtils;
import direnaj.util.NumberUtils;
import direnaj.util.TextUtils;
import twitter4j.Status;

public class StatisticCalculator {
	private String requestId;
	private DBObject requestIdObj;
	private DBObject query4CosSimilarityRequest;
	private String tracedHashtag;
	private String campaignId;
	private boolean bypassSimilarityCalculation;

	public StatisticCalculator(String requestId, DBObject requestIdObj, BasicDBObject query4CosSimilarityRequest,
			String tracedHashtag, String campaignId, boolean bypassSimilarityCalculation) {
		this.requestId = requestId;
		this.requestIdObj = requestIdObj;
		this.query4CosSimilarityRequest = query4CosSimilarityRequest;
		this.tracedHashtag = tracedHashtag;
		this.campaignId = campaignId;
		this.bypassSimilarityCalculation = bypassSimilarityCalculation;
	}

	public void calculateStatistics() throws Exception {
		Logger.getLogger(StatisticCalculator.class).debug("Statistics will be calculated for requestId : " + requestId);
		calculateHourlyEntityRatio();
		Logger.getLogger(StatisticCalculator.class)
				.debug("Hourly Entitiy ratios are calculated for requestId : " + requestId);

		calculateGeneralStatistics();
		calculateCampaignStatistics();
		calculateMeanVariance4All();
		Logger.getLogger(StatisticCalculator.class).debug("Mean Variences are calculated for requestId : " + requestId);

	}

	private void calculateGeneralStatistics() throws Exception {
		Logger.getLogger(StatisticCalculator.class)
				.debug("General Statistics are getting calculated for requestId : " + requestId);

		DBObject projectionKeys = new BasicDBObject();
		projectionKeys.put(MongoCollectionFieldNames.MONGO_USER_ID, 1);
		projectionKeys.put(MongoCollectionFieldNames.MONGO_USER_DAILY_AVARAGE_POST_COUNT, 1);
		projectionKeys.put("_id", 0);
		DBCollection orgBehaviourProcessInputData = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData();
		DBCursor requestUserIds = orgBehaviourProcessInputData.find(requestIdObj, projectionKeys);
		try {
			while (requestUserIds.hasNext()) {
				DBObject requestUser = requestUserIds.next();
				String userId = (String) requestUser.get(MongoCollectionFieldNames.MONGO_USER_ID);
				double userDailyPostCount = (double) requestUser
						.get(MongoCollectionFieldNames.MONGO_USER_DAILY_AVARAGE_POST_COUNT);

				BasicDBObject tweetQuery = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, campaignId)
						.append(MongoCollectionFieldNames.MONGO_TWEET_HASHTAG_ENTITIES_TEXT,
								new BasicDBObject("$regex", tracedHashtag).append("$options", "i"))
						.append("user.id", Long.valueOf(userId));

				DBObject keys = new BasicDBObject("createdAt", 1);
				keys.put("_id", 0);

				Set<DateTime> userDateLimits = new HashSet<>();

				DBCursor userTweetDates = DirenajMongoDriver.getInstance().getOrgBehaviourUserTweets().find(tweetQuery,
						keys);
				while (userTweetDates.hasNext()) {
					double creationTimeInRataDie = (double) userTweetDates.next()
							.get(MongoCollectionFieldNames.MONGO_TWEET_CREATED_AT);
					DateTime date = new DateTime(
							DateTimeUtils.getTwitterDateFromRataDieFormat(String.valueOf(creationTimeInRataDie)));
					userDateLimits.add(date.withTimeAtStartOfDay());
				}
				double dayCountOfHashtagUsage = 0d;
				double tweetCount = 0d;
				for (DateTime userLimitTime : userDateLimits) {
					dayCountOfHashtagUsage++;
					DateTime endOfDay = userLimitTime.withTime(23, 59, 59, 999);

					BasicDBObject tweetsRetrievalQuery = new BasicDBObject("user.id", Long.valueOf(userId))
							.append("createdAt",
									new BasicDBObject("$gt",
											DateTimeUtils.getRataDieFormat4Date(userLimitTime.toDate())).append("$lt",
													DateTimeUtils.getRataDieFormat4Date(endOfDay.toDate())))
							.append(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, campaignId);

					tweetCount += DirenajMongoDriver.getInstance().getOrgBehaviourUserTweets()
							.distinct("id", tweetsRetrievalQuery).size();

				}
				double dailyAvarageTweetCount4HashtagDays = NumberUtils.roundDouble(2,
						tweetCount / dayCountOfHashtagUsage);
				double hashtagDailyCountAvarageDailyCountRatio = NumberUtils.roundDouble(3,
						dailyAvarageTweetCount4HashtagDays / userDailyPostCount);
				// o date'leri baz alarak tweet istatistiğini hesapla
				BasicDBObject updateQuery = new BasicDBObject(MongoCollectionFieldNames.MONGO_REQUEST_ID, requestId)
						.append(MongoCollectionFieldNames.MONGO_USER_ID, userId);
				DirenajMongoDriverUtil.updateRequestInMongoByColumnName(orgBehaviourProcessInputData, updateQuery,
						MongoCollectionFieldNames.MONGO_USER_TWEET_AVERAGE_HASHTAG_DAYS,
						dailyAvarageTweetCount4HashtagDays, "$set");
				DirenajMongoDriverUtil.updateRequestInMongoByColumnName(orgBehaviourProcessInputData, updateQuery,
						MongoCollectionFieldNames.MONGO_USER_HASHTAG_DAY_AVARAGE_DAY_POST_COUNT_RATIO,
						hashtagDailyCountAvarageDailyCountRatio, "$set");
			}
		} finally {
			requestUserIds.close();
		}

	}

	private void calculateCampaignStatistics() {
		Logger.getLogger(StatisticCalculator.class)
				.debug("Campaign Statistics are getting calculated for requestId : " + requestId);
		BasicDBObject findQuery = new BasicDBObject();
		findQuery.put("_id", requestId);
		DBObject requestObj = DirenajMongoDriver.getInstance().getOrgBehaviorRequestCollection().findOne(findQuery);
		String campaignId = (String) requestObj.get(MongoCollectionFieldNames.MONGO_REQUEST_CAMPAIGN_ID);
		// check for toolkit campaign statistics
		BasicDBObject campaignQuery = new BasicDBObject();
		campaignQuery.put(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, campaignId);
		DBCollection campaignStatisticsCollection = DirenajMongoDriver.getInstance().getCampaignStatisticsCollection();
		DBObject campaignStatisticObj = campaignStatisticsCollection.findOne(campaignQuery);
		check4RecalculationStep(campaignStatisticObj);

		double distinctUserCount = Double.valueOf(
				String.valueOf(campaignStatisticObj.get(MongoCollectionFieldNames.MONGO_CAMPAIGN_DISTINCT_USER_COUNT)));

		DBObject distinctRetweetUserQuery = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, campaignId)
				.append(MongoCollectionFieldNames.MONGO_TWEET_HASHTAG_ENTITIES_TEXT,
						new BasicDBObject("$regex", tracedHashtag).append("$options", "i"))
				.append("retweetedStatus.id", new BasicDBObject("$exists", true));

		double distinctRetweetUserCount = DirenajMongoDriver.getInstance().getOrgBehaviourUserTweets()
				.distinct("user.id", distinctRetweetUserQuery).size();

		double distintRetweetUserPercentage = NumberUtils.roundDouble(4,
				distinctRetweetUserCount * 100d / distinctUserCount);

		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(campaignStatisticsCollection, campaignQuery,
				MongoCollectionFieldNames.MONGO_DISTINCT_RETWEET_USER_COUNT, distinctRetweetUserCount, "$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(campaignStatisticsCollection, campaignQuery,
				MongoCollectionFieldNames.MONGO_DISTINCT_RETWEET_USER_COUNT_PERCENTAGE, distintRetweetUserPercentage,
				"$set");

	}

	private void check4RecalculationStep(DBObject campaignObj) {
		if (!campaignObj.containsField(MongoCollectionFieldNames.MONGO_CAMPAIGN_HASHTAG_STANDARD_DEVIATION)) {
			Logger.getLogger(StatisticCalculator.class)
					.debug("All Campaign Features are getting calculated from scratch for requestId : " + requestId);
			calculateAllCampaignFeaturesFromScratch();
			Logger.getLogger(StatisticCalculator.class)
					.debug("All Campaign Features are recalculted for requestId : " + requestId);
		}
	}

	private void calculateAllCampaignFeaturesFromScratch() {

		BasicDBObject query = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_TYPE, "Search Api");
		DBCursor allCampaigns = DirenajMongoDriver.getInstance().getCampaignsCollection().find(query);
		try {
			DBCollection campaignStatisticsCollection = DirenajMongoDriver.getInstance()
					.getCampaignStatisticsCollection();
			while (allCampaigns.hasNext()) {
				DBObject campaigObj = allCampaigns.next();
				String campaignId = (String) campaigObj.get(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID);
				try {
					Logger.getLogger(StatisticCalculator.class).debug("Recalculated Campaign Id is " + campaignId);
					DBObject campaignQueryObj = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID,
							campaignId);
					DBObject campaignStatistic = campaignStatisticsCollection.findOne(campaignQueryObj);
					@SuppressWarnings("unused")
					double totalTweetCount = (double) campaignStatistic
							.get(MongoCollectionFieldNames.MONGO_CAMPAIGN_TOTAL_TWEET_COUNT);

					reAssignCampaignStatistic(campaignStatisticsCollection, campaignQueryObj, campaignStatistic,
							MongoCollectionFieldNames.MONGO_CAMPAIGN_RETWEET_COUNT,
							MongoCollectionFieldNames.MONGO_CAMPAIGN_RETWEET_COUNT_PERCENTAGE, 1);

					reAssignCampaignStatistic(campaignStatisticsCollection, campaignQueryObj, campaignStatistic,
							MongoCollectionFieldNames.MONGO_CAMPAIGN_REPLY_TWEET_COUNT,
							MongoCollectionFieldNames.MONGO_CAMPAIGN_REPLY_TWEET_COUNT_PERCENTAGE, 1);

					reAssignCampaignStatistic(campaignStatisticsCollection, campaignQueryObj, campaignStatistic,
							MongoCollectionFieldNames.MONGO_CAMPAIGN_MENTION_TWEET_COUNT,
							MongoCollectionFieldNames.MONGO_CAMPAIGN_MENTION_TWEET_COUNT_PERCENTAGE, 1);

					reAssignCampaignStatistic(campaignStatisticsCollection, campaignQueryObj, campaignStatistic,
							MongoCollectionFieldNames.MONGO_CAMPAIGN_DISTINCT_USER_COUNT,
							MongoCollectionFieldNames.MONGO_CAMPAIGN_TWEET_COUNT_PER_USER, 0);

					SummaryStatistics summaryStatistics = new SummaryStatistics();
					// get hashtag counts
					@SuppressWarnings("unchecked")
					Map<String, Double> hashTagCounts = (Map<String, Double>) campaignStatistic
							.get(MongoCollectionFieldNames.MONGO_CAMPAIGN_HASHTAG_COUNTS);
					if (!campaignStatistic.containsField(
							MongoCollectionFieldNames.MONGO_CAMPAIGN_TOTAL_DISTINCT_WORD_COUNT_PERCENTAGE)) {
						// get word count info
						double totalWordCount = (double) campaignStatistic
								.get(MongoCollectionFieldNames.MONGO_CAMPAIGN_TOTAL_WORD_COUNT);
						double totalDistinctWordCount = Double.valueOf(((String) campaignStatistic
								.get(MongoCollectionFieldNames.MONGO_CAMPAIGN_TOTAL_DISTINCT_WORD_COUNT))
										.split("-")[0]);
						double distinctWordCountPercentage = NumberUtils
								.roundDouble((totalDistinctWordCount * 100d / totalWordCount));
						// do updates
						DirenajMongoDriverUtil.updateRequestInMongoByColumnName(campaignStatisticsCollection,
								campaignQueryObj, MongoCollectionFieldNames.MONGO_CAMPAIGN_TOTAL_DISTINCT_WORD_COUNT,
								totalDistinctWordCount, "$set");
						DirenajMongoDriverUtil.updateRequestInMongoByColumnName(campaignStatisticsCollection,
								campaignQueryObj,
								MongoCollectionFieldNames.MONGO_CAMPAIGN_TOTAL_DISTINCT_WORD_COUNT_PERCENTAGE,
								distinctWordCountPercentage, "$set");
						double totalHashtagUsageCount = 0;
						for (Entry<String, Double> entry : hashTagCounts.entrySet()) {
							totalHashtagUsageCount += entry.getValue();
						}
						for (Entry<String, Double> entry : hashTagCounts.entrySet()) {
							double hashtagPercentage = NumberUtils.roundDouble(4,
									(entry.getValue() * 100d) / totalHashtagUsageCount);
							summaryStatistics.addValue(hashtagPercentage);
							entry.setValue(hashtagPercentage);
						}
						DirenajMongoDriverUtil.updateRequestInMongoByColumnName(campaignStatisticsCollection,
								campaignQueryObj, MongoCollectionFieldNames.MONGO_CAMPAIGN_HASHTAG_COUNTS,
								hashTagCounts, "$set");
					} else {
						for (Entry<String, Double> entry : hashTagCounts.entrySet()) {
							summaryStatistics.addValue(entry.getValue());
						}
					}
					// save variance
					double hashtagPercentageVariance = NumberUtils.roundDouble(4, summaryStatistics.getVariance());
					double hashtagStandardDeviation = NumberUtils.roundDouble(4,
							summaryStatistics.getStandardDeviation());

					DirenajMongoDriverUtil.updateRequestInMongoByColumnName(campaignStatisticsCollection,
							campaignQueryObj, MongoCollectionFieldNames.MONGO_CAMPAIGN_HASHTAG_VARIANCE,
							hashtagPercentageVariance, "$set");
					DirenajMongoDriverUtil.updateRequestInMongoByColumnName(campaignStatisticsCollection,
							campaignQueryObj, MongoCollectionFieldNames.MONGO_CAMPAIGN_HASHTAG_STANDARD_DEVIATION,
							hashtagStandardDeviation, "$set");
				} catch (Exception e) {
					Logger.getLogger(StatisticCalculator.class)
							.error("Error is taken during recalculation of campaign id " + campaignId, e);
				}
			}
		} finally {
			allCampaigns.close();
		}

	}

	private void reAssignCampaignStatistic(DBCollection campaignStatisticsCollection, DBObject campaignQueryObj,
			DBObject campaignStatistic, String existedColumn, String newColumn, int substringIndex) {
		try {
			// get retweet info
			String oldInfo = (String) campaignStatistic.get(existedColumn);
			if (!TextUtils.isEmpty(oldInfo) && oldInfo.contains("-")) {
				String[] split = oldInfo.split("-");
				double oldInfoFirstPart = Double.valueOf(split[0]);
				double oldInfoSecondPart = Double.valueOf(split[1].substring(substringIndex));
				// do updates
				DirenajMongoDriverUtil.updateRequestInMongoByColumnName(campaignStatisticsCollection, campaignQueryObj,
						existedColumn, oldInfoFirstPart, "$set");
				DirenajMongoDriverUtil.updateRequestInMongoByColumnName(campaignStatisticsCollection, campaignQueryObj,
						newColumn, oldInfoSecondPart, "$set");
			}
		} catch (Exception e) {

		}
	}

	public void calculateBasicUserMeanVariances(DBCollection orgBehaviourProcessInputData) {
		Logger.getLogger(StatisticCalculator.class)
				.debug("Basic User Mean Variance are getting calculated for requestId : " + requestId);
		calculateMeanVariance(orgBehaviourProcessInputData, requestIdObj, requestId,
				MongoCollectionFieldNames.MONGO_USER_FRIEND_FOLLOWER_RATIO, "USER", null);
		calculateMeanVariance(orgBehaviourProcessInputData, requestIdObj, requestId,
				MongoCollectionFieldNames.MONGO_USER_CREATION_DATE_IN_RATA_DIE, "USER", null);
		calculateMeanVariance(orgBehaviourProcessInputData, requestIdObj, requestId,
				MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT, "USER", null);
		calculateMeanVariance(orgBehaviourProcessInputData, requestIdObj, requestId,
				MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT, "USER", null);
		calculateMeanVariance(orgBehaviourProcessInputData, requestIdObj, requestId,
				MongoCollectionFieldNames.MONGO_USER_DAILY_AVARAGE_POST_COUNT, "USER", null);

	}

	private void calculateMeanVariance4All() {
		query4CosSimilarityRequest.put(MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT, new BasicDBObject("$gt", 5));

		Logger.getLogger(StatisticCalculator.class)
				.debug("Mean Variance are getting calculated for requestId : " + requestId);
		DBCollection orgBehaviourProcessInputData = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData();
		calculateBasicUserMeanVariances(orgBehaviourProcessInputData);

		DBObject query4UsersHave2AndMorePosts = new BasicDBObject("requestId", requestId);
		query4UsersHave2AndMorePosts.put(MongoCollectionFieldNames.MONGO_USER_HASHTAG_POST_COUNT,
				new BasicDBObject("$gte", 2));
		DBObject query4UsersHave10AndMorePosts = new BasicDBObject("requestId", requestId);
		query4UsersHave10AndMorePosts.put(MongoCollectionFieldNames.MONGO_USER_HASHTAG_POST_COUNT,
				new BasicDBObject("$gte", 10));
		DBObject query4UsersHave50AndMorePosts = new BasicDBObject("requestId", requestId);
		query4UsersHave50AndMorePosts.put(MongoCollectionFieldNames.MONGO_USER_HASHTAG_POST_COUNT,
				new BasicDBObject("$gte", 50));

		calculateUserRatios(query4UsersHave2AndMorePosts, query4UsersHave10AndMorePosts, query4UsersHave50AndMorePosts,
				orgBehaviourProcessInputData);

		calculateFriendFollowerRatios(query4UsersHave2AndMorePosts, query4UsersHave10AndMorePosts,
				query4UsersHave50AndMorePosts, orgBehaviourProcessInputData);

		calculateRouthTweetCountsMeanVariance(query4UsersHave2AndMorePosts, query4UsersHave10AndMorePosts,
				query4UsersHave50AndMorePosts, orgBehaviourProcessInputData);

		calculateMeanVariance(orgBehaviourProcessInputData, requestIdObj, requestId,
				MongoCollectionFieldNames.MONGO_USER_HASHTAG_POST_COUNT, "USER", null);

		calculateUserCreationDates(query4UsersHave2AndMorePosts, query4UsersHave10AndMorePosts,
				query4UsersHave50AndMorePosts, orgBehaviourProcessInputData);

		// get cos similarity ratios
		DBCollection orgBehaviourRequestedSimilarityCalculations = DirenajMongoDriver.getInstance()
				.getOrgBehaviourRequestedSimilarityCalculations();
		calculateMeanVariance(orgBehaviourRequestedSimilarityCalculations, query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.MONGO_HASHTAG_RATIO, "COS_SIM", null);
		calculateMeanVariance(orgBehaviourRequestedSimilarityCalculations, query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.MONGO_MENTION_RATIO, "COS_SIM", null);
		calculateMeanVariance(orgBehaviourRequestedSimilarityCalculations, query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.MONGO_URL_RATIO, "COS_SIM", null);
		calculateMeanVariance(orgBehaviourRequestedSimilarityCalculations, query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.MONGO_MEDIA_RATIO, "COS_SIM", null);

		calculateMeanVariance(orgBehaviourRequestedSimilarityCalculations, query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.MONGO_RETWEET_RATIO, "COS_SIM", null);
		calculateMeanVariance(orgBehaviourRequestedSimilarityCalculations, query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.MONGO_DISTINCT_RETWEET_RATIO, "COS_SIM", null);
		calculateMeanVariance(orgBehaviourRequestedSimilarityCalculations, query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.MONGO_DISTINCT_RETWEET_USER_RATIO, "COS_SIM", null);

		calculateMeanVariance(orgBehaviourRequestedSimilarityCalculations, query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT_DISTINCT_USER_RATIO, "COS_SIM", null);

		calculateMeanVariance(orgBehaviourRequestedSimilarityCalculations, query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.MONGO_DISTINCT_RETWEET_USER_DIVIDED_BY_RATIO, "COS_SIM", null);
		calculateMeanVariance(orgBehaviourRequestedSimilarityCalculations, query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.MONGO_DISTINCT_NON_RETWEET_USER_DIVIDED_BY_RATIO, "COS_SIM", null);
		calculateMeanVariance(orgBehaviourRequestedSimilarityCalculations, query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.MONGO_TOTAL_RETWEET_COUNT_DISTINCT_RETWEET_COUNT_RATIO, "COS_SIM", null);

		calculateMeanVariance(orgBehaviourRequestedSimilarityCalculations, query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.MONGO_TOTAL_DISTINCT_MENTION_RATIO, "COS_SIM", null);
		calculateMeanVariance(orgBehaviourRequestedSimilarityCalculations, query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.MONGO_RETWEETED_MENTION_DISTINCT_MENTION_RATIO, "COS_SIM", null);
		calculateMeanVariance(orgBehaviourRequestedSimilarityCalculations, query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.MONGO_NON_RETWEETED_MENTION_DISTINCT_MENTION_RATIO, "COS_SIM", null);

		// tweet similarity
		calculateMeanVariance(orgBehaviourRequestedSimilarityCalculations, query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.MOST_SIMILAR, "COS_SIM", null);
		calculateMeanVariance(orgBehaviourRequestedSimilarityCalculations, query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.VERY_SIMILAR, "COS_SIM", null);
		calculateMeanVariance(orgBehaviourRequestedSimilarityCalculations, query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.SIMILAR, "COS_SIM", null);
		calculateMeanVariance(orgBehaviourRequestedSimilarityCalculations, query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.SLIGHTLY_SIMILAR, "COS_SIM", null);
		calculateMeanVariance(orgBehaviourRequestedSimilarityCalculations, query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.NON_SIMILAR, "COS_SIM", null);

	}

	private void calculateRouthTweetCountsMeanVariance(DBObject query4UsersHave2AndMorePosts,
			DBObject query4UsersHave10AndMorePosts, DBObject query4UsersHave50AndMorePosts,
			DBCollection orgBehaviourProcessInputData) {
		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave2AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT, "USER",
				MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT + "_2");
		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave2AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT, "USER",
				MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT + "_2");

		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave10AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT, "USER",
				MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT + "_10");
		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave10AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT, "USER",
				MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT + "_10");

		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave50AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT, "USER",
				MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT + "_50");
		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave50AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT, "USER",
				MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT + "_50");
	}

	private void calculateFriendFollowerRatios(DBObject query4UsersHave2AndMorePosts,
			DBObject query4UsersHave10AndMorePosts, DBObject query4UsersHave50AndMorePosts,
			DBCollection orgBehaviourProcessInputData) {
		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave2AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_USER_FRIEND_FOLLOWER_RATIO, "USER",
				MongoCollectionFieldNames.MONGO_USER_FRIEND_FOLLOWER_RATIO + "_2");
		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave10AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_USER_FRIEND_FOLLOWER_RATIO, "USER",
				MongoCollectionFieldNames.MONGO_USER_FRIEND_FOLLOWER_RATIO + "_10");
		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave50AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_USER_FRIEND_FOLLOWER_RATIO, "USER",
				MongoCollectionFieldNames.MONGO_USER_FRIEND_FOLLOWER_RATIO + "_50");
	}

	private void calculateUserCreationDates(DBObject query4UsersHave2AndMorePosts,
			DBObject query4UsersHave10AndMorePosts, DBObject query4UsersHave50AndMorePosts,
			DBCollection orgBehaviourProcessInputData) {
		// for users with multiple posts
		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave2AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_USER_CREATION_DATE_IN_RATA_DIE, "USER",
				MongoCollectionFieldNames.MONGO_USER_CREATION_DATE_IN_RATA_DIE + "_2");
		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave10AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_USER_CREATION_DATE_IN_RATA_DIE, "USER",
				MongoCollectionFieldNames.MONGO_USER_CREATION_DATE_IN_RATA_DIE + "_10");
		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave50AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_USER_CREATION_DATE_IN_RATA_DIE, "USER",
				MongoCollectionFieldNames.MONGO_USER_CREATION_DATE_IN_RATA_DIE + "_50");
	}

	private void calculateUserRatios(DBObject query4UsersHave2AndMorePosts, DBObject query4UsersHave10AndMorePosts,
			DBObject query4UsersHave50AndMorePosts, DBCollection orgBehaviourProcessInputData) {
		calculateMeanVariance(orgBehaviourProcessInputData, requestIdObj, requestId,
				MongoCollectionFieldNames.MONGO_HASHTAG_RATIO, "USER", null);
		calculateMeanVariance(orgBehaviourProcessInputData, requestIdObj, requestId,
				MongoCollectionFieldNames.MONGO_URL_RATIO, "USER", null);
		calculateMeanVariance(orgBehaviourProcessInputData, requestIdObj, requestId,
				MongoCollectionFieldNames.MONGO_MENTION_RATIO, "USER", null);
		calculateMeanVariance(orgBehaviourProcessInputData, requestIdObj, requestId,
				MongoCollectionFieldNames.MONGO_MEDIA_RATIO, "USER", null);

		calculateMeanVariance(orgBehaviourProcessInputData, requestIdObj, requestId,
				MongoCollectionFieldNames.MONGO_USER_TWEET_AVERAGE_HASHTAG_DAYS, "USER", null);
		calculateMeanVariance(orgBehaviourProcessInputData, requestIdObj, requestId,
				MongoCollectionFieldNames.MONGO_USER_HASHTAG_DAY_AVARAGE_DAY_POST_COUNT_RATIO, "USER", null);

		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave2AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_HASHTAG_RATIO, "USER",
				MongoCollectionFieldNames.MONGO_HASHTAG_RATIO + "_2");
		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave2AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_URL_RATIO, "USER", MongoCollectionFieldNames.MONGO_URL_RATIO + "_2");
		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave2AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_MENTION_RATIO, "USER",
				MongoCollectionFieldNames.MONGO_MENTION_RATIO + "_2");
		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave2AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_MEDIA_RATIO, "USER",
				MongoCollectionFieldNames.MONGO_MEDIA_RATIO + "_2");

		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave10AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_HASHTAG_RATIO, "USER",
				MongoCollectionFieldNames.MONGO_HASHTAG_RATIO + "_10");
		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave10AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_URL_RATIO, "USER", MongoCollectionFieldNames.MONGO_URL_RATIO + "_10");
		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave10AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_MENTION_RATIO, "USER",
				MongoCollectionFieldNames.MONGO_MENTION_RATIO + "_10");
		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave10AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_MEDIA_RATIO, "USER",
				MongoCollectionFieldNames.MONGO_MEDIA_RATIO + "_10");

		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave50AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_HASHTAG_RATIO, "USER",
				MongoCollectionFieldNames.MONGO_HASHTAG_RATIO + "_50");
		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave50AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_URL_RATIO, "USER", MongoCollectionFieldNames.MONGO_URL_RATIO + "_50");
		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave50AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_MENTION_RATIO, "USER",
				MongoCollectionFieldNames.MONGO_MENTION_RATIO + "_50");
		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave50AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_MEDIA_RATIO, "USER",
				MongoCollectionFieldNames.MONGO_MEDIA_RATIO + "_50");

	}

	private void calculateHourlyEntityRatio() throws Exception {
		Gson statusDeserializer = Twitter4jUtil.getGsonObject4Deserialization();

		BasicDBObject query4AllSimilarityRequests = new BasicDBObject(
				MongoCollectionFieldNames.MONGO_COS_SIM_REQ_ORG_REQUEST_ID, requestId);
		// first do calculation
		DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations()
				.find(query4AllSimilarityRequests);
		double wholeRequestProcessTweetCount = 0d;
		try {
			while (paginatedResult.hasNext()) {
				DBObject requestedSimilarityCalculation = paginatedResult.next();
				String requestId = (String) requestedSimilarityCalculation.get("requestId");
				String originalRequestId = (String) requestedSimilarityCalculation.get("originalRequestId");
				// update
				DBObject updateQuery4RequestedCalculation = new BasicDBObject();
				updateQuery4RequestedCalculation.put("requestId", requestId);
				updateQuery4RequestedCalculation.put("originalRequestId", originalRequestId);
				// get time
				String lowerTimeInterval = (String) requestedSimilarityCalculation
						.get(MongoCollectionFieldNames.MONGO_LOWER_TIME_INTERVAL);
				String upperTimeInterval = (String) requestedSimilarityCalculation
						.get(MongoCollectionFieldNames.MONGO_UPPER_TIME_INTERVAL);

				double lowerTimeInRataDie = DateTimeUtils
						.getRataDieFormat4Date(DateTimeUtils.getTwitterDate(lowerTimeInterval));
				double upperTimeInRataDie = DateTimeUtils
						.getRataDieFormat4Date(DateTimeUtils.getTwitterDate(upperTimeInterval));
				// get total tweet count
				double totalTweetCount = 0d;
				double distinctUserCount = 0d;
				if (bypassSimilarityCalculation) {
					totalTweetCount = getTotalTweetCount4Request(lowerTimeInRataDie, upperTimeInRataDie);
					distinctUserCount = getDistinctUserCount4Request(lowerTimeInRataDie, upperTimeInRataDie);
					DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
							DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
							updateQuery4RequestedCalculation, MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT,
							totalTweetCount, "$set");
					DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
							DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
							updateQuery4RequestedCalculation, MongoCollectionFieldNames.MONGO_DISTINCT_USER_COUNT,
							distinctUserCount, "$set");
					if (totalTweetCount > 0) {
						DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
								DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
								updateQuery4RequestedCalculation, MongoCollectionFieldNames.MONGO_TWEET_FOUND, true,
								"$set");
					}
				} else {
					totalTweetCount = (double) requestedSimilarityCalculation
							.get(MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT);
					distinctUserCount = Double.valueOf((int) requestedSimilarityCalculation
							.get(MongoCollectionFieldNames.MONGO_DISTINCT_USER_COUNT));
				}
				wholeRequestProcessTweetCount += totalTweetCount;
				if (totalTweetCount <= 5d) {
					continue;
				}
				if (totalTweetCount == 0d) {
					totalTweetCount = 1d;
				}

				double tweetCountUserCountRatio = totalTweetCount / distinctUserCount;
				if (distinctUserCount == 0d) {
					distinctUserCount = 1d;
					tweetCountUserCountRatio = 0d;
				}

				Logger.getLogger(StatisticCalculator.class)
						.debug("Tweet Dependent Ratios will be calculated for for requestId : " + requestId);
				calculateTweetDependentRatios(statusDeserializer, totalTweetCount, tweetCountUserCountRatio,
						lowerTimeInRataDie, upperTimeInRataDie, updateQuery4RequestedCalculation);
				Logger.getLogger(StatisticCalculator.class)
						.debug("Hourly Retweet Ratios will be calculated for for requestId : " + requestId);
				calculateHourlyRetweetRatios(updateQuery4RequestedCalculation, totalTweetCount, distinctUserCount,
						lowerTimeInRataDie, upperTimeInRataDie);
				Logger.getLogger(StatisticCalculator.class)
						.debug("Hourly Non Retweet Ratios will be calculated for for requestId : " + requestId);
				calculateHourlyNonRetweetRatios(updateQuery4RequestedCalculation, totalTweetCount, distinctUserCount,
						lowerTimeInRataDie, upperTimeInRataDie);
			}
		} finally {
			paginatedResult.close();
		}
		BasicDBObject findQuery = new BasicDBObject();
		findQuery.put("_id", requestId);
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviorRequestCollection(), findQuery,
				MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT, wholeRequestProcessTweetCount, "$set");
	}

	private double getDistinctUserCount4Request(double lowerTimeInRataDie, double upperTimeInRataDie) {
		BasicDBObject tweetQuery = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, campaignId)
				.append("createdAt", new BasicDBObject("$gt", lowerTimeInRataDie).append("$lt", upperTimeInRataDie))
				.append(MongoCollectionFieldNames.MONGO_TWEET_HASHTAG_ENTITIES_TEXT,
						new BasicDBObject("$regex", tracedHashtag).append("$options", "i"));
		double distinctUserCount = DirenajMongoDriver.getInstance().getOrgBehaviourUserTweets()
				.distinct("user.id", tweetQuery).size();
		return distinctUserCount;
	}

	private double getTotalTweetCount4Request(double lowerTimeInRataDie, double upperTimeInRataDie) {
		BasicDBObject tweetQuery = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, campaignId)
				.append("createdAt", new BasicDBObject("$gt", lowerTimeInRataDie).append("$lt", upperTimeInRataDie))
				.append(MongoCollectionFieldNames.MONGO_TWEET_HASHTAG_ENTITIES_TEXT,
						new BasicDBObject("$regex", tracedHashtag).append("$options", "i"));
		return DirenajMongoDriver.getInstance().getOrgBehaviourUserTweets().count(tweetQuery);
	}

	private double getDistinctMentionCount4Request(double lowerTimeInRataDie, double upperTimeInRataDie) {
		BasicDBObject tweetQuery = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, campaignId)
				.append("createdAt", new BasicDBObject("$gt", lowerTimeInRataDie).append("$lt", upperTimeInRataDie))
				.append(MongoCollectionFieldNames.MONGO_TWEET_HASHTAG_ENTITIES_TEXT,
						new BasicDBObject("$regex", tracedHashtag).append("$options", "i"))
				.append("userMentionEntities.id", new BasicDBObject("$exists", true));

		double distinctMentionCount = DirenajMongoDriver.getInstance().getOrgBehaviourUserTweets()
				.distinct("userMentionEntities.id", tweetQuery).size();
		return distinctMentionCount;
	}

	private double getDistinctRetweetedMentionCount4Request(double lowerTimeInRataDie, double upperTimeInRataDie) {
		BasicDBObject tweetQuery = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, campaignId)
				.append("createdAt", new BasicDBObject("$gt", lowerTimeInRataDie).append("$lt", upperTimeInRataDie))
				.append(MongoCollectionFieldNames.MONGO_TWEET_HASHTAG_ENTITIES_TEXT,
						new BasicDBObject("$regex", tracedHashtag).append("$options", "i"))
				.append("userMentionEntities.id", new BasicDBObject("$exists", true))
				.append("retweetedStatus.id", new BasicDBObject("$exists", true));

		double distinctMentionCount = DirenajMongoDriver.getInstance().getOrgBehaviourUserTweets()
				.distinct("userMentionEntities.id", tweetQuery).size();
		return distinctMentionCount;
	}

	private void calculateHourlyRetweetRatios(DBObject updateQuery4RequestedCalculation, double totalTweetCount,
			double distinctUserCount, double lowerTimeInRataDie, double upperTimeInRataDie) {
		BasicDBObject tweetQuery = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, campaignId)
				.append("createdAt", new BasicDBObject("$gt", lowerTimeInRataDie).append("$lt", upperTimeInRataDie))
				.append(MongoCollectionFieldNames.MONGO_TWEET_HASHTAG_ENTITIES_TEXT,
						new BasicDBObject("$regex", tracedHashtag).append("$options", "i"))
				.append("retweetedStatus.id", new BasicDBObject("$exists", true));

		double totalRetweetCount = (double) DirenajMongoDriver.getInstance()
				.getOrgBehaviourRequestedSimilarityCalculations().findOne(updateQuery4RequestedCalculation)
				.get(MongoCollectionFieldNames.MONGO_TOTAL_RETWEET_COUNT);
		double distinctRetweetCount = DirenajMongoDriver.getInstance().getOrgBehaviourUserTweets()
				.distinct("retweetedStatus.id", tweetQuery).size();
		double distinctRetweetUserCount = DirenajMongoDriver.getInstance().getOrgBehaviourUserTweets()
				.distinct("user.id", tweetQuery).size();

		double distinctRetweetUserDividedByRatio = NumberUtils.roundDouble(3,
				distinctRetweetCount / distinctRetweetUserCount);
		double distinctRetweetRatio = NumberUtils.roundDouble(4, distinctRetweetCount / totalTweetCount);
		double distinctRetweetUserRatio = NumberUtils.roundDouble(4, distinctRetweetUserCount / distinctUserCount);
		double totalRetweetCountDistinctRetweetCountRatio = NumberUtils.roundDouble(4,
				totalRetweetCount / distinctRetweetCount);

		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
				updateQuery4RequestedCalculation, MongoCollectionFieldNames.MONGO_DISTINCT_RETWEET_POST_COUNT,
				distinctRetweetCount, "$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
				updateQuery4RequestedCalculation, MongoCollectionFieldNames.MONGO_DISTINCT_RETWEET_USER_COUNT,
				distinctRetweetUserCount, "$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
				updateQuery4RequestedCalculation,
				MongoCollectionFieldNames.MONGO_DISTINCT_RETWEET_USER_DIVIDED_BY_RATIO,
				distinctRetweetUserDividedByRatio, "$set");

		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
				updateQuery4RequestedCalculation, MongoCollectionFieldNames.MONGO_DISTINCT_RETWEET_RATIO,
				distinctRetweetRatio, "$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
				updateQuery4RequestedCalculation, MongoCollectionFieldNames.MONGO_DISTINCT_RETWEET_USER_RATIO,
				distinctRetweetUserRatio, "$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
				updateQuery4RequestedCalculation,
				MongoCollectionFieldNames.MONGO_TOTAL_RETWEET_COUNT_DISTINCT_RETWEET_COUNT_RATIO,
				totalRetweetCountDistinctRetweetCountRatio, "$set");
	}

	private void calculateHourlyNonRetweetRatios(DBObject updateQuery4RequestedCalculation, double totalTweetCount,
			double distinctUserCount, double lowerTimeInRataDie, double upperTimeInRataDie) {
		BasicDBObject tweetQuery = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, campaignId)
				.append("createdAt", new BasicDBObject("$gt", lowerTimeInRataDie).append("$lt", upperTimeInRataDie))
				.append(MongoCollectionFieldNames.MONGO_TWEET_HASHTAG_ENTITIES_TEXT,
						new BasicDBObject("$regex", tracedHashtag).append("$options", "i"))
				.append("retweetedStatus.id", new BasicDBObject("$exists", false));

		double distinctNoneRetweetCount = DirenajMongoDriver.getInstance().getOrgBehaviourUserTweets()
				.distinct("id", tweetQuery).size();
		double distinctNoneRetweetUserCount = DirenajMongoDriver.getInstance().getOrgBehaviourUserTweets()
				.distinct("user.id", tweetQuery).size();
		double distinctNoneRetweetUserDividedByRatio = NumberUtils.roundDouble(3,
				distinctNoneRetweetCount / distinctNoneRetweetUserCount);
		double distinctNoneRetweetRatio = NumberUtils.roundDouble(4, distinctNoneRetweetCount / totalTweetCount);
		double distinctNoneRetweetUserRatio = NumberUtils.roundDouble(4,
				distinctNoneRetweetUserCount / distinctUserCount);

		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
				updateQuery4RequestedCalculation, MongoCollectionFieldNames.MONGO_DISTINCT_NON_RETWEET_POST_COUNT,
				distinctNoneRetweetCount, "$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
				updateQuery4RequestedCalculation, MongoCollectionFieldNames.MONGO_DISTINCT_NON_RETWEET_USER_COUNT,
				distinctNoneRetweetUserCount, "$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
				updateQuery4RequestedCalculation,
				MongoCollectionFieldNames.MONGO_DISTINCT_NON_RETWEET_USER_DIVIDED_BY_RATIO,
				distinctNoneRetweetUserDividedByRatio, "$set");

		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
				updateQuery4RequestedCalculation, MongoCollectionFieldNames.MONGO_DISTINCT_NON_RETWEET_RATIO,
				distinctNoneRetweetRatio, "$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
				updateQuery4RequestedCalculation, MongoCollectionFieldNames.MONGO_DISTINCT_NON_RETWEET_USER_RATIO,
				distinctNoneRetweetUserRatio, "$set");
	}

	private void calculateTweetDependentRatios(Gson statusDeserializer, double totalTweetCount,
			double tweetCountUserCountRatio, double lowerTimeInRataDie, double upperTimeInRataDie,
			DBObject updateQuery) {

		BasicDBObject tweetQuery = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, campaignId)
				.append("createdAt", new BasicDBObject("$gt", lowerTimeInRataDie).append("$lt", upperTimeInRataDie))
				.append(MongoCollectionFieldNames.MONGO_TWEET_HASHTAG_ENTITIES_TEXT,
						new BasicDBObject("$regex", tracedHashtag).append("$options", "i"));

		BasicDBObject keys = new BasicDBObject("_id", false);
		// get cursor
		DBCursor userTweetCursor = DirenajMongoDriver.getInstance().getOrgBehaviourUserTweets().find(tweetQuery, keys)
				.addOption(Bytes.QUERYOPTION_NOTIMEOUT);

		//
		double hashtagRatio = 0d;
		double urlRatio = 0d;
		double mentionRatio = 0d;
		double retweetRatio = 0d;
		double mediaRatio = 0d;
		double retweetedMentionCount = 0d;

		while (userTweetCursor.hasNext()) {
			DBObject status = userTweetCursor.next();
			Status twitter4jStatus = Twitter4jUtil.deserializeTwitter4jStatusFromGson(statusDeserializer,
					status.toString());
			hashtagRatio += (double) (twitter4jStatus.getHashtagEntities().length - 1);
			urlRatio += (double) (twitter4jStatus.getURLEntities().length);
			mentionRatio += (double) (twitter4jStatus.getUserMentionEntities().length);
			if ((twitter4jStatus.getExtendedMediaEntities() != null
					&& twitter4jStatus.getExtendedMediaEntities().length > 0)
					|| (twitter4jStatus.getMediaEntities() != null && twitter4jStatus.getMediaEntities().length > 0)) {
				mediaRatio++;
			}

			if (twitter4jStatus.getRetweetCount() > 0) {
				retweetRatio += 1d;
				retweetedMentionCount += (double) (twitter4jStatus.getUserMentionEntities().length);
			}
		}

		// get mention count
		double totalMentionCount = mentionRatio;
		double distinctMentionCount4Request = getDistinctMentionCount4Request(lowerTimeInRataDie, upperTimeInRataDie);
		double distinctRetweetedMentionCount4Request = getDistinctRetweetedMentionCount4Request(lowerTimeInRataDie,
				upperTimeInRataDie);
		double nonRetweetedMentionCount = totalMentionCount - retweetedMentionCount;
		double distinctNonRetweetedDistinctMentionCount = distinctMentionCount4Request
				- distinctRetweetedMentionCount4Request;

		double totalRetweetMentionCountDistinctRetweetMentionCountRatio = NumberUtils.roundDouble(4,
				retweetedMentionCount / distinctRetweetedMentionCount4Request);

		double totalNonRetweetedMentionCountDistinctNonRetweetedMentionCountRatio = NumberUtils.roundDouble(4,
				nonRetweetedMentionCount / distinctNonRetweetedDistinctMentionCount);

		double totalMentionDistinctMentionRatio = NumberUtils.roundDouble(4,
				totalMentionCount / distinctMentionCount4Request);
		// retweet count
		double totalRetweetCount = retweetRatio;
		// normalize the ratio
		hashtagRatio = NumberUtils.roundDouble(4, hashtagRatio / totalTweetCount);
		urlRatio = NumberUtils.roundDouble(4, urlRatio / totalTweetCount);
		mentionRatio = NumberUtils.roundDouble(4, mentionRatio / totalTweetCount);
		mediaRatio = NumberUtils.roundDouble(4, mediaRatio / totalTweetCount);

		if (totalTweetCount == 1d) {
			retweetRatio = 0;
		} else {
			retweetRatio = NumberUtils.roundDouble(4, retweetRatio / totalTweetCount);
		}

		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(), updateQuery,
				MongoCollectionFieldNames.MONGO_HASHTAG_RATIO, hashtagRatio, "$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(), updateQuery,
				MongoCollectionFieldNames.MONGO_URL_RATIO, urlRatio, "$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(), updateQuery,
				MongoCollectionFieldNames.MONGO_MENTION_RATIO, mentionRatio, "$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(), updateQuery,
				MongoCollectionFieldNames.MONGO_RETWEET_RATIO, retweetRatio, "$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(), updateQuery,
				MongoCollectionFieldNames.MONGO_TOTAL_RETWEET_COUNT, totalRetweetCount, "$set");

		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(), updateQuery,
				MongoCollectionFieldNames.MONGO_MEDIA_RATIO, mediaRatio, "$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(), updateQuery,
				MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT_DISTINCT_USER_RATIO, tweetCountUserCountRatio,
				"$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(), updateQuery,
				MongoCollectionFieldNames.MONGO_TOTAL_MENTION_USER_COUNT, totalMentionCount, "$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(), updateQuery,
				MongoCollectionFieldNames.MONGO_DISTINCT_MENTION_COUNT, distinctMentionCount4Request, "$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(), updateQuery,
				MongoCollectionFieldNames.MONGO_TOTAL_DISTINCT_MENTION_RATIO, totalMentionDistinctMentionRatio, "$set");

		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(), updateQuery,
				MongoCollectionFieldNames.MONGO_RETWEETED_MENTION_USER_COUNT, retweetedMentionCount, "$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(), updateQuery,
				MongoCollectionFieldNames.MONGO_DISTINCT_RETWEETED_MENTION_USER_COUNT,
				distinctRetweetedMentionCount4Request, "$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(), updateQuery,
				MongoCollectionFieldNames.MONGO_RETWEETED_MENTION_DISTINCT_MENTION_RATIO,
				totalRetweetMentionCountDistinctRetweetMentionCountRatio, "$set");

		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(), updateQuery,
				MongoCollectionFieldNames.MONGO_NON_RETWEETED_MENTION_USER_COUNT, nonRetweetedMentionCount, "$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(), updateQuery,
				MongoCollectionFieldNames.MONGO_DISTINCT_NON_RETWEETED_MENTION_USER_COUNT,
				distinctNonRetweetedDistinctMentionCount, "$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(), updateQuery,
				MongoCollectionFieldNames.MONGO_NON_RETWEETED_MENTION_DISTINCT_MENTION_RATIO,
				totalNonRetweetedMentionCountDistinctNonRetweetedMentionCountRatio, "$set");

	}

	private void calculateMeanVariance(DBCollection collection, DBObject query, String requestId,
			String calculationField, String calculationDomain, String reduceCalculationType) {

		if (TextUtils.isEmpty(reduceCalculationType)) {
			reduceCalculationType = calculationField;
		}

		String mapFunction = "function map(){" //
				+ "emit(1, {" //
				+ "sum: this." + calculationField + "," //
				+ "min: this." + calculationField + "," //
				+ "max: this." + calculationField + "," //
				+ "count: 1," //
				+ "diff: 0" //
				+ "});" //
				+ "}";

		String reduceFunction = "function reduce(key, values){" //
				+ "return values.reduce(function reduce(previous, current, index, array) {" //
				+ "var delta = previous.sum/previous.count - current.sum/current.count; " //
				+ "var weight = (previous.count * current.count)/(previous.count + current.count); " //
				+ "return { " //
				+ "sum: ((previous.sum)) + ((current.sum)), " //
				+ "min: Math.min(previous.min, current.min).toFixed(2), " //
				+ "max: Math.max(previous.max, current.max).toFixed(2), " //
				+ "count: previous.count + current.count, " //
				+ "diff: previous.diff + current.diff + delta*delta*weight " //
				+ "};" //
				+ "})" //
				+ "}"; //

		if (calculationField.startsWith(MongoCollectionFieldNames.MONGO_USER_CREATION_DATE_IN_RATA_DIE)) {

			reduceFunction = "function reduce(key, values){" //
					+ "return values.reduce(function reduce(previous, current, index, array) {" //
					+ "var delta = previous.sum/previous.count - current.sum/current.count; " //
					+ "var weight = (previous.count * current.count)/(previous.count + current.count); " //
					+ "return { " //
					+ "sum: Math.round(previous.sum + current.sum), " //
					+ "min: Math.min(previous.min, current.min).toFixed(0), " //
					+ "max: Math.max(previous.max, current.max).toFixed(0), " //
					+ "count: previous.count + current.count, " //
					+ "diff: previous.diff + current.diff + delta*delta*weight " //
					+ "};" //
					+ "})" //
					+ "}"; //
		}

		String finalizeFunction = "function finalize(key, value){" //

				+ "if(isNaN(value.diff / (value.count - 1))){" //
				+ "value.sample_variance = 0;" //
				+ "value.sample_standard_deviation = 0;" //
				+ "value.average = 0;" //
				+ "value.population_variance = 0;" //
				+ "value.population_standard_deviation = 0;"//
				+ "} else {" //
				+ "value.average = (value.sum / value.count).toFixed(2);" //
				+ "value.population_variance = (value.diff / value.count).toFixed(2);" //
				+ "value.population_standard_deviation = Math.sqrt(value.population_variance).toFixed(2);"//
				+ "value.sample_variance = value.diff / (value.count - 1);" //
				+ "value.sample_standard_deviation = Math.sqrt(value.sample_variance);" //
				+ "}" //
				+ "value.requestId = \"" + requestId + "\";" //
				+ "value.calculationType =\"" + reduceCalculationType + "\";" //
				+ "value.calculationDomain =\"" + calculationDomain + "\";" //
				+ "delete value.diff;" //
				+ "return value;" //
				+ "}";

		MapReduceCommand cmd = new MapReduceCommand(collection, mapFunction, reduceFunction, null,
				MapReduceCommand.OutputType.INLINE, query);
		cmd.setFinalize(finalizeFunction);

		MapReduceOutput out = collection.mapReduce(cmd);

		for (DBObject o : out.results()) {
			DBObject calculationResult = (DBObject) o.get("value");
			if (calculationResult != null) {
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestMeanVarianceCalculations()
						.insert(calculationResult);
			}
		}

	}

}
