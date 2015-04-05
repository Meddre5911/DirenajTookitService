package direnaj.functionalities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import direnaj.adapter.DirenajInvalidJSONException;
import direnaj.domain.User;
import direnaj.domain.UserAccountProperties;
import direnaj.driver.DirenajDriverUtils;
import direnaj.driver.DirenajDriverVersion2;
import direnaj.driver.DirenajMongoDriver;
import direnaj.driver.DirenajMongoDriverUtil;
import direnaj.driver.DirenajNeo4jDriver;
import direnaj.servlet.OrganizedBehaviourDetectionRequestType;
import direnaj.twitter.UserAccountPropertyAnalyser;
import direnaj.util.CollectionUtil;
import direnaj.util.DateTimeUtils;
import direnaj.util.ListUtils;
import direnaj.util.TextUtils;

public class OrganizationDetector implements Runnable {

    private DirenajDriverVersion2 direnajDriver;
    private DirenajMongoDriver direnajMongoDriver;
    private String campaignId;
    private Integer topHashtagCount;
    private String requestDefinition;
    private String requestId;
    private List<String> tracedHashtagList;
    private OrganizedBehaviourDetectionRequestType detectionRequestType;

    public OrganizationDetector(String campaignId, int topHashtagCount, String requestDefinition, String tracedHashtag,
            OrganizedBehaviourDetectionRequestType detectionRequestType) {
        direnajDriver = new DirenajDriverVersion2();
        direnajMongoDriver = DirenajMongoDriver.getInstance();
        requestId = generateUniqueId4Request();
        this.campaignId = campaignId;
        this.topHashtagCount = topHashtagCount;
        this.requestDefinition = requestDefinition;
        this.tracedHashtagList = new Vector<>();
        if (!TextUtils.isEmpty(tracedHashtag)) {
            this.tracedHashtagList.add(tracedHashtag);
        }
        this.detectionRequestType = detectionRequestType;
        insertRequest2Mongo();
    }

    @Override
    public void run() {
        try {
            switch (detectionRequestType) {
            case CheckHashtagsInCampaign:
                detectOrganizedBehaviourInHashtags();
                break;
            case CheckSingleHashtagInCampaign:
                getMetricsOfUsersOfHashTag();
                break;
            }
        } catch (Exception e) {
            // FIXME do some logging
            e.printStackTrace();
        }

    }

    public void detectOrganizedBehaviourInHashtags() {
        try {
            Map<String, Double> hashtagCounts = direnajDriver.getHashtagCounts(campaignId);
            // FIXME doğru çalıştığı anlaşıldıktan sonra silinecek
            System.out.println("Hashtag Count Descending");
            for (Entry<String, Double> hashtag : hashtagCounts.entrySet()) {
                System.out.println(hashtag.getKey() + " - " + hashtag.getValue());
            }
            // get hashtag users
            TreeMap<String, Double> topHashtagCounts = CollectionUtil.discardOtherElementsOfMap(hashtagCounts,
                    topHashtagCount);
            // FIXME doğru çalıştığı anlaşıldıktan sonra silinecek
            System.out.println("Top Hashtags Descending");
            for (Entry<String, Double> hashtag : topHashtagCounts.entrySet()) {
                System.out.println(hashtag.getKey() + " - " + hashtag.getValue());
            }
            Set<String> topHashtags = topHashtagCounts.keySet();
            for (String topHashTag : topHashtags) {
                tracedHashtagList.add(topHashTag);
            }
            updateRequestInMongo();
            getMetricsOfUsersOfHashTag();

        } catch (Exception e) {
            // FIXME do some logging
            e.printStackTrace();
        }

    }

    private void updateRequestInMongo() {
        DBCollection organizedBehaviorCollection = direnajMongoDriver.getOrgBehaviorRequestCollection();
        BasicDBObject findQuery = new BasicDBObject();
        findQuery.put("_id", requestId);
        BasicDBObject updateQuery = new BasicDBObject();
        updateQuery.append("$set", new BasicDBObject().append("tracedHashtag", tracedHashtagList));
        organizedBehaviorCollection.update(findQuery, updateQuery);
    }

    private void insertRequest2Mongo() {
        DBCollection organizedBehaviorCollection = direnajMongoDriver.getOrgBehaviorRequestCollection();
        BasicDBObject document = new BasicDBObject();
        document.put("_id", requestId);
        document.put("requestType", detectionRequestType.name());
        document.put("requestDefinition", requestDefinition);
        document.put("campaignId", campaignId);
        document.put("topHashtagCount", topHashtagCount);
        document.put("tracedHashtag", tracedHashtagList);
        document.put("processCompleted", Boolean.FALSE);
        organizedBehaviorCollection.insert(document);
    }

    public void getMetricsOfUsersOfHashTag() throws DirenajInvalidJSONException, Exception {
        for (String tracedHashtag : tracedHashtagList) {
            direnajDriver.saveHashtagUsers2Mongo(campaignId, tracedHashtag, requestId);
            // FIXME burayi unutma
            saveAllUserTweets();
            startUserAnalysis();
        }
    }

