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
<title>Campaign Word Frequncies</title>
</head>
<body>

	<h1>Campaign Id : <%=request.getParameter("campaignId")%></h1>
	<table>
		<tr>
			<td><b>Word</b></td>
			<td><b>Word Frequency</b></td>
			<td><b>Word Usage Percentage In Campaign</b></td>
		</tr>
			<%
				Double totalPercentage = 0d;
				String campaignId = request.getParameter("campaignId");
				DBCollection campaignStatisticsCollection = DirenajMongoDriver.getInstance()
						.getCampaignStatisticsCollection();
				BasicDBObject query = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, campaignId);
				// get object
				DBObject campaignStatistic = campaignStatisticsCollection.findOne(query);
				Double totalWordCount = (Double)campaignStatistic.get(MongoCollectionFieldNames.MONGO_CAMPAIGN_TOTAL_WORD_COUNT);
				// get map
				Map<String,Double> wordFrequencyMap = (Map<String, Double>) campaignStatistic.get(MongoCollectionFieldNames.MONGO_CAMPAIGN_WORD_FREQUENCIES);
				Map<String,Double> sortedWordFrequencyMap = CollectionUtil.sortByComparator(wordFrequencyMap);
				StringBuilder sBuilder = new StringBuilder();
				for (Entry<String, Double> wordFrequency : sortedWordFrequencyMap.entrySet()) {
					Double wordPercentage = NumberUtils.roundDouble(4,(wordFrequency.getValue() * 100d) / totalWordCount);
					totalPercentage += wordPercentage;
					sBuilder.append("<tr><td>"+wordFrequency.getKey()+"</td><td>"+wordFrequency.getValue()+"</td><td>%"+wordPercentage+"</td></tr>");
				}
				sBuilder.append("<tr><td></td><td><b>Total Percentage : </b></td><td>"+totalPercentage+"</td></tr>");
				out.println(sBuilder.toString());
			%>
	</table>

</body>
</html>