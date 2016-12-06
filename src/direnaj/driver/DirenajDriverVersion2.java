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

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import direnaj.adapter.DirenajInvalidJSONException;
import direnaj.domain.User;
import direnaj.twitter.twitter4j.Twitter4jUtil;
import direnaj.util.CollectionUtil;
import twitter4j.HashtagEntity;
import twitter4j.Status;

public class DirenajDriverVersion2 {

	public DBCursor getTweets(String campaignID) throws Exception, DirenajInvalidJSONException {
		DBCursor tweetsOfCampaign = null;
		try {
			DBCollection tweetsCollection = DirenajMongoDriver.getInstance().getTweetsCollection();
			BasicDBObject tweetsRetrievalQuery = new BasicDBObject("campaign_id", campaignID);
			tweetsOfCampaign = tweetsCollection.find(tweetsRetrievalQuery).addOption(Bytes.QUERYOPTION_NOTIMEOUT);
		} catch (Exception e) {
			if (tweetsOfCampaign != null) {
				tweetsOfCampaign.close();
			}
			throw e;
		}
		return tweetsOfCampaign;
	}

	/**
	 * XXX - Bu direk Mongo'ya count atilarak yapilsa, daha dogru olur sanki
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
				Gson gsonObject4Deserialization = Twitter4jUtil.getGsonObject4Deserialization();
				Status status = Twitter4jUtil.deserializeTwitter4jStatusFromGson(gsonObject4Deserialization,
						DirenajDriverUtils.getTweet(tweet).toString());
				// hashtags of a single result
				HashtagEntity[] hashtagEntities = status.getHashtagEntities();
				// populate the temporary list
				for (int j = 0; j < hashtagEntities.length; j++) {
					String tweetHashTag = hashtagEntities[j].getText().toLowerCase(Locale.US);
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

	/**
	 * 
	 * @param campaignID
	 * @param tracedHashtag
	 * @param requestId
	 * @throws Exception
	 * @throws DirenajInvalidJSONException
	 */
	public void saveHashtagUsers2Mongo(String campaignID, String tracedHashtag, String requestId)
			throws Exception, DirenajInvalidJSONException {
		// list for user ids
		Set<User> users = new HashSet<>();
		DBCursor tweetCursor = getTweets(campaignID);
		try {
			while (tweetCursor.hasNext()) {
				JSONObject direnajTweetObject = new JSONObject(tweetCursor.next().toString());
				// "retrieved_by" : "drenaj_toolkit",
				if ("drenaj_toolkit".equals(direnajTweetObject.get("retrieved_by"))) {
					// Logger.getLogger(DirenajDriverVersion2.class).trace("Tweet
					// is retrieved by : drenaj_toolkit");
					Gson gsonObject4Deserialization = Twitter4jUtil.getGsonObject4Deserialization();
					Status status = Twitter4jUtil.deserializeTwitter4jStatusFromGson(gsonObject4Deserialization,
							DirenajDriverUtils.getTweet(direnajTweetObject).toString());
					// hashtags of a single result
					HashtagEntity[] hashtagEntities = status.getHashtagEntities();
					for (HashtagEntity hashtagEntity : hashtagEntities) {
						try {
							String tweetHashTag = hashtagEntity.getText().toLowerCase(Locale.US);
							if (tracedHashtag.equals(tweetHashTag)) {
								users.add(DirenajDriverUtils.parseUser(status));
								break;
							}
						} catch (Exception e) {
							Logger.getLogger(DirenajDriverVersion2.class)
									.error("Direnaj Driver Version 2 - saveHashtagUsers2Mongo", e);
						}
						users = savePreProcessUsersIfNeeded(users, requestId, false);
					}
				} else {
					Logger.getLogger(DirenajDriverVersion2.class).debug("Tweet is retrieved by : drenaj");
					JSONObject tweetData = DirenajDriverUtils.getTweet(direnajTweetObject);
					JSONArray hashtags = DirenajDriverUtils.getHashTags(DirenajDriverUtils.getEntities(tweetData));
					for (int j = 0; j < hashtags.length(); j++) {
						try {
							String tweetHashTag = hashtags.getJSONObject(j).get("text").toString()
									.toLowerCase(Locale.US);
							if (tracedHashtag.equals(tweetHashTag)) {
								users.add(DirenajDriverUtils.parseUser(tweetData));
								break;
							}
						} catch (Exception e) {
							Logger.getLogger(DirenajDriverVersion2.class)
									.error("Direnaj Driver Version 2 - saveHashtagUsers2Mongo", e);
						}
						users = savePreProcessUsersIfNeeded(users, requestId, false);
					}
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
				BasicDBObject duplicateUserControlQuery = new BasicDBObject("requestId", requestId).append("userId",
						user.getUserId());
				DBObject duplicateRecord = preProcessUsersCollections.findOne(duplicateUserControlQuery);
				if (duplicateRecord == null) {
					// Logger.getLogger(OrganizationDetector.class.getSimpleName())
					// .debug("savePreProcessUsersIfNeeded. User ( " +
					// user.getUserId()
					// + " ) will be inserted for requestId : " + requestId);
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
					preprocessUser.put("postCreationDate", user.getCampaignTweetPostDate());
					preprocessUser.put(MongoCollectionFieldNames.MONGO_USER_POST_TWEET_ID, user.getCampaignTweetId());
					preprocessUsers.add(preprocessUser);
				}
			}
			if (preprocessUsers.size() > 0) {
				preProcessUsersCollections.insert(preprocessUsers);
			}
			return new HashSet<User>();
		}
		return users;

	}

}
