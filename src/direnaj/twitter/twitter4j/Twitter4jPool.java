package direnaj.twitter.twitter4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import direnaj.util.PropertiesUtil;
import twitter4j.RateLimitStatus;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.conf.ConfigurationBuilder;

public class Twitter4jPool {

	private static Twitter4jPool instance = null;
	private List<Twitter> twitter4jObjects = null;

	private Twitter4jPool() {
		Integer twitter4jUserCount = PropertiesUtil.getInstance().getIntProperty("twitter4j.user.count", 1);
		twitter4jObjects = new ArrayList<>(twitter4jUserCount);
		for (int i = 1; i <= twitter4jUserCount; i++) {
			String consumerKey = PropertiesUtil.getInstance().getProperty("twitter4j.user." + i + ".consumerKey", null);
			String consumerSecret = PropertiesUtil.getInstance().getProperty("twitter4j.user." + i + ".consumerSecret",
					null);
			String accessToken = PropertiesUtil.getInstance().getProperty("twitter4j.user." + i + ".accessToken", null);
			String accessTokenSecret = PropertiesUtil.getInstance()
					.getProperty("twitter4j.user." + i + ".accessTokenSecret", null);

			ConfigurationBuilder build = new ConfigurationBuilder();
			build.setOAuthAccessToken(accessToken);
			build.setOAuthAccessTokenSecret(accessTokenSecret);
			build.setOAuthConsumerKey(consumerKey);
			build.setOAuthConsumerSecret(consumerSecret);
			build.setIncludeMyRetweetEnabled(true);
			OAuthAuthorization auth = new OAuthAuthorization(build.build());
			Twitter twitter = new TwitterFactory().getInstance(auth);
			twitter4jObjects.add(twitter);
		}
	}

	public static Twitter4jPool getInstance() {
		if (instance == null) {
			instance = new Twitter4jPool();
		}
		return instance;
	}

	public Twitter getAvailableTwitterObject(TwitterRestApiOperationTypes statusUsertimeline) {
		String resource = "statuses";
		Twitter availableObject = null;
		while (true) {
			Logger.getLogger(Twitter4jPool.class).trace("getAvailableTwitterObject for resource : " + resource
					+ " & TwitterRestApiOperationType : " + statusUsertimeline.name());
			for (Twitter twitter4jObject : twitter4jObjects) {
				if (isAvailable(twitter4jObject, resource, statusUsertimeline)) {
					availableObject = twitter4jObject;
					break;
				}
			}

			// if all objects do not have limits
			if (availableObject != null) {
				break;
			} else {
				try {
					int threadSleepTime = 60000;
					try {
						Twitter twitter = twitter4jObjects.get(0);
						RateLimitStatus rateLimitStatusObject = getRateLimitStatusObject(twitter, resource,
								statusUsertimeline);
						threadSleepTime = rateLimitStatusObject.getSecondsUntilReset() * 1000;
					} catch (TwitterException e) {
						Logger.getLogger(Twitter4jPool.class)
								.error("TwitterException - Error Taken in getAvailableTwitterObject method.", e);
					}
					Thread.sleep(threadSleepTime);
				} catch (InterruptedException e) {
					Logger.getLogger(Twitter4jPool.class)
							.error("InterruptedException - Error Taken in getAvailableTwitterObject method.", e);
				}
			}
		}
		return availableObject;

	}

	private boolean isAvailable(Twitter twitter4jObject, String resource,
			TwitterRestApiOperationTypes twitterRestApiOperationType) {
		boolean isTwitterObjAvailable = false;
		try {
			RateLimitStatus rateLimitStatus = getRateLimitStatusObject(twitter4jObject, resource,
					twitterRestApiOperationType);
			int remaining = rateLimitStatus.getRemaining();
			if (remaining > 0) {
				Logger.getLogger(Twitter4jPool.class).trace(
						"Available Twitter Object : " + twitter4jObject.getScreenName() + " Remaining : " + remaining);
				isTwitterObjAvailable = true;
			}
		} catch (TwitterException e) {
			Logger.getLogger(Twitter4jPool.class).error("Error Taken in isAvailable method.", e);
		}
		return isTwitterObjAvailable;
	}

	private RateLimitStatus getRateLimitStatusObject(Twitter twitter4jObject, String resource,
			TwitterRestApiOperationTypes twitterRestApiOperationType) throws TwitterException {
		Map<String, RateLimitStatus> rateLimitStatusMap = twitter4jObject.getRateLimitStatus(resource);
		RateLimitStatus rateLimitStatus = rateLimitStatusMap.get(twitterRestApiOperationType.toString());
		return rateLimitStatus;
	}

	// public static void main(String[] args) {
	// Twitter twitter = Twitter4jPool.getInstance()
	// .getAvailableTwitterObject(TwitterRestApiOperationTypes.STATUS_USERTIMELINE);
	// }
}
