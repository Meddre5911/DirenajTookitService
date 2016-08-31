package direnaj.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;

import direnaj.driver.DirenajMongoDriver;
import direnaj.driver.MongoCollectionFieldNames;
import direnaj.util.CollectionUtil;
import direnaj.util.DateTimeUtils;
import direnaj.util.NumberUtils;
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
			BasicDBObject query = new BasicDBObject("requestId", requestId);
			BasicDBObject query4CosSimilarityRequest = new BasicDBObject(
					MongoCollectionFieldNames.MONGO_COS_SIM_REQ_ORG_REQUEST_ID, requestId);
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
			} else if ("visualizeUserRoughTweetCounts".equals(requestType)) {
				visualizeUserRoughTweetCounts(jsonArray, query);
			} else if ("visualizeHourlyUserAndTweetCount".equals(requestType)) {
				visualizeHourlyUserAndTweetCount(jsonArray, query4CosSimilarityRequest);
			} else if ("visualizeHourlyTweetSimilarities".equals(requestType)) {
				visualizeHourlyTweetSimilarities(jsonArray, query4CosSimilarityRequest);
			} else if ("visualizeUserCreationTimesInBarChart".equals(requestType)) {
				visualizeUserCreationTimesInBarChart(jsonArray, query);
			} else if ("visualizeUserFriendFollowerRatioInBarChart".equals(requestType)) {
				visualizeUserFriendFollowerRatioInBarChart(jsonArray, query, requestId);
			} else if ("visualizeUserRoughHashtagTweetCountsInBarChart".equals(requestType)) {
				visualizeUserRoughHashtagTweetCountsInBarChart(jsonArray, query, requestId);
			}

			// FIXME 20160813 Sil
			jsonStr = jsonArray.toString();
			System.out.println("Request Type : " + requestType);
			System.out.println("Returned String : " + jsonStr);
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

	private void visualizeUserRoughHashtagTweetCountsInBarChart(JSONArray jsonArray, BasicDBObject query,
			String requestId) throws JSONException {
		List<String> limits = new ArrayList<>();
		limits.add("1");
		limits.add("2");
		limits.add("3-5");
		limits.add("6-10");
		limits.add("11-20");
		limits.add("21-50");
		limits.add("51-100");
		limits.add("100-200");
		limits.add("200-...");
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
			double userHashtagPostCount = NumberUtils.roundDouble(4,
					(double) next.get(MongoCollectionFieldNames.MONGO_USER_HASHTAG_POST_COUNT));
			CollectionUtil.findGenericRange(limits, rangePercentages, userHashtagPostCount);
		}
		CollectionUtil.calculatePercentage(rangePercentages, userCount);
		for (String limit : limits) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("ratio", limit);
			jsonObject.put("percentage", rangePercentages.get(limit));
			jsonArray.put(jsonObject);
		}
	}
	
	
	private void visualizeUserTweetEntityRatiosInBarChart(JSONArray jsonArray, BasicDBObject query) throws JSONException {
		// get cursor
		DBCursor urlRatioResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query);
				
		JSONArray urlRatioJsonArray = new JSONArray();
		// get objects from cursor
		// get url ratio
		int userNo = 0;
		while (urlRatioResult.hasNext()) {
			DBObject next = urlRatioResult.next();
			userNo++;
			urlRatioJsonArray.put(new JSONObject()
					.put("ratioValue",
							NumberUtils.roundDouble(4,
									(double) next.get(MongoCollectionFieldNames.MONGO_USER_URL_RATIO)))
					.put("userSequenceNo", userNo));
		}
		// hashtag ratio
		DBCursor hashtagRatioResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_HASHTAG_RATIO, 1));
		JSONArray hashtagRatioJsonArray = new JSONArray();
		// get objects from cursor
		userNo = 0;
		while (hashtagRatioResult.hasNext()) {
			DBObject next = hashtagRatioResult.next();
			userNo++;
			hashtagRatioJsonArray.put(new JSONObject()
					.put("ratioValue",
							NumberUtils.roundDouble(4,
									(double) next.get(MongoCollectionFieldNames.MONGO_USER_HASHTAG_RATIO)))
					.put("userSequenceNo", userNo));
		}
		// mention ratio
		DBCursor mentionRatioResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_MENTION_RATIO, 1));
		JSONArray mentionRatioJsonArray = new JSONArray();
		// get objects from cursor
		userNo = 0;
		while (mentionRatioResult.hasNext()) {
			DBObject next = mentionRatioResult.next();
			userNo++;
			mentionRatioJsonArray.put(new JSONObject()
					.put("ratioValue",
							NumberUtils.roundDouble(4,
									(double) next.get(MongoCollectionFieldNames.MONGO_USER_MENTION_RATIO)))
					.put("userSequenceNo", userNo));
		}
		// init to array
		jsonArray.put(new JSONObject().put("ratioType", MongoCollectionFieldNames.MONGO_USER_URL_RATIO).put("values",
				urlRatioJsonArray));
		jsonArray.put(new JSONObject().put("ratioType", MongoCollectionFieldNames.MONGO_USER_HASHTAG_RATIO)
				.put("values", hashtagRatioJsonArray));
		jsonArray.put(new JSONObject().put("ratioType", MongoCollectionFieldNames.MONGO_USER_MENTION_RATIO)
				.put("values", mentionRatioJsonArray));
	}

	private void visualizeUserFriendFollowerRatioInBarChart(JSONArray jsonArray, BasicDBObject query, String requestId)
			throws JSONException {
		// get cursor
		DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query);

		// get objects from cursor
		int userCount = 0;
		Map<Double, Double> ratioPercentages = new HashMap<>();
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

	/**
	 * 
	 * DBObject meanVarianceResult = getMeanVariance(query, requestId,	MongoCollectionFieldNames.MONGO_USER_FRIEND_FOLLOWER_RATIO);
	 * 
	 * @param query
	 * @param requestId
	 * @param calculationColumn
	 * @return
	 */
	private DBObject getMeanVariance(BasicDBObject query, String requestId, String calculationColumn) {
		DBObject calculationQuery = new BasicDBObject("requestId", requestId).append("calculationType",
				calculationColumn);
		DBObject meanVarianceResult = DirenajMongoDriver.getInstance().getOrgBehaviourRequestMeanVarianceCalculations()
				.findOne(calculationQuery);
		if (meanVarianceResult == null) {
			meanVarianceResult = calculateMeanVariance(
					DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData(),
					calculationColumn, query, requestId);
		}
		return meanVarianceResult;
	}

	private DBObject calculateMeanVariance(DBCollection collection, String calculationField, DBObject query,
			String requestId) {

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
				+ "sum: previous.sum + current.sum, " //
				+ "min: Math.min(previous.min, current.min), " //
				+ "max: Math.max(previous.max, current.max), " //
				+ "count: previous.count + current.count, " //
				+ "diff: previous.diff + current.diff + delta*delta*weight " //
				+ "};" //
				+ "})" //
				+ "}"; //

		String finalizeFunction = "function finalize(key, value){" //
				+ "value.average = value.sum / value.count;" //
				+ "value.population_variance = value.diff / value.count;" //
				+ "value.population_standard_deviation = Math.sqrt(value.population_variance);" //
				+ "value.sample_variance = value.diff / (value.count - 1);" //
				+ "value.sample_standard_deviation = Math.sqrt(value.sample_variance);" //
				+ "value.requestId = \"" + requestId + "\";" //
				+ "value.calculationType =\"" + calculationField + "\";" //
				+ "delete value.diff;" //
				+ "return value;" //
				+ "}";

		System.out.println("Finalize : " + finalizeFunction);

		MapReduceCommand cmd = new MapReduceCommand(collection, mapFunction, reduceFunction, null,
				MapReduceCommand.OutputType.INLINE, query);
		cmd.setFinalize(finalizeFunction);

		MapReduceOutput out = collection.mapReduce(cmd);

		for (DBObject o : out.results()) {
			DBObject calculationResult = (DBObject) o.get("value");
			if (calculationResult != null) {
				// FIXME 20160825 Index'i unutma
				DirenajMongoDriver.getInstance().getOrgBehaviourRequestMeanVarianceCalculations()
						.insert(calculationResult);
				return calculationResult;
			}
		}
		return null;

	}

	private void visualizeUserCreationTimesInBarChart(JSONArray jsonArray, BasicDBObject query)
			throws Exception, JSONException {
		// get cursor
		// FIXME 20160818 - Tarihe Göre sırala
		DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query);
		Map<String, Double> usersByDate = new HashMap<>();
		// get objects from cursor
		int userCount = 0;
		while (paginatedResult.hasNext()) {
			DBObject next = paginatedResult.next();
			userCount++;
			String twitterDateStr = (String) next.get(MongoCollectionFieldNames.MONGO_USER_CREATION_DATE);
			String userCreationDate = DateTimeUtils.getStringOfDate("yyyyMM",
					DateTimeUtils.getTwitterDate(twitterDateStr));
			CollectionUtil.incrementKeyValueInMap(usersByDate, userCreationDate);
		}
		CollectionUtil.calculatePercentage(usersByDate, userCount);
		usersByDate = CollectionUtil.sortByComparator4DateKey(usersByDate);
		for (Entry<String, Double> entry : usersByDate.entrySet()) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("creationDate", entry.getKey());
			jsonObject.put("percentage", entry.getValue());
			jsonArray.put(jsonObject);
		}
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
					DateTimeUtils.getTwitterDate(twitterDateStr));

			jsonArray.put(new JSONObject().put("time", twitterDate)
					.put(MongoCollectionFieldNames.NON_SIMILAR,
							NumberUtils.roundDouble(4, (double) next.get(MongoCollectionFieldNames.NON_SIMILAR) * 100d,
									100d))
					.put(MongoCollectionFieldNames.SLIGHTLY_SIMILAR,
							NumberUtils.roundDouble(4,
									(double) next.get(MongoCollectionFieldNames.SLIGHTLY_SIMILAR) * 100d, 100d))
					.put(MongoCollectionFieldNames.SIMILAR,
							NumberUtils.roundDouble(4, (double) next.get(MongoCollectionFieldNames.SIMILAR) * 100d,
									100d))
					.put(MongoCollectionFieldNames.VERY_SIMILAR,
							NumberUtils.roundDouble(4, (double) next.get(MongoCollectionFieldNames.VERY_SIMILAR) * 100d,
									100d))
					.put(MongoCollectionFieldNames.MOST_SIMILAR, NumberUtils.roundDouble(4,
							(double) next.get(MongoCollectionFieldNames.MOST_SIMILAR) * 100d, 100d)));
		}
	}

	private void visualizeHourlyUserAndTweetCount(JSONArray jsonArray, BasicDBObject query4CosSimilarityRequest)
			throws Exception, JSONException {
		DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations()
				.find(query4CosSimilarityRequest)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_COS_SIM_REQ_RATA_DIE_LOWER_TIME, 1));
		JSONArray tweetCountsJsonArray = new JSONArray();
		JSONArray distinctUserCountsJsonArray = new JSONArray();
		// get objects from cursor
		while (paginatedResult.hasNext()) {
			DBObject next = paginatedResult.next();
			// prepare json object
			String twitterDateStr = (String) next.get("lowerTimeInterval");
			String twitterDate = DateTimeUtils.getStringOfDate("yyyyMMdd HH:mm",
					DateTimeUtils.getTwitterDate(twitterDateStr));
			tweetCountsJsonArray.put(new JSONObject().put("time", twitterDate).put("value",
					next.get(MongoCollectionFieldNames.MONGO_DISTINCT_USER_COUNT)));
			// prepare json object
			distinctUserCountsJsonArray.put(new JSONObject().put("time", twitterDate).put("value",
					next.get(MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT)));

		}
		// init to array
		jsonArray.put(new JSONObject().put("valueType", MongoCollectionFieldNames.MONGO_DISTINCT_USER_COUNT)
				.put("values", tweetCountsJsonArray));
		jsonArray.put(new JSONObject().put("valueType", MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT).put("values",
				distinctUserCountsJsonArray));
	}

	private void visualizeUserRoughTweetCounts(JSONArray jsonArray, BasicDBObject query) throws JSONException {
		// buna özel bir müdahale gerekebilir
		// get cursor
		DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT, 1));
		JSONArray favoriteStatusCountsJsonArray = new JSONArray();
		// get objects from cursor
		int userNo = 0;
		while (paginatedResult.hasNext()) {
			DBObject next = paginatedResult.next();
			userNo++;
			// prepare json object
			favoriteStatusCountsJsonArray.put(new JSONObject()
					.put("ratioValue", (double) next.get(MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT))
					.put("userSequenceNo", userNo));

		}
		paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT, 1));
		JSONArray wholeStatusCountsJsonArray = new JSONArray();
		// get objects from cursor
		userNo = 0;
		while (paginatedResult.hasNext()) {
			DBObject next = paginatedResult.next();
			userNo++;
			// prepare json object
			wholeStatusCountsJsonArray.put(new JSONObject()
					.put("ratioValue", (double) next.get(MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT))
					.put("userSequenceNo", userNo));

		}
		// init to array
		jsonArray.put(new JSONObject().put("ratioType", MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT)
				.put("values", favoriteStatusCountsJsonArray));
		jsonArray.put(new JSONObject().put("ratioType", MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT).put("values",
				wholeStatusCountsJsonArray));
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
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_URL_RATIO, 1));
		JSONArray urlRatioJsonArray = new JSONArray();
		// get objects from cursor
		// get url ratio
		int userNo = 0;
		while (urlRatioResult.hasNext()) {
			DBObject next = urlRatioResult.next();
			userNo++;
			urlRatioJsonArray.put(new JSONObject()
					.put("ratioValue",
							NumberUtils.roundDouble(4,
									(double) next.get(MongoCollectionFieldNames.MONGO_USER_URL_RATIO)))
					.put("userSequenceNo", userNo));
		}
		// hashtag ratio
		DBCursor hashtagRatioResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_HASHTAG_RATIO, 1));
		JSONArray hashtagRatioJsonArray = new JSONArray();
		// get objects from cursor
		userNo = 0;
		while (hashtagRatioResult.hasNext()) {
			DBObject next = hashtagRatioResult.next();
			userNo++;
			hashtagRatioJsonArray.put(new JSONObject()
					.put("ratioValue",
							NumberUtils.roundDouble(4,
									(double) next.get(MongoCollectionFieldNames.MONGO_USER_HASHTAG_RATIO)))
					.put("userSequenceNo", userNo));
		}
		// mention ratio
		DBCursor mentionRatioResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query)
				.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_MENTION_RATIO, 1));
		JSONArray mentionRatioJsonArray = new JSONArray();
		// get objects from cursor
		userNo = 0;
		while (mentionRatioResult.hasNext()) {
			DBObject next = mentionRatioResult.next();
			userNo++;
			mentionRatioJsonArray.put(new JSONObject()
					.put("ratioValue",
							NumberUtils.roundDouble(4,
									(double) next.get(MongoCollectionFieldNames.MONGO_USER_MENTION_RATIO)))
					.put("userSequenceNo", userNo));
		}
		// init to array
		jsonArray.put(new JSONObject().put("ratioType", MongoCollectionFieldNames.MONGO_USER_URL_RATIO).put("values",
				urlRatioJsonArray));
		jsonArray.put(new JSONObject().put("ratioType", MongoCollectionFieldNames.MONGO_USER_HASHTAG_RATIO)
				.put("values", hashtagRatioJsonArray));
		jsonArray.put(new JSONObject().put("ratioType", MongoCollectionFieldNames.MONGO_USER_MENTION_RATIO)
				.put("values", mentionRatioJsonArray));
	}

	private void visualizeUserCreationTimes(JSONArray jsonArray, BasicDBObject query) throws JSONException, Exception {
		// get cursor
		// FIXME 20160818 - Tarihe Göre sırala
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
