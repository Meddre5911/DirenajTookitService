package direnaj.driver;

import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import direnaj.util.PropertiesUtil;

public class DirenajMongoDriver {

	private static DirenajMongoDriver mongoDriver = null;
	private MongoClient mongoClient = null;
	private DB mongoDB = null;
	private int bulkInsertSize;

	private DirenajMongoDriver() throws UnknownHostException {
		String mongoServerAddress = PropertiesUtil.getInstance().getProperty("mongo.server.address", null);
		String mongoServerPort = PropertiesUtil.getInstance().getProperty("mongo.server.port", null);
		String mongoUsedDb = PropertiesUtil.getInstance().getProperty("mongo.usedDB", null);

		mongoClient = new MongoClient(new MongoClientURI("mongodb://" + mongoServerAddress + ":" + mongoServerPort));

		mongoDB = mongoClient.getDB(mongoUsedDb);
		bulkInsertSize = Integer.valueOf(PropertiesUtil.getInstance().getProperty("mongo.bulk.insert.size", null));

		if (!mongoDB.collectionExists("OrgBehaviourRequests")) {
			mongoDB.createCollection("OrgBehaviourRequests", null);
			Logger.getLogger(DirenajMongoDriver.class.getSimpleName()).debug("OrgBehaviourRequests is created");
		}
		if (!mongoDB.collectionExists("OrgBehaviourPreProcessUsers")) {
			mongoDB.createCollection("OrgBehaviourPreProcessUsers", null);
			Logger.getLogger(DirenajMongoDriver.class.getSimpleName()).debug("OrgBehaviourPreProcessUsers is created");
		}
		if (!mongoDB.collectionExists("OrgBehaviourProcessInputData")) {
			mongoDB.createCollection("OrgBehaviourProcessInputData", null);
			Logger.getLogger(DirenajMongoDriver.class.getSimpleName()).debug("OrgBehaviourProcessInputData is created");
		}
		if (!mongoDB.collectionExists("OrgBehaviourCosSimilarity_TF")) {
			mongoDB.createCollection("OrgBehaviourCosSimilarity_TF", null);
			Logger.getLogger(DirenajMongoDriver.class.getSimpleName()).debug("OrgBehaviourCosSimilarity_TF is created");
		}
		if (!mongoDB.collectionExists("OrgBehaviourCosSimilarity_IDF")) {
			mongoDB.createCollection("OrgBehaviourCosSimilarity_IDF", null);
			Logger.getLogger(DirenajMongoDriver.class.getSimpleName())
					.debug("OrgBehaviourCosSimilarity_IDF is created");
		}
		if (!mongoDB.collectionExists("OrgBehaviourCosSimilarity_TF_IDF")) {
			mongoDB.createCollection("OrgBehaviourCosSimilarity_TF_IDF", null);
			Logger.getLogger(DirenajMongoDriver.class.getSimpleName())
					.debug("OrgBehaviourCosSimilarity_TF_IDF is created");
		}
		if (!mongoDB.collectionExists("OrgBehaviourTweetSimilarity")) {
			mongoDB.createCollection("OrgBehaviourTweetSimilarity", null);
			Logger.getLogger(DirenajMongoDriver.class.getSimpleName()).debug("OrgBehaviourTweetSimilarity is created");
		}
		if (!mongoDB.collectionExists("OrgBehaviourTweetsOfRequest")) {
			mongoDB.createCollection("OrgBehaviourTweetsOfRequest", null);
			Logger.getLogger(DirenajMongoDriver.class.getSimpleName()).debug("OrgBehaviourTweetsOfRequest is created");
		}
		if (!mongoDB.collectionExists("OrgBehaviourTweetsShortInfo")) {
			mongoDB.createCollection("OrgBehaviourTweetsShortInfo", null);
			Logger.getLogger(DirenajMongoDriver.class.getSimpleName()).debug("OrgBehaviourTweetsShortInfo is created");
		}
		if (!mongoDB.collectionExists("OrgBehaviourUserTweets")) {
			mongoDB.createCollection("OrgBehaviourUserTweets", null);
			Logger.getLogger(DirenajMongoDriver.class.getSimpleName()).debug("OrgBehaviourUserTweets is created");
		}
		if (!mongoDB.collectionExists("OrgBehaviourRequestedSimilarityCalculations")) {
			mongoDB.createCollection("OrgBehaviourRequestedSimilarityCalculations", null);
			Logger.getLogger(DirenajMongoDriver.class.getSimpleName())
					.debug("OrgBehaviourRequestedSimilarityCalculations is created");
		}
		if (!mongoDB.collectionExists("CampaignWords")) {
			mongoDB.createCollection("CampaignWords", null);
			Logger.getLogger(DirenajMongoDriver.class.getSimpleName()).debug("CampaignWords is created");
		}
		if (!mongoDB.collectionExists("CampaignStatistics")) {
			mongoDB.createCollection("CampaignStatistics", null);
			Logger.getLogger(DirenajMongoDriver.class.getSimpleName()).debug("CampaignStatistics is created");
		}
		if (!mongoDB.collectionExists("OrgBehaviourRequestMeanVarianceCalculations")) {
			mongoDB.createCollection("OrgBehaviourRequestMeanVarianceCalculations", null);
			Logger.getLogger(DirenajMongoDriver.class.getSimpleName())
					.debug("OrgBehaviourRequestMeanVarianceCalculations is created");
		}
	}

