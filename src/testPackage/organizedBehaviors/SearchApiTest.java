package testPackage.organizedBehaviors;

import com.mongodb.BasicDBObject;

import direnaj.driver.DirenajMongoDriver;
import direnaj.twitter.twitter4j.Twitter4jUtil;

public class SearchApiTest {

	public static void main(String[] args) {
		DirenajMongoDriver.getInstance().getCampaignsCollection().remove(new BasicDBObject("campaign_id", "Deneme2"));
		DirenajMongoDriver.getInstance().getTweetsCollection().remove(new BasicDBObject("campaign_id", "Deneme2"));

		Twitter4jUtil.createCampaign4PastTweets("Deneme2","", "#LGBTQHatesTrumpParty", "2016-06-16", "2016-06-14");
	}

}
