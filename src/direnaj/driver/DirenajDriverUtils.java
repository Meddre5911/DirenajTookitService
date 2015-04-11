package direnaj.driver;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import direnaj.adapter.DirenajInvalidJSONException;
import direnaj.domain.User;
import direnaj.util.DateTimeUtils;

/**
 * @author Erdem
 * 
 */
public class DirenajDriverUtils {

    /*
     * METHODS SUMMARY
     * 
     * 
     * public static JSONObject getEntities(JSONObject tweet)
     * 
     * public static JSONArray getHashTags(JSONObject entities)
     * 
     * public static JSONArray getUrls(JSONObject entities)
     * 
     * public static JSONArray getResults(JSONObject obj)
     * 
     * public static JSONObject getTweet(JSONObject tweetData)
     * 
     * public static JSONObject getTweetData(JSONArray results, int index)
     * 
     * public static String getSingleTweetText(JSONObject tweetData)
     * 
     * public static ArrayList<Map.Entry<String, Integer>>
     * sortCounts(Hashtable<String, Integer> t)
     */

    /**
     * @param tweet
     *            Tweet JSON object retrieved from Tweet Data JSON object
     * @return Entities JSON object from Tweet JSON object
     * @throws DirenajInvalidJSONException
     */
    public static JSONObject getEntities(JSONObject tweet) throws DirenajInvalidJSONException {
        try {
            return (JSONObject) tweet.get("entities");
        } catch (JSONException je) {
            throw new DirenajInvalidJSONException("getEntities : " + je.getMessage() + tweet.toString());
        }
    }

    public static String getSource(JSONObject tweet) throws DirenajInvalidJSONException {
        try {
            return tweet.getString("source");
        } catch (JSONException je) {
            throw new DirenajInvalidJSONException("getSource : " + je.getMessage() + tweet.toString());
        }
    }

    /**
     * @param entities
     *            Entities JSON object retrieved from Tweet JSON object
     * @return hashtags JSON Array object
     * @throws DirenajInvalidJSONException
     */
    public static JSONArray getHashTags(JSONObject entities) throws DirenajInvalidJSONException {
        try {
            return (JSONArray) entities.get("hashtags");
        } catch (JSONException je) {
            throw new DirenajInvalidJSONException("getHashTags : " + je.getMessage());
        }
    }

    public static String getHashTagText(JSONObject hashtag) throws DirenajInvalidJSONException {
        try {
            if (!hashtag.isNull("text")) {
                return hashtag.getString("text");
            } else {
                return null;
            }
        } catch (JSONException je) {
            throw new DirenajInvalidJSONException("getHashTagText : " + je.getMessage());
        }
    }

    public static List<String> getHashTagsList(JSONObject entities) throws DirenajInvalidJSONException {
        try {
            Vector<String> hashtagLists = new Vector<String>();
            JSONArray hashTags = getHashTags(entities);
            if (hashTags != null) {
                for (int index = 0; index < hashTags.length(); index++) {
                    String hashTagText = getHashTagText(hashTags.getJSONObject(index));
                    hashtagLists.add(hashTagText.toLowerCase(Locale.US));
                }
            }
            return hashtagLists;
        } catch (JSONException je) {
            throw new DirenajInvalidJSONException("getHashTagsList : " + je.getMessage());
        }
    }

    public static JSONArray getUrls(JSONObject entities) throws DirenajInvalidJSONException {
        try {
            return (JSONArray) entities.get("urls");
        } catch (JSONException je) {
            throw new DirenajInvalidJSONException("getUrls : " + je.getMessage());
        }
    }

    public static List<String> getUrlStrings(JSONObject entities) throws DirenajInvalidJSONException {
        try {
            JSONArray urls = getUrls(entities);
            Vector<String> urlStrings = new Vector<String>();
            for (int index = 0; index < urls.length(); index++) {
                JSONObject urlObject = (JSONObject) urls.get(index);
                urlStrings.add(urlObject.getString("expanded_url"));
            }
            return urlStrings;
        } catch (JSONException je) {
            throw new DirenajInvalidJSONException("getUrls : " + je.getMessage());
        }
    }

