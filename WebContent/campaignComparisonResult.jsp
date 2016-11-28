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
		BasicDBObject campaignComparisonQuery = new BasicDBObject(MongoCollectionFieldNames.MONGO_REQUEST_ID,requestId );
		DBObject campaignComparison = DirenajMongoDriver.getInstance().getOrgBehaviourCampaignComparisons().findOne(campaignComparisonQuery);
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
	</table>
		</br>
		</br>
	<table>
		<tr>
			<td><b>Compared Campaign Id - </b></td>
			<td><b>Compared Hashtag</b> - </td>
			<td><b>Same User Percentage - </b></td>
			<td><b>Same User Count - </b></td>
			<td><b>Total Compared User Count - </b></td>
			<td><b>Summary of Same Users</b></td>
		</tr>
		<%
			// get map
				List<DBObject> mongoDbObject4Comparisons = (List<DBObject>)campaignComparison.get(MongoCollectionFieldNames.MONGO_COMPARISON_RESULTS);
				StringBuilder sBuilder = new StringBuilder();
				for (DBObject comparisonResult : mongoDbObject4Comparisons) {
					
					String summaryStr = "<b>Actual Campaign Id : </b>" + campaignComparison.get(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID)
					+ "&nbsp;&nbsp;<b>Actual Hashtag : </b>" + campaignComparison.get(MongoCollectionFieldNames.MONGO_COMPARISON_ACTUAL_HASHTAG)
					+ "&nbsp;&nbsp;<b>Compared Campaign Id :</b>"+ comparisonResult.get(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID)
					+ "&nbsp;&nbsp;<b>Compared Hashtag : </b>" + comparisonResult.get("hashtag") 
					+ "<br><b>Total Compared User Count : </b>" + comparisonResult.get("totalComparedUserCount") 
					+ "&nbsp;&nbsp;<b>Same User Count :</b>" + comparisonResult.get("sameUserCount")
					+ "&nbsp;&nbsp;<b>Same User Percentage :</b>" + comparisonResult.get("sameUserPercentage") +"<br><br>";
					
					summaryStr = URLEncoder.encode(summaryStr,"UTF-8");
					
					sBuilder.append("<tr><td>" + comparisonResult.get(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID)
							+ "</td><td><a href=\"https://twitter.com/hashtag/" + comparisonResult.get("hashtag")
							+ "?src=hash\" target=\"_blank\">#" + comparisonResult.get("hashtag") + "</a></td><td> % "
							+ comparisonResult.get("sameUserPercentage") + "</td><td>"
							+ comparisonResult.get("sameUserCount")
							+"</td><td>"
							+ comparisonResult.get("totalComparedUserCount")
							+"</td><td>"
							+"<a href=campaignComparisonUsersSummary.jsp?requestId="+comparisonResult.get("requestId")+"&summaryStr="+summaryStr+">User Summary</a>"
							+"</td></tr>");
				}
			out.println(sBuilder.toString());
		%>
	</table>

</body>
</html>