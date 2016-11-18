package direnaj.functionalities.organizedBehaviour;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;

import direnaj.driver.DirenajMongoDriver;
import direnaj.driver.DirenajMongoDriverUtil;
import direnaj.driver.MongoCollectionFieldNames;
import direnaj.twitter.twitter4j.Twitter4jUtil;
import direnaj.util.NumberUtils;
import direnaj.util.TextUtils;
import twitter4j.Status;

public class StatisticCalculator {
	private String requestId;
	private DBObject requestIdObj;
	private DBObject query4CosSimilarityRequest;

	public StatisticCalculator(String requestId, DBObject requestIdObj, BasicDBObject query4CosSimilarityRequest) {
		this.requestId = requestId;
		this.requestIdObj = requestIdObj;
		this.query4CosSimilarityRequest = query4CosSimilarityRequest;
	}

	public void calculateStatistics() {
		Logger.getLogger(StatisticCalculator.class).debug("Statistics will be calculated for requestId : " + requestId);
		calculateHourlyEntityRatio();
		Logger.getLogger(StatisticCalculator.class)
				.debug("Hourly Entitiy ratios are calculated for requestId : " + requestId);

		calculateCampaignStatistics();
		calculateMeanVariance4All();
		Logger.getLogger(StatisticCalculator.class).debug("Mean Variences are calculated for requestId : " + requestId);

	}

