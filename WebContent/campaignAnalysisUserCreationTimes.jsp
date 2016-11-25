<!DOCTYPE html>
<%@page import="direnaj.driver.MongoCollectionFieldNames"%>
<%@page import="java.util.List"%>
<%@page import="com.mongodb.DBObject"%>
<%@page import="com.mongodb.BasicDBObject"%>
<%@page import="direnaj.driver.DirenajMongoDriverUtil"%>
<%@page import="direnaj.driver.DirenajMongoDriver"%>
<html>
<meta charset="utf-8">

<!-- Example based on http://bl.ocks.org/mbostock/3887118 -->
<!-- Tooltip example from http://www.d3noob.org/2013/01/adding-tooltips-to-d3js-graph.html -->

<style>
body {
  font: 11px sans-serif;
}

.axis path,
.axis line {
  fill: none;
  stroke: #000;
  shape-rendering: crispEdges;
}

.axis--x path {
  display: none;
}

.line {
  fill: none;
  stroke: steelblue;
  stroke-width: 1.5px;
}

.dot {
  stroke: #000;
}

.tooltip {
  position: absolute;
  width: 200px;
  height: 28px;
  pointer-events: none;
}

.divTable{
	display: table;
	width: 100%;
}
.divTableRow {
	display: table-row;
}
.divTableHeading {
	background-color: #EEE;
	display: table-header-group;
}
.divTableCell, .divTableHead {
	border: 1px solid #999999;
	display: table-cell;
	padding: 3px 10px;
}
.divTableHeading {
	background-color: #EEE;
	display: table-header-group;
	font-weight: bold;
}
.divTableFoot {
	background-color: #EEE;
	display: table-footer-group;
	font-weight: bold;
}
.divTableBody {
	display: table-row-group;
}
</style>
<head>
</head>
<body onload="prepareGraphs()">

<input type="hidden" name="requestId" id="requestId"
		value="<%=request.getParameter("requestId")%>">

<script src="js/d3Graph.js"></script>
<script type="text/javascript" src="js/jquery-1.11.2.min.js"></script>
<script type="text/javascript" src="js/d3.v3.min.js"></script>

<script>

