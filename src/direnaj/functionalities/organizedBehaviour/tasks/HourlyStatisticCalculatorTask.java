package direnaj.functionalities.organizedBehaviour.tasks;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import direnaj.domain.HourlyTweetFeatures;
import direnaj.driver.DirenajMongoDriver;
import direnaj.driver.DirenajMongoDriverUtil;
import direnaj.driver.MongoCollectionFieldNames;
import direnaj.functionalities.organizedBehaviour.ResumeBreakPoint;
import direnaj.functionalities.organizedBehaviour.StatisticCalculator;
import direnaj.twitter.twitter4j.Twitter4jUtil;
import direnaj.util.DateTimeUtils;
import direnaj.util.PropertiesUtil;
import twitter4j.Status;
import twitter4j.UserMentionEntity;

public class HourlyStatisticCalculatorTask implements Runnable {

	private CyclicBarrier cyclicBarrier;
	private Gson statusDeserializer;
	private String campaignId;
	private String tracedHashtag;
	private DBObject requestedSimilarityCalculation;
	private boolean bypassSimilarityCalculation;
	private int threadNumber;
	private String requestId;

	public HourlyStatisticCalculatorTask(CyclicBarrier cyclicBarrier, Gson statusDeserializer,
			DBObject requestedSimilarityCalculation, String campaignId, String tracedHashtag,
			boolean bypassSimilarityCalculation, int threadNumber, String requestId) {
		this.cyclicBarrier = cyclicBarrier;
		this.statusDeserializer = statusDeserializer;
		this.campaignId = campaignId;
		this.tracedHashtag = tracedHashtag;
		this.requestedSimilarityCalculation = requestedSimilarityCalculation;
		this.bypassSimilarityCalculation = bypassSimilarityCalculation;
		this.threadNumber = threadNumber;
		this.requestId = requestId;
	}

	@Override
	public void run() {
		try {
			try {
				calculateHourlyStatistics(requestedSimilarityCalculation, bypassSimilarityCalculation);
			} catch (Exception e) {
				Logger.getLogger(HourlyStatisticCalculatorTask.class)
						.error("HourlyStatisticCalculatorTask - calculateHourlyStatistics gets Exception", e);
			}
			cyclicBarrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			Logger.getLogger(HourlyStatisticCalculatorTask.class).error("HourlyStatisticCalculatorTask gets Exception",
					e);
		}
	}

	private void calculateHourlyStatistics(DBObject requestedSimilarityCalculation, boolean bypassSimilarityCalculation)
			throws Exception {
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

		HourlyTweetFeatures hourlyTweetFeatures = new HourlyTweetFeatures();
		if (bypassSimilarityCalculation) {
			iterateHourlyTweets(statusDeserializer, lowerTimeInRataDie, upperTimeInRataDie,
					updateQuery4RequestedCalculation, hourlyTweetFeatures);
		} else {
			hourlyTweetFeatures.setTotalTweetCount(
					(double) requestedSimilarityCalculation.get(MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT));
		}

		DirenajMongoDriverUtil.updateRequestInMongoByColumnName(
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations(),
				updateQuery4RequestedCalculation, MongoCollectionFieldNames.MONGO_RESUME_BREAKPOINT,
				ResumeBreakPoint.HOURLY_CALCULATION_DONE.name(), "$set");
	}

	private void iterateHourlyTweets(Gson statusDeserializer, double lowerTimeInRataDie, double upperTimeInRataDie,
			DBObject updateQuery4RequestedCalculation, HourlyTweetFeatures hourlyTweetFeatures) {
		Logger.getLogger(StatisticCalculator.class)
				.debug("Thread : " + threadNumber + " - Iteratively Calculations will be made for requestId : "
						+ updateQuery4RequestedCalculation.get(MongoCollectionFieldNames.MONGO_REQUEST_ID)
						+ " between times : " + lowerTimeInRataDie + " - " + upperTimeInRataDie);
		// prepare find query
		BasicDBObject tweetQuery = new BasicDBObject(MongoCollectionFieldNames.MONGO_REQUEST_ID, requestId)
				.append(MongoCollectionFieldNames.MONGO_TWEET_CREATION_DATE,
						new BasicDBObject("$gt", lowerTimeInRataDie).append("$lt", upperTimeInRataDie))
				.append(MongoCollectionFieldNames.MONGO_IS_HASHTAG_TWEET, true);

		// exclude mongo primary key
		BasicDBObject keys = new BasicDBObject("_id", false);
		keys.put(MongoCollectionFieldNames.MONGO_ACTUAL_TWEET_OBJECT, true);

		int batchSize = PropertiesUtil.getInstance().getIntProperty("toolkit.statisticCalculator.batchSizeCount", 500);
		// get cursor
		DBCursor userTweetCursor = DirenajMongoDriver.getInstance().getOrgBehaviourTweetsOfRequest()
				.find(tweetQuery, keys).batchSize(batchSize).addOption(Bytes.QUERYOPTION_NOTIMEOUT);
		Boolean applyHint = PropertiesUtil.getInstance().getBooleanProperty("toolkit.statisticCalculator.applyHint",
				false);
		if (applyHint) {
			DBObject hintObj = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, 1);
			hintObj.put("createdAt", 1);
			userTweetCursor.hint(hintObj);
		}

