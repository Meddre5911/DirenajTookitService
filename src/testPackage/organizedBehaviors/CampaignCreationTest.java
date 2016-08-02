package testPackage.organizedBehaviors;

import com.mongodb.BasicDBObject;

import direnaj.driver.DirenajMongoDriver;
import direnaj.functionalities.organizedBehaviour.CampaignCreator;

public class CampaignCreationTest {

	
	public static void main(String[] args) throws Exception {
		DirenajMongoDriver.getInstance().getCampaignWordCollection().remove(new BasicDBObject("campaign_id", "Deneme"));
		DirenajMongoDriver.getInstance().getCampaignStatisticsCollection().remove(new BasicDBObject("campaign_id", "Deneme"));
		
		CampaignCreator campaignCreator = new CampaignCreator("Deneme", "", "", "", "");
		campaignCreator.calculateCampaignFeatures();

	}
}
