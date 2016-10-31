package testPackage.organizedBehaviors;

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
//		DirenajMongoDriver.getInstance().getOrgBehaviorRequestCollection().remove(new BasicDBObject());
//		DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations().remove(new BasicDBObject());
//		
//		
//		DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().remove(new BasicDBObject());
//		DirenajMongoDriver.getInstance().getOrgBehaviourTweetsOfRequest().remove(new BasicDBObject());
//
//		DirenajMongoDriver.getInstance().getOrgBehaviourUserTweets().remove(new BasicDBObject());
//
//		DirenajMongoDriver.getInstance().getOrgBehaviorPreProcessUsers().remove(new BasicDBObject());
//		DirenajMongoDriver.getInstance().getOrgBehaviourProcessCosSimilarityIDF().remove(new BasicDBObject());
//
//		DirenajMongoDriver.getInstance().getOrgBehaviourCosSimilarityTF().remove(new BasicDBObject());
//		DirenajMongoDriver.getInstance().getOrgBehaviourTweetsShortInfo().remove(new BasicDBObject());
//		DirenajMongoDriver.getInstance().getOrgBehaviourProcessCosSimilarityTF_IDF().remove(new BasicDBObject());
//		DirenajMongoDriver.getInstance().getOrgBehaviourProcessTweetSimilarity().remove(new BasicDBObject());
//		DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations().remove(new BasicDBObject());

	
		
	
		
		
		
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
