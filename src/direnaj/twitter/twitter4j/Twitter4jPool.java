package direnaj.twitter.twitter4j;

import java.util.LinkedList;
import java.util.ListIterator;
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
	private LinkedList<Twitter> twitter4jObjects = null;
	private Twitter availableObject = null;

	private Twitter4jPool() {
		Integer twitter4jUserCount = PropertiesUtil.getInstance().getIntProperty("twitter4j.user.count", 1);
		twitter4jObjects = new LinkedList<>();
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
			build.setDebugEnabled(PropertiesUtil.getInstance().getBooleanProperty("twitter4j.isDebugEnabled", true));
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

		while (true) {
			if (availableObject != null && isAvailable(availableObject, resource, statusUsertimeline)) {
				break;
			}

			// find the beginning index in the list
			int previousAvailableObjIndex = -1;
			if (availableObject != null && twitter4jObjects.contains(availableObject)) {
				previousAvailableObjIndex = twitter4jObjects.indexOf(availableObject);
				Logger.getLogger(Twitter4jPool.class).debug(
						"Previos object is not available. previousAvailableObjIndex : " + previousAvailableObjIndex);
			}
			Logger.getLogger(Twitter4jPool.class).trace("getAvailableTwitterObject for resource : " + resource
					+ " & TwitterRestApiOperationType : " + statusUsertimeline.name());
			availableObject = null;
			// traverse to forward
			ListIterator<Twitter> it2Forward = twitter4jObjects.listIterator(previousAvailableObjIndex + 1);
			while (it2Forward.hasNext()) {
				Twitter twitter4jObject = it2Forward.next();
				if (isAvailable(twitter4jObject, resource, statusUsertimeline)) {
					availableObject = twitter4jObject;
					break;
				}
			}
			// traverse from beginning
			if (availableObject == null && previousAvailableObjIndex > 0) {
				ListIterator<Twitter> itFromBeginning = twitter4jObjects.subList(0, previousAvailableObjIndex)
						.listIterator();
				while (itFromBeginning.hasNext()) {
					Twitter twitter4jObject = itFromBeginning.next();
					if (isAvailable(twitter4jObject, resource, statusUsertimeline)) {
						availableObject = twitter4jObject;
						break;
					}
				}
			}

			// if all objects do not have limits
			if (availableObject != null) {
				break;
			} else {
				Logger.getLogger(Twitter4jPool.class).trace("No available object is found.");
				try {
					int threadSleepTime = 60000;
					try {
						Twitter twitter = twitter4jObjects.get(0);
						RateLimitStatus rateLimitStatusObject = getRateLimitStatusObject(twitter, resource,
								statusUsertimeline);
						threadSleepTime = (rateLimitStatusObject.getSecondsUntilReset() + 1) * 1000;
					} catch (TwitterException e) {
						Logger.getLogger(Twitter4jPool.class)
								.error("TwitterException - Error Taken in getAvailableTwitterObject method.", e);
						try {
							threadSleepTime = (e.getRateLimitStatus().getSecondsUntilReset() + 1) * 1000;
						} catch (Exception exp) {
							Logger.getLogger(Twitter4jPool.class).error("Exception getting getSecondsUntilReset.", e);
						}
					}
					Logger.getLogger(Twitter4jPool.class).debug("Thread is sleeping. Sleep Time : " + threadSleepTime);
					Thread.sleep(threadSleepTime);
				} catch (InterruptedException e) {
					Logger.getLogger(Twitter4jPool.class)
							.error("InterruptedException - Error Taken in getAvailableTwitterObject method.", e);
				}
			}
		}
		int availableObjIndex = twitter4jObjects.indexOf(availableObject);
		Logger.getLogger(Twitter4jPool.class).trace("Available Object Index is : " + availableObjIndex);
		return availableObject;

	}

	private boolean isAvailable(Twitter twitter4jObject, String resource,
			TwitterRestApiOperationTypes twitterRestApiOperationType) {
		boolean isTwitterObjAvailable = false;
		try {
			Logger.getLogger(Twitter4jPool.class).trace("Checking Twitter Object " + twitter4jObject.getId());
			RateLimitStatus rateLimitStatus = getRateLimitStatusObject(twitter4jObject, resource,
					twitterRestApiOperationType);
			int remaining = rateLimitStatus.getRemaining();
			if (remaining > 0) {
				Logger.getLogger(Twitter4jPool.class).debug(
						"Available Twitter Object : " + twitter4jObject.getScreenName() + " Remaining : " + remaining);
				isTwitterObjAvailable = true;
			} else {
				Logger.getLogger(Twitter4jPool.class).debug("No limit - Remaining : " + remaining);
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
	// Twitter4jPool.getInstance();
	// }
}