function prepareGraphs(){ 
// 	prepareUserCreationTimeGraph();
	prepareUserCreationTimeGraph('visualizeUserCreationTimesInBarChart&userHashtagPostCount=0','creationTimeGraph','userCrationDate','(%) Percentage');
	prepareUserCreationTimeGraph('visualizeUserCreationTimesInBarChart&userHashtagPostCount=2','creationTimeGraph_2','userCrationDate','(%) Percentage');
	prepareUserCreationTimeGraph('visualizeUserCreationTimesInBarChart&userHashtagPostCount=10','creationTimeGraph_10','userCrationDate','(%) Percentage');
	prepareUserCreationTimeGraph('visualizeUserCreationTimesInBarChart&userHashtagPostCount=50','creationTimeGraph_50','userCrationDate','(%) Percentage');
	
	prepareGroupedBarChart('visualizeUserTweetEntityRatiosInBarChart&userHashtagPostCount=0','userRatiosGraph','User Ratio Values','(%) Total User Percentage For Given Ratio');
	prepareGroupedBarChart('visualizeUserTweetEntityRatiosInBarChart&userHashtagPostCount=2','userRatiosGraph_2','User Ratio Values','(%) Total User Percentage For Given Ratio');
	prepareGroupedBarChart('visualizeUserTweetEntityRatiosInBarChart&userHashtagPostCount=10','userRatiosGraph_10','User Ratio Values','(%) Total User Percentage For Given Ratio');
	prepareGroupedBarChart('visualizeUserTweetEntityRatiosInBarChart&userHashtagPostCount=50','userRatiosGraph_50','User Ratio Values','(%) Total User Percentage For Given Ratio');

	// 	prepareMultiLineUserRatiosGraph('visualizeUserPostDeviceRatios','userPostDevicesRatiosGraph','PostDeviceRatio');
// 	prepareUserRatiosGraph('visualizeUserFriendFollowerRatio','userFriendFollowerRatiosGraph','friendFollowerRatio');
	
	prepareUserRatiosGraphInBarChart('visualizeUserFriendFollowerRatioInBarChart&userHashtagPostCount=0','userFriendFollowerRatiosGraph','Followers Count / Total Friend Follower Count','(%) Percentage');
	prepareUserRatiosGraphInBarChart('visualizeUserFriendFollowerRatioInBarChart&userHashtagPostCount=2','userFriendFollowerRatiosGraph_2','Followers Count / Total Friend Follower Count','(%) Percentage');
	prepareUserRatiosGraphInBarChart('visualizeUserFriendFollowerRatioInBarChart&userHashtagPostCount=10','userFriendFollowerRatiosGraph_10','Followers Count / Total Friend Follower Count','(%) Percentage');
	prepareUserRatiosGraphInBarChart('visualizeUserFriendFollowerRatioInBarChart&userHashtagPostCount=50','userFriendFollowerRatiosGraph_50','Followers Count / Total Friend Follower Count','(%) Percentage');
	
	
	prepareMultiLineUserRatiosGraphInDate('visualizeHourlyEntityRatios','statusHourlyEntityRatios','Tweet Post Time Interval','Ratios');
	
	prepareGroupedBarChart('visualizeUserRoughTweetCountsInBarChart&userHashtagPostCount=0','userRoughTweetCountsGraph','Tweet Count','(%) Total User Percentage For Given Tweet Count');
	prepareGroupedBarChart('visualizeUserRoughTweetCountsInBarChart&userHashtagPostCount=2','userRoughTweetCountsGraph_2','Tweet Count','(%) Total User Percentage For Given Tweet Count');
	prepareGroupedBarChart('visualizeUserRoughTweetCountsInBarChart&userHashtagPostCount=10','userRoughTweetCountsGraph_10','Tweet Count','(%) Total User Percentage For Given Tweet Count');
	prepareGroupedBarChart('visualizeUserRoughTweetCountsInBarChart&userHashtagPostCount=50','userRoughTweetCountsGraph_50','Tweet Count','(%) Total User Percentage For Given Tweet Count');
	
	prepareUserRatiosGraphInBarChart('visualizeUserRoughHashtagTweetCountsInBarChart','userHashtagCountsGraph','Post Counts With Given Hashtag','(%) Percentage of User Based on Their Post Counts With Given Hashtag');
	
	prepareMultiLineUserRatiosGraphInDate('visualizeHourlyUserAndTweetCount','hourlyUserAndTweetCountGraph','Tweet Post Time','Tweet / User Count');
	prepareMultiLineUserRatiosGraphInDate('visualizeHourlyRetweetedUserAndPostCount','hourlyRetweetedUserAndPostCountGraph','Retweet Post Time','Distinct Retweet / User Count');
	prepareMultiLineUserRatiosGraphInDate('visualizeHourlyNonRetweetedUserAndTweetCount','hourlyNonRetweetedUserAndPostCountGraph','Non Retweet Post Time','Distinct Non Retweet / User Count');
	
	
	prepareSingleLineUserRatiosGraphForAllSimilarites('visualizeHourlyTweetSimilarities','hourlyTweetSimilarities','Time','(%) Percentage of Similar Tweets within Given Time');
	prepareGroupedBarChartWithTime('visualizeHourlyTweetSimilarities','hourlyTweetSimilarities','Time','(%) Percentage of Similar Tweets within Given Time');

	prepareMultiLineUserRatiosGraphInDate('visualizeHourlyRetweetRatios','hourlyRetweetRatios','Time','(%) Percentage of Retweets');
	
	prepareMultiLineUserRatiosGraphInDate('visualizeHourlyTotalAndDistinctMentionCount','visualizeHourlyTotalAndDistinctMentionCount','Tweet Post Time','Total / Distinct Mention Count');
	prepareMultiLineUserRatiosGraphInDate('visualizeHourlyRetweetedTotalAndDistinctMentionCount','visualizeHourlyRetweetedTotalAndDistinctMentionCount','Tweet Post Time','Total / Distinct Mention Count');
	prepareMultiLineUserRatiosGraphInDate('visualizeHourlyNonRetweetedTotalAndDistinctMentionCount','visualizeHourlyNonRetweetedTotalAndDistinctMentionCount','Tweet Post Time','Total / Distinct Mention Count');

	getMeanVariance();
}

</script>


<div class="divTable" style="width: 100%;" >
<div class="divTableBody">

<div class="divTableRow">
	<%
		BasicDBObject findQuery = new BasicDBObject();
		findQuery.put("_id", request.getParameter("requestId"));
		DBObject requestObj = DirenajMongoDriver.getInstance().getOrgBehaviorRequestCollection().findOne(findQuery);
		// retrive features of request object
		String campaignId = (String) requestObj.get("campaignId");
		String requestDefinition = (String) requestObj.get("requestDefinition");
		String tracedHashtag = ((List<String>) requestObj.get("tracedHashtag")).get(0);
		String linkUrl = "https://twitter.com/hashtag/"+tracedHashtag+"?src=hash";
		
		BasicDBObject campaignQuery = new BasicDBObject();
		campaignQuery.put("campaign_id", campaignId);
		DBObject campaignStatistics = DirenajMongoDriver.getInstance().getCampaignStatisticsCollection().findOne(campaignQuery);
		Double hashtagVarience = (Double)campaignStatistics.get(MongoCollectionFieldNames.MONGO_CAMPAIGN_HASHTAG_VARIANCE); 
		Double hashtagStdDev = (Double )campaignStatistics.get(MongoCollectionFieldNames.MONGO_CAMPAIGN_HASHTAG_STANDARD_DEVIATION); 
		
		
	%>
