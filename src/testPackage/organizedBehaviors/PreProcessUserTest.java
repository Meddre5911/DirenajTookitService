package testPackage.organizedBehaviors;

import org.apache.log4j.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

import direnaj.domain.User;
import direnaj.driver.DirenajMongoDriver;
import direnaj.driver.MongoCollectionFieldNames;
import direnaj.functionalities.organizedBehaviour.OrganizationDetector;
import direnaj.util.DateTimeUtils;
import direnaj.util.TextUtils;

public class PreProcessUserTest {

	public static void main(String[] args) throws Exception {

		// delete object
		// DirenajMongoDriver.getInstance().getOrgBehaviorRequestCollection().remove(new
		// BasicDBObject());
		// DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations().remove(new
		// BasicDBObject());
		//
		//
		// DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().remove(new
		// BasicDBObject());
		// DirenajMongoDriver.getInstance().getOrgBehaviourTweetsOfRequest().remove(new
		// BasicDBObject());
		//
		// DirenajMongoDriver.getInstance().getOrgBehaviourUserTweets().remove(new
		// BasicDBObject());
		//
		// DirenajMongoDriver.getInstance().getOrgBehaviorPreProcessUsers().remove(new
		// BasicDBObject());
		// DirenajMongoDriver.getInstance().getOrgBehaviourProcessCosSimilarityIDF().remove(new
		// BasicDBObject());
		//
		// DirenajMongoDriver.getInstance().getOrgBehaviourCosSimilarityTF().remove(new
		// BasicDBObject());
		// DirenajMongoDriver.getInstance().getOrgBehaviourTweetsShortInfo().remove(new
		// BasicDBObject());
		// DirenajMongoDriver.getInstance().getOrgBehaviourProcessCosSimilarityTF_IDF().remove(new
		// BasicDBObject());
		// DirenajMongoDriver.getInstance().getOrgBehaviourProcessTweetSimilarity().remove(new
		// BasicDBObject());
		// DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations().remove(new
		// BasicDBObject());

		BasicDBObject queryObject = new BasicDBObject("requestId", "4345534");
		
//		DirenajMongoDriver.getInstance().getOrgBehaviorPreProcessUsers()
//				.remove(queryObject);
//		insertPreProcessUser2Collection("4345534", createTestUser());
//		insertPreProcessUser2Collection("4345534", createTestUser());
		
//		BasicDBObject updateQuery = new BasicDBObject();
//		updateQuery.append("$addToSet", new BasicDBObject().append("multiDeneme", "c"));
//		
//		
//		WriteResult updateMulti = DirenajMongoDriver.getInstance().getOrgBehaviorPreProcessUsers().updateMulti(queryObject,updateQuery );
//		updateMulti.getN();
		
		
		
		DBObject distinctRetweetUserQuery = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, "NBAwards")
				.append("$where", "this.hashtagEntities.length > 0")
				.append(MongoCollectionFieldNames.MONGO_TWEET_HASHTAG_ENTITIES_TEXT,
						new BasicDBObject("$regex", "NBAwards").append("$options", "i"))
				.append("retweetedStatus.id", new BasicDBObject("$exists", true));

		int distinctRetweetUserCount = DirenajMongoDriver.getInstance().getOrgBehaviourUserTweets()
				.distinct("user.id", distinctRetweetUserQuery).size();
		
		System.out.println(distinctRetweetUserCount);
		

	}
	
	private void updateRequestInMongoByColumnName(String columnName, Object updateValue) {
		Logger.getLogger(OrganizationDetector.class)
				.debug("updateRequestInMongo4ResumeProcess - do Upsert for requestId : " + "647843");
		DBCollection organizedBehaviorCollection = 	DirenajMongoDriver.getInstance().getOrgBehaviorRequestCollection();
		BasicDBObject findQuery = new BasicDBObject();
		findQuery.put("_id", "kjdl");
		BasicDBObject updateQuery = new BasicDBObject();
		updateQuery.append("$set", new BasicDBObject().append(columnName, updateValue));
		organizedBehaviorCollection.update(findQuery, updateQuery, true, false);
	}

	private static BasicDBObject insertPreProcessUser2Collection(String requestId, User user) {
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

		String[] arr = { "a" };
		preprocessUser.put("multiDeneme", arr);

		DirenajMongoDriver.getInstance().getOrgBehaviorPreProcessUsers().insert(preprocessUser);
		return preprocessUser;
	}

	private static User createTestUser() throws Exception {
		// define user test
		User user = new User("78555806", "Meddre5911");
		user.setFriendsCount(218);
		user.setFollowersCount(47);
		user.setFavoriteCount(114);
		user.setProtected(true);
		user.setVerified(false);
		user.setCreationDate(DateTimeUtils.getTwitterDateFromRataDieFormat("734046.381400463"));
		user.setCampaignTweetPostDate(DateTimeUtils.getTwitterDate("Sun Jan 24 15:30:25 +0000 2016"));
		user.setCampaignTweetId("691281908727156736");
		return user;
	}

}
