package direnaj.functionalities.organizedBehaviour;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;
import org.json.JSONException;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

import direnaj.domain.feature.ProcessedDoubleMapPercentageFeature;
import direnaj.domain.feature.ProcessedNestedPercentageFeature;
import direnaj.domain.feature.ProcessedPercentageFeature;
import direnaj.driver.DirenajMongoDriver;
import direnaj.driver.MongoCollectionFieldNames;
import direnaj.util.TextUtils;

public class OrganizedBehaviourFeatureExtractor implements Runnable {

	private static final String FEATURE_EXTRACTION_PATH = "/home/direnaj/toolkit/toolkitExtractedFeatures/";

	private String[] featureExtractionIds;

	public OrganizedBehaviourFeatureExtractor(String[] featureExtractionIds) {
		this.featureExtractionIds = featureExtractionIds;
	}

	private void extractFeatures() {

		DBCollection orgBehaviourProcessInputData = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData();
		DBCollection orgBehaviourRequestedSimilarityCalculations = DirenajMongoDriver.getInstance()
				.getOrgBehaviourRequestedSimilarityCalculations();

		PrintWriter writer = null;
		try {
			String fileName = FEATURE_EXTRACTION_PATH + "extractedFeatures_" + TextUtils.getTimeStamp() + ".txt";
			writer = new PrintWriter(fileName, "UTF-8");
			boolean isKeyAdded = false;
			for (String requestId : featureExtractionIds) {
				StringBuilder trainingData = new StringBuilder();
				StringBuilder trainingDataKeys = new StringBuilder();
				try {
					BasicDBObject query = new BasicDBObject("requestId", requestId);

					// user hashtag post counts variables
					// get percentage values
					extractPercentageFeature4Classification(trainingDataKeys, trainingData, "RoughHashtagPostCounts",
							FeatureExtractorUtil.extractUserRoughHashtagTweetCountsFeatures(query));

					// get mean variance values
					FeatureExtractorUtil.extractMeanVariance(trainingDataKeys, trainingData, "RoughHashtagPostCounts",
							orgBehaviourProcessInputData, requestId,
							MongoCollectionFieldNames.MONGO_USER_HASHTAG_POST_COUNT, "USER");

					// get avarage user daily counts
					extractPercentageFeature4Classification(trainingDataKeys, trainingData,
							"UserHashtagPostCount/AvaragePostHashtagCount",
							FeatureExtractorUtil.extractUserDailyTweetRatios(query,
									MongoCollectionFieldNames.MONGO_USER_HASHTAG_DAY_AVARAGE_DAY_POST_COUNT_RATIO));
					FeatureExtractorUtil.extractMeanVariance(trainingDataKeys, trainingData,
							"UserHashtagPostCount/AvaragePostHashtagCountMeanVariance", orgBehaviourProcessInputData,
							requestId, MongoCollectionFieldNames.MONGO_USER_DAILY_AVARAGE_POST_COUNT, "USER");
					FeatureExtractorUtil.extractMeanVariance(trainingDataKeys, trainingData,
							"UserHashtagPostCount/AvaragePostHashtagCountMeanVariance", orgBehaviourProcessInputData,
							requestId, MongoCollectionFieldNames.MONGO_USER_TWEET_AVERAGE_HASHTAG_DAYS, "USER");
					FeatureExtractorUtil.extractMeanVariance(trainingDataKeys, trainingData,
							"UserHashtagPostCount/AvaragePostHashtagCountMeanVariance", orgBehaviourProcessInputData,
							requestId, MongoCollectionFieldNames.MONGO_USER_HASHTAG_DAY_AVARAGE_DAY_POST_COUNT_RATIO,
							"USER");

					// get user daily avarage post count
					extractPercentageFeature4Classification(trainingDataKeys, trainingData,
							"UserDailyAvaragePostHashtagCount", FeatureExtractorUtil.extractUserDailyTweetRatios(query,
									MongoCollectionFieldNames.MONGO_USER_DAILY_AVARAGE_POST_COUNT));

					// get hourly hashtag-url-mention-media ratios
					FeatureExtractorUtil.extractMeanVariance(trainingDataKeys, trainingData, "HourlyRatio",
							orgBehaviourRequestedSimilarityCalculations, requestId,
							MongoCollectionFieldNames.MONGO_HASHTAG_RATIO, "COS_SIM");
					FeatureExtractorUtil.extractMeanVariance(trainingDataKeys, trainingData, "HourlyRatio",
							orgBehaviourRequestedSimilarityCalculations, requestId,
							MongoCollectionFieldNames.MONGO_MENTION_RATIO, "COS_SIM");
					FeatureExtractorUtil.extractMeanVariance(trainingDataKeys, trainingData, "HourlyRatio",
							orgBehaviourRequestedSimilarityCalculations, requestId,
							MongoCollectionFieldNames.MONGO_URL_RATIO, "COS_SIM");
					FeatureExtractorUtil.extractMeanVariance(trainingDataKeys, trainingData, "HourlyRatio",
							orgBehaviourRequestedSimilarityCalculations, requestId,
							MongoCollectionFieldNames.MONGO_MEDIA_RATIO, "COS_SIM");

					// get hourly retweet ratios
					FeatureExtractorUtil.extractMeanVariance(trainingDataKeys, trainingData, "HourlyRatio",
							orgBehaviourRequestedSimilarityCalculations, requestId,
							MongoCollectionFieldNames.MONGO_RETWEET_RATIO, "COS_SIM");
					FeatureExtractorUtil.extractMeanVariance(trainingDataKeys, trainingData, "HourlyRatio",
							orgBehaviourRequestedSimilarityCalculations, requestId,
							MongoCollectionFieldNames.MONGO_TOTAL_RETWEET_COUNT_DISTINCT_RETWEET_COUNT_RATIO,
							"COS_SIM");
					FeatureExtractorUtil.extractMeanVariance(trainingDataKeys, trainingData, "HourlyRatio",
							orgBehaviourRequestedSimilarityCalculations, requestId,
							MongoCollectionFieldNames.MONGO_DISTINCT_RETWEET_RATIO, "COS_SIM");
					FeatureExtractorUtil.extractMeanVariance(trainingDataKeys, trainingData, "HourlyRatio",
							orgBehaviourRequestedSimilarityCalculations, requestId,
							MongoCollectionFieldNames.MONGO_DISTINCT_RETWEET_USER_RATIO, "COS_SIM");

					// hourly TweetCountUserCountRatio
					FeatureExtractorUtil.extractMeanVariance(trainingDataKeys, trainingData, "HourlyRatio",
							orgBehaviourRequestedSimilarityCalculations, requestId,
							MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT_DISTINCT_USER_RATIO, "COS_SIM");

					// hourly distinctRetweetUserDividedByRatio
					FeatureExtractorUtil.extractMeanVariance(trainingDataKeys, trainingData, "HourlyRatio",
							orgBehaviourRequestedSimilarityCalculations, requestId,
							MongoCollectionFieldNames.MONGO_DISTINCT_RETWEET_USER_DIVIDED_BY_RATIO, "COS_SIM");

					// hourly distinctNonRetweetUserDividedByRatio
					FeatureExtractorUtil.extractMeanVariance(trainingDataKeys, trainingData, "HourlyRatio",
							orgBehaviourRequestedSimilarityCalculations, requestId,
							MongoCollectionFieldNames.MONGO_DISTINCT_NON_RETWEET_USER_DIVIDED_BY_RATIO, "COS_SIM");

					// hourly totalDistinctMentionRatio
					FeatureExtractorUtil.extractMeanVariance(trainingDataKeys, trainingData, "HourlyRatio",
							orgBehaviourRequestedSimilarityCalculations, requestId,
							MongoCollectionFieldNames.MONGO_TOTAL_DISTINCT_MENTION_RATIO, "COS_SIM");

					// hourly retweetedTotalDistinctMentionRatio
					FeatureExtractorUtil.extractMeanVariance(trainingDataKeys, trainingData, "HourlyRatio",
							orgBehaviourRequestedSimilarityCalculations, requestId,
							MongoCollectionFieldNames.MONGO_RETWEETED_MENTION_DISTINCT_MENTION_RATIO, "COS_SIM");

					// hourly nonRetweetedTotalDistinctMentionRatio
					FeatureExtractorUtil.extractMeanVariance(trainingDataKeys, trainingData, "HourlyRatio",
							orgBehaviourRequestedSimilarityCalculations, requestId,
							MongoCollectionFieldNames.MONGO_NON_RETWEETED_MENTION_DISTINCT_MENTION_RATIO, "COS_SIM");

					// user url-hashtag-mention-media ratios
					extractUserRatioPercentageFeature4Classification(trainingDataKeys, trainingData, "UserRatios",
							FeatureExtractorUtil.extractUserTweetEntityRatiosFeature(query, 0));

					FeatureExtractorUtil.extractMeanVariance(trainingDataKeys, trainingData, "UserRatiosMV",
							orgBehaviourProcessInputData, requestId, MongoCollectionFieldNames.MONGO_HASHTAG_RATIO,
							"USER");
					FeatureExtractorUtil.extractMeanVariance(trainingDataKeys, trainingData, "UserRatiosMV",
							orgBehaviourProcessInputData, requestId, MongoCollectionFieldNames.MONGO_URL_RATIO, "USER");
					FeatureExtractorUtil.extractMeanVariance(trainingDataKeys, trainingData, "UserRatiosMV",
							orgBehaviourProcessInputData, requestId, MongoCollectionFieldNames.MONGO_MEDIA_RATIO,
							"USER");
					FeatureExtractorUtil.extractMeanVariance(trainingDataKeys, trainingData, "UserRatiosMV",
							orgBehaviourProcessInputData, requestId, MongoCollectionFieldNames.MONGO_MENTION_RATIO,
							"USER");

					// user friend follower ratios ratios
					extractPercentageFeature4Classification(trainingDataKeys, trainingData, "UserFriendFollowerRatios",
							FeatureExtractorUtil.extractUserFriendFollowerRatioFeature(query, 0));

					FeatureExtractorUtil.extractMeanVariance(trainingDataKeys, trainingData,
							"UserFriendFollowerRatiosMV", orgBehaviourProcessInputData, requestId,
							MongoCollectionFieldNames.MONGO_USER_FRIEND_FOLLOWER_RATIO, "USER");

					// user rough tweets
					extractUserRoughTweetCountPercentageFeature4Classificationn(trainingDataKeys, trainingData,
							"UserRoughTweets", FeatureExtractorUtil.extractUserRoughTweetCountsFeature(query, 0));

					FeatureExtractorUtil.extractMeanVariance(trainingDataKeys, trainingData, "UserRoughTweetsMV",
							orgBehaviourProcessInputData, requestId, MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT,
							"USER");
					FeatureExtractorUtil.extractMeanVariance(trainingDataKeys, trainingData, "UserRoughTweetsMV",
							orgBehaviourProcessInputData, requestId,
							MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT, "USER");

					if (!isKeyAdded) {
						writer.println(trainingDataKeys.toString());
						isKeyAdded = true;
					}
					writer.println(trainingData.toString());

				} catch (JSONException e) {
					Logger.getLogger(OrganizedBehaviourFeatureExtractor.class)
							.error("Error in extractFeatures in OrganizedBehaviourFeatureExtractor for requestId : "
									+ requestId, e);
				}

			}
		} catch (FileNotFoundException | UnsupportedEncodingException e1) {
			e1.printStackTrace();
			Logger.getLogger(OrganizedBehaviourFeatureExtractor.class)
					.error("General Error in OrganizedBehaviourFeatureExtractor", e1);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	@Override
	public void run() {
		extractFeatures();
	}

	private void extractPercentageFeature4Classification(StringBuilder trainingDataKeys, StringBuilder trainingData,
			String featureInitial, ProcessedPercentageFeature percentageFeature) throws JSONException {
		StringBuilder values = new StringBuilder();
		StringBuilder keys = new StringBuilder();

		for (String limit : percentageFeature.getLimits()) {
			keys.append(featureInitial + "_" + limit + ",");
			values.append(percentageFeature.getRangePercentages().get(limit) + ",");
		}

		trainingData.append(values.toString());
		trainingDataKeys.append(keys.toString());
	}

	private void extractPercentageFeature4Classification(StringBuilder trainingDataKeys, StringBuilder trainingData,
			String featureInitial, ProcessedDoubleMapPercentageFeature percentageFeature) throws JSONException {
		StringBuilder values = new StringBuilder();
		StringBuilder keys = new StringBuilder();

		for (String limit : percentageFeature.getLimits()) {
			keys.append(featureInitial + "_" + limit + ",");
			values.append(percentageFeature.getRangePercentages().get(limit) + ",");
		}

		trainingData.append(values.toString());
		trainingDataKeys.append(keys.toString());
	}

	private void extractUserRatioPercentageFeature4Classification(StringBuilder trainingDataKeys,
			StringBuilder trainingData, String featureInitial, ProcessedNestedPercentageFeature percentageFeature)
			throws JSONException {
		StringBuilder values = new StringBuilder();
		StringBuilder keys = new StringBuilder();

		for (String limit : percentageFeature.getLimits()) {

			keys.append(featureInitial + "_" + limit + "_" + MongoCollectionFieldNames.MONGO_URL_RATIO + ",");
			keys.append(featureInitial + "_" + limit + "_" + MongoCollectionFieldNames.MONGO_HASHTAG_RATIO + ",");
			keys.append(featureInitial + "_" + limit + "_" + MongoCollectionFieldNames.MONGO_MENTION_RATIO + ",");
			keys.append(featureInitial + "_" + limit + "_" + MongoCollectionFieldNames.MONGO_MEDIA_RATIO + ",");

			values.append(
					percentageFeature.getRangePercentages().get(limit).get(MongoCollectionFieldNames.MONGO_URL_RATIO)
							+ ",");
			values.append(percentageFeature.getRangePercentages().get(limit)
					.get(MongoCollectionFieldNames.MONGO_HASHTAG_RATIO) + ",");
			values.append(percentageFeature.getRangePercentages().get(limit)
					.get(MongoCollectionFieldNames.MONGO_MENTION_RATIO) + ",");
			values.append(
					percentageFeature.getRangePercentages().get(limit).get(MongoCollectionFieldNames.MONGO_MEDIA_RATIO)
							+ ",");
		}

		trainingData.append(values.toString());
		trainingDataKeys.append(keys.toString());
	}

	private void extractUserRoughTweetCountPercentageFeature4Classificationn(StringBuilder trainingDataKeys,
			StringBuilder trainingData, String featureInitial,
			ProcessedNestedPercentageFeature userRoughTweetCountsFeature) {

		StringBuilder values = new StringBuilder();
		StringBuilder keys = new StringBuilder();

		for (String limit : userRoughTweetCountsFeature.getLimits()) {
			keys.append(featureInitial + "_" + limit + "_" + MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT + ",");
			keys.append(featureInitial + "_" + limit + "_" + MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT + ",");

			values.append(userRoughTweetCountsFeature.getRangePercentages().get(limit)
					.get(MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT) + ",");
			values.append(userRoughTweetCountsFeature.getRangePercentages().get(limit)
					.get(MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT) + ",");
		}

		trainingData.append(values.toString());
		trainingDataKeys.append(keys.toString());
	}
}
