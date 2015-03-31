package direnaj.functionalities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import direnaj.adapter.DirenajInvalidJSONException;
import direnaj.domain.User;
import direnaj.driver.DirenajDriverUtils;
import direnaj.driver.DirenajDriverVersion2;
import direnaj.driver.DirenajMongoDriver;
import direnaj.driver.DirenajMongoDriverUtil;
import direnaj.util.CollectionUtil;
import direnaj.util.DateTimeUtils;
import direnaj.util.TextUtils;

public class OrganizationDetector {

    private DirenajDriverVersion2 direnajDriver;
    private DirenajMongoDriver direnajMongoDriver;
    private Long originalLimit;
    private Integer skipValue;
    private String campaignId;
    private Integer topHashtagCount;
    private String requestDefinition;
    private String requestId;
    private String tracedHashtag;

    public OrganizationDetector(String direnajUserId, String direnajPassword, String campaignId, int topHashtagCount,
            String requestDefinition, String skip, String limit, String tracedHashtag) {
        direnajDriver = new DirenajDriverVersion2(direnajUserId, direnajPassword);
        direnajMongoDriver = DirenajMongoDriver.getInstance();
        originalLimit = getQueryLimit(campaignId, limit);
        skipValue = TextUtils.getIntegerValue(skip);
        requestId = generateUniqueId4Request();
        this.campaignId = campaignId;
        this.topHashtagCount = topHashtagCount;
        this.requestDefinition = requestDefinition;
        this.tracedHashtag = tracedHashtag;
        insertRequest2Mongo();
    }

    public void detectOrganizedBehaviourInHashtags() {
        try {
            Map<String, Double> hashtagCounts = direnajDriver.getHashtagCounts(campaignId, skipValue, originalLimit);
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
                getMetricsOfUsersOfHashTag();
            }

        } catch (Exception e) {
            // FIXME do some logging
            e.printStackTrace();
        }

    }

    private void insertRequest2Mongo() {
        DBCollection organizedBehaviorCollection = direnajMongoDriver.getOrgBehaviorRequestCollection();
        BasicDBObject document = new BasicDBObject();
        document.put("_id", requestId);
        document.put("requestDefinition", requestDefinition);
        document.put("campaignId", campaignId);
        document.put("topHashtagCount", topHashtagCount);
        document.put("skipValue", skipValue);
        document.put("originalLimit", originalLimit);
        document.put("tracedHashtag", tracedHashtag);
        document.put("processCompleted", Boolean.FALSE);
        organizedBehaviorCollection.insert(document);
    }

    public void getMetricsOfUsersOfHashTag() throws DirenajInvalidJSONException, Exception {
        direnajDriver.saveHashtagUsers2Mongo(campaignId, tracedHashtag, skipValue, originalLimit, requestId);
        // FIXME burayi unutma
        saveAllUserTweets();
        startUserAnalysis();

    }

    private void saveAllUserTweets() {
        // TODO Auto-generated method stub

    }

    private void startUserAnalysis() throws Exception {
        // get collections
        DBCollection tweetsCollection = direnajMongoDriver.getTweetsCollection();

        DBObject requestIdObj = new BasicDBObject("requestId", requestId);
        // get total user count for detection
        DBCollection orgBehaviorPreProcessUsers = direnajMongoDriver.getOrgBehaviorPreProcessUsers();
        Long preprocessUserCounts = direnajMongoDriver.executeCountQuery(orgBehaviorPreProcessUsers, requestIdObj);
        for (int i = 1; i <= preprocessUserCounts; i++) {
            DBObject preProcessUser = orgBehaviorPreProcessUsers.findOne(requestIdObj);
            User domainUser = DirenajMongoDriverUtil.parsePreProcessUsers(preProcessUser);

            BasicDBObject tweetsRetrievalQuery = new BasicDBObject("tweet.user.id_str", domainUser.getUserId()).append(
                    "tweet.created_at",
                    new BasicDBObject("$gt", DateTimeUtils.subtractWeeksFromDate(domainUser.getCampaignTweetPostDate(),
                            2))).append("tweet.created_at",
                    new BasicDBObject("$lt", DateTimeUtils.addWeeksToDate(domainUser.getCampaignTweetPostDate(), 2)));

            DBCursor tweetsOfUser = tweetsCollection.find(tweetsRetrievalQuery);
            try {
                while (tweetsOfUser.hasNext()) {
                    JSONObject tweetData = new JSONObject(tweetsOfUser.next().toString());
                    JSONObject tweet = DirenajDriverUtils.getTweet(tweetData);
                    JSONObject entities = DirenajDriverUtils.getEntities(tweet);
                    int usedHashtagCount = DirenajDriverUtils.getHashTags(entities).length();
                    List<String> urlStrings = DirenajDriverUtils.getUrlStrings(entities);
                    int mentionedUserCount = DirenajDriverUtils.getUserMentions(entities).length();
                    // get user
                    String tweetText = DirenajDriverUtils.getSingleTweetText(tweetData);
                    domainUser.addPost(tweetText);
                    domainUser.addUrlsToUser(urlStrings);
                    domainUser.addValue2CountOfHashtags((double) usedHashtagCount);
                    domainUser.addValue2CountOfMentionedUsers((double) mentionedUserCount);
                }
            } catch (JSONException e) {
                tweetsOfUser.close();
                e.printStackTrace();
            }

            // do hashtag / mention / url & twitter device ratio

        }

    }

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

}
