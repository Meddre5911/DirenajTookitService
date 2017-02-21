package direnaj.functionalities.organizedBehaviour;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import direnaj.domain.ExtractedFeature;
import direnaj.domain.ProcessedPercentageFeature;
import direnaj.driver.DirenajMongoDriver;
import direnaj.driver.MongoCollectionFieldNames;
import direnaj.util.CollectionUtil;
import direnaj.util.NumberUtils;

public class FeatureExtractorUtil {

	public static void visualizeUserRoughHashtagTweetCountsInBarChart(JSONArray jsonArray, BasicDBObject query)
			throws JSONException {
		ProcessedPercentageFeature percentageFeature = extractUserRoughHashtagTweetCountsFeatures(query);
		for (String limit : percentageFeature.getLimits()) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("ratio", limit);
			jsonObject.put("percentage", percentageFeature.getRangePercentages().get(limit));
			jsonArray.put(jsonObject);
		}
	}

	public static ExtractedFeature extractUserRoughHashtagTweetCountsFeatures4Classification(String featureInitial,
			BasicDBObject query) throws JSONException {
		StringBuilder values = new StringBuilder();
		StringBuilder keys = new StringBuilder();
		ProcessedPercentageFeature percentageFeature = extractUserRoughHashtagTweetCountsFeatures(query);

		for (String limit : percentageFeature.getLimits()) {
			keys.append(featureInitial + "_" + limit+",");
			values.append(percentageFeature.getRangePercentages().get(limit) + ",");
		}
		return new ExtractedFeature(keys.toString(), values.toString());
	}

	private static ProcessedPercentageFeature extractUserRoughHashtagTweetCountsFeatures(BasicDBObject query) {
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
		ProcessedPercentageFeature percentageFeature = new ProcessedPercentageFeature(limits, rangePercentages);
		return percentageFeature;
	}
}
