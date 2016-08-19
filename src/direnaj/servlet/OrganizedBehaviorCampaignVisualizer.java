package direnaj.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import direnaj.driver.DirenajMongoDriver;
import direnaj.driver.MongoCollectionFieldNames;
import direnaj.util.DateTimeUtils;
import direnaj.util.NumberUtils;
import direnaj.util.TextUtils;

public class OrganizedBehaviorCampaignVisualizer extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7542468189990963215L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json; charset=UTF-8");
		String requestType = req.getParameter("requestType");
		String jsonStr = "{}";

		// check for type
		try {
			JSONArray jsonArray = new JSONArray();
			String requestId = req.getParameter("requestId");
			BasicDBObject query = new BasicDBObject("requestId", requestId);
			BasicDBObject query4CosSimilarityRequest = new BasicDBObject(
					MongoCollectionFieldNames.MONGO_COS_SIM_REQ_ORG_REQUEST_ID, requestId);
			if ("visualizeUserCreationTimes".equals(requestType)) {
				// get cursor
				// FIXME 20160818 - Tarihe Göre sırala
				DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData()
						.find(query)
						.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_CREATION_DATE_IN_RATA_DIE, 1));
				// get objects from cursor
				int userNo = 0;
				while (paginatedResult.hasNext()) {
					DBObject next = paginatedResult.next();
					userNo++;
					String twitterDateStr = (String) next.get(MongoCollectionFieldNames.MONGO_USER_CREATION_DATE);

					JSONObject userProcessInputData = new JSONObject();
					userProcessInputData.put("userSequenceNo", userNo);
					userProcessInputData.put(MongoCollectionFieldNames.MONGO_USER_ID,
							TextUtils.getLongValue((String) next.get(MongoCollectionFieldNames.MONGO_USER_ID)));
					userProcessInputData.put(MongoCollectionFieldNames.MONGO_USER_CREATION_DATE,
							DateTimeUtils.getStringOfDate("yyyyMMdd", DateTimeUtils.getTwitterDate(twitterDateStr)));
					jsonArray.put(userProcessInputData);
				}
			} else if ("visualizeUserTweetEntityRatios".equals(requestType)) {
				// get cursor
				DBCursor urlRatioResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query)
						.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_URL_RATIO, 1));
				JSONArray urlRatioJsonArray = new JSONArray();
				// get objects from cursor
				// get url ratio
				int userNo = 0;
				while (urlRatioResult.hasNext()) {
					DBObject next = urlRatioResult.next();
					userNo++;
					urlRatioJsonArray.put(new JSONObject()
							.put("ratioValue", (double) next.get(MongoCollectionFieldNames.MONGO_USER_URL_RATIO))
							.put("userSequenceNo", userNo));
				}
				// hashtag ratio
				DBCursor hashtagRatioResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData()
						.find(query).sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_HASHTAG_RATIO, 1));
				JSONArray hashtagRatioJsonArray = new JSONArray();
				// get objects from cursor
				userNo = 0;
				while (hashtagRatioResult.hasNext()) {
					DBObject next = hashtagRatioResult.next();
					userNo++;
					hashtagRatioJsonArray.put(new JSONObject()
							.put("ratioValue", (double) next.get(MongoCollectionFieldNames.MONGO_USER_HASHTAG_RATIO))
							.put("userSequenceNo", userNo));
				}
				// mention ratio
				DBCursor mentionRatioResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData()
						.find(query).sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_MENTION_RATIO, 1));
				JSONArray mentionRatioJsonArray = new JSONArray();
				// get objects from cursor
				userNo = 0;
				while (mentionRatioResult.hasNext()) {
					DBObject next = mentionRatioResult.next();
					userNo++;
					mentionRatioJsonArray.put(new JSONObject()
							.put("ratioValue", (double) next.get(MongoCollectionFieldNames.MONGO_USER_MENTION_RATIO))
							.put("userSequenceNo", userNo));
				}
				// init to array
				jsonArray.put(new JSONObject().put("ratioType", MongoCollectionFieldNames.MONGO_USER_URL_RATIO)
						.put("values", urlRatioJsonArray));
				jsonArray.put(new JSONObject().put("ratioType", MongoCollectionFieldNames.MONGO_USER_HASHTAG_RATIO)
						.put("values", hashtagRatioJsonArray));
				jsonArray.put(new JSONObject().put("ratioType", MongoCollectionFieldNames.MONGO_USER_MENTION_RATIO)
						.put("values", mentionRatioJsonArray));
			} else if ("visualizeUserFriendFollowerRatio".equals(requestType)) {
				// get cursor
				DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData()
						.find(query)
						.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_FRIEND_FOLLOWER_RATIO, 1));
				// get objects from cursor
				int userNo = 0;
				while (paginatedResult.hasNext()) {
					DBObject next = paginatedResult.next();
					userNo++;
					// prepare json object
					JSONObject userProcessInputData = new JSONObject();
					userProcessInputData.put("userSequenceNo", userNo);
					userProcessInputData.put(MongoCollectionFieldNames.MONGO_USER_ID,
							TextUtils.getLongValue((String) next.get(MongoCollectionFieldNames.MONGO_USER_ID)));
					// get ratios
					userProcessInputData.put("ratioValue",
							(double) next.get(MongoCollectionFieldNames.MONGO_USER_FRIEND_FOLLOWER_RATIO));
					// init to array
					jsonArray.put(userProcessInputData);
				}
			} else if ("visualizeUserRoughHashtagTweetCounts".equals(requestType)) {
				// get cursor
				DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData()
						.find(query)
						.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_HASHTAG_POST_COUNT, 1));
				// get objects from cursor
				int userNo = 0;
				while (paginatedResult.hasNext()) {
					DBObject next = paginatedResult.next();
					userNo++;
					// prepare json object
					JSONObject userProcessInputData = new JSONObject();
					userProcessInputData.put("userSequenceNo", userNo);
					userProcessInputData.put(MongoCollectionFieldNames.MONGO_USER_ID,
							TextUtils.getLongValue((String) next.get(MongoCollectionFieldNames.MONGO_USER_ID)));
					// get ratios
					userProcessInputData.put("ratioValue",
							(double) next.get(MongoCollectionFieldNames.MONGO_USER_HASHTAG_POST_COUNT));
					// init to array
					jsonArray.put(userProcessInputData);
				}
			} else if ("visualizeUserPostDeviceRatios".equals(requestType)) {
				// get cursor
				DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData()
						.find(query)
						.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_POST_TWITTER_DEVICE_RATIO, 1));
				JSONArray twitterPostDeviceRatioJsonArray = new JSONArray();
				// get objects from cursor
				int userNo = 0;
				while (paginatedResult.hasNext()) {
					DBObject next = paginatedResult.next();
					userNo++;
					// prepare json object
					twitterPostDeviceRatioJsonArray
							.put(new JSONObject()
									.put("ratioValue",
											(double) next
													.get(MongoCollectionFieldNames.MONGO_USER_POST_TWITTER_DEVICE_RATIO))
									.put("userSequenceNo", userNo));

				}
				paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query)
						.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_POST_MOBILE_DEVICE_RATIO, 1));
				JSONArray mobilePostDeviceRatioJsonArray = new JSONArray();
				// get objects from cursor
				userNo = 0;
				while (paginatedResult.hasNext()) {
					DBObject next = paginatedResult.next();
					userNo++;
					// prepare json object
					mobilePostDeviceRatioJsonArray
							.put(new JSONObject()
									.put("ratioValue",
											(double) next
													.get(MongoCollectionFieldNames.MONGO_USER_POST_MOBILE_DEVICE_RATIO))
									.put("userSequenceNo", userNo));

				}
				paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query)
						.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_THIRD_PARTY_DEVICE_RATIO, 1));
				JSONArray thirdPartyPostRatioJsonArray = new JSONArray();
				// get objects from cursor
				userNo = 0;
				while (paginatedResult.hasNext()) {
					DBObject next = paginatedResult.next();
					userNo++;
					// prepare json object
					thirdPartyPostRatioJsonArray
							.put(new JSONObject()
									.put("ratioValue",
											(double) next
													.get(MongoCollectionFieldNames.MONGO_USER_THIRD_PARTY_DEVICE_RATIO))
									.put("userSequenceNo", userNo));

				}

				// init to array
				jsonArray.put(new JSONObject()
						.put("ratioType", MongoCollectionFieldNames.MONGO_USER_POST_TWITTER_DEVICE_RATIO)
						.put("values", twitterPostDeviceRatioJsonArray));
				jsonArray.put(
						new JSONObject().put("ratioType", MongoCollectionFieldNames.MONGO_USER_POST_MOBILE_DEVICE_RATIO)
								.put("values", mobilePostDeviceRatioJsonArray));
				jsonArray.put(
						new JSONObject().put("ratioType", MongoCollectionFieldNames.MONGO_USER_THIRD_PARTY_DEVICE_RATIO)
								.put("values", thirdPartyPostRatioJsonArray));
			} else if ("visualizeUserRoughTweetCounts".equals(requestType)) {
				// buna özel bir müdahale gerekebilir
				// get cursor
				DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData()
						.find(query).sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT, 1));
				JSONArray favoriteStatusCountsJsonArray = new JSONArray();
				// get objects from cursor
				int userNo = 0;
				while (paginatedResult.hasNext()) {
					DBObject next = paginatedResult.next();
					userNo++;
					// prepare json object
					favoriteStatusCountsJsonArray.put(new JSONObject()
							.put("ratioValue", (double) next.get(MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT))
							.put("userSequenceNo", userNo));

				}
				paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().find(query)
						.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT, 1));
				JSONArray wholeStatusCountsJsonArray = new JSONArray();
				// get objects from cursor
				userNo = 0;
				while (paginatedResult.hasNext()) {
					DBObject next = paginatedResult.next();
					userNo++;
					// prepare json object
					wholeStatusCountsJsonArray.put(new JSONObject()
							.put("ratioValue", (double) next.get(MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT))
							.put("userSequenceNo", userNo));

				}
				// init to array
				jsonArray.put(new JSONObject().put("ratioType", MongoCollectionFieldNames.MONGO_USER_FAVORITE_COUNT)
						.put("values", favoriteStatusCountsJsonArray));
				jsonArray.put(new JSONObject().put("ratioType", MongoCollectionFieldNames.MONGO_USER_STATUS_COUNT)
						.put("values", wholeStatusCountsJsonArray));
			} else if ("visualizeHourlyUserAndTweetCount".equals(requestType)) {
				DBCursor paginatedResult = DirenajMongoDriver.getInstance()
						.getOrgBehaviourRequestedSimilarityCalculations().find(query4CosSimilarityRequest)
						.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_COS_SIM_REQ_RATA_DIE_LOWER_TIME, 1));
				JSONArray tweetCountsJsonArray = new JSONArray();
				JSONArray distinctUserCountsJsonArray = new JSONArray();
				// get objects from cursor
				while (paginatedResult.hasNext()) {
					DBObject next = paginatedResult.next();
					// prepare json object
					String twitterDateStr = (String) next.get("lowerTimeInterval");
					String twitterDate = DateTimeUtils.getStringOfDate("yyyyMMdd HH:mm",
							DateTimeUtils.getTwitterDate(twitterDateStr));
					tweetCountsJsonArray.put(new JSONObject().put("time", twitterDate).put("value",
							next.get(MongoCollectionFieldNames.MONGO_DISTINCT_USER_COUNT)));
					// prepare json object
					distinctUserCountsJsonArray.put(new JSONObject().put("time", twitterDate).put("value",
							next.get(MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT)));

				}
				// init to array
				jsonArray.put(new JSONObject().put("valueType", MongoCollectionFieldNames.MONGO_DISTINCT_USER_COUNT)
						.put("values", tweetCountsJsonArray));
				jsonArray.put(new JSONObject().put("valueType", MongoCollectionFieldNames.MONGO_TOTAL_TWEET_COUNT)
						.put("values", distinctUserCountsJsonArray));

			} else if ("visualizeHourlyTweetSimilarities".equals(requestType)) {
				DBCursor paginatedResult = DirenajMongoDriver.getInstance()
						.getOrgBehaviourRequestedSimilarityCalculations().find(query4CosSimilarityRequest)
						.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_COS_SIM_REQ_RATA_DIE_LOWER_TIME, 1));
				// get objects from cursor
				while (paginatedResult.hasNext()) {
					DBObject next = paginatedResult.next();
					// prepare json object
					String twitterDateStr = (String) next.get("lowerTimeInterval");
					String twitterDate = DateTimeUtils.getStringOfDate("yyyyMMdd HH:mm",
							DateTimeUtils.getTwitterDate(twitterDateStr));

					jsonArray.put(new JSONObject().put("time", twitterDate)
							.put(MongoCollectionFieldNames.NON_SIMILAR,
									NumberUtils.roundDouble(4,
											(double) next.get(MongoCollectionFieldNames.NON_SIMILAR)))
							.put(MongoCollectionFieldNames.SLIGHTLY_SIMILAR,
									NumberUtils.roundDouble(4,
											(double) next.get(MongoCollectionFieldNames.SLIGHTLY_SIMILAR)))
							.put(MongoCollectionFieldNames.SIMILAR,
									NumberUtils.roundDouble(4, (double) next.get(MongoCollectionFieldNames.SIMILAR)))
							.put(MongoCollectionFieldNames.VERY_SIMILAR,
									NumberUtils.roundDouble(4,
											(double) next.get(MongoCollectionFieldNames.VERY_SIMILAR)))
							.put(MongoCollectionFieldNames.MOST_SIMILAR, NumberUtils.roundDouble(4,
									(double) next.get(MongoCollectionFieldNames.MOST_SIMILAR))));
				}
			}

			// FIXME 20160813 Sil
			jsonStr = jsonArray.toString();
			System.out.println("Request Type : " + requestType);
			System.out.println("Returned String : " + jsonStr);
		} catch (JSONException e) {
			Logger.getLogger(MongoPaginationServlet.class)
					.error("Error in OrganizedBehaviorCampaignVisualizer Servlet.", e);
		} catch (Exception e) {
			Logger.getLogger(MongoPaginationServlet.class)
					.error("Error in OrganizedBehaviorCampaignVisualizer Servlet.", e);
		}

		// return result
		PrintWriter printout = resp.getWriter();
		printout.println(jsonStr);
	}

}
