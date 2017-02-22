package direnaj.domain.feature;

import com.mongodb.DBObject;

import direnaj.driver.DirenajMongoDriver;
import direnaj.driver.DirenajMongoDriverUtil;
import direnaj.driver.MongoCollectionFieldNames;
import direnaj.util.NumberUtils;

public class HourlyTweetFeatures {

	// generic features
	private double totalTweetCount;
	private double distinctUserCount;
	private double tweetCountUserCountRatio;
	private double hashtagCount;

	private double urlCount;
	private double mediaCount;
	private double totalRetweetCount;

	private double hashtagRatio;
	private double urlRatio;
	private double mentionRatio;
	private double retweetRatio;
	private double mediaRatio;
	private double retweetedMentionCount;
	// mention features
	private double totalMentionCount;
	private double distinctMentionCount4Request;
	private double totalMentionDistinctMentionRatio;
	// retweet features
	private double distinctRetweetedMentionCount4Request;
	private double totalRetweetMentionCountDistinctRetweetMentionCountRatio;
	// none retweet features
	private double nonRetweetedMentionCount;
	private double distinctNonRetweetedDistinctMentionCount;
	private double totalNonRetweetedMentionCountDistinctNonRetweetedMentionCountRatio;
	// retweet features
	private double distinctRetweetCount;
	private double distinctRetweetUserCount;
	private double distinctRetweetUserDividedByRatio;
	private double distinctRetweetRatio;
	private double distinctRetweetUserRatio;
	private double totalRetweetCountDistinctRetweetCountRatio;
	// none retweet features
	private double distinctNoneRetweetCount;
	private double distinctNoneRetweetUserCount;
	private double distinctNoneRetweetUserDividedByRatio;
	private double distinctNoneRetweetRatio;
	private double distinctNoneRetweetUserRatio;

	public HourlyTweetFeatures() {
	}

	public double getTotalTweetCount() {
		if (totalTweetCount == 0d) {
			totalTweetCount = 1d;
		}
		return totalTweetCount;
	}

	public void setTotalTweetCount(double totalTweetCount) {
		this.totalTweetCount = totalTweetCount;
	}

	public double getDistinctUserCount() {
		if (distinctUserCount == 0d) {
			distinctUserCount = 1d;
			tweetCountUserCountRatio = 0d;
		}
		return distinctUserCount;
	}

	public void setDistinctUserCount(double distinctUserCount) {
		this.distinctUserCount = distinctUserCount;
	}

	public double getTweetCountUserCountRatio() {
		return tweetCountUserCountRatio;
	}

	public void setTweetCountUserCountRatio(double tweetCountUserCountRatio) {
		this.tweetCountUserCountRatio = tweetCountUserCountRatio;
	}

	public double getHashtagRatio() {
		return hashtagRatio;
	}

	public void setHashtagRatio(double hashtagRatio) {
		this.hashtagRatio = hashtagRatio;
	}

	public double getUrlRatio() {
		return urlRatio;
	}

	public void setUrlRatio(double urlRatio) {
		this.urlRatio = urlRatio;
	}

	public double getMentionRatio() {
		return mentionRatio;
	}

	public void setMentionRatio(double mentionRatio) {
		this.mentionRatio = mentionRatio;
	}

	public double getRetweetRatio() {
		return retweetRatio;
	}

	public void setRetweetRatio(double retweetRatio) {
		this.retweetRatio = retweetRatio;
	}

	public double getMediaRatio() {
		return mediaRatio;
	}

	public void setMediaRatio(double mediaRatio) {
		this.mediaRatio = mediaRatio;
	}

	public double getRetweetedMentionCount() {
		return retweetedMentionCount;
	}

	public void setRetweetedMentionCount(double retweetedMentionCount) {
		this.retweetedMentionCount = retweetedMentionCount;
	}

	public double getTotalMentionCount() {
		return totalMentionCount;
	}

	public void setTotalMentionCount(double totalMentionCount) {
		this.totalMentionCount = totalMentionCount;
	}

	public double getDistinctMentionCount4Request() {
		return distinctMentionCount4Request;
	}

	public void setDistinctMentionCount4Request(double distinctMentionCount4Request) {
		this.distinctMentionCount4Request = distinctMentionCount4Request;
	}

	public double getTotalMentionDistinctMentionRatio() {
		return totalMentionDistinctMentionRatio;
	}

	public void setTotalMentionDistinctMentionRatio(double totalMentionDistinctMentionRatio) {
		this.totalMentionDistinctMentionRatio = totalMentionDistinctMentionRatio;
	}

