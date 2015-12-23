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
        	Logger.getLogger(DirenajMongoDriver.class.getSimpleName()).debug("OrgBehaviourCosSimilarity_IDF is created");
        }
        if (!mongoDB.collectionExists("OrgBehaviourTweetSimilarity")) {
        	mongoDB.createCollection("OrgBehaviourTweetSimilarity", null);
        	Logger.getLogger(DirenajMongoDriver.class.getSimpleName()).debug("OrgBehaviourTweetSimilarity is created");
        }
        if (!mongoDB.collectionExists("OrgBehaviourTweetsOfRequest")) {
        	mongoDB.createCollection("OrgBehaviourTweetsOfRequest", null);
        	Logger.getLogger(DirenajMongoDriver.class.getSimpleName()).debug("OrgBehaviourTweetsOfRequest is created");
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
    public DBCollection getOrgBehaviourProcessTweetSimilarity() {
    	return mongoDB.getCollection("OrgBehaviourTweetSimilarity");
    }
    public DBCollection getOrgBehaviourTweetsOfRequest() {
    	return mongoDB.getCollection("OrgBehaviourTweetsOfRequest");
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
