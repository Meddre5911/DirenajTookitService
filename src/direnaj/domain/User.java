package direnaj.domain;

import java.util.List;
import java.util.Vector;

public class User implements Comparable<User> {

    private String userId;
    private String userScreenName;
    private List<String> posts;
    private double userDegree;

    public User(String userId, String screenName) {
        posts = new Vector<String>();
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
