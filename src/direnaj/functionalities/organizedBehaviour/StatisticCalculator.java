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
		DBCollection orgBehaviourProcessInputData = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData();
		calculateMeanVariance(orgBehaviourProcessInputData, requestIdObj, requestId,
				MongoCollectionFieldNames.MONGO_HASHTAG_RATIO, "USER");
		calculateMeanVariance(orgBehaviourProcessInputData, requestIdObj, requestId,
				MongoCollectionFieldNames.MONGO_URL_RATIO, "USER");
		calculateMeanVariance(orgBehaviourProcessInputData, requestIdObj, requestId,
				MongoCollectionFieldNames.MONGO_MENTION_RATIO, "USER");
		calculateMeanVariance(orgBehaviourProcessInputData, requestIdObj, requestId,
				MongoCollectionFieldNames.MONGO_USER_FRIEND_FOLLOWER_RATIO, "USER");
		calculateMeanVariance(orgBehaviourProcessInputData, requestIdObj, requestId,
				MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT, "USER");
		calculateMeanVariance(orgBehaviourProcessInputData, requestIdObj, requestId,
				MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT, "USER");
		calculateMeanVariance(orgBehaviourProcessInputData, requestIdObj, requestId,
				MongoCollectionFieldNames.MONGO_USER_HASHTAG_POST_COUNT, "USER");
		calculateMeanVariance(orgBehaviourProcessInputData, requestIdObj, requestId,
				MongoCollectionFieldNames.MONGO_USER_CREATION_DATE_IN_RATA_DIE, "USER");

		// get cos similarity ratios
		DBCollection orgBehaviourRequestedSimilarityCalculations = DirenajMongoDriver.getInstance()
				.getOrgBehaviourRequestedSimilarityCalculations();
		calculateMeanVariance(orgBehaviourRequestedSimilarityCalculations, query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.MONGO_HASHTAG_RATIO, "COS_SIM");
		calculateMeanVariance(orgBehaviourRequestedSimilarityCalculations, query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.MONGO_MENTION_RATIO, "COS_SIM");
		calculateMeanVariance(orgBehaviourRequestedSimilarityCalculations, query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.MONGO_URL_RATIO, "COS_SIM");
		calculateMeanVariance(orgBehaviourRequestedSimilarityCalculations, query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.MONGO_RETWEET_RATIO, "COS_SIM");
		calculateMeanVariance(orgBehaviourRequestedSimilarityCalculations, query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT_DISTINCT_USER_RATIO, "COS_SIM");
		// tweet similarity
		calculateMeanVariance(orgBehaviourRequestedSimilarityCalculations, query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.MOST_SIMILAR, "COS_SIM");
		calculateMeanVariance(orgBehaviourRequestedSimilarityCalculations, query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.VERY_SIMILAR, "COS_SIM");
		calculateMeanVariance(orgBehaviourRequestedSimilarityCalculations, query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.SIMILAR, "COS_SIM");
		calculateMeanVariance(orgBehaviourRequestedSimilarityCalculations, query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.SLIGHTLY_SIMILAR, "COS_SIM");
		calculateMeanVariance(orgBehaviourRequestedSimilarityCalculations, query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.NON_SIMILAR, "COS_SIM");

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
			String calculationField, String calculationDomain) {

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

		if (MongoCollectionFieldNames.MONGO_USER_CREATION_DATE_IN_RATA_DIE.equals(calculationField)) {

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
				+ "value.average = (value.sum / value.count).toFixed(2);" //
				+ "value.population_variance = (value.diff / value.count).toFixed(2);" //
				+ "value.population_standard_deviation = Math.sqrt(value.population_variance).toFixed(2);" //
				+ "value.sample_variance = value.diff / (value.count - 1);" //
				+ "value.sample_standard_deviation = Math.sqrt(value.sample_variance);" //
				+ "value.requestId = \"" + requestId + "\";" //
				+ "value.calculationType =\"" + calculationField + "\";" //
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
