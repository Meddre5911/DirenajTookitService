<%@page import="java.net.URLEncoder"%>
<%@page import="direnaj.driver.DirenajMongoDriver"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="direnaj.domain.*"%>
<%@ page import="java.util.Map.Entry"%>
<%@ page import="java.math.BigDecimal"%>
<%@ page import="direnaj.driver.MongoCollectionFieldNames"%>
<%@ page import="com.mongodb.*"%>
<%@ page import="direnaj.util.*"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Campaign Comparison Results</title>
</head>
<body>

	<%
		String requestId = request.getParameter(MongoCollectionFieldNames.MONGO_REQUEST_ID);
		BasicDBObject campaignComparisonQuery = new BasicDBObject(MongoCollectionFieldNames.MONGO_REQUEST_ID,
				requestId);
		DBObject campaignComparison = DirenajMongoDriver.getInstance().getOrgBehaviourCampaignComparisons()
				.findOne(campaignComparisonQuery);

		String actualCampaignId = (String) campaignComparison.get(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID);
		BasicDBObject actualCampaignQuery = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID,
				actualCampaignId);
		DBObject actualCampaignObject = DirenajMongoDriver.getInstance().getCampaignsCollection()
				.findOne(actualCampaignQuery);
	%>

	<table>
		<tr>
			<td><b>Actual Campaign Id :</b></td>
			<td><%=campaignComparison.get(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID)%>
			</td>
			<td><b>Actual Hashtag :</b></td>
			<td><%=campaignComparison.get(MongoCollectionFieldNames.MONGO_COMPARISON_ACTUAL_HASHTAG)%>
			</td>
		</tr>
		<tr>
			<td><b>Date :</b></td>
			<td><%=actualCampaignObject.get("minCampaignDate")%> - <%=actualCampaignObject.get("maxCampaignDate")%>
			</td>
		</tr>
	</table>
	</br>
	</br>
	<table>
		<tr>
			<td><b>Compared_Campaign_Id</b>&nbsp;&nbsp;</td>
			<td><b>Compared_Hashtag</b>&nbsp;&nbsp;</td>
			<td><b>Date</b>&nbsp;&nbsp;</td>
			<td><b>Same_User_Count</b>&nbsp;&nbsp;</td>
			<td><b>Same_User_Percentage(Actual_Campaign)</b>&nbsp;&nbsp;</td>
			<td><b>Total_Compared_User_Count(Actual_Campaign)</b>&nbsp;&nbsp;</td>
			<td><b>Same_User_Percentage(Compared_Campaign)</b>&nbsp;&nbsp;</td>
			<td><b>Total_Compared_User_Count(Compared_Campaign)</b>&nbsp;&nbsp;</td>
			<td><b>Summary_of_Same_Users</b>&nbsp;&nbsp;</td>
		</tr>
		<%
			// get map
			List<DBObject> mongoDbObject4Comparisons = (List<DBObject>) campaignComparison
					.get(MongoCollectionFieldNames.MONGO_COMPARISON_RESULTS);
			StringBuilder sBuilder = new StringBuilder();
			for (DBObject comparisonResult : mongoDbObject4Comparisons) {

				String summaryStr = "<b>Actual Campaign Id : </b>"
						+ campaignComparison.get(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID)
						+ "&nbsp;&nbsp;<b>Actual Hashtag : </b>"
						+ campaignComparison.get(MongoCollectionFieldNames.MONGO_COMPARISON_ACTUAL_HASHTAG)
						+ "&nbsp;&nbsp;<b>Compared Campaign Id :</b>"
						+ comparisonResult.get(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID)
						+ "&nbsp;&nbsp;<b>Compared Hashtag : </b>" + comparisonResult.get("hashtag")
						+ "<br><br>"
						+ "<b>Same User Count :</b>" + comparisonResult.get("sameUserCount")
						+"<b>&nbsp;&nbsp;Total Compared User Count (Actual Campaign): </b>"
						+ comparisonResult.get("totalComparedUserCount4ActualCampaign")
						+ "&nbsp;&nbsp;<b>Same User Percentage (Actual Campaign):</b>"
						+ comparisonResult.get("sameUserPercentage4ActualCampaign")
						+ "<br><br>"
						+"<b>Total Compared User Count (Compared Campaign): </b>"
						+ comparisonResult.get("totalComparedUserCount4ComparedCampaign")
						+ "&nbsp;&nbsp;<b>Same User Percentage (Compared Campaign):</b>"
						+ comparisonResult.get("sameUserPercentage4ComparedCampaign") + "<br><br>";

				summaryStr = URLEncoder.encode(summaryStr, "UTF-8");

				String comparedCampaignId = (String) comparisonResult.get(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID);
				BasicDBObject comparedCampaignQuery = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID,
						comparedCampaignId);
				DBObject comparedCampaignObject = DirenajMongoDriver.getInstance().getCampaignsCollection()
						.findOne(comparedCampaignQuery);

				sBuilder.append("<tr><td>" + comparisonResult.get(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID)
						+ "</td><td><a href=\"https://twitter.com/hashtag/" + comparisonResult.get("hashtag")
						+ "?src=hash\" target=\"_blank\">#" + comparisonResult.get("hashtag") + "</a></td><td>"
						+ comparedCampaignObject.get("minCampaignDate") + "<br>"
						+ comparedCampaignObject.get("maxCampaignDate") + "</td><td> "
						+ comparisonResult.get("sameUserCount") + "</td><td> % "
						+ comparisonResult.get("sameUserPercentage4ActualCampaign") + "</td><td>"
						+ comparisonResult.get("totalComparedUserCount4ActualCampaign") + "</td><td>% "
						+ comparisonResult.get("sameUserPercentage4ComparedCampaign") + "</td><td>"
						+ comparisonResult.get("totalComparedUserCount4ComparedCampaign") + "</td><td>"
						+ "<a href=campaignComparisonUsersSummary.jsp?requestId=" + comparisonResult.get("requestId")
						+ "&summaryStr=" + summaryStr + ">User Summary</a>" + "</td></tr>");
			}
			out.println(sBuilder.toString());
		%>
	</table>

</body>
</html>