package direnaj.functionalities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;
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
import direnaj.domain.UserAccountProperties;
import direnaj.driver.DirenajDriverUtils;
import direnaj.driver.DirenajDriverVersion2;
import direnaj.driver.DirenajMongoDriver;
import direnaj.driver.DirenajMongoDriverUtil;
import direnaj.twitter.UserAccountPropertyAnalyser;
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
        List<User> domainUsers = new Vector<User>();
        DBObject requestIdObj = new BasicDBObject("requestId", requestId);
        // get total user count for detection
        DBCollection orgBehaviorPreProcessUsers = direnajMongoDriver.getOrgBehaviorPreProcessUsers();
        Long preprocessUserCounts = direnajMongoDriver.executeCountQuery(orgBehaviorPreProcessUsers, requestIdObj);
        for (int i = 1; i <= preprocessUserCounts; i++) {
            DBObject preProcessUser = orgBehaviorPreProcessUsers.findOne(requestIdObj);
            User domainUser = analyzePreProcessUser(preProcessUser);
            // do hashtag / mention / url & twitter device ratio
            UserAccountPropertyAnalyser.getInstance().calculateUserAccountProperties(domainUser);
            domainUsers.add(domainUser);
            if ((i == preprocessUserCounts) || domainUsers.size() > DirenajMongoDriver.BULK_INSERT_SIZE) {
                domainUsers = saveOrganizedBehaviourInputData(domainUsers);
            }
        }

    }

    private List<User> saveOrganizedBehaviourInputData(List<User> domainUsers) {
        List<DBObject> allUserInputData = new Vector<>();
        for (User user : domainUsers) {
            UserAccountProperties accountProperties = user.getAccountProperties();
            BasicDBObject userInputData = new BasicDBObject();
            userInputData.put("requestId", requestId);
            userInputData.put("userId", user.getUserId());
            userInputData.put("userScreenName", user.getUserScreenName());

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