		// define HashSets
		Set<Long> distinctRetweetIds = new HashSet<>(1000);
		Set<Long> distinctUserIds = new HashSet<>(1000);
		Set<Long> distinctRetweetUserIds = new HashSet<>(1000);
		Set<Long> distinctNonRetweetUserIds = new HashSet<>(1000);
		Set<Long> distinctMentionUserIds = new HashSet<>(1000);
		Set<Long> distinctRetweetedMentionUserIds = new HashSet<>(1000);
		// iterate
		try {
			while (userTweetCursor.hasNext()) {
				DBObject actualTweet = userTweetCursor.next();
				DBObject status = (DBObject) actualTweet.get(MongoCollectionFieldNames.MONGO_ACTUAL_TWEET_OBJECT);
				Status twitter4jStatus = Twitter4jUtil.deserializeTwitter4jStatusFromGson(statusDeserializer,
						status.toString());
				// increment total tweet count
				hourlyTweetFeatures.incrementTotalTweetCount(1);
				distinctUserIds.add(twitter4jStatus.getUser().getId());
				// increment general features
				hourlyTweetFeatures.incrementHashtagCount(twitter4jStatus.getHashtagEntities().length - 1);
				hourlyTweetFeatures.incrementUrlCount(twitter4jStatus.getURLEntities().length);
				hourlyTweetFeatures.incrementTotalMentionCount(twitter4jStatus.getUserMentionEntities().length);
				// get mention count
				if (twitter4jStatus.getUserMentionEntities().length > 0) {
					for (UserMentionEntity userMentionEntity : twitter4jStatus.getUserMentionEntities()) {
						distinctMentionUserIds.add(userMentionEntity.getId());
					}
				}

				if ((twitter4jStatus.getExtendedMediaEntities() != null
						&& twitter4jStatus.getExtendedMediaEntities().length > 0)
						|| (twitter4jStatus.getMediaEntities() != null
								&& twitter4jStatus.getMediaEntities().length > 0)) {
					hourlyTweetFeatures.incrementMediaCount(1);
				}
				// increment retweet related features
				if (twitter4jStatus.getRetweetedStatus() != null && twitter4jStatus.getRetweetedStatus().getId() > 0) {
					hourlyTweetFeatures.incrementTotalRetweetCount(1);
					hourlyTweetFeatures.incrementRetweetedMentionCount(twitter4jStatus.getUserMentionEntities().length);
					distinctRetweetIds.add(twitter4jStatus.getRetweetedStatus().getId());
					distinctRetweetUserIds.add(twitter4jStatus.getUser().getId());
					// get mention ids
					for (UserMentionEntity userMentionEntity : twitter4jStatus.getUserMentionEntities()) {
						distinctRetweetedMentionUserIds.add(userMentionEntity.getId());
					}
				} else {
					distinctNonRetweetUserIds.add(twitter4jStatus.getUser().getId());
					hourlyTweetFeatures.incrementDistinctNoneRetweetCount(1);
				}
			}
			hourlyTweetFeatures.setDistinctUserCount(distinctUserIds.size());
			hourlyTweetFeatures.setDistinctRetweetCount(distinctRetweetIds.size());
			hourlyTweetFeatures.setDistinctRetweetUserCount(distinctRetweetUserIds.size());
			hourlyTweetFeatures.setDistinctNoneRetweetUserCount(distinctNonRetweetUserIds.size());
			hourlyTweetFeatures.setDistinctMentionCount4Request(distinctMentionUserIds.size());
			hourlyTweetFeatures.setDistinctRetweetedMentionCount4Request(distinctRetweetedMentionUserIds.size());
		} finally {
			userTweetCursor.close();
		}
		hourlyTweetFeatures.calculateAllRatios();
		hourlyTweetFeatures.save2Mongo(updateQuery4RequestedCalculation);
		Logger.getLogger(StatisticCalculator.class)
				.debug("Thread : " + threadNumber + " - Iteration Finished"
						+ " - Iterative Calculations for requestId : "
						+ updateQuery4RequestedCalculation.get(MongoCollectionFieldNames.MONGO_REQUEST_ID)
						+ " between times : " + lowerTimeInRataDie + " - " + upperTimeInRataDie);
	}

}
