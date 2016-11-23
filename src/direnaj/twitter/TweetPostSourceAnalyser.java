package direnaj.twitter;

import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class TweetPostSourceAnalyser {

    private static TweetPostSourceAnalyser instance = null;

    private List<String> twitterRegexList = new Vector<>();
    private List<String> mobileRegexList = new Vector<>();

    public static TweetPostSourceAnalyser getInstance() {
        if (instance == null) {
            instance = new TweetPostSourceAnalyser();
        }
        return instance;
    }

    private TweetPostSourceAnalyser() {
        // init twitter regex
        twitterRegexList.add("Twitter for");
        twitterRegexList.add("Twitter Web Client");
        twitterRegexList.add("mobile.twitter");
        // init mobile regex
        mobileRegexList.add("Echofon");
        mobileRegexList.add("Gravity");
        mobileRegexList.add("SimplyTweet");
        mobileRegexList.add("Speak To Tweet");
        mobileRegexList.add("BlackBerry");
        mobileRegexList.add("Android");
        mobileRegexList.add("iPhone");
        mobileRegexList.add("iPad");
    }

    /**
     * XXX burasi dogru calisiyor mu ? Kontrol edilecek.
     * 
     * @param tweetPostSource
     * @return
     */
    public TweetPostSource getTweetSource(String tweetPostSource) {
        // lower
        tweetPostSource = tweetPostSource.toLowerCase(Locale.US);
        for (String regex : twitterRegexList) {
            String lowerCaseRegex = regex.toLowerCase(Locale.US);
            if (tweetPostSource.indexOf(lowerCaseRegex) >= 0) {
                return TweetPostSource.TWITTER;
            }
        }
        for (String regex : mobileRegexList) {
            String lowerCaseRegex = regex.toLowerCase(Locale.US);
            if (tweetPostSource.indexOf(lowerCaseRegex) >= 0) {
                return TweetPostSource.MOBILE;
            }
        }
        return TweetPostSource.THIRDPARTY;
    }

    public String getTweetPostSource(String tweetPostSource) {
        // Example value is "<a href=\"http://linkis.com\" rel=\"nofollow\">Linkis.com</a>"
        int indexOf = tweetPostSource.indexOf(">");
        String newStr = tweetPostSource.substring(indexOf);
        int indexOf2 = newStr.indexOf("<");
        String sourceValue = newStr.substring(1, indexOf2);
//        System.out.println("Final Tweet Post Source : " + sourceValue);
        return sourceValue;
    }

}