	private void calculateCampaignStatistics() {
		BasicDBObject findQuery = new BasicDBObject();
		findQuery.put("_id", requestId);
		DBObject requestObj = DirenajMongoDriver.getInstance().getOrgBehaviorRequestCollection().findOne(findQuery);
		String campaignId = (String) requestObj.get(MongoCollectionFieldNames.MONGO_REQUEST_CAMPAIGN_ID);
		// check for toolkit campaign statistics
		BasicDBObject campaignQuery = new BasicDBObject();
		campaignQuery.put(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, campaignId);
		DBObject campaignObj = DirenajMongoDriver.getInstance().getCampaignStatisticsCollection()
				.findOne(campaignQuery);

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
					double totalTweetCount = (double) campaignStatistic
							.get(MongoCollectionFieldNames.MONGO_CAMPAIGN_TOTAL_TWEET_COUNT);
					// get retweet info
					String retweetInfo = (String) campaignStatistic
							.get(MongoCollectionFieldNames.MONGO_CAMPAIGN_RETWEET_COUNT);
					String[] split = retweetInfo.split("-");
					double retweetCount = Double.valueOf(split[0]);
					double retweetPercentage = Double.valueOf(split[1].substring(1));
					// do updates
					DirenajMongoDriverUtil.updateRequestInMongoByColumnName(campaignStatisticsCollection,
							campaignQueryObj, MongoCollectionFieldNames.MONGO_CAMPAIGN_RETWEET_COUNT, retweetCount,
							"$set");
					DirenajMongoDriverUtil.updateRequestInMongoByColumnName(campaignStatisticsCollection,
							campaignQueryObj, MongoCollectionFieldNames.MONGO_CAMPAIGN_RETWEET_COUNT_PERCENTAGE,
							retweetPercentage, "$set");

					// get reply info
					String replyInfo = (String) campaignStatistic
							.get(MongoCollectionFieldNames.MONGO_CAMPAIGN_REPLY_TWEET_COUNT);
					String[] replyArr = replyInfo.split("-");
					double replyCount = Double.valueOf(replyArr[0]);
					double replyPercentage = Double.valueOf(replyArr[1].substring(1));
					// do updates
					DirenajMongoDriverUtil.updateRequestInMongoByColumnName(campaignStatisticsCollection,
							campaignQueryObj, MongoCollectionFieldNames.MONGO_CAMPAIGN_REPLY_TWEET_COUNT, replyCount,
							"$set");
					DirenajMongoDriverUtil.updateRequestInMongoByColumnName(campaignStatisticsCollection,
							campaignQueryObj, MongoCollectionFieldNames.MONGO_CAMPAIGN_REPLY_TWEET_COUNT_PERCENTAGE,
							replyPercentage, "$set");
					// get mention info
					String mentionInfo = (String) campaignStatistic
							.get(MongoCollectionFieldNames.MONGO_CAMPAIGN_MENTION_TWEET_COUNT);
					String[] mentionArr = mentionInfo.split("-");
					double mentionCount = Double.valueOf(mentionArr[0]);
					double mentionPercentage = Double.valueOf(mentionArr[1].substring(1));
					// do updates
					DirenajMongoDriverUtil.updateRequestInMongoByColumnName(campaignStatisticsCollection,
							campaignQueryObj, MongoCollectionFieldNames.MONGO_CAMPAIGN_MENTION_TWEET_COUNT,
							mentionCount, "$set");
					DirenajMongoDriverUtil.updateRequestInMongoByColumnName(campaignStatisticsCollection,
							campaignQueryObj, MongoCollectionFieldNames.MONGO_CAMPAIGN_MENTION_TWEET_COUNT_PERCENTAGE,
							mentionPercentage, "$set");
					// get distinct user info
					String distinctUserInfo = (String) campaignStatistic
							.get(MongoCollectionFieldNames.MONGO_CAMPAIGN_DISTINCT_USER_TWEET_COUNT);
					String[] distinctUserArr = distinctUserInfo.split("-");
					double distinctUserCount = Double.valueOf(distinctUserArr[0]);
					double tweetPerUser = Double.valueOf(distinctUserArr[1].substring(1));
					// do updates
					DirenajMongoDriverUtil.updateRequestInMongoByColumnName(campaignStatisticsCollection,
							campaignQueryObj, MongoCollectionFieldNames.MONGO_CAMPAIGN_DISTINCT_USER_TWEET_COUNT,
							distinctUserCount, "$set");
					DirenajMongoDriverUtil.updateRequestInMongoByColumnName(campaignStatisticsCollection,
							campaignQueryObj, MongoCollectionFieldNames.MONGO_CAMPAIGN_TWEET_COUNT_PER_USER,
							tweetPerUser, "$set");

					// get word count info
					double totalWordCount = (double) campaignStatistic
							.get(MongoCollectionFieldNames.MONGO_CAMPAIGN_TOTAL_WORD_COUNT);
					double totalDistinctWordCount = Double.valueOf(((String) campaignStatistic
							.get(MongoCollectionFieldNames.MONGO_CAMPAIGN_TOTAL_DISTINCT_WORD_COUNT)).split("-")[0]);
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

					// get hashtag counts
					@SuppressWarnings("unchecked")
					Map<String, Double> hashTagCounts = (Map<String, Double>) campaignStatistic
							.get(MongoCollectionFieldNames.MONGO_CAMPAIGN_HASHTAG_COUNTS);
					double totalHashtagUsageCount = 0;
					for (Entry<String, Double> entry : hashTagCounts.entrySet()) {
						totalHashtagUsageCount += entry.getValue();
					}
					SummaryStatistics summaryStatistics = new SummaryStatistics();
					for (Entry<String, Double> entry : hashTagCounts.entrySet()) {
						double hashtagPercentage = NumberUtils
								.roundDouble((entry.getValue() * 100d) / totalHashtagUsageCount);
						summaryStatistics.addValue(hashtagPercentage);
						entry.setValue(hashtagPercentage);
					}
					DirenajMongoDriverUtil.updateRequestInMongoByColumnName(campaignStatisticsCollection,
							campaignQueryObj, MongoCollectionFieldNames.MONGO_CAMPAIGN_HASHTAG_COUNTS, hashTagCounts,
							"$set");
					// save variance
					double hashtagPercentageVariance = summaryStatistics.getVariance();
					double hashtagStandardDeviation = summaryStatistics.getStandardDeviation();
					
					DirenajMongoDriverUtil.updateRequestInMongoByColumnName(campaignStatisticsCollection,
							campaignQueryObj, MongoCollectionFieldNames.MONGO_CAMPAIGN_HASHTAG_VARIANCE,
							hashtagPercentageVariance, "$set");
					DirenajMongoDriverUtil.updateRequestInMongoByColumnName(campaignStatisticsCollection,
							campaignQueryObj, MongoCollectionFieldNames.MONGO_CAMPAIGN_HASHTAG_STANDARD_DEVIATION,
							hashtagStandardDeviation, "$set");
				} catch (Exception e) {
					Logger.getLogger(StatisticCalculator.class)
							.error("Error is taken during recalculation of campaign id " + campaignId);
				}
			}
		} finally {
			allCampaigns.close();
		}

	}

	private void calculateMeanVariance4All() {

		DBObject query4UsersHave2AndMorePosts = new BasicDBObject("requestId", requestId);
		query4UsersHave2AndMorePosts.put(MongoCollectionFieldNames.MONGO_USER_HASHTAG_POST_COUNT,
				new BasicDBObject("$gte", 2));
		DBObject query4UsersHave10AndMorePosts = new BasicDBObject("requestId", requestId);
		query4UsersHave10AndMorePosts.put(MongoCollectionFieldNames.MONGO_USER_HASHTAG_POST_COUNT,
				new BasicDBObject("$gte", 10));
		DBObject query4UsersHave50AndMorePosts = new BasicDBObject("requestId", requestId);
		query4UsersHave50AndMorePosts.put(MongoCollectionFieldNames.MONGO_USER_HASHTAG_POST_COUNT,
				new BasicDBObject("$gte", 50));

		DBCollection orgBehaviourProcessInputData = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData();

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
				MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT_DISTINCT_USER_RATIO, "COS_SIM", null);
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
		calculateMeanVariance(orgBehaviourProcessInputData, requestIdObj, requestId,
				MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT, "USER", null);
		calculateMeanVariance(orgBehaviourProcessInputData, requestIdObj, requestId,
				MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT, "USER", null);

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
		calculateMeanVariance(orgBehaviourProcessInputData, requestIdObj, requestId,
				MongoCollectionFieldNames.MONGO_USER_FRIEND_FOLLOWER_RATIO, "USER", null);
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
		calculateMeanVariance(orgBehaviourProcessInputData, requestIdObj, requestId,
				MongoCollectionFieldNames.MONGO_USER_CREATION_DATE_IN_RATA_DIE, "USER", null);
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

	private void calculateHourlyEntityRatio() {
		Gson statusDeserializer = Twitter4jUtil.getGsonObject4Deserialization();
		DBCollection tweetsCollection = DirenajMongoDriver.getInstance().getOrgBehaviourUserTweets();
		// first do calculation
		DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations()
				.find(query4CosSimilarityRequest);
		while (paginatedResult.hasNext()) {
			DBObject next = paginatedResult.next();
			String requestId = (String) next.get("requestId");
			String originalRequestId = (String) next.get("originalRequestId");
			double totalTweetCount = (double) next.get(MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT);
			if (totalTweetCount == 0d) {
				totalTweetCount = 1d;
			}
			double distinctUserCount = Double
					.valueOf((int) next.get(MongoCollectionFieldNames.MONGO_DISTINCT_USER_COUNT));
			double tweetCountUserCountRatio = totalTweetCount / distinctUserCount;
			if (distinctUserCount == 0d) {
				distinctUserCount = 1d;
				tweetCountUserCountRatio = 0d;
			}
			// get tweet info
			BasicDBObject query = new BasicDBObject("requestId", requestId);
			DBCursor similarTweets = DirenajMongoDriver.getInstance().getOrgBehaviourProcessTweetSimilarity()
					.find(query);
			//
			double hashtagRatio = 0d;
			double urlRatio = 0d;
			double mentionRatio = 0d;
			double retweetRatio = 0d;
			double mediaRatio = 0d;

			while (similarTweets.hasNext()) {
				DBObject similarTweet = similarTweets.next();
				// FIXME 20161116 Burada teker teker cekilmesin tweetler. Duzgun
				// birsey yapalim buraya
				String tweetId = (String) similarTweet.get(MongoCollectionFieldNames.MONGO_TWEET_ID);
				DBObject tweetQuery = new BasicDBObject();
				tweetQuery.put("id", Long.valueOf(tweetId));
				BasicDBObject keys = new BasicDBObject("_id", false);
				DBObject status = tweetsCollection.findOne(tweetQuery, keys);
				Status twitter4jStatus = Twitter4jUtil.deserializeTwitter4jStatusFromGson(statusDeserializer,
						status.toString());
				hashtagRatio += (double) (twitter4jStatus.getHashtagEntities().length - 1);
				urlRatio += (double) (twitter4jStatus.getURLEntities().length);
				mentionRatio += (double) (twitter4jStatus.getUserMentionEntities().length);
				if ((twitter4jStatus.getExtendedMediaEntities() != null
						&& twitter4jStatus.getExtendedMediaEntities().length > 0)
						|| (twitter4jStatus.getMediaEntities() != null
								&& twitter4jStatus.getMediaEntities().length > 0)) {
					mediaRatio++;
				}

				if (twitter4jStatus.getRetweetCount() > 0) {
					retweetRatio += 1d;
				}
			}
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

			// update
			DBObject updateQuery = new BasicDBObject();
			updateQuery.put("requestId", requestId);
			updateQuery.put("originalRequestId", originalRequestId);
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
					MongoCollectionFieldNames.MONGO_MEDIA_RATIO, mediaRatio, "$set");
			DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
					DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(), updateQuery,
					MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT_DISTINCT_USER_RATIO, tweetCountUserCountRatio,
					"$set");
		}
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
