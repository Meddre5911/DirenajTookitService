package testPackage.organizedBehaviors;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

import direnaj.driver.DirenajMongoDriver;
import direnaj.util.DateTimeUtils;

public class OrganizedBehaviourDetectionTest {

	public static void main(String[] args) {
		insertRequest2Mongo();
	}
	
	public static void insertRequest2Mongo() {
		DBCollection organizedBehaviorCollection = DirenajMongoDriver.getInstance().getOrgBehaviorRequestCollection();
		BasicDBObject document = new BasicDBObject();
		document.put("_id", "20160211");
		document.put("requestType", "");
		document.put("requestDefinition", "");
		document.put("campaignId", "");
		document.put("topHashtagCount", "");
		document.put("tracedHashtag", "");
		document.put("processCompleted", Boolean.TRUE);
		document.put("similartyCalculationCompleted", Boolean.TRUE);
		document.put("statusChangeTime", DateTimeUtils.getLocalDate());
		organizedBehaviorCollection.insert(document);
	}
	
}
