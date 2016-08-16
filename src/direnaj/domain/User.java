package direnaj.domain;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import direnaj.twitter.TweetPostSource;
import direnaj.twitter.TweetPostSourceAnalyser;

public class User implements Comparable<User> {

	private String userId;
	private String userScreenName;
	private double userDegree;
	private double friendsCount;
	private double followersCount;
	private double favoriteCount;
	private double postCount;
	private double hashtagPostCount;
	private double wholeStatusesCount;
	private boolean isProtected;
	private boolean isVerified;
	private Date creationDate;
	private Date campaignTweetPostDate;
	private String campaignTweetId;
	private UserAccountProperties accountProperties;
	private List<String> posts;
	private List<String> usedUrls;
	private double mobileDevicePostCount;
	private double twitterDevicePostCount;
	private double thirdPartyDevicePostCount;
	private double usedUrlCount;

	/**
	 * count of Mentioned Users in all posted tweets
	 */
	private double countOfMentionedUsers;
	/**
	 * count of Hashtags in all posted tweets
	 */
	private double countOfHashtags;

	public User(String userId, String screenName) {
		posts = new LinkedList<String>();
		usedUrls = new LinkedList<String>();
		friendsCount = 0L;
		followersCount = 0L;
		countOfMentionedUsers = 0L;
		countOfHashtags = 0L;
		setHashtagPostCount(0d);
		setUserId(userId);
		setUserScreenName(screenName);
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserScreenName() {
		return userScreenName;
	}

	public void setUserScreenName(String userScreenName) {
		this.userScreenName = userScreenName;
	}

	public List<String> getPosts() {
		return posts;
	}

	public void addPost(String post) {
		posts.add(post);
	}

	public void addPostsToUser(List<String> otherPosts) {
		if (otherPosts != null) {
			posts.addAll(otherPosts);
		}
	}

	public double getUserDegree() {
		return userDegree;
	}

	public void setUserDegree(double userDegree) {
		this.userDegree = userDegree;
	}

	public double getFriendsCount() {
		return friendsCount;
	}

	public void setFriendsCount(double friendsCount) {
		this.friendsCount = friendsCount;
	}

	public String getCampaignTweetId() {
		return campaignTweetId;
	}

	public void setCampaignTweetId(String campaignTweetId) {
		this.campaignTweetId = campaignTweetId;
	}

	public double getFollowersCount() {
		return followersCount;
	}

	public void setFollowersCount(double followersCount) {
		this.followersCount = followersCount;
	}

	public boolean isProtected() {
		return isProtected;
	}

	public void setProtected(boolean isProtected) {
		this.isProtected = isProtected;
	}

	public boolean isVerified() {
		return isVerified;
	}

	public void setVerified(boolean isVerified) {
		this.isVerified = isVerified;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public UserAccountProperties getAccountProperties() {
		if (accountProperties == null) {
			accountProperties = new UserAccountProperties();
		}
		return accountProperties;
	}

	public void setAccountProperties(UserAccountProperties accountProperties) {
		this.accountProperties = accountProperties;
	}

	public List<String> getUsedUrls() {
		return usedUrls;
	}

	public void addUrlsToUser(List<String> otherUrls) {
		if (otherUrls != null) {
			usedUrls.addAll(otherUrls);
		}
	}

	public double getCountOfMentionedUsers() {
		return countOfMentionedUsers;
	}

	public void setCountOfMentionedUsers(double countOfMentionedUsers) {
		this.countOfMentionedUsers = countOfMentionedUsers;
	}

	public double getCountOfHashtags() {
		return countOfHashtags;
	}

	public void setCountOfHashtags(double countOfHashtags) {
		this.countOfHashtags = countOfHashtags;
	}

	public void addValue2CountOfMentionedUsers(double countOfMentionedUsers) {
		this.countOfMentionedUsers += countOfMentionedUsers;
	}

	public void addValue2CountOfHashtags(double countOfHashtags) {
		this.countOfHashtags += countOfHashtags;
	}

	public Date getCampaignTweetPostDate() {
		return campaignTweetPostDate;
	}

	public void setCampaignTweetPostDate(Date campaignTweetPostDate) {
		this.campaignTweetPostDate = campaignTweetPostDate;
	}

	public double getPostCount() {
		return postCount;
	}

	public void setPostCount(double postCount) {
		this.postCount = postCount;
	}

	public double getMobileDevicePostCount() {
		return mobileDevicePostCount;
	}

	public void setMobileDevicePostCount(double mobileDevicePostCount) {
		this.mobileDevicePostCount = mobileDevicePostCount;
	}

	public double getTwitterDevicePostCount() {
		return twitterDevicePostCount;
	}

	public void setTwitterDevicePostCount(double apiDevicePostCount) {
		this.twitterDevicePostCount = apiDevicePostCount;
	}

	public double getThirdPartyDevicePostCount() {
		return thirdPartyDevicePostCount;
	}

	public void setThirdPartyDevicePostCount(double thirdPartyDevicePostCount) {
		this.thirdPartyDevicePostCount = thirdPartyDevicePostCount;
	}

	public double getUsedUrlCount() {
		return usedUrlCount;
	}

	public void setUsedUrlCount(double usedUrlCount) {
		this.usedUrlCount = usedUrlCount;
	}

	public void incrementPostCount() {
		postCount++;
	}
	
	public void incrementHashtagPostCount() {
		setHashtagPostCount(getHashtagPostCount() + 1);
	}

	public double calculateFriendFollowerRatio() {
		Double ratioValue = new Double(0);
		try {
			double totalFriendFollowerCount = getFollowersCount() + getFriendsCount();
			if (totalFriendFollowerCount > 0) {
				ratioValue = getFollowersCount() / totalFriendFollowerCount;
			}
			return ratioValue;
		} catch (Exception e) {
			return new Double(0);
		}
	}

	/**
	 * 
	 * @param tweetPostSource
	 */
	public void incrementPostDeviceCount(String tweetPostSource) {
		TweetPostSource tweetSource = TweetPostSourceAnalyser.getInstance().getTweetSource(tweetPostSource);
		switch (tweetSource) {
		case MOBILE:
			mobileDevicePostCount++;
			break;
		case TWITTER:
			twitterDevicePostCount++;
			break;
		case THIRDPARTY:
		default:
			thirdPartyDevicePostCount++;
			break;
		}

	}

	@Override
	public boolean equals(Object obj) {
		try {
			boolean isEqual = false;
			if (obj instanceof User) {
				User user = (User) obj;
				if (this.userId.equalsIgnoreCase(user.getUserId())) {
					isEqual = true;
				}
			}
			return isEqual;
		} catch (Exception e) {
			return false;
		}
	}

	public int hashCode() {
		return (int) (Double.valueOf(userId) % 1000);
	}

	@Override
	public int compareTo(User o) {
		Double result = Double.valueOf(userId) - Double.valueOf(o.getUserId());
		if (result > 0d) {
			return 1;
		} else if (result < 0d) {
			return -1;
		} else {
			return 0;
		}
	}

	public void addValue2CountOfUsedUrls(double usedUrlCountInPost) {
		usedUrlCount += usedUrlCountInPost;

	}

	public double getFavoriteCount() {
		return favoriteCount;
	}

	public void setFavoriteCount(double favoriteCount) {
		this.favoriteCount = favoriteCount;
	}

	public double getHashtagPostCount() {
		return hashtagPostCount;
	}

	public void setHashtagPostCount(double hashtagPostCount) {
		this.hashtagPostCount = hashtagPostCount;
	}

	public double getWholeStatusesCount() {
		return wholeStatusesCount;
	}

	public void setWholeStatusesCount(double wholeStatusesCount) {
		this.wholeStatusesCount = wholeStatusesCount;
	}

}
