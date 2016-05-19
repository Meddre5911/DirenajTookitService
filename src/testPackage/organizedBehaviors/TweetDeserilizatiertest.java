package testPackage.organizedBehaviors;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import direnaj.driver.DirenajMongoDriver;
import direnaj.twitter.twitter4j.Twitter4jUtil;
import direnaj.util.DateTimeUtils;
import twitter4j.Status;

public class TweetDeserilizatiertest {

	public static void main(String[] args) {
		// { "user.id" : 5402612 , "createdAt" : { "$gt" : 736413.354212963 ,
		// "$lt" : 736525.3125462963}}

		Gson gsonObject4Deserialization = Twitter4jUtil.getGsonObject4Deserialization();
		BasicDBObject tweetsRetrievalQuery = new BasicDBObject("user.id", Long.valueOf("5402612")).append("createdAt",
				new BasicDBObject("$gt", 736413.354212963d).append("$lt", 736525.3125462963d));
		BasicDBObject keys = new BasicDBObject("_id", false);
		DBCursor result = DirenajMongoDriver.getInstance().getOrgBehaviourUserTweets().find(tweetsRetrievalQuery,keys);
		while (result.hasNext()) {
			String str = result.next().toString();

			System.out.println(str);

			Status status = Twitter4jUtil.deserializeTwitter4jStatusFromGson(gsonObject4Deserialization, str);
			System.out.println("Status is retrieved");
		}
	}

}
