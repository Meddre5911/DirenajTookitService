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

    public static JSONObject getEntities(JSONObject tweet) throws DirenajInvalidJSONException {
        try {
            return (JSONObject) tweet.get("entities");
        } catch (JSONException je) {
            throw new DirenajInvalidJSONException("getEntities : " + je.getMessage() + tweet.toString());
        }
    }

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
                User user = new User(getUserId(userMention), getUserScreenName(userMention));
                userMentions.add(user);
            }
            return userMentions;
        } catch (JSONException je) {
            throw new DirenajInvalidJSONException("getUserMentions : " + je.getMessage());
        }
    }

    public static JSONArray getResults(JSONObject obj) throws DirenajInvalidJSONException {
        try {
            return obj.getJSONArray("results");
        } catch (JSONException e) {
            throw new DirenajInvalidJSONException("getReults : " + e.getMessage());
        }
    }

    public static JSONObject getTweet(JSONObject tweetData) throws DirenajInvalidJSONException {
        try {
            return (JSONObject) tweetData.get("tweet");
        } catch (JSONException e) {
            throw new DirenajInvalidJSONException("getTweet : " + e.getMessage());
        }
    }

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
            User user = new User(getUserId(userJson), getUserScreenName(userJson));
            return user;
        } catch (Exception e) {
            throw new DirenajInvalidJSONException("parseUser : " + e.getMessage());
        }
    }

    public static String getUserId(JSONObject user) throws DirenajInvalidJSONException {
        try {
            return user.getString("id_str");
        } catch (Exception e) {
            throw new DirenajInvalidJSONException("getUserId : " + e.getMessage());
        }
    }

    public static String getUserScreenName(JSONObject user) throws DirenajInvalidJSONException {
        try {
            return user.getString("screen_name");
        } catch (Exception e) {
            throw new DirenajInvalidJSONException("getUserScreenName : " + e.getMessage());
        }
    }

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

    public static Date getTweetCreationDate(JSONObject tweet) throws DirenajInvalidJSONException {
        try {
            String createdTime = String.valueOf(tweet.get("created_at"));
            return DateTimeUtils.getTwitterDateFromRateDieFormat(createdTime);
        } catch (Exception e) {
            throw new DirenajInvalidJSONException("getTweetCreationDate : " + e.getMessage());
        }
    }

}
