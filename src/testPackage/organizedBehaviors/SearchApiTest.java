package testPackage.organizedBehaviors;

import com.google.gson.Gson;

import direnaj.twitter.twitter4j.Twitter4jPool;
import direnaj.twitter.twitter4j.TwitterRestApiOperationTypes;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

public class SearchApiTest {

	public static void main(String[] args) throws TwitterException {
		Paging paging = new Paging(1, 200);

		ResponseList<Status> userTimeline = Twitter4jPool.getInstance()
				.getAvailableTwitterObject(TwitterRestApiOperationTypes.STATUS_USERTIMELINE)
				.getUserTimeline(Long.valueOf(78555806), paging);

		// get json of object
		Gson gson = new Gson();
		String json = gson.toJson(userTimeline);
		System.out.println(json + "\n");

	}

}
