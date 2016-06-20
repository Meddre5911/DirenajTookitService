package direnaj.twitter.twitter4j;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import direnaj.domain.User;
import direnaj.driver.DirenajMongoDriver;
import direnaj.functionalities.organizedBehaviour.OrganizationDetector;
import direnaj.twitter.twitter4j.external.DrenajCampaignRecord;
import direnaj.twitter.twitter4j.external.DrenajCampaignStatus;
import direnaj.twitter.twitter4j.external.DrenajStatusJSONImpl;
import direnaj.util.DateTimeUtils;
import direnaj.util.PropertiesUtil;
import twitter4j.MediaEntity;
import twitter4j.MediaEntity.Size;
import twitter4j.MediaEntityJSONImpl;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class Twitter4jUtil {

	public static void saveTweetsOfUser(User user) {
		try {
			getEarliestTweets(user);
			Logger.getLogger(Twitter4jUtil.class.getSimpleName())
					.debug("Earliest Tweets has been collected for User Name :" + user.getUserScreenName()
							+ ", User Id :" + user.getUserId());
			getRecentTweets(user);
			Logger.getLogger(Twitter4jUtil.class.getSimpleName())
					.debug("Recent Tweets has been collected for User Name :" + user.getUserScreenName() + ", User Id :"
							+ user.getUserId());
		} catch (TwitterException e) {
			Logger.getLogger(Twitter4jUtil.class.getSimpleName()).error("Twitter4jUtil saveTweetsOfUser", e);
			e.printStackTrace();
		}
	}

	private static void getEarliestTweets(User user) throws TwitterException {
		Integer tweetDuration = PropertiesUtil.getInstance().getIntProperty("tweet.checkInterval.inWeeks", 2);
		Date lowestDate = DateTimeUtils.subtractWeeksFromDateInDateFormat(user.getCampaignTweetPostDate(),
				tweetDuration);
		// first collect earlier tweets
		int pageNumber = 1;
		Paging paging = new Paging(1, 200);
		paging.setMaxId(Long.valueOf(user.getCampaignTweetId()));
		for (int i = 0; i < 20; i++) {
			boolean isEarlierTweetsRemaining = false;
			ResponseList<Status> userTimeline = Twitter4jPool.getInstance()
					.getAvailableTwitterObject(TwitterRestApiOperationTypes.STATUS_USERTIMELINE)
					.getUserTimeline(Long.valueOf(user.getUserId()), paging);
			saveTweets(userTimeline);
			// Status To JSON String
			int arraySize = userTimeline.size();
			if (arraySize >= 1) {

				Date tweetCreationDateOfFirstTweet = userTimeline.get(0).getCreatedAt();
				Date tweetCreationDateOfLastTweet = userTimeline.get(arraySize - 1).getCreatedAt();

				Logger.getLogger(Twitter4jUtil.class)
						.debug("User Campaign Tweet Post Date :"
								+ DateTimeUtils.getRataDieFormat4Date(user.getCampaignTweetPostDate())
								+ " - EarliestTweets - Rata Die Interval of Retrieved Tweets : "
								+ DateTimeUtils.getRataDieFormat4Date(tweetCreationDateOfFirstTweet) + " - "
								+ DateTimeUtils.getRataDieFormat4Date(tweetCreationDateOfLastTweet));

				if (tweetCreationDateOfLastTweet.after(lowestDate)) {
					isEarlierTweetsRemaining = true;
					paging.setPage(++pageNumber);
				}
			}
			if (!isEarlierTweetsRemaining) {
				break;
			}
		}
	}

	private static void getRecentTweets(User user) throws TwitterException {
		// first collect earlier tweets
		int pageNumber = 1;
		Paging paging = new Paging(1, 200);
		paging.setSinceId(Long.valueOf(user.getCampaignTweetId()));
		for (int i = 0; i < 20; i++) {
			boolean isRecentTweetsRemaining = false;
			ResponseList<Status> userTimeline = Twitter4jPool.getInstance()
					.getAvailableTwitterObject(TwitterRestApiOperationTypes.STATUS_USERTIMELINE)
					.getUserTimeline(Long.valueOf(user.getUserId()), paging);
			saveTweets(userTimeline);
			// Status To JSON String
			int arraySize = userTimeline.size();
			if (arraySize >= 1) {
				paging.setPage(++pageNumber);
				isRecentTweetsRemaining = true;
				Date tweetCreationDateOfFirstTweet = userTimeline.get(0).getCreatedAt();
				Date tweetCreationDateOfLastTweet = userTimeline.get(arraySize - 1).getCreatedAt();

				Logger.getLogger(Twitter4jUtil.class)
						.debug("User Campaign Tweet Post Date :"
								+ DateTimeUtils.getRataDieFormat4Date(user.getCampaignTweetPostDate())
								+ " - RecentTweets Rata Die Interval of Retrieved Tweets : "
								+ DateTimeUtils.getRataDieFormat4Date(tweetCreationDateOfFirstTweet) + " - "
								+ DateTimeUtils.getRataDieFormat4Date(tweetCreationDateOfLastTweet));

				// Date tweetCreationDate = userTimeline.get(arraySize -
				// 1).getCreatedAt();
				// if (highestDate.before(tweetCreationDate)) {
				// paging.setPage(++pageNumber);
				// isRecentTweetsRemaining = true;
				// }
			}
			if (!isRecentTweetsRemaining) {
				break;
			}
		}
	}

	private static void saveTweets(ResponseList<Status> userTimeline) {
		if (userTimeline != null && userTimeline.size() > 0) {

			// get json of object
			Gson gson = getGsonObject4Serialization();
			String json = gson.toJson(userTimeline);
			// System.out.println(json + "\n");

			// save object to db
			@SuppressWarnings("unchecked")
			List<DBObject> mongoDbObject = (List<DBObject>) JSON.parse(json);
			DirenajMongoDriver.getInstance().getOrgBehaviourUserTweets().insert(mongoDbObject);
		}
	}

	public static Status deserializeTwitter4jStatusFromGson(Gson gson, String statusInJson) {
		try {
			Status twitter4jStatus = (Status) gson.fromJson(statusInJson, DrenajStatusJSONImpl.class);
			return twitter4jStatus;
		} catch (Exception e) {
			Logger.getLogger(Twitter4jUtil.class).error("Error during deserializing of tweet : \n" + statusInJson);
			throw e;
		}
	}

	public static Gson getGsonObject4Deserialization() {

		JsonDeserializer<Date> dateJsonDeserializer = new JsonDeserializer<Date>() {
			@Override
			public Date deserialize(JsonElement json, Type arg1, com.google.gson.JsonDeserializationContext arg2)
					throws JsonParseException {
				Date date = null;
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
				String dateStr = json.getAsJsonPrimitive().getAsString();
				try {
					date = DateTimeUtils.getTwitterDateFromRataDieFormat(dateStr);
				} catch (Exception e) {
					try {
						date = sdf.parse(dateStr);
					} catch (Exception e1) {
						Logger.getLogger(OrganizationDetector.class.getSimpleName()).error("Date Format Exception.", e);
					}
				}
				return date;
			}
		};
		
		// FIXME ileride burayı düzeltmemiz gerekebilir
		JsonDeserializer<MediaEntity.Size> mediaEntitysizeDeserializer = new JsonDeserializer<MediaEntity.Size>() {
			@Override
			public Size deserialize(JsonElement arg0, Type arg1, JsonDeserializationContext arg2)
					throws JsonParseException {
				try {
					twitter4j.MediaEntityJSONImpl.Size size = new MediaEntityJSONImpl.Size();
					JsonObject asJsonObject = arg0.getAsJsonObject();
					size.setHeight(asJsonObject.get("height").getAsInt());
					size.setWidth(asJsonObject.get("width").getAsInt());
					size.setResize(asJsonObject.get("resize").getAsInt());
					return size;
				} catch (Exception e) {
					Logger.getLogger(OrganizationDetector.class.getSimpleName())
							.error("MediaEntity.Size Deserialize Exception.", e);
				}
				return null;
			}
		};

		// get json of object
		Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, dateJsonDeserializer)
				.registerTypeAdapter(MediaEntity.Size.class, mediaEntitysizeDeserializer).create();
		return gson;
	}

	public static Gson getGsonObject4Serialization() {
		// serialize Date in object in RataDie Format
		JsonSerializer<Date> ser = new JsonSerializer<Date>() {
			@Override
			public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
				return src == null ? null : new JsonPrimitive(DateTimeUtils.getRataDieFormat4Date(src));
			}
		};
		// get gson
		Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, ser).create();
		return gson;
	}

	/**
	 * @param campaignName
	 * @param hashtagQuery
	 * @param maxDate
	 * @param minDate
	 * @param minDateStr
	 */
	public static void createCampaign4PastTweets(String campaignName, String campaignDefinition, String hashtagQuery,
			String maxDate, String minDate) {
		try {
			String[] allHashtags = hashtagQuery.split(",");
			if (allHashtags != null && allHashtags.length > 0) {
				for (int i = 0; i < allHashtags.length; i++) {
					// save tweets
					collectPastTweets4Campaign(campaignName, allHashtags[i], maxDate, minDate);
				}
			}
		} catch (Exception e) {
			Logger.getLogger(Twitter4jUtil.class).error("createCampaign4PastTweets - Error", e);
		}
	}

	public static void insertCampaignRecord(String campaignName, String campaignDefinition, String hashtagQuery) {
		// insert record into campaign collection
		DrenajCampaignRecord drenajCampaignRecord = new DrenajCampaignRecord("Search Api", campaignDefinition,
				DateTimeUtils.getLocalDateInRataDieFormat(), campaignName, hashtagQuery);
		// get json of object
		Gson gson = getGsonObject4Serialization();
		String json = gson.toJson(drenajCampaignRecord);
		Logger.getLogger(Twitter4jUtil.class).debug("Campaign Record is inserted for campagin_id : " + campaignName);
		Logger.getLogger(Twitter4jUtil.class).debug("Campaign Record is : " + json);
		// save object to db
		DBObject mongoDbObject = (DBObject) JSON.parse(json);
		DirenajMongoDriver.getInstance().getCampaignsCollection().insert(mongoDbObject);
	}

	private static void collectPastTweets4Campaign(String campaignName, String hashtagQuery, String maxDateStr,
			String minDateStr) throws Exception {
		boolean collectTweets = true;
		Long maxId = null;
		String untilDate = maxDateStr;
		Date minDate = DateTimeUtils.getDate("yyyy-MM-dd", minDateStr);
		while (collectTweets) {
			try {
				Logger.getLogger(Twitter4jUtil.class)
						.debug("collectPastTweets4Campaign - untilDate : " + untilDate + " & maxId : " + maxId);
				Twitter availableTwitterObject = Twitter4jPool.getInstance()
						.getAvailableTwitterObject(TwitterRestApiOperationTypes.SEARCH_TWEETS);
				Query query = new Query(hashtagQuery);
				query.setCount(200);
				query.setUntil(untilDate);
				if (maxId != null) {
					query.setMaxId(maxId);
				}
				QueryResult queryResult = availableTwitterObject.search(query);
				List<Status> tweets = queryResult.getTweets();
				if (tweets != null && tweets.size() > 0) {
					Logger.getLogger(Twitter4jUtil.class)
							.debug("collectPastTweets4Campaign - Retrieved Tweets size : " + tweets.size());
					saveTweetResults2TweetCollection(campaignName, tweets);
					int arrayLength = tweets.size() - 1;
					Status earliestStatus = tweets.get(arrayLength);
					Date earliestTweetDate = earliestStatus.getCreatedAt();
					// if we retrieve tweets earlier than minDate
					Logger.getLogger(Twitter4jUtil.class)
							.debug("collectPastTweets4Campaign - MinDate : " + minDate + " & Earliest Tweet Date : "
									+ earliestTweetDate + " & MaxId : " + maxId + " & Earliest Tweet Id : "
									+ earliestStatus.getId());
					if (earliestTweetDate.before(minDate) || (maxId != null && maxId.equals(earliestStatus.getId()))) {
						collectTweets = false;
						Logger.getLogger(Twitter4jUtil.class)
								.debug("collectPastTweets4Campaign - TweetCollection Ended");
					} else {
						maxId = earliestStatus.getId() - 1;
						untilDate = DateTimeUtils.getStringOfDate("yyyy-MM-dd", earliestTweetDate);
					}
				} else {
					Logger.getLogger(Twitter4jUtil.class).debug("collectPastTweets4Campaign - TweetCollection Ended");
					collectTweets = false;
				}

			} catch (TwitterException e) {
				Logger.getLogger(Twitter4jUtil.class).error("collectPastTweets4Campaign - Error", e);
			}
		}
	}

	private static void saveTweetResults2TweetCollection(String campaignName, List<Status> tweets) {
		List<DrenajCampaignStatus> campaignStatuses = new ArrayList<>(tweets.size());
		double retriavalTime = DateTimeUtils.getLocalDateInRataDieFormat();
		for (Status tweet : tweets) {
			campaignStatuses.add(new DrenajCampaignStatus(campaignName, tweet, retriavalTime));
		}
		// get json of object
		Gson gson = getGsonObject4Serialization();
		String json = gson.toJson(campaignStatuses);
		// System.out.println(json + "\n");
		// save object to db
		@SuppressWarnings("unchecked")
		List<DBObject> mongoDbObject = (List<DBObject>) JSON.parse(json);
		DirenajMongoDriver.getInstance().getTweetsCollection().insert(mongoDbObject);
	}

}
