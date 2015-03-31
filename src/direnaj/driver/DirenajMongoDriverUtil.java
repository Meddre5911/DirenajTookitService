package direnaj.driver;

import com.mongodb.DBObject;

import direnaj.domain.User;
import direnaj.util.DateTimeUtils;

public class DirenajMongoDriverUtil {

    public static User parsePreProcessUsers(DBObject preProcessUser) throws Exception {
        User user = new User((String) preProcessUser.get("userId"), (String) preProcessUser.get("userScreenName"));
        user.setFollowersCount((double) preProcessUser.get("followerCount"));
        user.setFriendsCount((double) preProcessUser.get("friendCount"));
        user.setProtected((boolean) preProcessUser.get("isProtected"));
        user.setVerified((boolean) preProcessUser.get("isVerified"));
        user.setCreationDate(DateTimeUtils.getTwitterDate((String) preProcessUser.get("creationDate")));
        user.setCampaignTweetPostDate(DateTimeUtils.getTwitterDate((String) preProcessUser.get("postCreationDate")));
        return user;
    }

}
