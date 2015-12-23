package direnaj.domain;

public class UserTweets {

	private String tweetId;
	private String tweetText;
	private boolean isHashtagTweet;

	public boolean isHashtagTweet() {
		return isHashtagTweet;
	}

	public void setHashtagTweet(boolean isHashtagTweet) {
		this.isHashtagTweet = isHashtagTweet;
	}

	public String getTweetText() {
		return tweetText;
	}

	public void setTweetText(String tweetText) {
		this.tweetText = tweetText;
	}

	public String getTweetId() {
		return tweetId;
	}

	public void setTweetId(String tweetId) {
		this.tweetId = tweetId;
	}

}