    /**
     * @param entities
     *            Entities JSON object in Tweet JSON object
     * @return Array of Mentioned Users in tweet
     * @throws DirenajInvalidJSONException
     */
    public static JSONArray getUserMentions(JSONObject entities) throws DirenajInvalidJSONException {
        try {
            return (JSONArray) entities.get("user_mentions");
        } catch (JSONException je) {
            throw new DirenajInvalidJSONException("getUserMentions : " + je.getMessage());
        }
    }

    public static List<User> getUserMentionsList(JSONObject entities) throws DirenajInvalidJSONException {
        try {
            JSONArray userMentionLists = getUserMentions(entities);
            Vector<User> userMentions = new Vector<User>();
            for (int index = 0; index < userMentionLists.length(); index++) {
                JSONObject userMention = (JSONObject) userMentionLists.get(index);
                User user = new User(getUserIdStr(userMention), getUserScreenName(userMention));
                userMentions.add(user);
            }
            return userMentions;
        } catch (JSONException je) {
            throw new DirenajInvalidJSONException("getUserMentions : " + je.getMessage());
        }
    }

    /**
     * @param obj
     *            Main JSON Data retrieved from Direnaj after request is sent
     * @return
     * @throws DirenajInvalidJSONException
     */
    public static JSONArray getResults(JSONObject obj) throws DirenajInvalidJSONException {
        try {
            return obj.getJSONArray("results");
        } catch (JSONException e) {
            throw new DirenajInvalidJSONException("getReults : " + e.getMessage());
        }
    }

    /**
     * @param tweetData
     *            Tweet Data JSON object which is retrived from the Results JSON Array
     * @return tweet JSON object in the tweet data
     * @throws DirenajInvalidJSONException
     */
    public static JSONObject getTweet(JSONObject tweetData) throws DirenajInvalidJSONException {
        try {
            return (JSONObject) tweetData.get("tweet");
        } catch (JSONException e) {
            throw new DirenajInvalidJSONException("getTweet : " + e.getMessage());
        }
    }

    /**
     * @param results
     *            Result Json Array retrieved from Main JSON data that Direnaj is sent
     * @param index
     *            index of Tweet data in the results array
     *            <p>
     *            <b>Note that : </b> Tweet data mentioned here does not only compose of tweet text. It has all data
     *            relevant with tweet such as user, tweet, entities, hashtags etc ...
     * @return tweet JSON object in the array
     * @throws DirenajInvalidJSONException
     */
    public static JSONObject getTweetData(JSONArray results, int index) throws DirenajInvalidJSONException {
        try {
            return (JSONObject) results.get(index);
        } catch (JSONException e) {
            throw new DirenajInvalidJSONException("getTweetData : " + e.getMessage());
        }
    }

    public static JSONObject getUser(JSONObject tweet) throws DirenajInvalidJSONException {
        try {
            return tweet.getJSONObject("user");
        } catch (Exception e) {
            throw new DirenajInvalidJSONException("getUser : " + e.getMessage());
        }
    }

    public static User parseUser(JSONObject tweet) throws DirenajInvalidJSONException {
        try {
            // get user Json object
            JSONObject userJson = getUser(tweet);
            // get user domain
            User user = new User(getUserIdStr(userJson), getUserScreenName(userJson));
            // set user info
            user.setFollowersCount(getUserFollowerCount(userJson));
            user.setFriendsCount(getUserFriendCount(userJson));
            user.setProtected(getUserProtectedInfo(userJson));
            user.setVerified(getUserVerifiedInfo(userJson));
            user.setCreationDate(getObjectCreationDate(userJson));
            user.setCampaignTweetPostDate(getObjectCreationDate(tweet));
            user.setFavoriteCount(getUserFavoriteCount(userJson));
            return user;
        } catch (Exception e) {
            throw new DirenajInvalidJSONException("parseUser : " + e.getMessage());
        }
    }

