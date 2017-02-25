package direnaj.functionalities.organizedBehaviour;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.json.JSONException;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import direnaj.domain.feature.ExtractedFeature;
import direnaj.domain.feature.ProcessedDoubleMapPercentageFeature;
import direnaj.domain.feature.ProcessedNestedPercentageFeature;
import direnaj.domain.feature.ProcessedPercentageFeature;
import direnaj.driver.DirenajMongoDriver;
import direnaj.driver.MongoCollectionFieldNames;
import direnaj.servlet.MongoPaginationServlet;
import direnaj.util.CollectionUtil;
import direnaj.util.DateTimeUtils;
import direnaj.util.NumberUtils;

public class FeatureExtractorUtil {

	public static ExtractedFeature extractPercentageFeature4Classification(String featureInitial,
			ProcessedPercentageFeature percentageFeature) throws JSONException {
		StringBuilder values = new StringBuilder();
		StringBuilder keys = new StringBuilder();

		for (String limit : percentageFeature.getLimits()) {
			keys.append(featureInitial + "_" + limit + ";");
			values.append(percentageFeature.getRangePercentages().get(limit) + ";");
		}
		return new ExtractedFeature(keys.toString(), values.toString());
	}

	public static void extractMeanVariance(StringBuilder trainingDataKeys, StringBuilder trainingData,
			String featureInitial, DBCollection orgBehaviourProcessInputData, String requestId, String calculationType,
			String calculationDomain) {

		featureInitial += "_" + calculationType;

		StringBuilder values = new StringBuilder();
		StringBuilder keys = new StringBuilder();
		DBObject meanVariance = getMeanVariance(orgBehaviourProcessInputData, requestId, calculationType,
				calculationDomain);

		keys.append(featureInitial + "_average;");
		keys.append(featureInitial + "_population_variance;");
		keys.append(featureInitial + "_population_standard_deviation;");
		keys.append(featureInitial + "_minValue;");
		keys.append(featureInitial + "_maxValue;");

		values.append(String.valueOf(meanVariance.get("average")) + ";");
		values.append(String.valueOf(meanVariance.get("population_variance")) + ";");
		values.append(String.valueOf(meanVariance.get("population_standard_deviation")) + ";");
		values.append(String.valueOf(meanVariance.get("min")) + ";");
		values.append(String.valueOf(meanVariance.get("max")) + ";");

		trainingData.append(values.toString());
		trainingDataKeys.append(keys.toString());

	}

