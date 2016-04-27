package testPackage.organizedBehaviors;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import direnaj.driver.DirenajMongoDriver;
import direnaj.twitter.twitter4j.Twitter4jUtil;
import twitter4j.Status;

public class TweetDeserilizatiertest {
	
	public static void main(String[] args) {
		
		DBObject query = new BasicDBObject("id", Long.valueOf("722642049409753088"));
		DBObject result = DirenajMongoDriver.getInstance().getOrgBehaviourUserTweets().findOne(query);
		
		
		String str = result.toString();
		
		
		Gson gsonObject4Deserialization = Twitter4jUtil.getGsonObject4Deserialization();
		Status status = Twitter4jUtil.deserializeTwitter4jStatusFromGson(gsonObject4Deserialization, str);
		System.out.println("Status is retrieved"); 
	}

}
