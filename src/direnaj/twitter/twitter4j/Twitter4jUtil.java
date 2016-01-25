package direnaj.twitter.twitter4j;

import java.util.Date;

import org.apache.log4j.Logger;

import direnaj.domain.User;
import direnaj.util.DateTimeUtils;
import twitter4j.JSONArray;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

public class Twitter4jUtil {

	public static void saveTweetsOfUser(User user) {
		try {
			System.out.println("Earliest Tweets");
			getEarliestTweets(user);
			System.out.println("Recent Tweets");
			getRecentTweets(user);
		} catch (TwitterException e) {
			Logger.getLogger(Twitter4jUtil.class.getSimpleName()).error("Twitter4jUtil saveTweetsOfUser", e);
			e.printStackTrace();
		}
	}

	private static void getEarliestTweets(User user) throws TwitterException {
		Date lowestDate = DateTimeUtils.subtractWeeksFromDateInDateFormat(user.getCampaignTweetPostDate(), 2);
		// first collect earlier tweets
		int pageNumber = 1;
		boolean isEarlierTweetsRemaining = false;
		Paging paging = new Paging();
		paging.setCount(200);
		paging.setMaxId(Long.valueOf(user.getCampaignTweetId()));
		paging.setPage(pageNumber);
		for (int i = 0; i < 20; i++) {
			ResponseList<Status> userTimeline = Twitter4jPool.getInstance()
					.getAvailableTwitterObject(TwitterRestApiOperationTypes.STATUS_USERTIMELINE)
					.getUserTimeline(Long.valueOf(user.getUserId()), paging);
			saveTweets(userTimeline);
			// Status To JSON String
			int arraySize = userTimeline.size();
			if (arraySize >= 199) {
				Date tweetCreationDate = userTimeline.get(198).getCreatedAt();
				if (tweetCreationDate.after(lowestDate)) {
					paging.setPage(++pageNumber);
					isEarlierTweetsRemaining = true;
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
		boolean isRecentTweetsRemaining = false;
		Paging paging = new Paging();
		paging.setCount(200);
		paging.setSinceId(Long.valueOf(user.getCampaignTweetId()));
		paging.setPage(pageNumber);
		for (int i = 0; i < 20; i++) {
			ResponseList<Status> userTimeline = Twitter4jPool.getInstance()
					.getAvailableTwitterObject(TwitterRestApiOperationTypes.STATUS_USERTIMELINE)
					.getUserTimeline(Long.valueOf(user.getUserId()), paging);
			saveTweets(userTimeline);
			// Status To JSON String
			int arraySize = userTimeline.size();
			if (arraySize >= 199) {
				Date tweetCreationDate = userTimeline.get(198).getCreatedAt();
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
		JSONArray jsonArray = new JSONArray(userTimeline);
		String str = jsonArray.toString();
		System.out.println("JSON : \n" + str + "\n");
	}
	
	public static void main(String[] args) throws Exception {
		User user = new User("78555806","Meddre5911");
		user.setCampaignTweetPostDate(DateTimeUtils.getTwitterDate("Mon Jan 04 09:18:39 +0000 2016"));
		user.setCampaignTweetId("683940594016718800");
		Twitter4jUtil.saveTweetsOfUser(user);
	}

}