<div id="summaryInfo" class="divTableCell">
	<b>Request Campaign Id :</b> <%=campaignId%> 
	&nbsp;&nbsp;<b>Description :</b> <%=requestDefinition%> 
	&nbsp;&nbsp;<b>Traced Hashtag :</b><a href=<%=linkUrl %> target="_blank"> #<%=tracedHashtag%></a>
</div>
</div>
<div id="requestStatistics" class="divTableCell">
	<b>Campaign Hashtag Variance & Std. Deviation :  </b> <%=hashtagVarience%> & <%=hashtagStdDev%> 
</div>
</div>

<div class="divTableRow">
<div id="creationTimeGraph" class="divTableCell"><b><big>Creation Times of Users :</big></b> <br></div>
</div>

<div class="divTableRow">
<div id="userCreationDateMeanVariance" class="divTableCell">
</div>
</div>

<div class="divTableRow">
<div id="creationTimeGraph_2" class="divTableCell"><b><big>Creation Times of Users who used campaign Hashtag in 2 or more Tweets :</big></b> <br></div>
</div>

<div class="divTableRow">
<div id="userCreationDateMeanVariance_2" class="divTableCell">
</div>
</div>

<div class="divTableRow">
<div id="creationTimeGraph_10" class="divTableCell"><b><big>Creation Times of Users who used campaign Hashtag in 10 or more Tweets :</big></b> <br></div>
</div>

<div class="divTableRow">
<div id="userCreationDateMeanVariance_10" class="divTableCell">
</div>
</div>

<div class="divTableRow">
<div id="creationTimeGraph_50" class="divTableCell"><b><big>Creation Times of Users who used campaign Hashtag in 50 or more Tweets :</big></b> <br></div>
</div>

<div class="divTableRow">
<div id="userCreationDateMeanVariance_50" class="divTableCell">
</div>
</div>

<div class="divTableRow">
<div id="userRatiosGraph" class="divTableCell"><b><big>Ratios of Users :</big></b> <br></div>
</div>

<div class="divTableRow">
<div id="userRatiosGraphMeanVariance" class="divTableCell">
</div>
</div>

<div class="divTableRow">
<div id="userRatiosGraph_2" class="divTableCell"><b><big>Ratios of Users who used campaign Hashtag in 2 or more Tweets :</big></b> <br></div>
</div>

<div class="divTableRow">
<div id="userRatiosGraphMeanVariance_2" class="divTableCell">
</div>
</div>

<div class="divTableRow">
<div id="userRatiosGraph_10" class="divTableCell"><b><big>Ratios of Users who used campaign Hashtag in 10 or more Tweets :</big></b> <br></div>
</div>

<div class="divTableRow">
<div id="userRatiosGraphMeanVariance_10" class="divTableCell">
</div>
</div>

<div class="divTableRow">
<div id="userRatiosGraph_50" class="divTableCell"><b><big>Ratios of Users who used campaign Hashtag in 50 or more Tweets :</big></b> <br></div>
</div>

<div class="divTableRow">
<div id="userRatiosGraphMeanVariance_50" class="divTableCell">
</div>
</div>

<div class="divTableRow">
<!-- <div id="userPostDevicesRatiosGraph" class="divTableCell"><b><big>Post Device Ratios of Users :</big></b> <br></div> -->
</div>


<div class="divTableRow">
<div id="userFriendFollowerRatiosGraph" class="divTableCell"><b><big>Friend Follower Ratios of Users :</big></b> <br></div>
</div>

<div class="divTableRow">
<div id="friendFollowerRatiosMeanVariance" class="divTableCell">
</div>
</div>

<div class="divTableRow">
<div id="userFriendFollowerRatiosGraph_2" class="divTableCell"><b><big>Friend Follower Ratios of Users who used campaign Hashtag in 2 or more Tweets :</big></b> <br></div>
</div>

<div class="divTableRow">
<div id="friendFollowerRatiosMeanVariance_2" class="divTableCell">
</div>
</div>

<div class="divTableRow">
<div id="userFriendFollowerRatiosGraph_10" class="divTableCell"><b><big>Friend Follower Ratios of Users who used campaign Hashtag in 10 or more Tweets :</big></b> <br></div>
</div>

<div class="divTableRow">
<div id="friendFollowerRatiosMeanVariance_10" class="divTableCell">
</div>
</div>

<div class="divTableRow">
<div id="userFriendFollowerRatiosGraph_50" class="divTableCell"><b><big>Friend Follower Ratios of Users who used campaign Hashtag in 50 or more Tweets :</big></b> <br></div>
</div>

