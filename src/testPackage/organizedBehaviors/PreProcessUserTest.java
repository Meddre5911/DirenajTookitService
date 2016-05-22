package testPackage.organizedBehaviors;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
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
		DirenajMongoDriver.getInstance().getOrgBehaviorRequestCollection().remove(new BasicDBObject());
		DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations().remove(new BasicDBObject());
		
		
		DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().remove(new BasicDBObject());
		DirenajMongoDriver.getInstance().getOrgBehaviourTweetsOfRequest().remove(new BasicDBObject());

		DirenajMongoDriver.getInstance().getOrgBehaviourUserTweets().remove(new BasicDBObject());

		DirenajMongoDriver.getInstance().getOrgBehaviorPreProcessUsers().remove(new BasicDBObject());
		DirenajMongoDriver.getInstance().getOrgBehaviourProcessCosSimilarityIDF().remove(new BasicDBObject());

		DirenajMongoDriver.getInstance().getOrgBehaviourCosSimilarityTF().remove(new BasicDBObject());
		DirenajMongoDriver.getInstance().getOrgBehaviourTweetsShortInfo().remove(new BasicDBObject());
		DirenajMongoDriver.getInstance().getOrgBehaviourProcessCosSimilarityTF_IDF().remove(new BasicDBObject());
		DirenajMongoDriver.getInstance().getOrgBehaviourProcessTweetSimilarity().remove(new BasicDBObject());
		DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations().remove(new BasicDBObject());

		//
		// // create test entity
		// String requestId = "20160211";
		// BasicDBObject requestIdObj = new
		// BasicDBObject(MongoCollectionFieldNames.MONGO_REQUEST_ID, requestId);
		// User user = createTestUser();
		// // add it to preprocess db
		// BasicDBObject preprocessUser =
		// insertPreProcessUser2Collection(requestId, user);
		// OrganizationDetector organizationDetector = new
		// OrganizationDetector(requestId, true, "");
		//
		//
		// organizationDetector.collectTweetsOfAllUsers(requestId);
		// organizationDetector.saveData4UserAnalysis();
		// organizationDetector.calculateTweetSimilarities();
		// organizationDetector.changeRequestStatusInMongo(true);
		// organizationDetector.removePreProcessUsers();

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
	//
	// public void changeRequestStatusInMongo(boolean requestStatus) {
	// DBCollection organizedBehaviorCollection =
	// direnajMongoDriver.getOrgBehaviorRequestCollection();
	// BasicDBObject findQuery = new BasicDBObject();
	// findQuery.put("_id", requestId);
	// BasicDBObject setQuery = new BasicDBObject();
	// BasicDBObject updateQuery = new BasicDBObject();
	// updateQuery.append("processCompleted", requestStatus);
	// updateQuery.append("statusChangeTime", DateTimeUtils.getLocalDate());
	// updateQuery.append(MongoCollectionFieldNames.MONGO_EARLIEST_TWEET_TIME,
	// TextUtils.getNotNullValue(earliestTweetDate));
	// updateQuery.append(MongoCollectionFieldNames.MONGO_LATEST_TWEET_TIME,
	// TextUtils.getNotNullValue(latestTweetDate));
	//
	//
	// setQuery.append("$set", updateQuery);
	// WriteResult update = organizedBehaviorCollection.update(findQuery,
	// updateQuery,true,false);
	// }

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

	/*
	 * 
	 * 
	 * 
	 * > db.OrgBehaviourUserTweets.findOne() { "_id" :
	 * ObjectId("56bb317dccf23b38c968fc12"), "createdAt" : 736327.559363426,
	 * "id" : NumberLong("681828387309092864"), "text" :
	 * "Flash had more than 300 bugs reported in 2015 alone https://t.co/rK2Fx55EqY via @thenextweb"
	 * , "source" :
	 * "<a href=\"http://twitter.com\" rel=\"nofollow\">Twitter Web Client</a>",
	 * "isTruncated" : false, "inReplyToStatusId" : -1, "inReplyToUserId" : -1,
	 * "isFavorited" : false, "isRetweeted" : false, "favoriteCount" : 0,
	 * "retweetCount" : 0, "isPossiblySensitive" : false, "lang" : "en",
	 * "contributorsIDs" : [ ], "userMentionEntities" : [ { "name" :
	 * "The Next Web", "screenName" : "TheNextWeb", "id" : 10876852, "start" :
	 * 80, "end" : 91 } ], "urlEntities" : [ { "url" :
	 * "https://t.co/rK2Fx55EqY", "expandedURL" : "http://tnw.to/f4xi4",
	 * "displayURL" : "tnw.to/f4xi4", "start" : 52, "end" : 75 } ],
	 * "hashtagEntities" : [ ], "mediaEntities" : [ ], "extendedMediaEntities" :
	 * [ ], "symbolEntities" : [ ], "currentUserRetweetId" : -1, "user" : { "id"
	 * : 78555806, "name" : "Erdem Begenilmis", "screenName" : "Meddre5911",
	 * "location" : "", "description" :
	 * "Happiness is real when it is shared ...", "descriptionURLEntities" : [
	 * ], "isContributorsEnabled" : false, "profileImageUrl" :
	 * "http://pbs.twimg.com/profile_images/489753788152942593/3Buvh6A6_normal.jpeg",
	 * "profileImageUrlHttps" :
	 * "https://pbs.twimg.com/profile_images/489753788152942593/3Buvh6A6_normal.jpeg",
	 * "isDefaultProfileImage" : false, "isProtected" : true, "followersCount" :
	 * 47, "profileBackgroundColor" : "C0DEED", "profileTextColor" : "333333",
	 * "profileLinkColor" : "0084B4", "profileSidebarFillColor" : "DDEEF6",
	 * "profileSidebarBorderColor" : "C0DEED", "profileUseBackgroundImage" :
	 * true, "isDefaultProfile" : true, "showAllInlineMedia" : false,
	 * "friendsCount" : 218, "createdAt" : 734046.381400463, "favouritesCount" :
	 * 114, "utcOffset" : -18000, "timeZone" : "Quito",
	 * "profileBackgroundImageUrl" :
	 * "http://abs.twimg.com/images/themes/theme1/bg.png",
	 * "profileBackgroundImageUrlHttps" :
	 * "https://abs.twimg.com/images/themes/theme1/bg.png",
	 * "profileBackgroundTiled" : false, "lang" : "en", "statusesCount" : 492,
	 * "isGeoEnabled" : false, "isVerified" : false, "translator" : false,
	 * "listedCount" : 1, "isFollowRequestSent" : false }, "quotedStatusId" : -1
	 * }
	 * 
	 * 
	 * 
	 */

}
