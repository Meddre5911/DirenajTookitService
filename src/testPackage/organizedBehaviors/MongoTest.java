package testPackage.organizedBehaviors;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;

import direnaj.driver.DirenajMongoDriver;
import direnaj.driver.MongoCollectionFieldNames;
import direnaj.util.DateTimeUtils;
import direnaj.util.TextUtils;

public class MongoTest {

	public static void main(String[] args) throws Exception {

		DBCollection collection = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData();
		String requestId = "201608182253474005f237733-07fd-49d1-af9d-bd34ec20ccee";
		DBObject query = new BasicDBObject("requestId", requestId);

		MongoTest.calculateMeanVariance(collection, MongoCollectionFieldNames.MONGO_USER_HASHTAG_RATIO, query,
				requestId);
	}

	public static void insertRequest2Mongo(String requestId) {
		DBCollection organizedBehaviorCollection = DirenajMongoDriver.getInstance().getOrgBehaviorRequestCollection();
		BasicDBObject document = new BasicDBObject();
		document.put("_id", requestId);
		document.put("requestType", "");
		document.put("requestDefinition", "");
		document.put("campaignId", "");
		document.put("topHashtagCount", "");
		document.put("tracedHashtag", "");
		document.put("processCompleted", Boolean.FALSE);
		document.put("similartyCalculationCompleted", Boolean.FALSE);
		document.put("statusChangeTime", DateTimeUtils.getLocalDate());
		organizedBehaviorCollection.insert(document);
	}

	public static void updateRequestInMongo(String requestId) {
		DBCollection organizedBehaviorCollection = DirenajMongoDriver.getInstance().getOrgBehaviorRequestCollection();
		BasicDBObject findQuery = new BasicDBObject();
		findQuery.put("_id", requestId);
		BasicDBObject updateQuery = new BasicDBObject();
		updateQuery.append("$set", new BasicDBObject().append("tracedHashtag", "Deneme")).append("$set",
				new BasicDBObject().append(MongoCollectionFieldNames.MONGO_EARLIEST_TWEET_TIME,
						TextUtils.getNotNullValue(DateTimeUtils.getLocalDate())));

		organizedBehaviorCollection.update(findQuery, updateQuery, true, false);
	}

	public static void calculateMeanVariance(DBCollection orgBehaviourRequestMeanVarianceCalculations,
			String calculationField, DBObject query, String requestId) {

		
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
				+ "value._id = ObjectId();" //
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

		MapReduceCommand cmd = new MapReduceCommand(orgBehaviourRequestMeanVarianceCalculations, mapFunction,
				reduceFunction, null, MapReduceCommand.OutputType.INLINE, query);
		cmd.setFinalize(finalizeFunction);

		MapReduceOutput out = orgBehaviourRequestMeanVarianceCalculations.mapReduce(cmd);

		for (DBObject o : out.results()) {
			DBObject object = (DBObject) o.get("value");
			System.out.println(object.toString());
		}

	}

}
