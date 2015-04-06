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
        String mongoServerAddress = PropertiesUtil.getInstance().getProperty("mongo.server.address");
        String mongoServerPort = PropertiesUtil.getInstance().getProperty("mongo.server.port");
        String mongoUsedDb = PropertiesUtil.getInstance().getProperty("mongo.usedDB");

        mongoClient = new MongoClient(new MongoClientURI("mongodb://" + mongoServerAddress + ":" + mongoServerPort));
        mongoDB = mongoClient.getDB(mongoUsedDb);
        bulkInsertSize = Integer.valueOf(PropertiesUtil.getInstance().getProperty("mongo.bulk.insert.size"));

        if (!mongoDB.collectionExists("OrgBehaviourRequests")) {
            mongoDB.createCollection("OrgBehaviourRequests", null);
        }
        if (!mongoDB.collectionExists("OrgBehaviourPreProcessUsers")) {
            mongoDB.createCollection("OrgBehaviourPreProcessUsers", null);
        }

        if (!mongoDB.collectionExists("OrgBehaviourProcessInputData")) {
            mongoDB.createCollection("OrgBehaviourProcessInputData", null);
        }
    }

    public static DirenajMongoDriver getInstance() {
        if (mongoDriver == null) {
            try {
                mongoDriver = new DirenajMongoDriver();
            } catch (UnknownHostException e) {
                Logger.getLogger(DirenajMongoDriver.class).error("Error in getInstance", e);
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

    public Long executeCountQuery(DBCollection collection, DBObject query) {
        return collection.count(query);
    }

    public static void main(String[] args) {
        DirenajMongoDriver.getInstance();
    }

    public int getBulkInsertSize() {
        return bulkInsertSize;
    }

    public void setBulkInsertSize(int bulkInsertSize) {
        this.bulkInsertSize = bulkInsertSize;
    }

}
