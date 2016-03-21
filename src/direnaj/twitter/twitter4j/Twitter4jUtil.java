package direnaj.twitter.twitter4j;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import direnaj.domain.User;
import direnaj.driver.DirenajMongoDriver;
import direnaj.util.DateTimeUtils;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

public class Twitter4jUtil {

	public static void saveTweetsOfUser(User user) {
		try {
			getEarliestTweets(user);
			Logger.getLogger(Twitter4jUtil.class.getSimpleName())
					.debug("Earliest Tweets has been collected for User Name :" + user.getUserScreenName()
							+ ", User Id :" + user.getUserId());
			getRecentTweets(user);
			Logger.getLogger(Twitter4jUtil.class.getSimpleName())
					.debug("Recent Tweets has been collected for User Name :" + user.getUserScreenName() + ", User Id :"
							+ user.getUserId());
		} catch (TwitterException e) {
			Logger.getLogger(Twitter4jUtil.class.getSimpleName()).error("Twitter4jUtil saveTweetsOfUser", e);
			e.printStackTrace();
		}
	}

	private static void getEarliestTweets(User user) throws TwitterException {
		Date lowestDate = DateTimeUtils.subtractWeeksFromDateInDateFormat(user.getCampaignTweetPostDate(), 2);
		// first collect earlier tweets
		int pageNumber = 1;
		Paging paging = new Paging(1, 200);
		paging.setMaxId(Long.valueOf(user.getCampaignTweetId()));
		for (int i = 0; i < 20; i++) {
			boolean isEarlierTweetsRemaining = false;
			ResponseList<Status> userTimeline = Twitter4jPool.getInstance()
					.getAvailableTwitterObject(TwitterRestApiOperationTypes.STATUS_USERTIMELINE)
					.getUserTimeline(Long.valueOf(user.getUserId()), paging);
			saveTweets(userTimeline);
			// Status To JSON String
			int arraySize = userTimeline.size();
			if (arraySize >= 1) {
				Date tweetCreationDate = userTimeline.get(arraySize - 1).getCreatedAt();
				if (tweetCreationDate.after(lowestDate)) {
					isEarlierTweetsRemaining = true;
					paging.setPage(++pageNumber);
				}
			}
			if (!isEarlierTweetsRemaining) {
				break;
			}
		}
	}

	private static void getRecentTweets(User user) throws TwitterException {
		// get lowest & highest dates
		Date highestDate = DateTimeUtils.addWeeksToDateInDateFormat(user.getCampaignTweetPostDate(), 2);
		// first collect earlier tweets
		int pageNumber = 1;
		Paging paging = new Paging(1, 200);
		paging.setSinceId(Long.valueOf(user.getCampaignTweetId()));
		for (int i = 0; i < 20; i++) {
			boolean isRecentTweetsRemaining = false;
			ResponseList<Status> userTimeline = Twitter4jPool.getInstance()
					.getAvailableTwitterObject(TwitterRestApiOperationTypes.STATUS_USERTIMELINE)
					.getUserTimeline(Long.valueOf(user.getUserId()), paging);
			saveTweets(userTimeline);
			// Status To JSON String
			int arraySize = userTimeline.size();
			if (arraySize >= 1) {
				Date tweetCreationDate = userTimeline.get(arraySize - 1).getCreatedAt();
				if (tweetCreationDate.before(highestDate)) {
					paging.setPage(++pageNumber);
					isRecentTweetsRemaining = true;
				}
			}
			if (!isRecentTweetsRemaining) {
				break;
			}
		}
	}

	private static void saveTweets(ResponseList<Status> userTimeline) {
		if (userTimeline != null && userTimeline.size() > 0) {

			// serialize Date in object in RataDie Format
			JsonSerializer<Date> ser = new JsonSerializer<Date>() {
				@Override
				public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
					return src == null ? null : new JsonPrimitive(DateTimeUtils.getRataDieFormat4Date(src));
				}
			};

			// get json of object
			Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, ser).create();
			String json = gson.toJson(userTimeline);
			// System.out.println(json + "\n");

			// save object to db
			@SuppressWarnings("unchecked")
			List<DBObject> mongoDbObject = (List<DBObject>) JSON.parse(json);
			DirenajMongoDriver.getInstance().getOrgBehaviourUserTweets().insert(mongoDbObject);
		}
	}

	// public static void main(String[] args) throws Exception {
	// User user = new User("78555806", "Meddre5911");
	// user.setCampaignTweetPostDate(DateTimeUtils.getTwitterDate("Mon Jan 04
	// 09:18:39 +0000 2016"));
	// user.setCampaignTweetId("683940594016718800");
	// Twitter4jUtil.saveTweetsOfUser(user);
	// }

}