    private void saveAllUserTweets() {
        // TODO Auto-generated method stub

    }

    private void startUserAnalysis() throws Exception {
        List<User> domainUsers = new Vector<User>();
        DBObject requestIdObj = new BasicDBObject("requestId", requestId);
        // get total user count for detection
        DBCollection orgBehaviorPreProcessUsers = direnajMongoDriver.getOrgBehaviorPreProcessUsers();
        Long preprocessUserCounts = direnajMongoDriver.executeCountQuery(orgBehaviorPreProcessUsers, requestIdObj);
        List<String> userIds = new Vector<>();
        for (int i = 1; i <= preprocessUserCounts; i++) {
            DBObject preProcessUser = orgBehaviorPreProcessUsers.findOne(requestIdObj);
            User domainUser = analyzePreProcessUser(preProcessUser);
            // do hashtag / mention / url & twitter device ratio
            UserAccountPropertyAnalyser.getInstance().calculateUserAccountProperties(domainUser);
            domainUsers.add(domainUser);
            userIds.add(domainUser.getUserId());
            if ((i == preprocessUserCounts)
                    || domainUsers.size() > DirenajMongoDriver.getInstance().getBulkInsertSize()) {
                domainUsers = saveOrganizedBehaviourInputData(domainUsers);
            }
        }
        calculateClosenessCentrality(userIds);

    }

    private void calculateClosenessCentrality(List<String> userIds) {
        String subgraphEdgeLabel = createSubgraphByAddingEdges(userIds);
        HashMap<String, Double> userClosenessCentralities = calculateInNeo4J(userIds, subgraphEdgeLabel);
        bulkUpdateMongo4ClosenessCentrality(userClosenessCentralities);
        clearNeo4jSubGraph(subgraphEdgeLabel);
    }

    private void clearNeo4jSubGraph(String subgraphEdgeLabel) {
        if (!TextUtils.isEmpty(subgraphEdgeLabel)) {
            String deleteRelationshipCypher = "MATCH (u:User)-[r:" + subgraphEdgeLabel + "]-(t:User) DELETE r";
            DirenajNeo4jDriver.getInstance().executeNoResultCypher(deleteRelationshipCypher);
        }
    }

    private void bulkUpdateMongo4ClosenessCentrality(HashMap<String, Double> userClosenessCentralities) {
        DBCollection processInputDataCollection = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData();
        BulkWriteOperation bulkWriteOperation = processInputDataCollection.initializeUnorderedBulkOperation();

        for (Map.Entry<String, Double> entry : userClosenessCentralities.entrySet()) {
            bulkWriteOperation.find(new BasicDBObject("requestId", requestId).append("userId", entry.getKey()))
                    .updateOne(new BasicDBObject("$set", new BasicDBObject("closenessCentrality", entry.getValue())));

        }
        bulkWriteOperation.execute();
    }

    private HashMap<String, Double> calculateInNeo4J(List<String> userIds, String subgraphEdgeLabel) {
        HashMap<String, Double> userClosenessCentralities = new HashMap<>();
        if (!TextUtils.isEmpty(subgraphEdgeLabel)) {
            for (String userId : userIds) {
                String closenessCalculateQuery = "START rel=relationship(*) " //
                        + "WHERE has(rel.type) and rel.type='" + subgraphEdgeLabel + "'" //
                        + "WITH rel " //
                        + "MATCH (a), (b) WHERE a<>b and a.id_str = '" + userId + "' " //
                        + "WITH length(shortestPath((a)<-[rel:" + subgraphEdgeLabel + "]-(b))) AS dist, a, b " //
                        + "RETURN DISTINCT sum(1.0/dist) AS closenessCentrality";

                Map<String, Object> cypherResult = DirenajNeo4jDriver.getInstance().executeSingleResultCypher(
                        closenessCalculateQuery, ListUtils.getListOfStrings("closenessCentrality"));
                double closenessCentrality = Double.valueOf(cypherResult.get("closenessCentrality").toString());
                userClosenessCentralities.put(userId, closenessCentrality);
            }
        }
        return userClosenessCentralities;
    }

    private String createSubgraphByAddingEdges(List<String> userIds) {
        String newRelationName = "FOLLOWS_" + requestId;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("hopCount", 3);
        params.put("userIds", userIds);

        String cypherQuery = "MATCH p = (begin:User)-[r:FOLLOWS*..{hopCount}]-(end:User) " //
                + "WHERE begin.id_str in {userIds} " //
                + "WITH distinct nodes(p) as nodes " //   
                + "MATCH (x)-[FOLLOWS]->(y) " // find all relationships between nodes
                + "WHERE x in nodes and y in nodes " // which were found earlier
                + "CREATE (x)-[r1:" + newRelationName + " {type:" + newRelationName + "}]->(y) " //
                + "WITH nodes " // 
                + "MATCH (z)<-[FOLLOWS]-(t) " // find all relationships between nodes
                + "WHERE z in nodes and t in nodes " // which were found earlier
                + "CREATE (z)<-[r2:" + newRelationName + " {type:" + newRelationName + "}]-(t) RETURN 1 ";

        DirenajNeo4jDriver.getInstance().executeNoResultCypher(cypherQuery, params);
        return newRelationName;
    }

