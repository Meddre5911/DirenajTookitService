package direnaj.driver;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class DirenajMongoDriver {
    public static final int BULK_INSERT_SIZE = 500;

    private static DirenajMongoDriver mongoDriver = null;
    private MongoClient mongoClient = null;
    private DB mongoDB = null;

    private DirenajMongoDriver() throws UnknownHostException {
        mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
        mongoDB = mongoClient.getDB("direnaj_staging");

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
                // FIXME - do some logging
                e.printStackTrace();
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

}
