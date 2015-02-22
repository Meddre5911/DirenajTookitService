package direnaj.functionalities.sna;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.joda.time.DateTime;
import org.json.JSONObject;

import direnaj.driver.DirenajDriver;
import direnaj.driver.DirenajDriverUtils;

public class HashtagAnalysis {

    public HashMap<DateTime, Integer> getTimelyIntervalAnalysis(String userID, String password, String campaignId,
            int skip, int limit, String searchedHashtag, AnalysisIntervals timeInterval, String timeDuration)
            throws Exception {
        HashMap<Date, List<JSONObject>> tweetsInTimeBasis = new HashMap<Date, List<JSONObject>>();
        HashMap<DateTime, Integer> hashtaggedTweetsInInterval = new HashMap<DateTime, Integer>();

        DirenajDriver direnajDriver = new DirenajDriver(userID, password);
        ArrayList<JSONObject> collectedTweets = direnajDriver.collectTweets(campaignId, skip, limit);
        for (Iterator<JSONObject> iterator = collectedTweets.iterator(); iterator.hasNext();) {
            JSONObject tweetData = (JSONObject) iterator.next();
            JSONObject tweet = DirenajDriverUtils.getTweet(tweetData);
            List<String> hashTagsList = DirenajDriverUtils.getHashTagsList(DirenajDriverUtils.getEntities(tweet));
            Date tweetCreationDate = DirenajDriverUtils.getTweetCreationDate(tweet);
            if (hashTagsList.contains(searchedHashtag)) {
                List<JSONObject> tweets;
                if (tweetsInTimeBasis.containsKey(tweetCreationDate)) {
                    tweets = tweetsInTimeBasis.get(tweetCreationDate);
                } else {
                    tweets = new Vector<JSONObject>();
                }
                tweets.add(tweet);
                tweetsInTimeBasis.put(tweetCreationDate, tweets);
            }
        }
        // order dates
        Set<Date> keySet = tweetsInTimeBasis.keySet();
        Object[] allDates = keySet.toArray();
        Arrays.sort(allDates);
        getTimeBaseAnalysis(timeInterval, allDates, tweetsInTimeBasis, hashtaggedTweetsInInterval, timeDuration);
        return hashtaggedTweetsInInterval;
    }

    public static JFreeChart getGraphicsOfHashtags(HashMap<DateTime, Integer> hashtaggedTweetsInInterval,
            AnalysisIntervals intervals, String searchedHashtag) {
        Object[] allTimeIntervals = hashtaggedTweetsInInterval.keySet().toArray();
        Arrays.sort(allTimeIntervals);
        TimeSeries pop = null;
        switch (intervals) {
        case MINUTE:
            pop = new TimeSeries("Tweets Hashtagged as #" + searchedHashtag + "In Minute Basis", Minute.class);
            break;
        case HOUR:
            pop = new TimeSeries("Tweets Hashtagged as #" + searchedHashtag + "In Hour Basis", Hour.class);
            break;
        default:
            break;
        }

        for (int i = 0; i < allTimeIntervals.length; i++) {
            DateTime dateTime = (DateTime) allTimeIntervals[i];
            pop.add(new Minute(dateTime.toDate()), hashtaggedTweetsInInterval.get(allTimeIntervals[i]));
        }
        // Create a time series chart
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(pop);
        JFreeChart chart = ChartFactory.createTimeSeriesChart(searchedHashtag + "_Hashtag", "Date", "Tweet Count",
                dataset, true, true, false);
        return chart;
    }

    private void getTimeBaseAnalysis(AnalysisIntervals timeInterval, Object[] allDates,
            HashMap<Date, List<JSONObject>> timeBasisTweets, HashMap<DateTime, Integer> hashtaggedTweetsInInterval,
            String timeDuration) throws ParseException {
        DateTime start = new DateTime((Date) allDates[0]);
        DateTime incrementedDate = null;
        switch (timeInterval) {
        case MINUTE:
            incrementedDate = start.plusMinutes(Integer.valueOf(timeDuration));
            break;
        case HOUR:
            incrementedDate = start.plusHours(Integer.valueOf(timeDuration));
            break;
        default:
            break;
        }
        for (int i = 0; i < allDates.length; i++) {
            Date tweetDate = (Date) allDates[i];
            DateTime dateTime = new DateTime(tweetDate);
            if (dateTime.compareTo(incrementedDate) > 0) {
                switch (timeInterval) {
                case MINUTE:
                    incrementedDate = incrementedDate.plusMinutes(Integer.valueOf(timeDuration));
                    break;
                case HOUR:
                    incrementedDate = incrementedDate.plusHours(Integer.valueOf(timeDuration));
                    break;
                default:
                    break;
                }
            }
            // set tweet counts
            int totalTweets = 0;
            if (hashtaggedTweetsInInterval.containsKey(incrementedDate)) {
                totalTweets = hashtaggedTweetsInInterval.get(incrementedDate);
            }
            List<JSONObject> list = timeBasisTweets.get(tweetDate);
            hashtaggedTweetsInInterval.put(incrementedDate, totalTweets + list.size());
        }
    }

}
