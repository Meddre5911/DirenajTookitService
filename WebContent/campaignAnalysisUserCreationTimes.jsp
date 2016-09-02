<!DOCTYPE html>
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
	prepareUserCreationTimeGraph('visualizeUserCreationTimesInBarChart','creationTimeGraph','userCrationDate','(%) Percentage');
	prepareGroupedBarChart('visualizeUserTweetEntityRatiosInBarChart','userRatiosGraph','Percentage');
	prepareMultiLineUserRatiosGraph('visualizeUserPostDeviceRatios','userPostDevicesRatiosGraph','PostDeviceRatio');
// 	prepareUserRatiosGraph('visualizeUserFriendFollowerRatio','userFriendFollowerRatiosGraph','friendFollowerRatio');
	prepareUserRatiosGraphInBarChart('visualizeUserFriendFollowerRatioInBarChart','userFriendFollowerRatiosGraph','friendFollowerRatio','(%) Percentage');
	
	
	
	prepareGroupedBarChart('visualizeUserRoughTweetCountsInBarChart','userRoughTweetCountsGraph','Percentage');
	prepareUserRatiosGraphInBarChart('visualizeUserRoughHashtagTweetCountsInBarChart','userHashtagCountsGraph','HashtagPostCounts','(%) Percentage');
	prepareMultiLineUserRatiosGraphInDate('visualizeHourlyUserAndTweetCount','hourlyUserAndTweetCountGraph','Count');
	prepareSingleLineUserRatiosGraphInDate('visualizeHourlyTweetSimilarities','hourlyTweetSimilarities','Percentage');
	prepareGroupedBarChartWithTime('visualizeHourlyTweetSimilarities','hourlyTweetSimilarities','Percentage');
}

</script>


<div class="divTable" style="width: 100%;" >
<div class="divTableBody">

<div class="divTableRow">
<div class="divTableCell">
	<%
		BasicDBObject findQuery = new BasicDBObject();
		findQuery.put("_id", request.getParameter("requestId"));
		DBObject requestObj = DirenajMongoDriver.getInstance().getOrgBehaviorRequestCollection().findOne(findQuery);
		// retrive features of request object
		String campaignId = (String) requestObj.get("campaignId");
		String requestDefinition = (String) requestObj.get("requestDefinition");
		String tracedHashtag = ((List<String>) requestObj.get("tracedHashtag")).get(0);
		String linkUrl = "https://twitter.com/hashtag/"+tracedHashtag+"?src=hash";
	%>
	<b>Request Summary Info : </b> <br><br>
	<b>Campaign Id :</b> <%=campaignId%> <br><br>
	<b>Organized Behaviour Request Definition :</b> <%=requestDefinition%> <br><br>
	<b>Traced Hashtag :</b><a href=<%=linkUrl %> target="_blank"> #<%=tracedHashtag%></a> <br><br>
	
</div>
</div>

<div class="divTableRow">
<div id="creationTimeGraph" class="divTableCell"><b><big>Creation Times of Users :</big></b> <br></div>
</div>

<div class="divTableRow">

<div id="userRatiosGraph" class="divTableCell"><b><big>Ratios of Users :</big></b> <br></div>
</div>

<div class="divTableRow">
<!-- <div id="userPostDevicesRatiosGraph" class="divTableCell"><b><big>Post Device Ratios of Users :</big></b> <br></div> -->
</div>
<div class="divTableRow">
<div id="userFriendFollowerRatiosGraph" class="divTableCell"><b><big>Friend Follower Ratios of Users :</big></b> <br></div>
</div>

<div class="divTableRow">
<div id="userRoughTweetCountsGraph" class="divTableCell"><b><big>Favorite & Posted Tweet Counts of Users :</big></b> <br></div>
</div>

<div class="divTableRow">
<div id="userHashtagCountsGraph" class="divTableCell"><b><big>Post Counts of Users with Campaign Hashtag :</big></b> <br></div>
</div>

<div class="divTableRow">
<div id="hourlyUserAndTweetCountGraph" class="divTableCell"><b><big>Hour Basis Distinct Tweet & User Count of Campaign :</big></b> <br></div>
</div>

<div class="divTableRow">
<div id="hourlyTweetSimilarities" class="divTableCell"><b><big>Hour Basis Percentages of Most Similar (btw:0-30) Posts In Campaign Compared to All Posts :</big></b> <br></div>
</div>

<div class="divTableRow">
<div id="hourlyTweetSimilarities_MOST_SIMILAR" class="divTableCell"><b><big>Hour Basis Percentages of Most Similar (btw:0-30) Posts In Campaign Compared to All Posts :</big></b> <br></div>
</div>
<div class="divTableRow">
<div id="hourlyTweetSimilarities_VERY_SIMILAR" class="divTableCell"><b><big>Hour Basis Percentages of Very Similar (btw:30-45) Posts In Campaign Compared to All Posts :</big></b></div>
</div>
<div class="divTableRow">
<div id="hourlyTweetSimilarities_SIMILAR" class="divTableCell"><b><big>Hour Basis Percentages of Similar (btw:45-60) Posts In Campaign Compared to All Posts :</big></b></div>
</div>
<div class="divTableRow">
<div id="hourlyTweetSimilarities_SLIGHTLY_SIMILAR" class="divTableCell"><b><big>Hour Basis Percentages of Slightly Similar (btw:60-89) Posts In Campaign Compared to All Posts :</big></b></div>
</div>
<div class="divTableRow">
<div id="hourlyTweetSimilarities_NON_SIMILAR" class="divTableCell"><b><big>Hour Basis Percentages of None Similar (90 degree) Posts In Campaign Compared to All Posts :</big></b></div>
</div>


</div>
</div>

</body>
</html>