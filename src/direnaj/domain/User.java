package direnaj.domain;

import java.util.Date;
import java.util.List;
import java.util.Vector;

public class User implements Comparable<User> {

    private String userId;
    private String userScreenName;
    private double userDegree;
    private double friendsCount;
    private double followersCount;
    private boolean isProtected;
    private boolean isVerified;
    private Date creationDate;
    private UserAccountProperties accountProperties;
    private List<String> posts;
    private List<String> usedUrls;
    /**
     * count of Mentioned Users in all posted tweets
     */
    private double countOfMentionedUsers;
    /**
     * count of Hashtags in all posted tweets
     */
    private double countOfHashtags;

    public User(String userId, String screenName) {
        posts = new Vector<String>();
        usedUrls = new Vector<String>();
        friendsCount = 0L;
        followersCount = 0L;
        countOfMentionedUsers = 0L;
        countOfHashtags = 0L;
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
}
