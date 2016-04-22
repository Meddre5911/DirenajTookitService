package testPackage.organizedBehaviors;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

import direnaj.driver.DirenajMongoDriver;
import direnaj.driver.MongoCollectionFieldNames;
import direnaj.util.DateTimeUtils;
import direnaj.util.TextUtils;

public class MongoTest {

	public static void main(String[] args) throws Exception {
		// delete object
		DirenajMongoDriver.getInstance().getOrgBehaviorRequestCollection().remove(new BasicDBObject());

		// create test entity
		String requestId = "20160211";
		MongoTest.insertRequest2Mongo(requestId);
		MongoTest.updateRequestInMongo(requestId);
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

		organizedBehaviorCollection.update(findQuery, updateQuery,true,false);
	}

}
