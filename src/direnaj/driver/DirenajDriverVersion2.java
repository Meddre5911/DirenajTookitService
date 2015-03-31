package direnaj.driver;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import direnaj.adapter.DirenajDataHandler;
import direnaj.adapter.DirenajInvalidJSONException;
import direnaj.domain.User;
import direnaj.util.CollectionUtil;

public class DirenajDriverVersion2 {

    private int oneTimeCallLimit = 400;

    private String userID;
    private String password;

    public DirenajDriverVersion2(String uID, String pass) {
        userID = uID;
        password = pass;
    }

    public List<JSONObject> getTweets(String campaignID, int skip, int originalLimit) throws Exception,
            DirenajInvalidJSONException {
        Vector<JSONObject> tweets = new Vector<JSONObject>();
        int totalLoopCount = (int) Math.ceil((double) originalLimit / oneTimeCallLimit);
        int requestLimit = originalLimit % oneTimeCallLimit;

        for (int loopCount = 0; loopCount < totalLoopCount; loopCount++) {
            // get data from dataHandler
            JSONObject obj = DirenajDataHandler.getData(this.userID, this.password, campaignID, skip, requestLimit);
            JSONArray results = DirenajDriverUtils.getResults(obj);

            for (int i = 0; i < results.length(); i++) {
                try {
                    tweets.add(DirenajDriverUtils.getTweetData(results, i));
                } catch (JSONException e) {
                    // FIXME - do some logging
                    e.printStackTrace();
                    continue;
                }
            }
            skip += requestLimit * loopCount;
            requestLimit = oneTimeCallLimit;
        }
        return tweets;
    }

    public Map<String, Double> getHashtagCounts(String campaignID, int skip, Long originalLimit) throws Exception,
            DirenajInvalidJSONException {
        // map for hashtags
        TreeMap<String, Double> hashtagCounts = new TreeMap<>();
        // request load balancing
        int totalLoopCount = (int) Math.ceil((double) originalLimit / oneTimeCallLimit);
        long requestLimit = originalLimit % oneTimeCallLimit;

        for (int loopCount = 0; loopCount < totalLoopCount; loopCount++) {
            List<JSONObject> tweets = getTweets(campaignID, skip, (int) requestLimit);
            for (JSONObject tweet : tweets) {
                // hashtags of a single result
                JSONArray hashtags = DirenajDriverUtils.getHashTags(DirenajDriverUtils.getEntities(DirenajDriverUtils
                        .getTweet(tweet)));
                // populate the temporary list
                for (int j = 0; j < hashtags.length(); j++) {
                    String tweetHashTag = hashtags.getJSONObject(j).get("text").toString().toLowerCase(Locale.US);
                    CollectionUtil.incrementKeyValueInMap(hashtagCounts, tweetHashTag);
                }
            }
            skip += requestLimit * loopCount;
            requestLimit = oneTimeCallLimit;
        }
        return CollectionUtil.sortByComparator(hashtagCounts);
    }

    public void saveHashtagUsers2Mongo(String campaignID, String tracedHashtag, int skip, Long originalLimit,
            String requestId) throws Exception, DirenajInvalidJSONException {
        // list for user ids
        List<User> users = new Vector<>();
        // request load balancing
        int totalLoopCount = (int) Math.ceil((double) originalLimit / oneTimeCallLimit);
        long requestLimit = originalLimit % oneTimeCallLimit;

        for (int loopCount = 1; loopCount <= totalLoopCount; loopCount++) {
            List<JSONObject> tweets = getTweets(campaignID, skip, (int) requestLimit);
            for (JSONObject direnajTweetObject : tweets) {
                // hashtags of a single result
                JSONObject tweetData = DirenajDriverUtils.getTweet(direnajTweetObject);
                JSONArray hashtags = DirenajDriverUtils.getHashTags(DirenajDriverUtils.getEntities(tweetData));
                for (int j = 0; j < hashtags.length(); j++) {
                    String tweetHashTag = hashtags.getJSONObject(j).get("text").toString().toLowerCase(Locale.US);
                    if (tracedHashtag.equals(tweetHashTag)) {
                        users.add(DirenajDriverUtils.parseUser(tweetData));
                        break;
                    }
                }
            }
            skip += requestLimit * loopCount;
            requestLimit = oneTimeCallLimit;

            users = savePreProcessUsersIfNeeded(users, requestId, loopCount, totalLoopCount);
        }
        // FIXME do some logging
    }

    private List<User> savePreProcessUsersIfNeeded(List<User> users, String requestId, int loopCount, int totalLoopCount)
            throws DirenajInvalidJSONException {
        if ((loopCount == totalLoopCount) || users.size() > DirenajMongoDriver.BULK_INSERT_SIZE) {
            DBCollection preProcessUsersCollections = DirenajMongoDriver.getInstance().getOrgBehaviorPreProcessUsers();
            List<DBObject> preprocessUsers = new Vector<>();
            for (User user : users) {
                BasicDBObject duplicateUserControlQuery = new BasicDBObject("requestId", requestId).append("userId",
                        user.getUserId());
                DBObject duplicateRecord = preProcessUsersCollections.findOne(duplicateUserControlQuery);
                if (duplicateRecord == null) {
                    BasicDBObject preprocessUser = new BasicDBObject();
                    preprocessUser.put("requestId", requestId);
                    preprocessUser.put("userId", user.getUserId());
                    preprocessUser.put("userScreenName", user.getUserScreenName());
                    preprocessUser.put("friendFollowerRatio", user.calculateFriendFollowerRatio());
                    preprocessUser.put("friendCount", user.getFriendsCount());
                    preprocessUser.put("followerCount", user.getFollowersCount());
                    preprocessUser.put("isProtected", user.isProtected());
                    preprocessUser.put("isVerified", user.isVerified());
                    preprocessUser.put("creationDate", user.getCreationDate());
                    preprocessUser.put("postCreationDate", user.getCampaignTweetPostDate());
                    preprocessUsers.add(preprocessUser);
                }
            }
            preProcessUsersCollections.insert(preprocessUsers);
            return new Vector<User>();
        }
        return users;

    }

}
