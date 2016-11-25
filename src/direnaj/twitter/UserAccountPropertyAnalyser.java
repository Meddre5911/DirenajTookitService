package direnaj.twitter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.json.JSONArray;
import org.json.JSONObject;

import direnaj.domain.User;
import direnaj.domain.UserAccountProperties;
import direnaj.functionalities.sna.BotAccountCalculationWeight;
import direnaj.util.DateTimeUtils;
import direnaj.util.HTTPUtil;
import direnaj.util.NumberUtils;
import direnaj.util.TextUtils;
import direnaj.util.URLUtil;

public class UserAccountPropertyAnalyser {

	private static UserAccountPropertyAnalyser userAccountPropertyAnalyser;
	private Date controlDate;

	private UserAccountPropertyAnalyser() {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
			String dateInString = "31-03-2007";
			controlDate = sdf.parse(dateInString);
		} catch (ParseException e) {
			// XXX loglama yapilacak
			e.printStackTrace();
		}
	}

	public static UserAccountPropertyAnalyser getInstance() {
		if (userAccountPropertyAnalyser == null) {
			userAccountPropertyAnalyser = new UserAccountPropertyAnalyser();
		}
		return userAccountPropertyAnalyser;
	}

	@Deprecated
	public static void analyseUserAccountProperties(ArrayList<User> users) {
		List<String> shortenURLServices = getShortenURLServices();

		Date controlDate = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
			String dateInString = "31-03-2007";
			controlDate = sdf.parse(dateInString);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		for (User user : users) {
			double postedTweetCount = (double) user.getPosts().size();
			// initialize User Account Properties
			UserAccountProperties accountProperties = new UserAccountProperties();
			try {
				// get friend follower ratio
				double totalFriendFollowerCount = user.getFollowersCount() + user.getFriendsCount();
				if (totalFriendFollowerCount > 0) {
					accountProperties.setFriendFollowerRatio(user.getFollowersCount() / totalFriendFollowerCount);
				}
			} catch (ArithmeticException e) {
			}
			accountProperties.setProtected(user.isProtected());
			accountProperties.setVerified(user.isVerified());
			if (controlDate != null && user.getCreationDate().compareTo(controlDate) < 0) {
				accountProperties.setEarlierThanMarch2007(true);
			}
			// calculate ratios
			accountProperties.setUrlRatio(((double) user.getUsedUrls().size()) / postedTweetCount);
			accountProperties.setHashtagRatio(user.getCountOfHashtags() / postedTweetCount);
			accountProperties.setMentionRatio(user.getCountOfMentionedUsers() / postedTweetCount);
			// set spam link ratio

			// if (user.getUsedUrls().size() > 0) {
			// double numberOfSpamLinks =
			// getNumberOfSpamLinks(shortenURLServices, user.getUsedUrls());
			// accountProperties.setSpamLinkRatio(numberOfSpamLinks / (double)
			// user.getUsedUrls().size());
			// }
			user.setAccountProperties(accountProperties);
		}
	}

	@Deprecated
	private static List<String> getShortenURLServices() {
		Vector<String> shortenURLServices = new Vector<String>();
		try {
			String services = HTTPUtil.sendGetRequest("http://api.longurl.org/v2/services?format=json");
			JSONObject json = new JSONObject(services);
			JSONArray names = json.names();
			for (int i = 0; i < names.length(); i++) {
				shortenURLServices.add(names.getString(i));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return shortenURLServices;
	}

	@Deprecated
	private static double getNumberOfSpamLinks(List<String> shortenURLServices, List<String> usedUrls) {
		double count = 0;
		for (String url : usedUrls) {
			String urlDomainName = URLUtil.getURLDomainName(url);
			if (!TextUtils.isEmpty(urlDomainName) && usedUrls.contains(urlDomainName)) {
				url = getOriginalURL(url);
			}
			if (isSpamURL(url)) {
				System.out.println("SPAM URL FOUND : " + url);
				count++;
			}
		}
		return count;
	}

	@Deprecated
	private static boolean isSpamURL(String url) {
		boolean isSpam = false;
		try {
			String encodedUrl = URLEncoder.encode(url, "UTF-8");
			String response = HTTPUtil.sendGetRequest(
					"https://sb-ssl.google.com/safebrowsing/api/lookup?client=api&apikey=ABQIAAAAQnXmJsd0dfyNH4ko7cGHzxR6Ob1nfPRsiZxBAkhmy1f6vKkanw&appver=1.0&pver=3.0&url="
							+ encodedUrl);
			if (!TextUtils.isEmpty(response)) {
				isSpam = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isSpam;
	}

	@Deprecated
	private static String getOriginalURL(String url) {
		String originalURL = "";
		try {
			String encodedUrl = URLEncoder.encode(url, "UTF-8");
			String response = HTTPUtil.sendGetRequest("http://api.longurl.org/v2/expand?url=" + encodedUrl);
			int beginning = response.lastIndexOf("[");
			int lastIndex = response.lastIndexOf("]");
			originalURL = response.substring(beginning + 1, lastIndex - 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return originalURL;
	}

	@Deprecated
	public static Hashtable<User, BigDecimal> calculateProbabilityOfBeingHuman(ArrayList<User> bulkUsersInTweets,
			BotAccountCalculationWeight botAccountCalculationWeight) {
		Hashtable<User, BigDecimal> userProbabilities = new Hashtable<User, BigDecimal>();
		for (User user : bulkUsersInTweets) {
			UserAccountProperties userProperties = user.getAccountProperties();
			BigDecimal userHumanProbability = userProperties.calculateHumanProbability(botAccountCalculationWeight);
			userProbabilities.put(user, userHumanProbability.setScale(10, RoundingMode.HALF_DOWN));
		}
		return userProbabilities;
	}

	public void calculateUserAccountProperties(User user) {
		double totalPostCount = user.getPostCount();
		if (totalPostCount == 0d) {
			totalPostCount = 1d;
		}
		// initialize User Account Properties
		UserAccountProperties accountProperties = user.getAccountProperties();
		accountProperties.setProtected(user.isProtected());
		accountProperties.setVerified(user.isVerified());
		if (controlDate != null && user.getCreationDate().compareTo(controlDate) < 0) {
			accountProperties.setEarlierThanMarch2007(true);
		}
		// calculate ratios
		accountProperties.setFriendFollowerRatio(NumberUtils.roundDouble(user.calculateFriendFollowerRatio()));
		accountProperties.setUrlRatio(NumberUtils.roundDouble(user.getUsedUrlCount() / totalPostCount));
		accountProperties.setHashtagRatio(NumberUtils.roundDouble(user.getCountOfHashtags() / totalPostCount));
		accountProperties.setMentionRatio(NumberUtils.roundDouble(user.getCountOfMentionedUsers() / totalPostCount));
		accountProperties.setMediaPostRatio(NumberUtils.roundDouble(user.getCountOfMediaPosts() / totalPostCount));

		// get days
		DateTime userCreationDate = new DateTime(user.getCreationDate());
		DateTime now = new DateTime(DateTimeUtils.getLocalDate());
		double twitterDay = Days.daysBetween(userCreationDate, now).getDays();
		double wholeStatusesCount = user.getWholeStatusesCount();
		double dailyAvaregePostCount = NumberUtils.roundDouble(4, wholeStatusesCount / twitterDay);
		accountProperties.setAvarageDailyPostCount(dailyAvaregePostCount);
	}
}
