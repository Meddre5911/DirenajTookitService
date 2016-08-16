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
			if ("visualizeUserCreationTimes".equals(requestType)) {
				// get cursor
				DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData()
						.find(query).sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_ID, 1));
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
							DateTimeUtils.getStringOfDate("dd-MM-yyyy", DateTimeUtils.getTwitterDate(twitterDateStr)));
					jsonArray.put(userProcessInputData);
				}
			} else if ("visualizeUserTweetEntityRatios".equals(requestType)) {
				// get cursor
				DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData()
						.find(query).sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_ID, 1));
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
					userProcessInputData.put(MongoCollectionFieldNames.MONGO_USER_URL_RATIO,
							(double) next.get(MongoCollectionFieldNames.MONGO_USER_URL_RATIO));
					userProcessInputData.put(MongoCollectionFieldNames.MONGO_USER_HASHTAG_RATIO,
							(double) next.get(MongoCollectionFieldNames.MONGO_USER_HASHTAG_RATIO));
					userProcessInputData.put(MongoCollectionFieldNames.MONGO_USER_MENTION_RATIO,
							(double) next.get(MongoCollectionFieldNames.MONGO_USER_MENTION_RATIO));
					// init to array
					jsonArray.put(userProcessInputData);
				}
			} else if ("visualizeUserFriendFollowerRatio".equals(requestType)) {
				// get cursor
				DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData()
						.find(query).sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_ID, 1));
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
					userProcessInputData.put(MongoCollectionFieldNames.MONGO_USER_FRIEND_FOLLOWER_RATIO,
							(double) next.get(MongoCollectionFieldNames.MONGO_USER_FRIEND_FOLLOWER_RATIO));
					// init to array
					jsonArray.put(userProcessInputData);
				}
			} else if ("visualizeUserPostDeviceRatios".equals(requestType)) {
				// get cursor
				DBCursor paginatedResult = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData()
						.find(query).sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_USER_ID, 1));
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
					userProcessInputData.put(MongoCollectionFieldNames.MONGO_USER_POST_TWITTER_DEVICE_RATIO,
							(double) next.get(MongoCollectionFieldNames.MONGO_USER_POST_TWITTER_DEVICE_RATIO));
					userProcessInputData.put(MongoCollectionFieldNames.MONGO_USER_POST_MOBILE_DEVICE_RATIO,
							(double) next.get(MongoCollectionFieldNames.MONGO_USER_POST_MOBILE_DEVICE_RATIO));
					userProcessInputData.put(MongoCollectionFieldNames.MONGO_USER_THIRD_PARTY_DEVICE_RATIO,
							(double) next.get(MongoCollectionFieldNames.MONGO_USER_THIRD_PARTY_DEVICE_RATIO));
					// init to array
					jsonArray.put(userProcessInputData);
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
