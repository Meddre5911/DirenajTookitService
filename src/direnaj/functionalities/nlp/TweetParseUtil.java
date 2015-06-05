package direnaj.functionalities.nlp;



import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import direnaj.util.TextUtils;

public class TweetParseUtil {
    /**
     * XXX burada https'i de goz onunde tutmak lazim. Birden fazla url bulma
     * olayina da bak.
     * 
     * @param tweetText
     * @return
     */
    
    public static String onlyText(String tweetText) {
        
        // stripping URLS
        while(!TextUtils.isEmpty(tweetText) && 
                ((tweetText.contains("http:")) || (tweetText.contains("https:")))) {
            int indexOfHttp = tweetText.indexOf("http:");
            if (indexOfHttp == -1) {
                indexOfHttp = tweetText.indexOf("https:");
            }
            
            int possibleEndPoint = tweetText.indexOf(" ", indexOfHttp);
            
            int endPoint = (possibleEndPoint != -1) ? possibleEndPoint : tweetText.length();
            tweetText = tweetText.substring(0, indexOfHttp) + tweetText.substring(endPoint, tweetText.length());
        }
        
        // stripping hashtags
        String patternStr = "(?:\\s|\\A)[##]+([A-Za-z0-9-_]+)";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(tweetText);
        String result = "";
        while(matcher.find()) {
            result = matcher.group();
            result = result.replace(" ",  "");
            int tagIndex = tweetText.indexOf(result);
            tweetText = tweetText.substring(0, tagIndex) + 
                    tweetText.substring(tagIndex+result.length(), tweetText.length());
        }
        
        // stripping mentions
        String pattStr = "(?:\\s|\\A)[@]+([A-Za-z0-9-_]+)";
        Pattern patt = Pattern.compile(pattStr);
        Matcher match = patt.matcher(tweetText);
        while (match.find()) {
            String rslt = match.group();
            rslt = rslt.replace(" ", "");
            int mentionIndex = tweetText.indexOf(rslt);
            tweetText = tweetText.substring(0, mentionIndex) +
                    tweetText.substring(mentionIndex+rslt.length(), tweetText.length());
        }
        
        return tweetText;
    }
    
    public static List<String> getUrlsInText(String tweetText) {
        Vector<String> urlList = new Vector<String>();
        while (!TextUtils.isEmpty(tweetText) && (tweetText.contains("http:"))) {
            int indexOfHttp = tweetText.indexOf("http:");
            int endPoint = (tweetText.indexOf(" ", indexOfHttp) != -1) ? tweetText
                    .indexOf(" ", indexOfHttp) : tweetText.length();
            String url = tweetText.substring(indexOfHttp, endPoint);
            urlList.add(url);
            tweetText = tweetText.substring(endPoint);
        }
        return urlList;
    }

    public static List<String> getHashtagsInText(String tweetText) {
        Vector<String> hashTagList = new Vector<String>();
        String patternStr = "(?:\\s|\\A)[##]+([A-Za-z0-9-_]+)";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(tweetText);
        String result = "";
        // Search for Hashtags
        while (matcher.find()) {
            result = matcher.group();
            result = result.replace(" ", "");
            String hashtag = result.toLowerCase(Locale.US).trim();
            if(!hashTagList.contains(hashtag)){
                hashTagList.add(hashtag);
            }
        }
        return hashTagList;
    }

    public static List<String> getUserMentionsText(String tweetText) {
        Vector<String> hashTagList = new Vector<String>();
        String patternStr = "(?:\\s|\\A)[@]+([A-Za-z0-9-_]+)";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(tweetText);
        while (matcher.find()) {
            String result = matcher.group();
            result = result.replace(" ", "");
            hashTagList.add(result);
        }
        return hashTagList;
    }
    
    public static void main(String[]args) {
        System.out.println(TweetParseUtil.onlyText("hede @caner"));
    }

}