<div class="divTableRow">
<div id="friendFollowerRatiosMeanVariance_50" class="divTableCell">
</div>
</div>











<div class="divTableRow">
<div id="userRoughTweetCountsGraph" class="divTableCell"><b><big>Favorite & Posted Tweet Counts of Users :</big></b> <br></div>
</div>

<div class="divTableRow">
<div id="userRoughTweetCountsMeanVariance" class="divTableCell">

</div>
</div>

<div class="divTableRow">
<div id="userRoughTweetCountsGraph_2" class="divTableCell"><b><big>Favorite & Posted Tweet Counts of Users who used campaign Hashtag in 2 or more Tweets :</big></b> <br></div>
</div>

<div class="divTableRow">
<div id="userRoughTweetCountsMeanVariance_2" class="divTableCell">

</div>
</div>

<div class="divTableRow">
<div id="userRoughTweetCountsGraph_10" class="divTableCell"><b><big>Favorite & Posted Tweet Counts of Users who used campaign Hashtag in 10 or more Tweets :</big></b> <br></div>
</div>

<div class="divTableRow">
<div id="userRoughTweetCountsMeanVariance_10" class="divTableCell">

</div>
</div>

<div class="divTableRow">
<div id="userRoughTweetCountsGraph_50" class="divTableCell"><b><big>Favorite & Posted Tweet Counts of Users who used campaign Hashtag in 50 or more Tweets :</big></b> <br></div>
</div>

<div class="divTableRow">
<div id="userRoughTweetCountsMeanVariance_50" class="divTableCell">

</div>
</div>



<div class="divTableRow">
<div id="userHashtagCountsGraph" class="divTableCell"><b><big>Post Counts of Users with Campaign Hashtag :</big></b> <br></div>
</div>

<div class="divTableRow">
<div id="userHashtagPostCountMeanVariance" class="divTableCell">
</div>
</div>

<div class="divTableRow">
<div id="statusHourlyEntityRatios" class="divTableCell"><b><big>Hourly Status Entity Ratios :</big></b> <br></div>
</div>

<div class="divTableRow">
<div id="statusHourlyEntityRatiosMeanVariance" class="divTableCell"></div>
</div>

<div class="divTableRow">
<div id="hourlyRetweetRatios" class="divTableCell"><b><big>Hourly Retweet Ratios :</big></b> <br></div>
</div>

<div class="divTableRow">
<div id="hourlyRetweetRatiosMeanVariance" class="divTableCell"></div>
</div>


<div class="divTableRow">
<div id="hourlyUserAndTweetCountGraph" class="divTableCell"><b><big>Hour Basis Distinct Tweet & User Count of Campaign :</big></b> <br></div>
</div>

<div class="divTableRow">
<div id="hourlyTweetUserCountRatioMeanVariance" class="divTableCell"></div>
</div>


<div class="divTableRow">
<div id="hourlyRetweetedUserAndPostCountGraph" class="divTableCell"><b><big>Hour Basis Distinct Retweet & User Count of Campaign :</big></b> <br></div>
</div>

<div class="divTableRow">
<div id="hourlyDistinctRetweetUserCountRatioMeanVariance" class="divTableCell"></div>
</div>

<div class="divTableRow">
<div id="hourlyNonRetweetedUserAndPostCountGraph" class="divTableCell"><b><big>Hour Basis Distinct Non Retweet & User Count of Campaign :</big></b> <br></div>
</div>
<div class="divTableRow">
<div id="hourlyDistinctNonRetweetUserCountRatioMeanVariance" class="divTableCell"></div>
</div>


<div class="divTableRow">
<div id="visualizeHourlyTotalAndDistinctMentionCount" class="divTableCell"><b><big>Hour Basis  Total & Distinct Mention Counts of Campaign :</big></b> <br></div>
</div>

<div class="divTableRow">
<div id="visualizeHourlyRetweetedTotalAndDistinctMentionCount" class="divTableCell"><b><big>Hour Basis Retweeted Total & Distinct Mention Counts of Campaign :</big></b> <br></div>
</div>

<div class="divTableRow">
<div id="visualizeHourlyNonRetweetedTotalAndDistinctMentionCount" class="divTableCell"><b><big>Hour Basis Non Retweeted Total & Distinct Mention Counts of Campaign :</big></b> <br></div>
</div>




<div class="divTableRow">
<div id="hourlyTweetSimilarities" class="divTableCell"><b><big>Hour Basis Percentages of Most Similar (btw:0-30) Posts In Campaign Compared to All Posts :</big></b> <br></div>
</div>

<div class="divTableRow">
<div id="hourlyTweetSimilaritiesMeanVariance" class="divTableCell"></div>
</div>




</div>
</div>

</body>
</html>