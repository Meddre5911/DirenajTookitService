<!DOCTYPE html>
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
<body onload="prepareGraphs()">

<input type="hidden" name="requestId" id="requestId"
		value="<%=request.getParameter("requestId")%>">

<script src="js/d3.v3.min.js"></script>
<script src="js/d3Graph.js"></script>
<script type="text/javascript" src="js/jquery-1.11.2.min.js"></script>

<script>

function prepareGraphs(){ 
	prepareUserCreationTimeGraph();
	prepareUserRatiosGraph();
}

</script>


<div class="divTable" style="width: 100%;" >
<div class="divTableBody">
<div class="divTableRow">
<div id="creationTimeGraph" class="divTableCell"></div>
<div id="userRatiosGraph" class="divTableCell"></div>
</div>
<div class="divTableRow">
<div class="divTableCell">&nbsp;</div>
<div class="divTableCell">&nbsp;</div>
</div>
<div class="divTableRow">
<div class="divTableCell">&nbsp;</div>
<div class="divTableCell">&nbsp;</div>
</div>
<div class="divTableRow">
<div class="divTableCell">&nbsp;</div>
<div class="divTableCell">&nbsp;</div>
</div>
</div>
</div>

</body>
</html>