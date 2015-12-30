package direnaj.driver;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import direnaj.domain.User;

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

}