	public double getDistinctRetweetedMentionCount4Request() {
		return distinctRetweetedMentionCount4Request;
	}

	public void setDistinctRetweetedMentionCount4Request(double distinctRetweetedMentionCount4Request) {
		this.distinctRetweetedMentionCount4Request = distinctRetweetedMentionCount4Request;
	}

	public double getTotalRetweetMentionCountDistinctRetweetMentionCountRatio() {
		return totalRetweetMentionCountDistinctRetweetMentionCountRatio;
	}

	public void setTotalRetweetMentionCountDistinctRetweetMentionCountRatio(
			double totalRetweetMentionCountDistinctRetweetMentionCountRatio) {
		this.totalRetweetMentionCountDistinctRetweetMentionCountRatio = totalRetweetMentionCountDistinctRetweetMentionCountRatio;
	}

	public double getNonRetweetedMentionCount() {
		return nonRetweetedMentionCount;
	}

	public void setNonRetweetedMentionCount(double nonRetweetedMentionCount) {
		this.nonRetweetedMentionCount = nonRetweetedMentionCount;
	}

	public double getDistinctNonRetweetedDistinctMentionCount() {
		return distinctNonRetweetedDistinctMentionCount;
	}

	public void setDistinctNonRetweetedDistinctMentionCount(double distinctNonRetweetedDistinctMentionCount) {
		this.distinctNonRetweetedDistinctMentionCount = distinctNonRetweetedDistinctMentionCount;
	}

	public double getTotalNonRetweetedMentionCountDistinctNonRetweetedMentionCountRatio() {
		return totalNonRetweetedMentionCountDistinctNonRetweetedMentionCountRatio;
	}

	public void setTotalNonRetweetedMentionCountDistinctNonRetweetedMentionCountRatio(
			double totalNonRetweetedMentionCountDistinctNonRetweetedMentionCountRatio) {
		this.totalNonRetweetedMentionCountDistinctNonRetweetedMentionCountRatio = totalNonRetweetedMentionCountDistinctNonRetweetedMentionCountRatio;
	}

	public double getTotalRetweetCount() {
		return totalRetweetCount;
	}

	public void setTotalRetweetCount(double totalRetweetCount) {
		this.totalRetweetCount = totalRetweetCount;
	}

	public double getDistinctRetweetCount() {
		return distinctRetweetCount;
	}

	public void setDistinctRetweetCount(double distinctRetweetCount) {
		this.distinctRetweetCount = distinctRetweetCount;
	}

	public double getDistinctRetweetUserCount() {
		return distinctRetweetUserCount;
	}

	public void setDistinctRetweetUserCount(double distinctRetweetUserCount) {
		this.distinctRetweetUserCount = distinctRetweetUserCount;
	}

	public double getDistinctRetweetUserDividedByRatio() {
		return distinctRetweetUserDividedByRatio;
	}

	public void setDistinctRetweetUserDividedByRatio(double distinctRetweetUserDividedByRatio) {
		this.distinctRetweetUserDividedByRatio = distinctRetweetUserDividedByRatio;
	}

	public double getDistinctRetweetRatio() {
		return distinctRetweetRatio;
	}

	public void setDistinctRetweetRatio(double distinctRetweetRatio) {
		this.distinctRetweetRatio = distinctRetweetRatio;
	}

	public double getDistinctRetweetUserRatio() {
		return distinctRetweetUserRatio;
	}

	public void setDistinctRetweetUserRatio(double distinctRetweetUserRatio) {
		this.distinctRetweetUserRatio = distinctRetweetUserRatio;
	}

	public double getTotalRetweetCountDistinctRetweetCountRatio() {
		return totalRetweetCountDistinctRetweetCountRatio;
	}

	public void setTotalRetweetCountDistinctRetweetCountRatio(double totalRetweetCountDistinctRetweetCountRatio) {
		this.totalRetweetCountDistinctRetweetCountRatio = totalRetweetCountDistinctRetweetCountRatio;
	}

	public double getDistinctNoneRetweetCount() {
		return distinctNoneRetweetCount;
	}

	public void setDistinctNoneRetweetCount(double distinctNoneRetweetCount) {
		this.distinctNoneRetweetCount = distinctNoneRetweetCount;
	}

	public double getDistinctNoneRetweetUserCount() {
		return distinctNoneRetweetUserCount;
	}

	public void setDistinctNoneRetweetUserCount(double distinctNoneRetweetUserCount) {
		this.distinctNoneRetweetUserCount = distinctNoneRetweetUserCount;
	}

	public double getDistinctNoneRetweetUserDividedByRatio() {
		return distinctNoneRetweetUserDividedByRatio;
	}