    private List<User> saveOrganizedBehaviourInputData(List<User> domainUsers) {
        List<DBObject> allUserInputData = new Vector<>();
        for (User user : domainUsers) {
            UserAccountProperties accountProperties = user.getAccountProperties();
            BasicDBObject userInputData = new BasicDBObject();
            userInputData.put("requestId", requestId);
            userInputData.put("userId", user.getUserId());
            userInputData.put("userScreenName", user.getUserScreenName());
            userInputData.put("closenessCentrality", new Double(0));

            userInputData.put("friendFollowerRatio", accountProperties.getFriendFollowerRatio());
            userInputData.put("urlRatio", accountProperties.getUrlRatio());
            userInputData.put("hashtagRatio", accountProperties.getHashtagRatio());
            userInputData.put("mentionRatio", accountProperties.getMentionRatio());
            userInputData.put("postWebDeviceRatio", accountProperties.getWebPostRatio());
            userInputData.put("postMobileDeviceRatio", accountProperties.getMobilePostRatio());
            userInputData.put("postApiDeviceRatio", accountProperties.getApiPostRatio());
            userInputData.put("postThirdPartyDeviceRatio", accountProperties.getThirdPartyPostRatio());

            userInputData.put("isProtected", user.isProtected());
            userInputData.put("isVerified", user.isVerified());
            userInputData.put("creationDate", user.getCreationDate());
            allUserInputData.add(userInputData);
        }
        direnajMongoDriver.getOrgBehaviourProcessInputData().insert(allUserInputData);
        return new Vector<User>();
    }

    private User analyzePreProcessUser(DBObject preProcessUser) throws Exception {
        // get collection
        DBCollection tweetsCollection = direnajMongoDriver.getTweetsCollection();
        // parse user
        User domainUser = DirenajMongoDriverUtil.parsePreProcessUsers(preProcessUser);
        BasicDBObject tweetsRetrievalQuery = new BasicDBObject("tweet.user.id_str", domainUser.getUserId())
                .append("tweet.created_at",
                        new BasicDBObject("$gt", DateTimeUtils.subtractWeeksFromDate(
                                domainUser.getCampaignTweetPostDate(), 2)))
                .append("tweet.created_at",
                        new BasicDBObject("$lt", DateTimeUtils.addWeeksToDate(domainUser.getCampaignTweetPostDate(), 2)));

        DBCursor tweetsOfUser = tweetsCollection.find(tweetsRetrievalQuery);
        try {
            while (tweetsOfUser.hasNext()) {
                JSONObject tweetData = new JSONObject(tweetsOfUser.next().toString());
                JSONObject tweet = DirenajDriverUtils.getTweet(tweetData);
                JSONObject entities = DirenajDriverUtils.getEntities(tweet);
                String tweetPostSource = DirenajDriverUtils.getSource(tweet);
                // FIXME burasi sonradan kullanılacak
                String tweetText = DirenajDriverUtils.getSingleTweetText(tweetData);

                int usedHashtagCount = DirenajDriverUtils.getHashTags(entities).length();
                List<String> urlStrings = DirenajDriverUtils.getUrlStrings(entities);
                int mentionedUserCount = DirenajDriverUtils.getUserMentions(entities).length();
                // get user
                domainUser.incrementPostCount();
                // spam link olayina girersek, url string'leri kullanacagiz
                //                    domainUser.addUrlsToUser(urlStrings);
                domainUser.addValue2CountOfUsedUrls(urlStrings.size());
                domainUser.addValue2CountOfHashtags((double) usedHashtagCount);
                domainUser.addValue2CountOfMentionedUsers((double) mentionedUserCount);
                domainUser.incrementPostDeviceCount(tweetPostSource);
            }
        } catch (JSONException e) {
            tweetsOfUser.close();
            e.printStackTrace();
        }
        return domainUser;
    }

    /**
     * @param campaignId
     * @param limit
     * @return
     * 
     * @deprecated
     */
    private Long getQueryLimit(String campaignId, String limit) {
        Long originalLimit;
        if (TextUtils.isEmpty(limit)) {
            DBObject campaignCountQuery = new BasicDBObject("campaign_id", campaignId);
            // get total tweet count
            originalLimit = direnajMongoDriver.executeCountQuery(direnajMongoDriver.getTweetsCollection(),
                    campaignCountQuery);
        } else {
            originalLimit = Long.valueOf(limit);
        }
        return originalLimit;
    }

    private String generateUniqueId4Request() {
        // get current time
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmssSS");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    public static void main(String[] args) {
        int i = 51 / 50;
        System.out.println(i);
    }
}