	/**
	 * 
	 * DBObject meanVarianceResult = getMeanVariance(query, requestId,
	 * MongoCollectionFieldNames.MONGO_USER_FRIEND_FOLLOWER_RATIO);
	 * 
	 * @param requestId
	 * @param calculationType
	 * 
	 * @return
	 */
	public static DBObject getMeanVariance(DBCollection collection, String requestId, String calculationType,
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

	public static ProcessedPercentageFeature extractUserRoughHashtagTweetCountsFeatures(BasicDBObject query) {
		List<String> limits = new ArrayList<>();
		limits.add("1");
		limits.add("2");
		limits.add("3-5");
		limits.add("6-10");
		limits.add("11-20");
		limits.add("21-50");
		limits.add("51-100");
		limits.add("100-...");
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
		ProcessedPercentageFeature percentageFeature = new ProcessedPercentageFeature(limits, rangePercentages);
		return percentageFeature;
	}

	public static ProcessedPercentageFeature extractUserDailyTweetRatios(BasicDBObject query, String mongoValueField)
			throws JSONException {
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
		limits.add("1001-...");
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
		ProcessedPercentageFeature percentageFeature = new ProcessedPercentageFeature(limits, rangePercentages);
		return percentageFeature;
	}

	public static ProcessedNestedPercentageFeature extractUserTweetEntityRatiosFeature(BasicDBObject query,
			int userPostCountWithHashtag) throws JSONException {
		Map<String, Map<String, Double>> ratioValues = new HashMap<>();
		// define limits
		List<String> limits = new ArrayList<>();
		limits.add("0");
		limits.add("0.0001-0.5");
		limits.add("0.6-0.9");
		for (int i = 1; i <= 10; i++) {
			limits.add(String.valueOf(i));

		}
		limits.add("11-...");
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

		ProcessedNestedPercentageFeature percentageFeature = new ProcessedNestedPercentageFeature(limits, ratioValues);
		return percentageFeature;
	}

	public static ProcessedDoubleMapPercentageFeature extractUserFriendFollowerRatioFeature(BasicDBObject query,
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

		ProcessedDoubleMapPercentageFeature percentageFeature = new ProcessedDoubleMapPercentageFeature(limits,
				ratioPercentages);
		return percentageFeature;
	}

	public static ProcessedNestedPercentageFeature extractUserRoughTweetCountsFeature(BasicDBObject query,
			int userPostCountWithHashtag) throws JSONException {

		Map<String, Map<String, Double>> ratioValues = new HashMap<>();
		// define limits
		List<String> limits = new ArrayList<>();
		// limits between 0 - 1000
		limits.add("0");
		int previous = 1;
		for (int i = 100; i <= 1000; i = i + 500) {
			limits.add(previous + "-" + i);
			previous = i + 1;
		}
		// limits between 1000 - 10000
		previous = 1001;
		for (int i = 2000; i <= 10000; i = i + 2000) {
			limits.add(previous + "-" + i);
			previous = i + 1;
		}
		// limits between 10000 - 100000
		previous = 10001;
		for (int i = 20000; i <= 100000; i = i + 10000) {
			limits.add(previous + "-" + i);
			previous = i + 1;
		}
		limits.add("100001-...");

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

		ProcessedNestedPercentageFeature percentageFeature = new ProcessedNestedPercentageFeature(limits, ratioValues);
		return percentageFeature;
	}

	public static Map<String, Double> getUserCreationTimePercentageData(BasicDBObject query) throws Exception {
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

	public static Map<String, Double> getUserCreationTimePercentageDataWithBuckets(BasicDBObject query)
			throws Exception {
		// define buckets
		List<String> buckets = new LinkedList<>();
		buckets.add("200001-201412");
		buckets.add("201501-201503");
		buckets.add("201504-201506");
		buckets.add("201507-201509");
		buckets.add("201510-201512");
		buckets.add("201601-201603");
		buckets.add("201604-201606");
		buckets.add("201607-201609");
		buckets.add("201610-201612");
		buckets.add("201506-201801");
		buckets.add("201504-201801");
		// get creation dates
		Map<String, Double> usersByDate = getUserCreationTimePercentageData(query);
		// calculate bucket percentages
		Map<String, Double> creationtimeBuckets = new LinkedHashMap<>();
		for (String dateBucket : buckets) {
			String[] split = dateBucket.split("-");
			String lowerDateStr = split[0];
			String upperDateStr = split[1];
			int lowerDate = Integer.valueOf(lowerDateStr);
			int upperDate = Integer.valueOf(upperDateStr);
			double bucketPercentage = 0;
			for (Entry<String, Double> userCreationDateEntry : usersByDate.entrySet()) {
				int userCreationDate = Integer.valueOf(userCreationDateEntry.getKey());
				if (lowerDate <= userCreationDate && upperDate >= userCreationDate) {
					bucketPercentage += userCreationDateEntry.getValue();
				}
			}
			creationtimeBuckets.put(lowerDateStr.substring(2) + "-" + upperDateStr.substring(2), bucketPercentage);
		}
		return creationtimeBuckets;
	}

	public static ProcessedPercentageFeature extractUserCreationTimePercentageDataWithBuckets(BasicDBObject query)
			throws Exception {
		Map<String, Double> userCreationTimePercentageDataWithBuckets = getUserCreationTimePercentageDataWithBuckets(
				query);
		ProcessedPercentageFeature percentageFeature = new ProcessedPercentageFeature(
				new ArrayList<>(userCreationTimePercentageDataWithBuckets.keySet()),
				userCreationTimePercentageDataWithBuckets);
		return percentageFeature;
	}

}