	public void setDistinctNoneRetweetUserDividedByRatio(double distinctNoneRetweetUserDividedByRatio) {
		this.distinctNoneRetweetUserDividedByRatio = distinctNoneRetweetUserDividedByRatio;
	}

	public double getDistinctNoneRetweetRatio() {
		return distinctNoneRetweetRatio;
	}

	public void setDistinctNoneRetweetRatio(double distinctNoneRetweetRatio) {
		this.distinctNoneRetweetRatio = distinctNoneRetweetRatio;
	}

	public double getDistinctNoneRetweetUserRatio() {
		return distinctNoneRetweetUserRatio;
	}

	public void setDistinctNoneRetweetUserRatio(double distinctNoneRetweetUserRatio) {
		this.distinctNoneRetweetUserRatio = distinctNoneRetweetUserRatio;
	}

	public double getHashtagCount() {
		return hashtagCount;
	}

	public void setHashtagCount(double hashtagCount) {
		this.hashtagCount = hashtagCount;
	}

	public double getUrlCount() {
		return urlCount;
	}

	public void setUrlCount(double urlCount) {
		this.urlCount = urlCount;
	}

	public double getMediaCount() {
		return mediaCount;
	}

	public void setMediaCount(double mediaCount) {
		this.mediaCount = mediaCount;
	}

	public void incrementHashtagCount(double incrementCount) {
		hashtagCount += incrementCount;
	}

	public void incrementUrlCount(double incrementCount) {
		urlCount += incrementCount;
	}

	public void incrementTotalMentionCount(double incrementCount) {
		totalMentionCount += incrementCount;
	}

	public void incrementTotalRetweetCount(double incrementCount) {
		totalRetweetCount += incrementCount;
	}

	public void incrementMediaCount(double incrementCount) {
		mediaCount += incrementCount;
	}

	public void incrementRetweetedMentionCount(double incrementCount) {
		retweetedMentionCount += incrementCount;
	}

	public void incrementTotalTweetCount(double incrementCount) {
		totalTweetCount += incrementCount;
	}

	public void incrementDistinctNoneRetweetCount(double incrementCount) {
		distinctNoneRetweetCount += incrementCount;
	}

	public void calculateAllRatios() {
		// calculate general tweet features
		tweetCountUserCountRatio = NumberUtils.roundDouble(4,totalTweetCount / distinctUserCount);
		hashtagRatio = NumberUtils.roundDouble(4, hashtagCount / totalTweetCount);
		urlRatio = NumberUtils.roundDouble(4, urlCount / totalTweetCount);
		mentionRatio = NumberUtils.roundDouble(4, totalMentionCount / totalTweetCount);
		mediaRatio = NumberUtils.roundDouble(4, mediaCount / totalTweetCount);
		if (totalTweetCount == 1d) {
			retweetRatio = 0;
		} else {
			retweetRatio = NumberUtils.roundDouble(4, totalRetweetCount / totalTweetCount);
		}
		// calculate mention features
		nonRetweetedMentionCount = totalMentionCount - retweetedMentionCount;
		distinctNonRetweetedDistinctMentionCount = distinctMentionCount4Request - distinctRetweetedMentionCount4Request;
		totalRetweetMentionCountDistinctRetweetMentionCountRatio = NumberUtils.roundDouble(4,
				retweetedMentionCount / distinctRetweetedMentionCount4Request);
		totalNonRetweetedMentionCountDistinctNonRetweetedMentionCountRatio = NumberUtils.roundDouble(4,
				nonRetweetedMentionCount / distinctNonRetweetedDistinctMentionCount);
		totalMentionDistinctMentionRatio = NumberUtils.roundDouble(4, totalMentionCount / distinctMentionCount4Request);
		// calculate retweet features
		distinctRetweetUserDividedByRatio = NumberUtils.roundDouble(3, distinctRetweetCount / distinctRetweetUserCount);
		distinctRetweetRatio = NumberUtils.roundDouble(4, distinctRetweetCount / totalTweetCount);
		distinctRetweetUserRatio = NumberUtils.roundDouble(4, distinctRetweetUserCount / distinctUserCount);
		totalRetweetCountDistinctRetweetCountRatio = NumberUtils.roundDouble(4,
				totalRetweetCount / distinctRetweetCount);
		// calculate none retweet feature
		distinctNoneRetweetCount = totalTweetCount - totalRetweetCount;
		distinctNoneRetweetUserDividedByRatio = NumberUtils.roundDouble(3,
				distinctNoneRetweetCount / distinctNoneRetweetUserCount);
		distinctNoneRetweetRatio = NumberUtils.roundDouble(4, distinctNoneRetweetCount / totalTweetCount);
		distinctNoneRetweetUserRatio = NumberUtils.roundDouble(4, distinctNoneRetweetUserCount / distinctUserCount);
	}

