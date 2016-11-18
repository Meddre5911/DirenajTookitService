package direnaj.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import direnaj.driver.DirenajMongoDriver;
import direnaj.driver.MongoCollectionFieldNames;

public class MongoPaginationServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");

		PrintWriter out = response.getWriter();
		String pageType = request.getParameter("pageType");
		JSONArray jsonArray = new JSONArray();
		JSONObject responseJSonObject = new JSONObject();

		HttpSession session = request.getSession(true);

		Integer pageDisplayLength = Integer.valueOf(request.getParameter("iDisplayLength"));
		// Fetch the page number from client
		Integer pageNumber = (Integer.valueOf(request.getParameter("iDisplayStart")) / pageDisplayLength);
		String sEcho = request.getParameter("sEcho");
		int recordCounts = 0;

		try {
			if ("requestListPagination".equals(pageType)) {
				// mongo call
				DBCollection orgBehaviorRequestCollection = DirenajMongoDriver.getInstance()
						.getOrgBehaviorRequestCollection();
				if (session.getAttribute("collectionRecordCount") == null) {
					recordCounts = (int) orgBehaviorRequestCollection.count();
					session.setAttribute("collectionRecordCount", recordCounts);
				} else {
					recordCounts = Integer.valueOf(session.getAttribute("collectionRecordCount").toString());
				}
				// get cursor
				DBCursor paginatedResult = orgBehaviorRequestCollection.find().sort(new BasicDBObject("_id", -1))
						.skip(pageNumber * pageDisplayLength).limit(pageDisplayLength);
				// get objects from cursor
				try {
					while (paginatedResult.hasNext()) {
						jsonArray.put(new JSONObject(paginatedResult.next().toString()));
					}
				} catch (JSONException e) {
					Logger.getLogger(MongoPaginationServlet.class).error("Error in MongoPaginationServlet.", e);
				}

			} else if ("requestToolkitCampaigns".equals(pageType)) {
				// mongo call
				DBCollection campaignCollection = DirenajMongoDriver.getInstance().getCampaignsCollection();
				if (session.getAttribute("collectionRecordCount") == null) {
					recordCounts = (int) campaignCollection.count();
					session.setAttribute("collectionRecordCount", recordCounts);
				} else {
					recordCounts = Integer.valueOf(session.getAttribute("collectionRecordCount").toString());
				}
				BasicDBObject query = new BasicDBObject("campaign_type", "Search Api");
				// get cursor
				DBCursor paginatedResult = campaignCollection.find(query).sort(new BasicDBObject("_id", -1))
						.skip(pageNumber * pageDisplayLength).limit(pageDisplayLength);
				// get objects from cursor
				try {
					while (paginatedResult.hasNext()) {
						jsonArray.put(new JSONObject(paginatedResult.next().toString()));
					}
				} catch (JSONException e) {
					Logger.getLogger(MongoPaginationServlet.class).error("Error in MongoPaginationServlet.", e);
				}
			} else if ("requestInputData".equals(pageType)) {
				String requestId = request.getParameter("retrievedRequestId");
				DBCollection inputDataCollection = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData();
				BasicDBObject query = new BasicDBObject("requestId", requestId);

				if (session.getAttribute("userInputDataCount") == null) {
					recordCounts = (int) inputDataCollection.count(query);
					session.setAttribute("userInputDataCount", recordCounts);
				} else {
					recordCounts = Integer.valueOf(session.getAttribute("userInputDataCount").toString());
				}
				// get cursor
				DBCursor paginatedResult = inputDataCollection.find(query).skip(pageNumber * pageDisplayLength)
						.limit(pageDisplayLength);
				// get objects from cursor
				try {
					while (paginatedResult.hasNext()) {
						jsonArray.put(new JSONObject(paginatedResult.next().toString()));
					}
				} catch (JSONException e) {
					Logger.getLogger(MongoPaginationServlet.class).error("Error in MongoPaginationServlet.", e);
				}

			} else if ("requestCampaignAnalysis".equals(pageType)) {
				String campaignId = request.getParameter("campaignId");
				DBCollection campaignStatisticsCollection = DirenajMongoDriver.getInstance()
						.getCampaignStatisticsCollection();
				BasicDBObject query = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, campaignId);

				if (session.getAttribute("userInputDataCount") == null) {
					recordCounts = (int) campaignStatisticsCollection.count(query);
					session.setAttribute("userInputDataCount", recordCounts);
				} else {
					recordCounts = Integer.valueOf(session.getAttribute("userInputDataCount").toString());
				}
				// get cursor
				DBCursor paginatedResult = campaignStatisticsCollection.find(query).skip(pageNumber * pageDisplayLength)
						.limit(pageDisplayLength);
				// get objects from cursor
				try {
					while (paginatedResult.hasNext()) {
						DBObject record = paginatedResult.next();
						jsonArray.put(new JSONObject(record.toString()));
					}
				} catch (JSONException e) {
					Logger.getLogger(MongoPaginationServlet.class).error("Error in MongoPaginationServlet.", e);
				}

			} else if ("requestAllCampaignAnalysis".equals(pageType)) {
				DBCollection campaignStatisticsCollection = DirenajMongoDriver.getInstance()
						.getCampaignStatisticsCollection();

				recordCounts = (int) campaignStatisticsCollection.count();
				session.setAttribute("userInputDataCount", recordCounts);
				
				// get cursor
				DBCursor paginatedResult = campaignStatisticsCollection.find()
						.sort(new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_TOTAL_TWEET_COUNT, -1))
						.skip(pageNumber * pageDisplayLength).limit(pageDisplayLength);
				// get objects from cursor
				try {
					while (paginatedResult.hasNext()) {
						DBObject record = paginatedResult.next();
						DBObject campaignQuery = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID,
								record.get(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID));
						DBObject campaignRecord = DirenajMongoDriver.getInstance().getCampaignsCollection()
								.findOne(campaignQuery);
						String hashtagQueries = (String) campaignRecord.get("query_terms");
						String[] hashtags = hashtagQueries.split(",");
						StringBuilder sBuilder = new StringBuilder();
						for (int i = 0; i < hashtags.length; i++) {
							String hashtag = hashtags[i].substring(1);
							sBuilder.append("<a href=\"https://twitter.com/hashtag/" + hashtag
									+ "?src=hash\" target=\"_blank\">#" + hashtag + "</a> ");
						}
						record.put("queryHashtags", sBuilder.toString());
						jsonArray.put(new JSONObject(record.toString()));
					}
				} catch (JSONException e) {
					Logger.getLogger(MongoPaginationServlet.class).error("Error in MongoPaginationServlet.", e);
				}

			} else if ("requestTweetSimilaritiesInRequest".equals(pageType)) {

				String requestId = request.getParameter("retrievedRequestId");
				DBCollection tweetSimilarityCollection = DirenajMongoDriver.getInstance()
						.getOrgBehaviourProcessTweetSimilarity();
				BasicDBObject query = new BasicDBObject("requestId", requestId);

				if (session.getAttribute("similarTweetsDataCount") == null) {
					recordCounts = (int) tweetSimilarityCollection.count(query);
					session.setAttribute("similarTweetsDataCount", recordCounts);
				} else {
					recordCounts = Integer.valueOf(session.getAttribute("similarTweetsDataCount").toString());
				}
				// get cursor
				DBCursor paginatedResult = tweetSimilarityCollection.find(query).skip(pageNumber * pageDisplayLength)
						.limit(pageDisplayLength);
				// get objects from cursor
				try {
					while (paginatedResult.hasNext()) {
						jsonArray.put(new JSONObject(paginatedResult.next().toString()));
					}
				} catch (JSONException e) {
					Logger.getLogger(MongoPaginationServlet.class).error("Error in MongoPaginationServlet.", e);
				}
			} else if ("requestTweetSimilarityCalculationsInRequest".equals(pageType)) {

				String requestId = request.getParameter("retrievedRequestId");
				DBCollection similarityCalculationRequestCollection = DirenajMongoDriver.getInstance()
						.getOrgBehaviourRequestedSimilarityCalculations();
				BasicDBObject query = new BasicDBObject("originalRequestId", requestId)
						.append(MongoCollectionFieldNames.MONGO_TWEET_FOUND, true);

				if (session.getAttribute("requestedSimilarityCalculations") == null) {
					recordCounts = (int) similarityCalculationRequestCollection.count(query);
					session.setAttribute("requestedSimilarityCalculations", recordCounts);
				} else {
					recordCounts = Integer.valueOf(session.getAttribute("requestedSimilarityCalculations").toString());
				}
				// get cursor
				DBCursor paginatedResult = similarityCalculationRequestCollection.find(query)
						.skip(pageNumber * pageDisplayLength).limit(pageDisplayLength);
				// get objects from cursor
				try {
					while (paginatedResult.hasNext()) {
						jsonArray.put(new JSONObject(paginatedResult.next().toString()));
					}
				} catch (JSONException e) {
					Logger.getLogger(MongoPaginationServlet.class).error("Error in MongoPaginationServlet.", e);
				}
			}
			responseJSonObject.put("iTotalRecords", recordCounts);
			responseJSonObject.put("iTotalDisplayRecords", recordCounts);
			responseJSonObject.put("sEcho", sEcho);
			responseJSonObject.put("aaData", jsonArray);
			out.print(responseJSonObject.toString());
		} catch (Exception e) {
			e.printStackTrace();
			response.setContentType("text/html");
			response.getWriter().print(e.getMessage());
		}
	}

}
