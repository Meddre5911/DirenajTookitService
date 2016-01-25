package direnaj.twitter.twitter4j;

import java.util.ArrayList;
import java.util.List;

import direnaj.util.PropertiesUtil;
import twitter4j.Twitter;
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
			String accessToken = PropertiesUtil.getInstance().getProperty("twitter4j.user." + i + ".accessToken",
					null);
			String accessTokenSecret = PropertiesUtil.getInstance().getProperty("twitter4j.user." + i + ".accessTokenSecret",
					null);

			
			
			ConfigurationBuilder build = new ConfigurationBuilder();
		    build.setOAuthAccessToken(accessToken);
		    build.setOAuthAccessTokenSecret(accessTokenSecret);
		    build.setOAuthConsumerKey(consumerKey);
		    build.setOAuthConsumerSecret(consumerSecret);
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
		return twitter4jObjects.get(0);
	}
}