	public void save2Mongo(DBObject updateQuery4RequestedCalculation) {
		// generic updates
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
				updateQuery4RequestedCalculation, MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT, totalTweetCount,
				"$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
				updateQuery4RequestedCalculation, MongoCollectionFieldNames.MONGO_DISTINCT_USER_COUNT,
				distinctUserCount, "$set");
		if (totalTweetCount > 0) {
			DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
					DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
					updateQuery4RequestedCalculation, MongoCollectionFieldNames.MONGO_TWEET_FOUND, true, "$set");
		}
		if (totalTweetCount > 5d) {
			saveGeneralFeatures(updateQuery4RequestedCalculation);
			// mention features
			saveMentionFeatures(updateQuery4RequestedCalculation);
			// retweet features
			saveRetweetFeatures(updateQuery4RequestedCalculation);
			// none retwet features
			saveNoneRetweetFeatures(updateQuery4RequestedCalculation);
		}
	}

	private void saveNoneRetweetFeatures(DBObject updateQuery4RequestedCalculation) {
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

	private void saveRetweetFeatures(DBObject updateQuery4RequestedCalculation) {
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

	private void saveMentionFeatures(DBObject updateQuery4RequestedCalculation) {
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
				updateQuery4RequestedCalculation, MongoCollectionFieldNames.MONGO_TOTAL_MENTION_USER_COUNT,
				totalMentionCount, "$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
				updateQuery4RequestedCalculation, MongoCollectionFieldNames.MONGO_DISTINCT_MENTION_COUNT,
				distinctMentionCount4Request, "$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
				updateQuery4RequestedCalculation, MongoCollectionFieldNames.MONGO_TOTAL_DISTINCT_MENTION_RATIO,
				totalMentionDistinctMentionRatio, "$set");
		// retweet mention features
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
				updateQuery4RequestedCalculation, MongoCollectionFieldNames.MONGO_RETWEETED_MENTION_USER_COUNT,
				retweetedMentionCount, "$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
				updateQuery4RequestedCalculation, MongoCollectionFieldNames.MONGO_DISTINCT_RETWEETED_MENTION_USER_COUNT,
				distinctRetweetedMentionCount4Request, "$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
				updateQuery4RequestedCalculation,
				MongoCollectionFieldNames.MONGO_RETWEETED_MENTION_DISTINCT_MENTION_RATIO,
				totalRetweetMentionCountDistinctRetweetMentionCountRatio, "$set");
		// none retweet mention features
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
				updateQuery4RequestedCalculation, MongoCollectionFieldNames.MONGO_NON_RETWEETED_MENTION_USER_COUNT,
				nonRetweetedMentionCount, "$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
				updateQuery4RequestedCalculation,
				MongoCollectionFieldNames.MONGO_DISTINCT_NON_RETWEETED_MENTION_USER_COUNT,
				distinctNonRetweetedDistinctMentionCount, "$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
				updateQuery4RequestedCalculation,
				MongoCollectionFieldNames.MONGO_NON_RETWEETED_MENTION_DISTINCT_MENTION_RATIO,
				totalNonRetweetedMentionCountDistinctNonRetweetedMentionCountRatio, "$set");
	}

	private void saveGeneralFeatures(DBObject updateQuery4RequestedCalculation) {
		// general features
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
				updateQuery4RequestedCalculation, MongoCollectionFieldNames.MONGO_HASHTAG_RATIO, hashtagRatio, "$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
				updateQuery4RequestedCalculation, MongoCollectionFieldNames.MONGO_URL_RATIO, urlRatio, "$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
				updateQuery4RequestedCalculation, MongoCollectionFieldNames.MONGO_MENTION_RATIO, mentionRatio, "$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
				updateQuery4RequestedCalculation, MongoCollectionFieldNames.MONGO_RETWEET_RATIO, retweetRatio, "$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
				updateQuery4RequestedCalculation, MongoCollectionFieldNames.MONGO_MEDIA_RATIO, mediaRatio, "$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
				updateQuery4RequestedCalculation, MongoCollectionFieldNames.MONGO_TOTAL_RETWEET_COUNT,
				totalRetweetCount, "$set");
		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
				updateQuery4RequestedCalculation, MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT_DISTINCT_USER_RATIO,
				tweetCountUserCountRatio, "$set");
	}

}