	public static DirenajMongoDriver getInstance() {
		if (mongoDriver == null) {
			try {
				mongoDriver = new DirenajMongoDriver();
			} catch (UnknownHostException e) {
				Logger.getLogger(DirenajMongoDriver.class.getSimpleName()).error("Error in getInstance", e);
			}
		}
		return mongoDriver;
	}

	public DBCollection getTweetsCollection() {
		return mongoDB.getCollection("tweets");
	}

	public DBCollection getCampaignsCollection() {
		return mongoDB.getCollection("campaigns");
	}

	public DBCollection getCampaignWordCollection() {
		return mongoDB.getCollection("CampaignWords");
	}

	public DBCollection getCampaignStatisticsCollection() {
		return mongoDB.getCollection("CampaignStatistics");
	}

	public DBCollection getOrgBehaviorRequestCollection() {
		return mongoDB.getCollection("OrgBehaviourRequests");
	}

	public DBCollection getOrgBehaviorPreProcessUsers() {
		return mongoDB.getCollection("OrgBehaviourPreProcessUsers");
	}

	public DBCollection getOrgBehaviourProcessInputData() {
		return mongoDB.getCollection("OrgBehaviourProcessInputData");
	}

	public DBCollection getOrgBehaviourCosSimilarityTF() {
		return mongoDB.getCollection("OrgBehaviourCosSimilarity_TF");
	}

	public DBCollection getOrgBehaviourProcessCosSimilarityIDF() {
		return mongoDB.getCollection("OrgBehaviourCosSimilarity_IDF");
	}

	public DBCollection getOrgBehaviourProcessCosSimilarityTF_IDF() {
		return mongoDB.getCollection("OrgBehaviourCosSimilarity_TF_IDF");
	}

	public DBCollection getOrgBehaviourProcessTweetSimilarity() {
		return mongoDB.getCollection("OrgBehaviourTweetSimilarity");
	}

	public DBCollection getOrgBehaviourTweetsOfRequest() {
		return mongoDB.getCollection("OrgBehaviourTweetsOfRequest");
	}

	public DBCollection getOrgBehaviourTweetsShortInfo() {
		return mongoDB.getCollection("OrgBehaviourTweetsShortInfo");
	}

	public DBCollection getOrgBehaviourUserTweets() {
		return mongoDB.getCollection("OrgBehaviourUserTweets");
	}

	public DBCollection getOrgBehaviourRequestedSimilarityCalculations() {
		return mongoDB.getCollection("OrgBehaviourRequestedSimilarityCalculations");
	}
	
	public DBCollection getOrgBehaviourRequestMeanVarianceCalculations() {
		return mongoDB.getCollection("OrgBehaviourRequestMeanVarianceCalculations");
	}

	public DBCollection getTestCollection() {
		return mongoDB.getCollection("testCollection");
	}

	public Long executeCountQuery(DBCollection collection, DBObject query) {
		return collection.count(query);
	}

	public int getBulkInsertSize() {
		return bulkInsertSize;
	}

	public void setBulkInsertSize(int bulkInsertSize) {
		this.bulkInsertSize = bulkInsertSize;
	}

	public static void main(String[] args) {
		DirenajMongoDriver.getInstance();
	}

}
