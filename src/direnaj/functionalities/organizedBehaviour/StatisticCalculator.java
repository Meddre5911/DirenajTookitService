package direnaj.functionalities.organizedBehaviour;

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
		calculateMeanVariance4All();
		Logger.getLogger(StatisticCalculator.class).debug("Mean Variences are calculated for requestId : " + requestId);

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

		calculateUserRatios(query4UsersHave10AndMorePosts, query4UsersHave50AndMorePosts, orgBehaviourProcessInputData);

		calculateMeanVariance(orgBehaviourProcessInputData, requestIdObj, requestId,
				MongoCollectionFieldNames.MONGO_USER_FRIEND_FOLLOWER_RATIO, "USER", null);
		calculateMeanVariance(orgBehaviourProcessInputData, requestIdObj, requestId,
				MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT, "USER", null);
		calculateMeanVariance(orgBehaviourProcessInputData, requestIdObj, requestId,
				MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT, "USER", null);
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

	private void calculateUserRatios(DBObject query4UsersHave10AndMorePosts, DBObject query4UsersHave50AndMorePosts,
			DBCollection orgBehaviourProcessInputData) {
		calculateMeanVariance(orgBehaviourProcessInputData, requestIdObj, requestId,
				MongoCollectionFieldNames.MONGO_HASHTAG_RATIO, "USER", null);
		calculateMeanVariance(orgBehaviourProcessInputData, requestIdObj, requestId,
				MongoCollectionFieldNames.MONGO_URL_RATIO, "USER", null);
		calculateMeanVariance(orgBehaviourProcessInputData, requestIdObj, requestId,
				MongoCollectionFieldNames.MONGO_MENTION_RATIO, "USER", null);

		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave10AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_HASHTAG_RATIO, "USER",
				MongoCollectionFieldNames.MONGO_HASHTAG_RATIO + "_2");
		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave10AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_URL_RATIO, "USER", MongoCollectionFieldNames.MONGO_URL_RATIO + "_2");
		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave10AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_MENTION_RATIO, "USER",
				MongoCollectionFieldNames.MONGO_MENTION_RATIO + "_2");

		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave10AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_HASHTAG_RATIO, "USER",
				MongoCollectionFieldNames.MONGO_HASHTAG_RATIO + "_10");
		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave10AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_URL_RATIO, "USER", MongoCollectionFieldNames.MONGO_URL_RATIO + "_10");
		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave10AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_MENTION_RATIO, "USER",
				MongoCollectionFieldNames.MONGO_MENTION_RATIO + "_10");

		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave50AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_HASHTAG_RATIO, "USER",
				MongoCollectionFieldNames.MONGO_HASHTAG_RATIO + "_50");
		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave50AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_URL_RATIO, "USER", MongoCollectionFieldNames.MONGO_URL_RATIO + "_50");
		calculateMeanVariance(orgBehaviourProcessInputData, query4UsersHave50AndMorePosts, requestId,
				MongoCollectionFieldNames.MONGO_MENTION_RATIO, "USER",
				MongoCollectionFieldNames.MONGO_MENTION_RATIO + "_50");
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

			while (similarTweets.hasNext()) {
				DBObject similarTweet = similarTweets.next();
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
				if (twitter4jStatus.getRetweetCount() > 0) {
					retweetRatio += 1d;
				}
			}
			// normalize the ratio
			hashtagRatio = NumberUtils.roundDouble(4, hashtagRatio / totalTweetCount);
			urlRatio = NumberUtils.roundDouble(4, urlRatio / totalTweetCount);
			mentionRatio = NumberUtils.roundDouble(4, mentionRatio / totalTweetCount);
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
