package direnaj.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import direnaj.driver.DirenajMongoDriver;
import direnaj.driver.MongoCollectionFieldNames;
import direnaj.functionalities.organizedBehaviour.FeatureExtractorUtil;
import direnaj.util.CollectionUtil;
import direnaj.util.DateTimeUtils;
import direnaj.util.NumberUtils;
import direnaj.util.PropertiesUtil;
import direnaj.util.TextUtils;

public class OrganizedBehaviorCampaignVisualizer extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7542468189990963215L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json; charset=UTF-8");
		String requestType = req.getParameter("requestType");
		String jsonStr = "{}";

		// check for type
		try {
			JSONArray jsonArray = new JSONArray();
			String requestId = req.getParameter("requestId");

			int userPostCountWithHashtag = NumberUtils.getInt(req.getParameter("userHashtagPostCount"));

			BasicDBObject query = new BasicDBObject("requestId", requestId);
			BasicDBObject query4CosSimilarityRequest = new BasicDBObject(
					MongoCollectionFieldNames.MONGO_COS_SIM_REQ_ORG_REQUEST_ID, requestId);
			query4CosSimilarityRequest.put(MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT,
					new BasicDBObject("$gt", 5));
			if ("visualizeUserCreationTimes".equals(requestType)) {
				visualizeUserCreationTimes(jsonArray, query);
			} else if ("visualizeUserTweetEntityRatios".equals(requestType)) {
				visualizeUserTweetEntityRatios(jsonArray, query);
			} else if ("visualizeUserFriendFollowerRatio".equals(requestType)) {
				visualizeUserFriendFollowerRatio(jsonArray, query);
			} else if ("visualizeUserRoughHashtagTweetCounts".equals(requestType)) {
				visualizeUserRoughHashtagTweetCounts(jsonArray, query);
			} else if ("visualizeUserPostDeviceRatios".equals(requestType)) {
				visualizeUserPostDeviceRatios(jsonArray, query);
			} else if ("visualizeUserRoughTweetCountsInBarChart".equals(requestType)) {
				visualizeUserRoughTweetCountsInBarChart(jsonArray, query, userPostCountWithHashtag);
			} else if ("visualizeUserDailyTweetRatiosInBarChart".equals(requestType)) {
				visualizeUserDailyTweetRatiosInBarChart(jsonArray, query,
						MongoCollectionFieldNames.MONGO_USER_HASHTAG_DAY_AVARAGE_DAY_POST_COUNT_RATIO);
			} else if ("visualizeAvarageDailyPostCountInBarChart".equals(requestType)) {
				visualizeUserDailyTweetRatiosInBarChart(jsonArray, query,
						MongoCollectionFieldNames.MONGO_USER_DAILY_AVARAGE_POST_COUNT);
			} else if ("visualizeHourlyUserAndTweetCount".equals(requestType)) {
				query4CosSimilarityRequest.remove(MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT);
				visualizeHourlyUserAndTweetCount(jsonArray, query4CosSimilarityRequest,
						MongoCollectionFieldNames.MONGO_DISTINCT_USER_COUNT,
						MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT);
			} else if ("visualizeHourlyRetweetedUserAndPostCount".equals(requestType)) {
				query4CosSimilarityRequest.remove(MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT);
				visualizeHourlyUserAndTweetCount(jsonArray, query4CosSimilarityRequest,
						MongoCollectionFieldNames.MONGO_DISTINCT_RETWEET_POST_COUNT,
						MongoCollectionFieldNames.MONGO_DISTINCT_RETWEET_USER_COUNT);
			} else if ("visualizeHourlyNonRetweetedUserAndTweetCount".equals(requestType)) {
				query4CosSimilarityRequest.remove(MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT);
				visualizeHourlyUserAndTweetCount(jsonArray, query4CosSimilarityRequest,
						MongoCollectionFieldNames.MONGO_DISTINCT_NON_RETWEET_POST_COUNT,
						MongoCollectionFieldNames.MONGO_DISTINCT_NON_RETWEET_USER_COUNT);
			}

			else if ("visualizeHourlyTotalAndDistinctMentionCount".equals(requestType)) {
				query4CosSimilarityRequest.remove(MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT);
				visualizeHourlyUserAndTweetCount(jsonArray, query4CosSimilarityRequest,
						MongoCollectionFieldNames.MONGO_TOTAL_MENTION_USER_COUNT,
						MongoCollectionFieldNames.MONGO_DISTINCT_MENTION_COUNT);
			}

			else if ("visualizeHourlyRetweetedTotalAndDistinctMentionCount".equals(requestType)) {
				query4CosSimilarityRequest.remove(MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT);
				visualizeHourlyUserAndTweetCount(jsonArray, query4CosSimilarityRequest,
						MongoCollectionFieldNames.MONGO_RETWEETED_MENTION_USER_COUNT,
						MongoCollectionFieldNames.MONGO_DISTINCT_RETWEETED_MENTION_USER_COUNT);
			}

			else if ("visualizeHourlyNonRetweetedTotalAndDistinctMentionCount".equals(requestType)) {
				query4CosSimilarityRequest.remove(MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT);
				visualizeHourlyUserAndTweetCount(jsonArray, query4CosSimilarityRequest,
						MongoCollectionFieldNames.MONGO_NON_RETWEETED_MENTION_USER_COUNT,
						MongoCollectionFieldNames.MONGO_DISTINCT_NON_RETWEETED_MENTION_USER_COUNT);
			}

			else if ("visualizeHourlyTweetSimilarities".equals(requestType)) {
				visualizeHourlyTweetSimilarities(jsonArray, query4CosSimilarityRequest);
			} else if ("visualizeUserCreationTimesInBarChart".equals(requestType)) {
				visualizeUserCreationTimesInBarChart(jsonArray, query, userPostCountWithHashtag);
			} else if ("visualizeUserFriendFollowerRatioInBarChart".equals(requestType)) {
				visualizeUserFriendFollowerRatioInBarChart(jsonArray, query, requestId, userPostCountWithHashtag);
			} else if ("visualizeUserRoughHashtagTweetCountsInBarChart".equals(requestType)) {
				FeatureExtractorUtil.visualizeUserRoughHashtagTweetCountsInBarChart(jsonArray, query);
			} else if ("visualizeUserTweetEntityRatiosInBarChart".equals(requestType)) {
				visualizeUserTweetEntityRatiosInBarChart(jsonArray, query, userPostCountWithHashtag);
			} else if ("visualizeHourlyEntityRatios".equals(requestType)) {
				visualizeHourlyEntityRatios(jsonArray, query4CosSimilarityRequest);
			} else if ("visualizeHourlyRetweetRatios".equals(requestType)) {
				visualizeHourlyTweetRatios(jsonArray, query4CosSimilarityRequest);
			} else if (("getMeanVariance".equals(requestType))) {
				getMeanVariance4All(jsonArray, query, query4CosSimilarityRequest, requestId);
			} else if (("getMeanVarianceUserBasics".equals(requestType))) {
				getMeanVariance4UserBasics(jsonArray, query, query4CosSimilarityRequest, requestId);
			}

			jsonStr = jsonArray.toString();
			Boolean printJson2Log = PropertiesUtil.getInstance().getBooleanProperty("toolkit.visualizer.printJson",
					false);
			if (printJson2Log) {
				Logger.getLogger(MongoPaginationServlet.class).debug("Request Type : " + requestType);
				Logger.getLogger(MongoPaginationServlet.class).debug("Returned String : " + jsonStr);
			}
		} catch (JSONException e) {
			Logger.getLogger(MongoPaginationServlet.class)
					.error("Error in OrganizedBehaviorCampaignVisualizer Servlet.", e);
		} catch (Exception e) {
			Logger.getLogger(MongoPaginationServlet.class)
					.error("Error in OrganizedBehaviorCampaignVisualizer Servlet.", e);
		}

		// return result
		PrintWriter printout = resp.getWriter();
		printout.println(jsonStr);
	}



	private void visualizeUserDailyTweetRatiosInBarChart(JSONArray jsonArray, BasicDBObject query,
			String mongoValueField) throws JSONException {
		List<String> limits = new ArrayList<>();
		limits.add("0-0.9");
		limits.add("1");
		limits.add("2");
		limits.add("3-5");
		limits.add("6-10");
		limits.add("11-20");
		limits.add("21-50");
		limits.add("51-100");
		limits.add("101-150");
		limits.add("151-200");
		limits.add("201-500");
		limits.add("501-1000");
		limits.add("1001-2000");
		limits.add("2001-...");
		Map<String, Double> rangePercentages = new HashMap<>();
		for (String limit : limits) {
			rangePercentages.put(limit, 0d);
		}

		// get cursor
		DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query);
		// get objects from cursor
		int userCount = 0;
		while (paginatedResult.hasNext()) {
			userCount++;
			DBObject next = paginatedResult.next();
			double userDailyPostCountRatio = (double) next.get(mongoValueField);
			if (userDailyPostCountRatio < 1d) {
				userDailyPostCountRatio = NumberUtils.roundDouble(1, userDailyPostCountRatio);
			} else {
				userDailyPostCountRatio = NumberUtils.roundDouble(0, userDailyPostCountRatio);
			}

			CollectionUtil.findGenericRange(limits, rangePercentages, userDailyPostCountRatio);
		}
		CollectionUtil.calculatePercentage(rangePercentages, userCount);
		for (String limit : limits) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("ratio", limit);
			jsonObject.put("percentage", rangePercentages.get(limit));
			jsonArray.put(jsonObject);
		}
	}

	private void visualizeUserTweetEntityRatiosInBarChart(JSONArray jsonArray, BasicDBObject query,
			int userPostCountWithHashtag) throws JSONException {
		Map<String, Map<String, Double>> ratioValues = new HashMap<>();
		// define limits
		List<String> limits = new ArrayList<>();
		limits.add("0");
		limits.add("0.0001-0.5");
		limits.add("0.6-0.9");
		for (int i = 1; i <= 20; i++) {
			limits.add(String.valueOf(i));

		}
		limits.add("21-...");
		// init hash map
		for (String limit : limits) {
			// range percentages
			Map<String, Double> rangePercentages = new HashMap<>();
			rangePercentages.put(MongoCollectionFieldNames.MONGO_URL_RATIO, 0d);
			rangePercentages.put(MongoCollectionFieldNames.MONGO_HASHTAG_RATIO, 0d);
			rangePercentages.put(MongoCollectionFieldNames.MONGO_MENTION_RATIO, 0d);
			rangePercentages.put(MongoCollectionFieldNames.MONGO_MEDIA_RATIO, 0d);
			// add to ratio values
			ratioValues.put(limit, rangePercentages);
		}

		// get cursor
		query.put(MongoCollectionFieldNames.MONGO_USER_HASHTAG_POST_COUNT,
				new BasicDBObject("$gte", userPostCountWithHashtag));
		DBCursor processInputResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query);

		// get objects from cursor
		// get url ratio
		int userCount = 0;
		while (processInputResult.hasNext()) {
			userCount++;
			DBObject next = processInputResult.next();
			// url ratio
			double urlRatio = NumberUtils.roundDouble(1, (double) next.get(MongoCollectionFieldNames.MONGO_URL_RATIO));
			if (urlRatio > 1d) {
				urlRatio = NumberUtils.roundDouble(0, urlRatio);
			}
			// hashtag ratio
			double hashtagRatio = NumberUtils.roundDouble(1,
					(double) next.get(MongoCollectionFieldNames.MONGO_HASHTAG_RATIO));
			if (hashtagRatio > 1d) {
				hashtagRatio = NumberUtils.roundDouble(0, hashtagRatio);
			}
			// mention ratio
			double mentionRatio = NumberUtils.roundDouble(1,
					(double) next.get(MongoCollectionFieldNames.MONGO_MENTION_RATIO));
			if (mentionRatio > 1d) {
				mentionRatio = NumberUtils.roundDouble(0, mentionRatio);
			}
			// media ratio
			double mediaRatio = NumberUtils.roundDouble(1,
					(double) next.get(MongoCollectionFieldNames.MONGO_MEDIA_RATIO));
			if (mediaRatio > 1d) {
				mediaRatio = NumberUtils.roundDouble(0, mediaRatio);
			}

			// get range
			CollectionUtil.findGenericRange(limits, ratioValues, MongoCollectionFieldNames.MONGO_URL_RATIO, urlRatio);
			CollectionUtil.findGenericRange(limits, ratioValues, MongoCollectionFieldNames.MONGO_HASHTAG_RATIO,
					hashtagRatio);
			CollectionUtil.findGenericRange(limits, ratioValues, MongoCollectionFieldNames.MONGO_MENTION_RATIO,
					mentionRatio);
			CollectionUtil.findGenericRange(limits, ratioValues, MongoCollectionFieldNames.MONGO_MEDIA_RATIO,
					mediaRatio);
		}

		CollectionUtil.calculatePercentageForNestedMap(ratioValues, userCount);
		for (String limit : limits) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("ratio", limit);
			jsonObject.put(MongoCollectionFieldNames.MONGO_URL_RATIO,
					ratioValues.get(limit).get(MongoCollectionFieldNames.MONGO_URL_RATIO));
			jsonObject.put(MongoCollectionFieldNames.MONGO_HASHTAG_RATIO,
					ratioValues.get(limit).get(MongoCollectionFieldNames.MONGO_HASHTAG_RATIO));
			jsonObject.put(MongoCollectionFieldNames.MONGO_MENTION_RATIO,
					ratioValues.get(limit).get(MongoCollectionFieldNames.MONGO_MENTION_RATIO));
			jsonObject.put(MongoCollectionFieldNames.MONGO_MEDIA_RATIO,
					ratioValues.get(limit).get(MongoCollectionFieldNames.MONGO_MEDIA_RATIO));
			jsonArray.put(jsonObject);
		}
	}

	private void visualizeUserFriendFollowerRatioInBarChart(JSONArray jsonArray, BasicDBObject query, String requestId,
			int userPostCountWithHashtag) throws JSONException {

		// Collection initialization
		List<String> limits = new ArrayList<>();
		limits.add("0");
		for (int i = 1; i <= 9; i++) {
			limits.add("0." + i);
		}
		limits.add("1");
		Map<Double, Double> ratioPercentages = new HashMap<>();
		for (String limit : limits) {
			ratioPercentages.put(Double.valueOf(limit), 0d);
		}

		if (userPostCountWithHashtag > 0) {
			// get cursor
			query.put(MongoCollectionFieldNames.MONGO_USER_HASHTAG_POST_COUNT,
					new BasicDBObject("$gte", userPostCountWithHashtag));
		}
		DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query);

		// get objects from cursor
		int userCount = 0;

		while (paginatedResult.hasNext()) {
			userCount++;
			DBObject next = paginatedResult.next();
			double friendFolloweRatio = NumberUtils.roundDouble(1,
					(double) next.get(MongoCollectionFieldNames.MONGO_USER_FRIEND_FOLLOWER_RATIO));
			CollectionUtil.incrementKeyValueInMap(ratioPercentages, friendFolloweRatio);
		}
		CollectionUtil.calculatePercentage(ratioPercentages, userCount);
		ratioPercentages = CollectionUtil.sortByComparator4Key(ratioPercentages);

		for (Entry<Double, Double> entry : ratioPercentages.entrySet()) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("ratio", entry.getKey());
			jsonObject.put("percentage", entry.getValue());
			jsonArray.put(jsonObject);
		}

	}

	private void getMeanVariance4UserBasics(JSONArray jsonArray, BasicDBObject query,
			BasicDBObject query4CosSimilarityRequest, String requestId) throws Exception {
		Logger.getLogger(MongoPaginationServlet.class)
				.debug("getMeanVariance4UserBasics is started for requestId : " + requestId);

		// get user ratios
		DBCollection orgBehaviourProcessInputData = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData();
		getUserCreationTimeMeanVariance(jsonArray, query, requestId, orgBehaviourProcessInputData,
				MongoCollectionFieldNames.MONGO_USER_CREATION_DATE_IN_RATA_DIE);
		getFriendFollowerRatioMeanVariances(jsonArray, query, requestId, orgBehaviourProcessInputData, "");
		getRougtTweetCountsMeanVariance(jsonArray, query, requestId, orgBehaviourProcessInputData, "");

		DBObject userDailyAvaragePostCount = getMeanVariance(orgBehaviourProcessInputData, query, requestId,
				MongoCollectionFieldNames.MONGO_USER_DAILY_AVARAGE_POST_COUNT, "USER");
		jsonArray.put(userDailyAvaragePostCount.toMap());

		Logger.getLogger(MongoPaginationServlet.class)
				.debug("getMeanVariance4UserBasics is finished for requestId : " + requestId);

	}

	private void getMeanVariance4All(JSONArray jsonArray, BasicDBObject query, BasicDBObject query4CosSimilarityRequest,
			String requestId) throws Exception {

		Logger.getLogger(MongoPaginationServlet.class)
				.debug("getMeanVariance4All is started for requestId : " + requestId);

		// get user ratios
		DBCollection orgBehaviourProcessInputData = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData();

		getUserRatioMeanVariances(jsonArray, query, requestId, orgBehaviourProcessInputData, "");
		getUserRatioMeanVariances(jsonArray, query, requestId, orgBehaviourProcessInputData, "_2");
		getUserRatioMeanVariances(jsonArray, query, requestId, orgBehaviourProcessInputData, "_10");
		getUserRatioMeanVariances(jsonArray, query, requestId, orgBehaviourProcessInputData, "_50");

		getFriendFollowerRatioMeanVariances(jsonArray, query, requestId, orgBehaviourProcessInputData, "");
		getFriendFollowerRatioMeanVariances(jsonArray, query, requestId, orgBehaviourProcessInputData, "_2");
		getFriendFollowerRatioMeanVariances(jsonArray, query, requestId, orgBehaviourProcessInputData, "_10");
		getFriendFollowerRatioMeanVariances(jsonArray, query, requestId, orgBehaviourProcessInputData, "_50");

		getRougtTweetCountsMeanVariance(jsonArray, query, requestId, orgBehaviourProcessInputData, "");
		getRougtTweetCountsMeanVariance(jsonArray, query, requestId, orgBehaviourProcessInputData, "_2");
		getRougtTweetCountsMeanVariance(jsonArray, query, requestId, orgBehaviourProcessInputData, "_10");
		getRougtTweetCountsMeanVariance(jsonArray, query, requestId, orgBehaviourProcessInputData, "_50");

		getUserCreationTimeMeanVariance(jsonArray, query, requestId, orgBehaviourProcessInputData,
				MongoCollectionFieldNames.MONGO_USER_CREATION_DATE_IN_RATA_DIE);
		getUserCreationTimeMeanVariance(jsonArray, query, requestId, orgBehaviourProcessInputData,
				MongoCollectionFieldNames.MONGO_USER_CREATION_DATE_IN_RATA_DIE + "_2");
		getUserCreationTimeMeanVariance(jsonArray, query, requestId, orgBehaviourProcessInputData,
				MongoCollectionFieldNames.MONGO_USER_CREATION_DATE_IN_RATA_DIE + "_10");
		getUserCreationTimeMeanVariance(jsonArray, query, requestId, orgBehaviourProcessInputData,
				MongoCollectionFieldNames.MONGO_USER_CREATION_DATE_IN_RATA_DIE + "_50");

		DBObject userHashtagPostCountMeanVariance = getMeanVariance(orgBehaviourProcessInputData, query, requestId,
				MongoCollectionFieldNames.MONGO_USER_HASHTAG_POST_COUNT, "USER");
		jsonArray.put(userHashtagPostCountMeanVariance.toMap());

		DBObject userDailyAvaragePostCount = getMeanVariance(orgBehaviourProcessInputData, query, requestId,
				MongoCollectionFieldNames.MONGO_USER_DAILY_AVARAGE_POST_COUNT, "USER");
		DBObject userDailyAvaragePostCount4HashtagDays = getMeanVariance(orgBehaviourProcessInputData, query, requestId,
				MongoCollectionFieldNames.MONGO_USER_TWEET_AVERAGE_HASHTAG_DAYS, "USER");
		DBObject userDailyAvaragePostCountRatios = getMeanVariance(orgBehaviourProcessInputData, query, requestId,
				MongoCollectionFieldNames.MONGO_USER_HASHTAG_DAY_AVARAGE_DAY_POST_COUNT_RATIO, "USER");

		jsonArray.put(userDailyAvaragePostCount.toMap());
		jsonArray.put(userDailyAvaragePostCount4HashtagDays.toMap());
		jsonArray.put(userDailyAvaragePostCountRatios.toMap());

		// get cos similarity ratios
		DBCollection orgBehaviourRequestedSimilarityCalculations = DirenajMongoDriver.getInstance()
				.getOrgBehaviourRequestedSimilarityCalculations();

		DBObject hourlyTweetHashtagRatioMeanVariance = getMeanVariance(orgBehaviourRequestedSimilarityCalculations,
				query4CosSimilarityRequest, requestId, MongoCollectionFieldNames.MONGO_HASHTAG_RATIO, "COS_SIM");
		DBObject hourlyTweetMentionRatioMeanVariance = getMeanVariance(orgBehaviourRequestedSimilarityCalculations,
				query4CosSimilarityRequest, requestId, MongoCollectionFieldNames.MONGO_MENTION_RATIO, "COS_SIM");
		DBObject hourlyTweetUrlRatioMeanVariance = getMeanVariance(orgBehaviourRequestedSimilarityCalculations,
				query4CosSimilarityRequest, requestId, MongoCollectionFieldNames.MONGO_URL_RATIO, "COS_SIM");
		DBObject hourlyMediaRatioMeanVariance = getMeanVariance(orgBehaviourRequestedSimilarityCalculations,
				query4CosSimilarityRequest, requestId, MongoCollectionFieldNames.MONGO_MEDIA_RATIO, "COS_SIM");
		DBObject hourlyRetweetRatioMeanVariance = getMeanVariance(orgBehaviourRequestedSimilarityCalculations,
				query4CosSimilarityRequest, requestId, MongoCollectionFieldNames.MONGO_RETWEET_RATIO, "COS_SIM");
		DBObject hourlyTweetUserCountRatioMeanVariance = getMeanVariance(orgBehaviourRequestedSimilarityCalculations,
				query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT_DISTINCT_USER_RATIO, "COS_SIM");

		DBObject hourlyRetweetUserCountRatioMeanVariance = getMeanVariance(orgBehaviourRequestedSimilarityCalculations,
				query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.MONGO_DISTINCT_RETWEET_USER_DIVIDED_BY_RATIO, "COS_SIM");
		DBObject hourlyNonRetweetUserCountRatioMeanVariance = getMeanVariance(
				orgBehaviourRequestedSimilarityCalculations, query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.MONGO_DISTINCT_NON_RETWEET_USER_DIVIDED_BY_RATIO, "COS_SIM");
		DBObject totalRetweetCountDistinctRetweetCountRatio = getMeanVariance(
				orgBehaviourRequestedSimilarityCalculations, query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.MONGO_TOTAL_RETWEET_COUNT_DISTINCT_RETWEET_COUNT_RATIO, "COS_SIM");
		DBObject distinctRetweetRatio = getMeanVariance(orgBehaviourRequestedSimilarityCalculations,
				query4CosSimilarityRequest, requestId, MongoCollectionFieldNames.MONGO_DISTINCT_RETWEET_RATIO,
				"COS_SIM");
		DBObject distinctRetweetUserRatio = getMeanVariance(orgBehaviourRequestedSimilarityCalculations,
				query4CosSimilarityRequest, requestId, MongoCollectionFieldNames.MONGO_DISTINCT_RETWEET_USER_RATIO,
				"COS_SIM");

		DBObject totalDistinctMentionRatio = getMeanVariance(orgBehaviourRequestedSimilarityCalculations,
				query4CosSimilarityRequest, requestId, MongoCollectionFieldNames.MONGO_TOTAL_DISTINCT_MENTION_RATIO,
				"COS_SIM");
		DBObject retweetedTotalDistinctMentionRatio = getMeanVariance(orgBehaviourRequestedSimilarityCalculations,
				query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.MONGO_RETWEETED_MENTION_DISTINCT_MENTION_RATIO, "COS_SIM");
		DBObject nonRetweetedTotalDistinctMentionRatio = getMeanVariance(orgBehaviourRequestedSimilarityCalculations,
				query4CosSimilarityRequest, requestId,
				MongoCollectionFieldNames.MONGO_NON_RETWEETED_MENTION_DISTINCT_MENTION_RATIO, "COS_SIM");

		// tweet similarity
		// DBObject hourlyMostSimilarTweetsRatioMeanVariance =
		// getMeanVariance(orgBehaviourRequestedSimilarityCalculations,
		// query4CosSimilarityRequest, requestId,
		// MongoCollectionFieldNames.MOST_SIMILAR, "COS_SIM");
		// DBObject hourlyVerySimilarTweetsRatioMeanVariance =
		// getMeanVariance(orgBehaviourRequestedSimilarityCalculations,
		// query4CosSimilarityRequest, requestId,
		// MongoCollectionFieldNames.VERY_SIMILAR, "COS_SIM");
		// DBObject hourlySimilarTweetsRatioMeanVariance =
		// getMeanVariance(orgBehaviourRequestedSimilarityCalculations,
		// query4CosSimilarityRequest, requestId,
		// MongoCollectionFieldNames.SIMILAR, "COS_SIM");
		// DBObject hourlySlightlySimilarTweetsRatioMeanVariance =
		// getMeanVariance(
		// orgBehaviourRequestedSimilarityCalculations,
		// query4CosSimilarityRequest, requestId,
		// MongoCollectionFieldNames.SLIGHTLY_SIMILAR, "COS_SIM");
		// DBObject hourlyNonSimilarTweetsRatioMeanVariance =
		// getMeanVariance(orgBehaviourRequestedSimilarityCalculations,
		// query4CosSimilarityRequest, requestId,
		// MongoCollectionFieldNames.NON_SIMILAR, "COS_SIM");

		jsonArray.put(hourlyTweetHashtagRatioMeanVariance.toMap());
		jsonArray.put(hourlyTweetMentionRatioMeanVariance.toMap());
		jsonArray.put(hourlyTweetUrlRatioMeanVariance.toMap());
		jsonArray.put(hourlyMediaRatioMeanVariance.toMap());
		jsonArray.put(hourlyRetweetRatioMeanVariance.toMap());
		jsonArray.put(hourlyTweetUserCountRatioMeanVariance.toMap());
		jsonArray.put(hourlyRetweetUserCountRatioMeanVariance.toMap());
		jsonArray.put(hourlyNonRetweetUserCountRatioMeanVariance.toMap());
		jsonArray.put(totalRetweetCountDistinctRetweetCountRatio.toMap());
		jsonArray.put(distinctRetweetRatio.toMap());
		jsonArray.put(distinctRetweetUserRatio.toMap());

		jsonArray.put(totalDistinctMentionRatio.toMap());
		jsonArray.put(retweetedTotalDistinctMentionRatio.toMap());
		jsonArray.put(nonRetweetedTotalDistinctMentionRatio.toMap());

		Logger.getLogger(MongoPaginationServlet.class)
				.debug("getMeanVariance4All is finished for requestId : " + requestId);

	}

	private void getRougtTweetCountsMeanVariance(JSONArray jsonArray, BasicDBObject query, String requestId,
			DBCollection orgBehaviourProcessInputData, String userPostCount) {

		try {
			DBObject userStatusCountMeanVariance = getMeanVariance(orgBehaviourProcessInputData, query, requestId,
					MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT + userPostCount, "USER");
			DBObject userFavoriteCountMeanVariance = getMeanVariance(orgBehaviourProcessInputData, query, requestId,
					MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT + userPostCount, "USER");
			jsonArray.put(userStatusCountMeanVariance.toMap());
			jsonArray.put(userFavoriteCountMeanVariance.toMap());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void getFriendFollowerRatioMeanVariances(JSONArray jsonArray, BasicDBObject query, String requestId,
			DBCollection orgBehaviourProcessInputData, String userPostCount) {
		try {
			DBObject friendFollowerRatioMeanVariance = getMeanVariance(orgBehaviourProcessInputData, query, requestId,
					MongoCollectionFieldNames.MONGO_USER_FRIEND_FOLLOWER_RATIO + userPostCount, "USER");
			jsonArray.put(friendFollowerRatioMeanVariance.toMap());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			jsonArray.put(new BasicDBObject().toMap());
		}
	}

	private void getUserRatioMeanVariances(JSONArray jsonArray, BasicDBObject query, String requestId,
			DBCollection orgBehaviourProcessInputData, String userPostCount) {
		try {
			DBObject hashtagRatioMeanVariance = getMeanVariance(orgBehaviourProcessInputData, query, requestId,
					MongoCollectionFieldNames.MONGO_HASHTAG_RATIO + userPostCount, "USER");
			DBObject urlRatioMeanVariance = getMeanVariance(orgBehaviourProcessInputData, query, requestId,
					MongoCollectionFieldNames.MONGO_URL_RATIO + userPostCount, "USER");
			DBObject mentionRatioMeanVariance = getMeanVariance(orgBehaviourProcessInputData, query, requestId,
					MongoCollectionFieldNames.MONGO_MENTION_RATIO + userPostCount, "USER");
			DBObject mediaRatioMeanVariance = getMeanVariance(orgBehaviourProcessInputData, query, requestId,
					MongoCollectionFieldNames.MONGO_MEDIA_RATIO + userPostCount, "USER");

			jsonArray.put(hashtagRatioMeanVariance.toMap());
			jsonArray.put(urlRatioMeanVariance.toMap());
			jsonArray.put(mentionRatioMeanVariance.toMap());
			jsonArray.put(mediaRatioMeanVariance.toMap());
		} catch (Exception e) {

		}
	}

	private void getUserCreationTimeMeanVariance(JSONArray jsonArray, DBObject query, String requestId,
			DBCollection orgBehaviourProcessInputData, String calculationtype) throws Exception {
		try {
			DBObject userCreationDateMeanVariance = getMeanVariance(orgBehaviourProcessInputData, query, requestId,
					calculationtype, "USER");

			String averageCreationDateStr = String.valueOf(userCreationDateMeanVariance.get("average"));
			String minCreationDateStr = String.valueOf(userCreationDateMeanVariance.get("min"));
			String maxCreationDateStr = String.valueOf(userCreationDateMeanVariance.get("max"));

			Date averageCreationDate = DateTimeUtils.getUTCDateFromRataDieFormat(averageCreationDateStr);
			Date minCreationDate = DateTimeUtils.getUTCDateFromRataDieFormat(minCreationDateStr);
			Date maxCreationDate = DateTimeUtils.getUTCDateFromRataDieFormat(maxCreationDateStr);

			userCreationDateMeanVariance.put("average",
					DateTimeUtils.getUTCDateTimeStringInGenericFormat(averageCreationDate));
			userCreationDateMeanVariance.put("min", DateTimeUtils.getUTCDateTimeStringInGenericFormat(minCreationDate));
			userCreationDateMeanVariance.put("max", DateTimeUtils.getUTCDateTimeStringInGenericFormat(maxCreationDate));
			jsonArray.put(userCreationDateMeanVariance.toMap());
		} catch (Exception e) {

		}
	}

	/**
	 * 
	 * DBObject meanVarianceResult = getMeanVariance(query, requestId,
	 * MongoCollectionFieldNames.MONGO_USER_FRIEND_FOLLOWER_RATIO);
	 * 
	 * @param query
	 * @param requestId
	 * @param calculationType
	 * @return
	 */
	private DBObject getMeanVariance(DBCollection collection, DBObject query, String requestId, String calculationType,
			String calculationDomain) {
		DBObject meanVarianceResult = new BasicDBObject();
		try {
			Logger.getLogger(MongoPaginationServlet.class)
					.debug("getMeanVariance is executed for requestId : " + requestId + ". Calculation Type : "
							+ calculationType + ". CalculationDomain: " + calculationDomain);

			DBObject calculationQuery = new BasicDBObject("requestId", requestId)
					.append("calculationType", calculationType).append("calculationDomain", calculationDomain);
			meanVarianceResult = DirenajMongoDriver.getInstance().getOrgBehaviourRequestMeanVarianceCalculations()
					.findOne(calculationQuery);
		} catch (Exception e) {
			Logger.getLogger(MongoPaginationServlet.class).error("getMeanVariance got for requestId : " + requestId
					+ ". Calculation Type : " + calculationType + ". CalculationDomain: " + calculationDomain, e);
		}
		return meanVarianceResult;
	}

	private void visualizeUserCreationTimesInBarChart(JSONArray jsonArray, BasicDBObject query,
			int userPostCountWithHashtag) throws Exception, JSONException {

		Map<String, Double> usersByDate = getUserCreationTimePercentageData(query);
		Map<String, Double> usersOfMultipleHashtagByDate = null;
		if (userPostCountWithHashtag > 0) {
			// get cursor
			query.put(MongoCollectionFieldNames.MONGO_USER_HASHTAG_POST_COUNT,
					new BasicDBObject("$gte", userPostCountWithHashtag));
			usersOfMultipleHashtagByDate = getUserCreationTimePercentageData(query);
		}
		if (userPostCountWithHashtag == 0) {
			for (Entry<String, Double> entry : usersByDate.entrySet()) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("creationDate", entry.getKey());
				jsonObject.put("percentage", entry.getValue());
				jsonArray.put(jsonObject);
			}
		} else if (userPostCountWithHashtag > 0) {
			for (Entry<String, Double> entry : usersByDate.entrySet()) {
				JSONObject jsonObject = new JSONObject();
				String dateInStr = entry.getKey();
				jsonObject.put("creationDate", dateInStr);
				jsonObject.put("percentage", usersOfMultipleHashtagByDate.get(dateInStr));
				jsonArray.put(jsonObject);
			}
		}

	}

	private Map<String, Double> getUserCreationTimePercentageData(BasicDBObject query) throws Exception {
		DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query);
		Map<String, Double> usersByDate = new HashMap<>();
		// get objects from cursor
		int userCount = 0;
		try {
			while (paginatedResult.hasNext()) {
				DBObject next = paginatedResult.next();
				userCount++;
				String twitterDateStr = (String) next.get(MongoCollectionFieldNames.MONGO_USER_CREATION_DATE);
				String userCreationDate = DateTimeUtils.getStringOfDate("yyyyMM",
						DateTimeUtils.getTwitterDate(twitterDateStr));
				CollectionUtil.incrementKeyValueInMap(usersByDate, userCreationDate);
			}
		} finally {
			paginatedResult.close();
		}
		CollectionUtil.calculatePercentage(usersByDate, userCount);
		usersByDate = CollectionUtil.sortByComparator4DateKey(usersByDate);
		return usersByDate;
	}

	private void visualizeHourlyTweetSimilarities(JSONArray jsonArray, BasicDBObject query4CosSimilarityRequest)
			throws Exception, JSONException {
		DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations()
				.find(query4CosSimilarityRequest)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_COS_SIM_REQ_RATA_DIE_LOWER_TIME, 1));
		// get objects from cursor
		while (paginatedResult.hasNext()) {
			DBObject next = paginatedResult.next();
			// prepare json object
			String twitterDateStr = (String) next.get("lowerTimeInterval");
			String twitterDate = DateTimeUtils.getStringOfDate("yyyyMMdd HH:mm",
					DateTimeUtils.getUTCDateTime(DateTimeUtils.getTwitterDate(twitterDateStr)));

			jsonArray.put(new JSONObject().put("time", twitterDate)
					.put(MongoCollectionFieldNames.NON_SIMILAR + " (90 Degree)",
							NumberUtils.roundDouble(4, (double) next.get(MongoCollectionFieldNames.NON_SIMILAR) * 100d,
									100d))
					.put(MongoCollectionFieldNames.SLIGHTLY_SIMILAR + " (60-89 Degree)",
							NumberUtils.roundDouble(4,
									(double) next.get(MongoCollectionFieldNames.SLIGHTLY_SIMILAR) * 100d, 100d))
					.put(MongoCollectionFieldNames.SIMILAR + " (60 Degree)",
							NumberUtils.roundDouble(4, (double) next.get(MongoCollectionFieldNames.SIMILAR) * 100d,
									100d))
					.put(MongoCollectionFieldNames.VERY_SIMILAR + " (30-45 Degree)",
							NumberUtils.roundDouble(4, (double) next.get(MongoCollectionFieldNames.VERY_SIMILAR) * 100d,
									100d))
					.put(MongoCollectionFieldNames.MOST_SIMILAR + " (0-30 Degree)", NumberUtils.roundDouble(4,
							(double) next.get(MongoCollectionFieldNames.MOST_SIMILAR) * 100d, 100d)));
		}
	}

	private void visualizeHourlyUserAndTweetCount(JSONArray jsonArray, BasicDBObject query4CosSimilarityRequest,
			String firstValueType, String secondValueType) throws Exception, JSONException {
		DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations()
				.find(query4CosSimilarityRequest)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_COS_SIM_REQ_RATA_DIE_LOWER_TIME, 1));
		JSONArray valuesOfFirstValueType = new JSONArray();
		JSONArray valuesOfSecondValueType = new JSONArray();
		// get objects from cursor
		while (paginatedResult.hasNext()) {
			DBObject next = paginatedResult.next();
			// prepare json object
			String twitterDateStr = (String) next.get("lowerTimeInterval");
			String twitterDate = DateTimeUtils.getStringOfDate("yyyyMMdd HH:mm",
					DateTimeUtils.getUTCDateTime(DateTimeUtils.getTwitterDate(twitterDateStr)));
			valuesOfFirstValueType
					.put(new JSONObject().put("time", twitterDate).put("value", next.get(firstValueType)));
			// prepare json object
			valuesOfSecondValueType
					.put(new JSONObject().put("time", twitterDate).put("value", next.get(secondValueType)));

		}
		// init to array
		jsonArray.put(new JSONObject().put("valueType", firstValueType).put("values", valuesOfFirstValueType));
		jsonArray.put(new JSONObject().put("valueType", secondValueType).put("values", valuesOfSecondValueType));
	}

	private void visualizeHourlyTweetRatios(JSONArray jsonArray, BasicDBObject query4CosSimilarityRequest)
			throws Exception, JSONException {
		JSONArray retweetRatioJsonArray = new JSONArray();
		JSONArray distinctRetweetRatioJsonArray = new JSONArray();
		JSONArray distinctRetweetUserRatioJsonArray = new JSONArray();

		DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations()
				.find(query4CosSimilarityRequest)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_COS_SIM_REQ_RATA_DIE_LOWER_TIME, 1));
		// get objects from cursor
		while (paginatedResult.hasNext()) {
			DBObject next = paginatedResult.next();
			// prepare json object
			String twitterDateStr = (String) next.get("lowerTimeInterval");
			double retweetRatio = NumberUtils.roundDouble(4,
					(double) next.get(MongoCollectionFieldNames.MONGO_RETWEET_RATIO), 1) * 100d;
			double distinctRetweetRatio = NumberUtils.roundDouble(4,
					(double) next.get(MongoCollectionFieldNames.MONGO_DISTINCT_RETWEET_RATIO), 1) * 100d;
			double distinctRetweetUsertRatio = NumberUtils.roundDouble(4,
					(double) next.get(MongoCollectionFieldNames.MONGO_DISTINCT_RETWEET_USER_RATIO), 1) * 100d;

			String twitterDate = DateTimeUtils.getStringOfDate("yyyyMMdd HH:mm",
					DateTimeUtils.getUTCDateTime(DateTimeUtils.getTwitterDate(twitterDateStr)));

			retweetRatioJsonArray.put(new JSONObject().put("time", twitterDate).put("value", retweetRatio));
			distinctRetweetRatioJsonArray
					.put(new JSONObject().put("time", twitterDate).put("value", distinctRetweetRatio));
			distinctRetweetUserRatioJsonArray
					.put(new JSONObject().put("time", twitterDate).put("value", distinctRetweetUsertRatio));
		}
		// init to array
		jsonArray.put(new JSONObject().put("valueType", MongoCollectionFieldNames.MONGO_RETWEET_RATIO).put("values",
				retweetRatioJsonArray));
		jsonArray.put(new JSONObject().put("valueType", MongoCollectionFieldNames.MONGO_DISTINCT_RETWEET_RATIO)
				.put("values", distinctRetweetRatioJsonArray));
		jsonArray.put(new JSONObject().put("valueType", MongoCollectionFieldNames.MONGO_DISTINCT_RETWEET_USER_RATIO)
				.put("values", distinctRetweetUserRatioJsonArray));

	}

	private void visualizeHourlyEntityRatios(JSONArray jsonArray, BasicDBObject query4CosSimilarityRequest)
			throws Exception, JSONException {
		JSONArray urlRatioJsonArray = new JSONArray();
		JSONArray hashtagRatioJsonArray = new JSONArray();
		JSONArray mentionRatioJsonArray = new JSONArray();
		JSONArray mediaRatioJsonArray = new JSONArray();
		DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations()
				.find(query4CosSimilarityRequest)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_COS_SIM_REQ_RATA_DIE_LOWER_TIME, 1));
		// get objects from cursor
		while (paginatedResult.hasNext()) {
			DBObject next = paginatedResult.next();
			// prepare json object
			String twitterDateStr = (String) next.get("lowerTimeInterval");
			double urlRatio = (double) next.get(MongoCollectionFieldNames.MONGO_URL_RATIO);
			double hashtagRatio = (double) next.get(MongoCollectionFieldNames.MONGO_HASHTAG_RATIO);
			double mentionRatio = (double) next.get(MongoCollectionFieldNames.MONGO_MENTION_RATIO);
			double mediaRatio = (double) next.get(MongoCollectionFieldNames.MONGO_MEDIA_RATIO);

			String twitterDate = DateTimeUtils.getStringOfDate("yyyyMMdd HH:mm",
					DateTimeUtils.getUTCDateTime(DateTimeUtils.getTwitterDate(twitterDateStr)));

			urlRatioJsonArray.put(new JSONObject().put("time", twitterDate).put("value", urlRatio));
			hashtagRatioJsonArray.put(new JSONObject().put("time", twitterDate).put("value", hashtagRatio));
			mentionRatioJsonArray.put(new JSONObject().put("time", twitterDate).put("value", mentionRatio));
			mediaRatioJsonArray.put(new JSONObject().put("time", twitterDate).put("value", mediaRatio));
		}
		// init to array
		jsonArray.put(new JSONObject().put("valueType", MongoCollectionFieldNames.MONGO_URL_RATIO).put("values",
				urlRatioJsonArray));
		jsonArray.put(new JSONObject().put("valueType", MongoCollectionFieldNames.MONGO_HASHTAG_RATIO).put("values",
				hashtagRatioJsonArray));
		jsonArray.put(new JSONObject().put("valueType", MongoCollectionFieldNames.MONGO_MENTION_RATIO).put("values",
				mentionRatioJsonArray));
		jsonArray.put(new JSONObject().put("valueType", MongoCollectionFieldNames.MONGO_MEDIA_RATIO).put("values",
				mediaRatioJsonArray));

	}

	private void visualizeUserRoughTweetCountsInBarChart(JSONArray jsonArray, BasicDBObject query,
			int userPostCountWithHashtag) throws JSONException {

		Map<String, Map<String, Double>> ratioValues = new HashMap<>();
		// define limits
		List<String> limits = new ArrayList<>();
		// limits between 0 - 1000
		limits.add("0");
		int previous = 1;
		for (int i = 100; i <= 1000; i = i + 100) {
			limits.add(previous + "-" + i);
			previous = i + 1;
		}
		// limits between 1000 - 10000
		previous = 1001;
		for (int i = 2000; i <= 10000; i = i + 1000) {
			limits.add(previous + "-" + i);
			previous = i + 1;
		}
		limits.add("10000-50000");
		limits.add("50001-100000");
		// limits between 100.000 - 1.000.000
		previous = 100001;
		for (int i = 200000; i <= 1000000; i = i + 100000) {
			limits.add(previous + "-" + i);
			previous = i + 1;
		}
		limits.add("1000001-...");

		// init hash map
		for (String limit : limits) {
			// range percentages
			Map<String, Double> rangePercentages = new HashMap<>();
			rangePercentages.put(MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT, 0d);
			rangePercentages.put(MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT, 0d);
			// add to ratio values
			ratioValues.put(limit, rangePercentages);
		}

		// get cursor
		// get cursor
		if (userPostCountWithHashtag > 0) {
			query.put(MongoCollectionFieldNames.MONGO_USER_HASHTAG_POST_COUNT,
					new BasicDBObject("$gte", userPostCountWithHashtag));
		}
		DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query);
		// get objects from cursor
		int userCount = 0;
		while (paginatedResult.hasNext()) {
			userCount++;
			DBObject next = paginatedResult.next();
			double favoriteStatusCount = (double) next.get(MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT);
			double statusCount = (double) next.get(MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT);
			CollectionUtil.findGenericRange(limits, ratioValues, MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT,
					favoriteStatusCount);
			CollectionUtil.findGenericRange(limits, ratioValues, MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT,
					statusCount);
		}

		CollectionUtil.calculatePercentageForNestedMap(ratioValues, userCount);
		for (String limit : limits) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("ratio", limit);
			jsonObject.put(MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT,
					ratioValues.get(limit).get(MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT));
			jsonObject.put(MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT,
					ratioValues.get(limit).get(MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT));
			jsonArray.put(jsonObject);
		}

	}

	private void visualizeUserPostDeviceRatios(JSONArray jsonArray, BasicDBObject query) throws JSONException {
		// get cursor
		DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_POST_TWITTER_DEVICE_RATIO, 1));
		JSONArray twitterPostDeviceRatioJsonArray = new JSONArray();
		// get objects from cursor
		int userNo = 0;
		while (paginatedResult.hasNext()) {
			DBObject next = paginatedResult.next();
			userNo++;
			// prepare json object
			twitterPostDeviceRatioJsonArray
					.put(new JSONObject()
							.put("ratioValue",
									NumberUtils.roundDouble(4,
											(double) next
													.get(MongoCollectionFieldNames.MONGO_USER_POST_TWITTER_DEVICE_RATIO)))
					.put("userSequenceNo", userNo));

		}
		paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_POST_MOBILE_DEVICE_RATIO, 1));
		JSONArray mobilePostDeviceRatioJsonArray = new JSONArray();
		// get objects from cursor
		userNo = 0;
		while (paginatedResult.hasNext()) {
			DBObject next = paginatedResult.next();
			userNo++;
			// prepare json object
			mobilePostDeviceRatioJsonArray
					.put(new JSONObject()
							.put("ratioValue",
									NumberUtils.roundDouble(4,
											(double) next
													.get(MongoCollectionFieldNames.MONGO_USER_POST_MOBILE_DEVICE_RATIO)))
					.put("userSequenceNo", userNo));

		}
		paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_THIRD_PARTY_DEVICE_RATIO, 1));
		JSONArray thirdPartyPostRatioJsonArray = new JSONArray();
		// get objects from cursor
		userNo = 0;
		while (paginatedResult.hasNext()) {
			DBObject next = paginatedResult.next();
			userNo++;
			// prepare json object
			thirdPartyPostRatioJsonArray
					.put(new JSONObject()
							.put("ratioValue",
									NumberUtils.roundDouble(4,
											(double) next
													.get(MongoCollectionFieldNames.MONGO_USER_THIRD_PARTY_DEVICE_RATIO)))
					.put("userSequenceNo", userNo));

		}

		// init to array
		jsonArray.put(new JSONObject().put("ratioType", MongoCollectionFieldNames.MONGO_USER_POST_TWITTER_DEVICE_RATIO)
				.put("values", twitterPostDeviceRatioJsonArray));
		jsonArray.put(new JSONObject().put("ratioType", MongoCollectionFieldNames.MONGO_USER_POST_MOBILE_DEVICE_RATIO)
				.put("values", mobilePostDeviceRatioJsonArray));
		jsonArray.put(new JSONObject().put("ratioType", MongoCollectionFieldNames.MONGO_USER_THIRD_PARTY_DEVICE_RATIO)
				.put("values", thirdPartyPostRatioJsonArray));
	}

	/**
	 * @deprecated
	 * 
	 * @param jsonArray
	 * @param query
	 * @throws JSONException
	 */
	private void visualizeUserRoughHashtagTweetCounts(JSONArray jsonArray, BasicDBObject query) throws JSONException {
		// get cursor
		DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_HASHTAG_POST_COUNT, 1));
		// get objects from cursor
		int userNo = 0;
		while (paginatedResult.hasNext()) {
			DBObject next = paginatedResult.next();
			userNo++;
			// prepare json object
			JSONObject userProcessInputData = new JSONObject();
			userProcessInputData.put("userSequenceNo", userNo);
			userProcessInputData.put(MongoCollectionFieldNames.MONGO_USER_ID,
					TextUtils.getLongValue((String) next.get(MongoCollectionFieldNames.MONGO_USER_ID)));
			// get ratios
			userProcessInputData.put("ratioValue", NumberUtils.roundDouble(4,
					(double) next.get(MongoCollectionFieldNames.MONGO_USER_HASHTAG_POST_COUNT)));
			// init to array
			jsonArray.put(userProcessInputData);
		}
	}

	/**
	 * @deprecated
	 * 
	 * @param jsonArray
	 * @param query
	 * @throws JSONException
	 */
	private void visualizeUserFriendFollowerRatio(JSONArray jsonArray, BasicDBObject query) throws JSONException {
		// get cursor
		DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_FRIEND_FOLLOWER_RATIO, 1));
		// get objects from cursor
		int userNo = 0;
		while (paginatedResult.hasNext()) {
			DBObject next = paginatedResult.next();
			userNo++;
			// prepare json object
			JSONObject userProcessInputData = new JSONObject();
			userProcessInputData.put("userSequenceNo", userNo);
			userProcessInputData.put(MongoCollectionFieldNames.MONGO_USER_ID,
					TextUtils.getLongValue((String) next.get(MongoCollectionFieldNames.MONGO_USER_ID)));
			// get ratios
			userProcessInputData.put("ratioValue", NumberUtils.roundDouble(4,
					(double) next.get(MongoCollectionFieldNames.MONGO_USER_FRIEND_FOLLOWER_RATIO)));
			// init to array
			jsonArray.put(userProcessInputData);
		}
	}

	/**
	 * 
	 * @deprecated
	 * 
	 * @param jsonArray
	 * @param query
	 * @throws JSONException
	 */
	private void visualizeUserTweetEntityRatios(JSONArray jsonArray, BasicDBObject query) throws JSONException {
		// get cursor
		DBCursor urlRatioResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_URL_RATIO, 1));
		JSONArray urlRatioJsonArray = new JSONArray();
		// get objects from cursor
		// get url ratio
		int userNo = 0;
		while (urlRatioResult.hasNext()) {
			DBObject next = urlRatioResult.next();
			userNo++;
			urlRatioJsonArray
					.put(new JSONObject()
							.put("ratioValue",
									NumberUtils.roundDouble(4,
											(double) next.get(MongoCollectionFieldNames.MONGO_URL_RATIO)))
					.put("userSequenceNo", userNo));
		}
		// hashtag ratio
		DBCursor hashtagRatioResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_HASHTAG_RATIO, 1));
		JSONArray hashtagRatioJsonArray = new JSONArray();
		// get objects from cursor
		userNo = 0;
		while (hashtagRatioResult.hasNext()) {
			DBObject next = hashtagRatioResult.next();
			userNo++;
			hashtagRatioJsonArray.put(new JSONObject()
					.put("ratioValue",
							NumberUtils.roundDouble(4,
									(double) next.get(MongoCollectionFieldNames.MONGO_HASHTAG_RATIO)))
					.put("userSequenceNo", userNo));
		}
		// mention ratio
		DBCursor mentionRatioResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_MENTION_RATIO, 1));
		JSONArray mentionRatioJsonArray = new JSONArray();
		// get objects from cursor
		userNo = 0;
		while (mentionRatioResult.hasNext()) {
			DBObject next = mentionRatioResult.next();
			userNo++;
			mentionRatioJsonArray.put(new JSONObject()
					.put("ratioValue",
							NumberUtils.roundDouble(4,
									(double) next.get(MongoCollectionFieldNames.MONGO_MENTION_RATIO)))
					.put("userSequenceNo", userNo));
		}
		// init to array
		jsonArray.put(new JSONObject().put("ratioType", MongoCollectionFieldNames.MONGO_URL_RATIO).put("values",
				urlRatioJsonArray));
		jsonArray.put(new JSONObject().put("ratioType", MongoCollectionFieldNames.MONGO_HASHTAG_RATIO).put("values",
				hashtagRatioJsonArray));
		jsonArray.put(new JSONObject().put("ratioType", MongoCollectionFieldNames.MONGO_MENTION_RATIO).put("values",
				mentionRatioJsonArray));
	}

	/**
	 * @deprecated
	 * 
	 * @param jsonArray
	 * @param query
	 * @throws JSONException
	 * @throws Exception
	 */
	private void visualizeUserCreationTimes(JSONArray jsonArray, BasicDBObject query) throws JSONException, Exception {
		// get cursor
		DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_CREATION_DATE_IN_RATA_DIE, 1));
		// get objects from cursor
		int userNo = 0;
		while (paginatedResult.hasNext()) {
			DBObject next = paginatedResult.next();
			userNo++;
			String twitterDateStr = (String) next.get(MongoCollectionFieldNames.MONGO_USER_CREATION_DATE);

			JSONObject userProcessInputData = new JSONObject();
			userProcessInputData.put("userSequenceNo", userNo);
			userProcessInputData.put(MongoCollectionFieldNames.MONGO_USER_ID,
					TextUtils.getLongValue((String) next.get(MongoCollectionFieldNames.MONGO_USER_ID)));
			userProcessInputData.put(MongoCollectionFieldNames.MONGO_USER_CREATION_DATE,
					DateTimeUtils.getStringOfDate("yyyyMMdd", DateTimeUtils.getTwitterDate(twitterDateStr)));
			jsonArray.put(userProcessInputData);
		}
	}

}