    /**
     * @param user
     *            User JSON object retrieved from Tweet JSON object
     * @return user verified or not
     * @throws DirenajInvalidJSONException
     */
    public static boolean getUserVerifiedInfo(JSONObject user) throws DirenajInvalidJSONException {
        try {
            return user.getBoolean("verified");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @param user
     *            User JSON object retrieved from Tweet JSON object
     * @return user has a protected account or not
     * @throws DirenajInvalidJSONException
     */
    public static boolean getUserProtectedInfo(JSONObject user) throws DirenajInvalidJSONException {
        try {
            return user.getBoolean("protected");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @param user
     *            User JSON object retrieved from Tweet JSON object
     * @return user follower count
     * @throws DirenajInvalidJSONException
     */
    public static Long getUserFollowerCount(JSONObject user) throws DirenajInvalidJSONException {
        try {
            return user.getLong("followers_count");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @param user
     *            User JSON object retrieved from Tweet JSON object
     * @return user friend count
     * @throws DirenajInvalidJSONException
     */
    public static Long getUserFriendCount(JSONObject user) throws DirenajInvalidJSONException {
        try {
            return user.getLong("friends_count");
        } catch (Exception e) {
            return null;
        }
    }

    public static Long getUserFavoriteCount(JSONObject user) throws DirenajInvalidJSONException {
        try {
            return user.getLong("favourites_count");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @param user
     *            User JSON object retrieved from Tweet JSON object
     * @return userId of user
     * @throws DirenajInvalidJSONException
     */
    public static String getUserIdStr(JSONObject user) throws DirenajInvalidJSONException {
        try {
            return user.getString("id_str");
        } catch (Exception e) {
            throw new DirenajInvalidJSONException("getUserId : " + e.getMessage());
        }
    }

    /**
     * @param user
     *            User JSON object retrieved from Tweet JSON object
     * @return user Screen Name of user
     * @throws DirenajInvalidJSONException
     */
    public static String getUserScreenName(JSONObject user) throws DirenajInvalidJSONException {
        try {
            return user.getString("screen_name");
        } catch (Exception e) {
            throw new DirenajInvalidJSONException("getUserScreenName : " + e.getMessage());
        }
    }

    /**
     * @param tweetData
     *            TweetData JSON object retrieved from Main Results JSON Array
     * @return text of Tweet
     * @throws DirenajInvalidJSONException
     */
    public static String getSingleTweetText(JSONObject tweetData) throws DirenajInvalidJSONException {
        try {
            return tweetData.getJSONObject("tweet").get("text").toString();
        } catch (Exception e) {
            throw new DirenajInvalidJSONException("getTweetText : " + e.getMessage());
        }
    }

    public static User getRepliedUser(JSONObject tweet) throws DirenajInvalidJSONException {
        try {
            User replyUser = null;
            if (!tweet.isNull("in_reply_to_user_id_str") && !tweet.isNull("in_reply_to_screen_name")) {
                String repliedUserId = tweet.getString("in_reply_to_user_id_str");
                String repliedUserScreenName = tweet.getString("in_reply_to_screen_name");
                replyUser = new User(repliedUserId, repliedUserScreenName);
            }
            return replyUser;
        } catch (Exception e) {
            throw new DirenajInvalidJSONException("getRepliedUser : " + e.getMessage());
        }
    }

    public static JSONObject getRetweetedTweet(JSONObject tweet) throws DirenajInvalidJSONException {
        try {
            if (!tweet.isNull("retweeted_status")) {
                return tweet.getJSONObject("retweeted_status");
            }
            return null;
        } catch (Exception e) {
            throw new DirenajInvalidJSONException("getRetweetedTweet : " + e.getMessage());
        }

    }

    public static User getRetweetedUser(JSONObject tweet) throws DirenajInvalidJSONException {
        User retweetedUser = null;
        try {
            JSONObject retweetedTweet = getRetweetedTweet(tweet);
            if (retweetedTweet != null) {
                retweetedUser = parseUser(retweetedTweet);
            }
        } catch (Exception e) {
            throw new DirenajInvalidJSONException("getRetweetedUser : " + e.getMessage());
        }
        return retweetedUser;
    }

    public static Date getObjectCreationDate(JSONObject object) throws DirenajInvalidJSONException {
        try {
            String createdTime = String.valueOf(object.get("created_at"));
            return DateTimeUtils.getTwitterDateFromRataDieFormat(createdTime);
        } catch (Exception e) {
            throw new DirenajInvalidJSONException("getTweetCreationDate : " + e.getMessage());
        }
    }

}
