package direnaj.driver;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import direnaj.domain.User;
import direnaj.functionalities.organizedBehaviour.CosineSimilarityRequestData;
import direnaj.functionalities.organizedBehaviour.ResumeBreakPoint;
import direnaj.util.TextUtils;

public class DirenajMongoDriverUtil {

	public static User parsePreProcessUsers(DBObject preProcessUser) throws Exception {
		User user = new User((String) preProcessUser.get("userId"), (String) preProcessUser.get("userScreenName"));
		user.setFollowersCount((double) preProcessUser.get("followerCount"));
		user.setFriendsCount((double) preProcessUser.get("friendCount"));
		user.setFavoriteCount((double) preProcessUser.get("favoriteCount"));
		user.setProtected((boolean) preProcessUser.get("isProtected"));
		user.setVerified((boolean) preProcessUser.get("isVerified"));
		user.setCreationDate((Date) preProcessUser.get("creationDate"));
		user.setCampaignTweetPostDate((Date) preProcessUser.get("postCreationDate"));
		user.setCampaignTweetId((String) preProcessUser.get(MongoCollectionFieldNames.MONGO_USER_POST_TWEET_ID));
		return user;
	}

	public static List<DBObject> insertBulkData2CollectionIfNeeded(DBCollection dbCollection, List<DBObject> dbObjects,
			boolean saveAnyway) {
		if (dbObjects.size() > 0
				&& (saveAnyway || dbObjects.size() >= DirenajMongoDriver.getInstance().getBulkInsertSize())) {
			dbCollection.insert(dbObjects);
			return new ArrayList<DBObject>(DirenajMongoDriver.getInstance().getBulkInsertSize());
		}
		return dbObjects;
	}

	public static String getSuitableColumnName(String str) {
		// mongo collection column can not have '.' in it
		if (str.contains(".")) {
			str = str.replace('.', '_');
		}
		if (str.equals("_id")) {
			str = "id";
		}
		// parse $ chars
		return parseInvalidChars(str);
	}

	public static String parseInvalidChars(String str) {
		if (TextUtils.isEmpty(str) || !str.startsWith("$")) {
			return str;
		}
		if (str.startsWith("$")) {
			str = str.substring(1);
		}
		return parseInvalidChars(str);
	}

	public static String getTweetText4CosSimilarity(Long tweetId) {
		BasicDBObject query = new BasicDBObject("id", tweetId);
		DBObject findOne = DirenajMongoDriver.getInstance().getOrgBehaviourUserTweets().findOne(query);
		return TextUtils.getNotNullValue(findOne.get("text"));
	}


	public static void cleanData4ResumeProcess(DBObject requestObj, ResumeBreakPoint resumeBreakPoint) {
		if (resumeBreakPoint != null) {
			switch (resumeBreakPoint) {
			case INIT:
				DirenajMongoDriver.getInstance().getOrgBehaviorPreProcessUsers().remove(requestObj);
				Logger.getLogger(DirenajMongoDriverUtil.class).debug("INIT phase unnecessary data is removed");
			case TWEET_COLLECTION_COMPLETED:
				DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().remove(requestObj);
				DirenajMongoDriver.getInstance().getOrgBehaviourTweetsOfRequest().remove(requestObj);
				Logger.getLogger(DirenajMongoDriverUtil.class)
						.debug("TWEET_COLLECTION_COMPLETED phase unnecessary data is removed");
				break;
			case USER_ANALYZE_COMPLETED:
				break;
			default:
			}

		}
	}

	public static void cleanData4ResumeProcess(CosineSimilarityRequestData cosSimilarityRequestData) {
		if (cosSimilarityRequestData.getResumeBreakPoint() != null) {
			switch (cosSimilarityRequestData.getResumeBreakPoint()) {
			case COS_SIMILARITY_INIT:
				DirenajMongoDriver.getInstance().getOrgBehaviourCosSimilarityTF()
						.remove(cosSimilarityRequestData.getRequestIdObject());
				DirenajMongoDriver.getInstance().getOrgBehaviourTweetsShortInfo()
						.remove(cosSimilarityRequestData.getRequestIdObject());
				Logger.getLogger(DirenajMongoDriverUtil.class)
						.debug("COS_SIMILARITY_INIT phase unnecessary data is removed");
			case TF_CALCULATION_COMPLETED:
				DirenajMongoDriver.getInstance().getOrgBehaviourProcessCosSimilarityIDF()
						.remove(cosSimilarityRequestData.getRequestIdObject());
				Logger.getLogger(DirenajMongoDriverUtil.class)
						.debug("TF_CALCULATION_COMPLETED phase unnecessary data is removed");
			case IDF_CALCULATION_COMPLETED:
				DirenajMongoDriver.getInstance().getOrgBehaviourProcessCosSimilarityTF_IDF()
						.remove(cosSimilarityRequestData.getRequestIdObject());
				Logger.getLogger(DirenajMongoDriverUtil.class)
						.debug("IDF_CALCULATION_COMPLETED phase unnecessary data is removed");
			case TF_IDF_CALCULATION_COMPLETED:
				DirenajMongoDriver.getInstance().getOrgBehaviourProcessTweetSimilarity()
						.remove(cosSimilarityRequestData.getRequestIdObject());
				Logger.getLogger(DirenajMongoDriverUtil.class)
						.debug("TF_IDF_CALCULATION_COMPLETED phase unnecessary data is removed");
				break;
			case SIMILARTY_CALCULATED:
				break;
			default:
			}

		}
	}

}
