package direnaj.driver;

import java.util.Date;

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

}
