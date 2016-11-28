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
	prepareUserCreationTimeGraph('visualizeUserCreationTimesInBarChart&userHashtagPostCount=0','creationTimeGraph','userCrationDate','(%) Percentage');
	prepareUserRatiosGraphInBarChart('visualizeUserFriendFollowerRatioInBarChart&userHashtagPostCount=0','userFriendFollowerRatiosGraph','Followers Count / Total Friend Follower Count','(%) Percentage');
	prepareGroupedBarChart('visualizeUserRoughTweetCountsInBarChart&userHashtagPostCount=0','userRoughTweetCountsGraph','Tweet Count','(%) Total User Percentage For Given Tweet Count');
	
	prepareUserRatiosGraphInBarChart('visualizeAvarageDailyPostCountInBarChart','visualizeAvarageDailyPostCountInBarChart','Daily Avarage Post Counts','(%) User Percentage');
	getMeanVariance('getMeanVarianceUserBasics');
}

</script>


<div class="divTable" style="width: 100%;" >
<div class="divTableBody">

<div class="divTableRow">
<div id="summaryInfo" class="divTableCell">
<%=request.getParameter("summaryStr")%>
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
<div id="userFriendFollowerRatiosGraph" class="divTableCell"><b><big>Friend Follower Ratios of Users :</big></b> <br></div>
</div>

<div class="divTableRow">
<div id="friendFollowerRatiosMeanVariance" class="divTableCell">
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
<div id="visualizeAvarageDailyPostCountInBarChart" class="divTableCell"><b><big>User Daily Avarage Post Counts:</big></b> <br></div>
</div>

<div class="divTableRow">
<div id="userDailyAvarageTweetCountMeanVariance" class="divTableCell">

</div>
</div>







</div>
</div>

</body>
</html>