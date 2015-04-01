package direnaj.driver;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import direnaj.adapter.DirenajInvalidJSONException;
import direnaj.domain.User;
import direnaj.util.CollectionUtil;

public class DirenajDriverVersion2 {

    public DBCursor getTweets(String campaignID) throws Exception, DirenajInvalidJSONException {
        DBCollection tweetsCollection = DirenajMongoDriver.getInstance().getTweetsCollection();
        BasicDBObject tweetsRetrievalQuery = new BasicDBObject("campaign_id", campaignID);
        DBCursor tweetsOfCampaign = tweetsCollection.find(tweetsRetrievalQuery);
        return tweetsOfCampaign;
    }

    public Map<String, Double> getHashtagCounts(String campaignID) throws Exception, DirenajInvalidJSONException {
        // map for hashtags
        TreeMap<String, Double> hashtagCounts = new TreeMap<>();

        DBCursor tweetCursor = getTweets(campaignID);
        try {
            while (tweetCursor.hasNext()) {
                JSONObject tweet = new JSONObject(tweetCursor.next().toString());
                // hashtags of a single result
                JSONArray hashtags = DirenajDriverUtils.getHashTags(DirenajDriverUtils.getEntities(DirenajDriverUtils
                        .getTweet(tweet)));
                // populate the temporary list
                for (int j = 0; j < hashtags.length(); j++) {
                    String tweetHashTag = hashtags.getJSONObject(j).get("text").toString().toLowerCase(Locale.US);
                    CollectionUtil.incrementKeyValueInMap(hashtagCounts, tweetHashTag);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            tweetCursor.close();
        }

        return CollectionUtil.sortByComparator(hashtagCounts);
    }

    public void saveHashtagUsers2Mongo(String campaignID, String tracedHashtag, String requestId) throws Exception,
            DirenajInvalidJSONException {
        // list for user ids
        List<User> users = new Vector<>();
        DBCursor tweetCursor = getTweets(campaignID);
        try {
            while (tweetCursor.hasNext()) {
                JSONObject direnajTweetObject = new JSONObject(tweetCursor.next().toString());
                JSONObject tweetData = DirenajDriverUtils.getTweet(direnajTweetObject);
                JSONArray hashtags = DirenajDriverUtils.getHashTags(DirenajDriverUtils.getEntities(tweetData));
                for (int j = 0; j < hashtags.length(); j++) {
                    String tweetHashTag = hashtags.getJSONObject(j).get("text").toString().toLowerCase(Locale.US);
                    if (tracedHashtag.equals(tweetHashTag)) {
                        users.add(DirenajDriverUtils.parseUser(tweetData));
                        break;
                    }
                }
                users = savePreProcessUsersIfNeeded(users, requestId, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            tweetCursor.close();
        }
        savePreProcessUsersIfNeeded(users, requestId, true);
    }

    private List<User> savePreProcessUsersIfNeeded(List<User> users, String requestId, boolean saveAnyway)
            throws DirenajInvalidJSONException {
        if (saveAnyway || users.size() > DirenajMongoDriver.getInstance().getBulkInsertSize()) {
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
