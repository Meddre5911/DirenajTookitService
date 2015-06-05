package testPackage.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import direnaj.domain.User;
import direnaj.driver.DirenajMongoDriver;
import direnaj.driver.DirenajMongoDriverUtil;
import direnaj.util.DateTimeUtils;

public class MongoTester {

    public static void main(String[] args) throws Exception {

        DBObject preProcessUser = DirenajMongoDriver.getInstance().getOrgBehaviorPreProcessUsers()
                .findOne(new BasicDBObject("userId", "1632442946"));
        User domainUser = DirenajMongoDriverUtil.parsePreProcessUsers(preProcessUser);
        BasicDBObject tweetsRetrievalQuery = new BasicDBObject("tweet.user.id_str", domainUser.getUserId()).append(
                "tweet.created_at",
                new BasicDBObject("$gt", DateTimeUtils.subtractWeeksFromDate(domainUser.getCampaignTweetPostDate(), 2))
                        .append("$lt", DateTimeUtils.addWeeksToDate(domainUser.getCampaignTweetPostDate(), 2)));

        DBCursor tweetsOfUser = DirenajMongoDriver.getInstance().getTweetsCollection().find(tweetsRetrievalQuery);
        try {
            while (tweetsOfUser.hasNext()) {
                System.out.println(tweetsOfUser.next().toString());
            }
        } finally {
            tweetsOfUser.close();
        }
    }

}
