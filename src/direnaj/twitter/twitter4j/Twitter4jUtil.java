package direnaj.twitter.twitter4j;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
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
import direnaj.twitter.twitter4j.external.DrnjStatusJSONImpl;
import direnaj.util.DateTimeUtils;
import direnaj.util.PropertiesUtil;
import twitter4j.MediaEntity;
import twitter4j.MediaEntityJSONImpl;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.MediaEntity.Size;

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
				Date tweetCreationDate = userTimeline.get(arraySize - 1).getCreatedAt();
				if (tweetCreationDate.after(lowestDate)) {
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
		// get lowest & highest dates
		Integer tweetDuration = PropertiesUtil.getInstance().getIntProperty("tweet.checkInterval.inWeeks", 2);
		Date highestDate = DateTimeUtils.addWeeksToDateInDateFormat(user.getCampaignTweetPostDate(), tweetDuration);
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
				Date tweetCreationDate = userTimeline.get(arraySize - 1).getCreatedAt();
				if (tweetCreationDate.before(highestDate)) {
					paging.setPage(++pageNumber);
					isRecentTweetsRemaining = true;
				}
			}
			if (!isRecentTweetsRemaining) {
				break;
			}
		}
	}

	private static void saveTweets(ResponseList<Status> userTimeline) {
		if (userTimeline != null && userTimeline.size() > 0) {

			// serialize Date in object in RataDie Format
			JsonSerializer<Date> ser = new JsonSerializer<Date>() {
				@Override
				public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
					return src == null ? null : new JsonPrimitive(DateTimeUtils.getRataDieFormat4Date(src));
				}
			};

			// get json of object
			Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, ser).create();
			String json = gson.toJson(userTimeline);
//			 System.out.println(json + "\n");

			// save object to db
			@SuppressWarnings("unchecked")
			List<DBObject> mongoDbObject = (List<DBObject>) JSON.parse(json);
			DirenajMongoDriver.getInstance().getOrgBehaviourUserTweets().insert(mongoDbObject);
		}
	}
	
	public static Status deserializeTwitter4jStatusFromGson(Gson gson,String statusInJson) {
		Status twitter4jStatus = (Status) gson.fromJson(statusInJson, DrnjStatusJSONImpl.class);
		return twitter4jStatus;
	}

	public static Gson getGsonObject4Deserialization() {
		JsonDeserializer<Date> dateJsonDeserializer = new JsonDeserializer<Date>() {
			@Override
			public Date deserialize(JsonElement json, Type arg1,
					com.google.gson.JsonDeserializationContext arg2) throws JsonParseException {
				Date date = null;
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
				String dateStr = json.getAsJsonPrimitive().getAsString();
				try {
					date = DateTimeUtils.getTwitterDateFromRataDieFormat(dateStr);
				} catch (Exception e) {
					try {
						date = sdf.parse(dateStr);
					} catch (Exception e1) {
						Logger.getLogger(OrganizationDetector.class.getSimpleName())
								.error("Date Format Exception.", e);
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

	// public static void main(String[] args) throws Exception {
	// User user = new User("78555806", "Meddre5911");
	// user.setCampaignTweetPostDate(DateTimeUtils.getTwitterDate("Mon Jan 04
	// 09:18:39 +0000 2016"));
	// user.setCampaignTweetId("683940594016718800");
	// Twitter4jUtil.saveTweetsOfUser(user);
	// }

}
