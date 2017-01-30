package testPackage.organizedBehaviors;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import direnaj.driver.DirenajMongoDriver;
import direnaj.driver.MongoCollectionFieldNames;
import direnaj.twitter.twitter4j.Twitter4jUtil;
import direnaj.util.PropertiesUtil;
import twitter4j.Status;

public class HourlyCalculatorTest {

	public static void main(String[] args) {
		Gson statusDeserializer = Twitter4jUtil.getGsonObject4Deserialization();
		BasicDBObject tweetQuery = new BasicDBObject(MongoCollectionFieldNames.MONGO_REQUEST_ID,
				"201701280003318511c5b3595-7045-47e5-8e32-60ecb4dc4d23").append(
						MongoCollectionFieldNames.MONGO_TWEET_CREATION_DATE,
						new BasicDBObject("$gt", 736710.9601851852d).append("$lt", 736724.9601851852));

		// exclude mongo primary key
		BasicDBObject keys = new BasicDBObject("_id", false);
		keys.put(MongoCollectionFieldNames.MONGO_ACTUAL_TWEET_OBJECT, true);

		int batchSize = PropertiesUtil.getInstance().getIntProperty("toolkit.statisticCalculator.batchSizeCount", 500);
		// get cursor
		DBCursor userTweetCursor = DirenajMongoDriver.getInstance().getOrgBehaviourTweetsOfRequest()
				.find(tweetQuery, keys).batchSize(batchSize).addOption(Bytes.QUERYOPTION_NOTIMEOUT);

		// iterate
		try {
			while (userTweetCursor.hasNext()) {
				DBObject actualTweet = userTweetCursor.next();
				DBObject status = (DBObject) actualTweet.get(MongoCollectionFieldNames.MONGO_ACTUAL_TWEET_OBJECT);
				Status twitter4jStatus = Twitter4jUtil.deserializeTwitter4jStatusFromGson(statusDeserializer,
						status.toString());
				
				twitter4jStatus.getCampaign_id();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
