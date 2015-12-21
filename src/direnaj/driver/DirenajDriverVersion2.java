package direnaj.driver;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Logger;
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

    /**
     * FIXME - Bu direk Mongo'ya count atilarak yapilsa, daha dogru olur sanki
     * 
     * @param campaignID
     * @return
     * @throws Exception
     * @throws DirenajInvalidJSONException
     */
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
            Logger.getLogger(DirenajDriverVersion2.class).error("Direnaj Driver Version 2 - getHashtagCounts", e);
        } finally {
            tweetCursor.close();
        }
        return CollectionUtil.sortByComparator(hashtagCounts);
    }

    public void saveHashtagUsers2Mongo(String campaignID, String tracedHashtag, String requestId) throws Exception,
            DirenajInvalidJSONException {
        // list for user ids
        Set<User> users = new HashSet<>();
        DBCursor tweetCursor = getTweets(campaignID);
        try {
            while (tweetCursor.hasNext()) {
                JSONObject direnajTweetObject = new JSONObject(tweetCursor.next().toString());
                JSONObject tweetData = DirenajDriverUtils.getTweet(direnajTweetObject);
                JSONArray hashtags = DirenajDriverUtils.getHashTags(DirenajDriverUtils.getEntities(tweetData));
                for (int j = 0; j < hashtags.length(); j++) {
                    try {
                        String tweetHashTag = hashtags.getJSONObject(j).get("text").toString().toLowerCase(Locale.US);
                        if (tracedHashtag.equals(tweetHashTag)) {
                            users.add(DirenajDriverUtils.parseUser(tweetData));
                            break;
                        }
                    } catch (Exception e) {
                        Logger.getLogger(DirenajDriverVersion2.class).error(
                                "Direnaj Driver Version 2 - saveHashtagUsers2Mongo", e);
                    }
                    users = savePreProcessUsersIfNeeded(users, requestId, false);
                }
            }
        } finally {
            tweetCursor.close();
        }
        savePreProcessUsersIfNeeded(users, requestId, true);
    }

    private Set<User> savePreProcessUsersIfNeeded(Set<User> users, String requestId, boolean saveAnyway)
            throws DirenajInvalidJSONException {
        if (saveAnyway || users.size() > DirenajMongoDriver.getInstance().getBulkInsertSize()) {
            DBCollection preProcessUsersCollections = DirenajMongoDriver.getInstance().getOrgBehaviorPreProcessUsers();
            List<DBObject> preprocessUsers = new Vector<>();
            for (User user : users) {
            	// FIXME her user icin DB'ye gitmek sikinti yaratabilir. Bir incele
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
                    preprocessUser.put("favoriteCount", user.getFavoriteCount());
                    preprocessUser.put("isProtected", user.isProtected());
                    preprocessUser.put("isVerified", user.isVerified());
                    preprocessUser.put("creationDate", user.getCreationDate());
                    // FIXME 20150604 bu property'nin aynı hashtag için birden fazla kez atılmış tweet için kullanılabilmesi lazım,
                    // ona gore degisikligi yap
                    preprocessUser.put("postCreationDate", user.getCampaignTweetPostDate());
                    preprocessUsers.add(preprocessUser);
                }
            }
            preProcessUsersCollections.insert(preprocessUsers);
            return new HashSet<User>();
        }
        return users;

    }

}
