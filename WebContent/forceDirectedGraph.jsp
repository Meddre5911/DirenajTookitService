<!DOCTYPE html>
<meta charset="utf-8">
<style>
.node {
	stroke: #fff;
	stroke-width: 1.5px;
}

.link {
	stroke: #999;
	stroke-opacity: .6;
}
</style>
<body>
<!-- <form action="communitySentimentAnalysis" method="post"> -->
<!-- 	<div id = "second"> -->
<!-- 		Apply Community Sentiment Analysis For <input type="text" name="searchedCommunity" value="community1"><br> -->
<!-- 		 	 Classification Method For Sentiment Analysis : -->
<!-- 			 &nbsp;&nbsp;&nbsp;&nbsp; -->
<!-- 	 		 <input type="radio" name="classificationMethod" value="NaiveBayesUnigram" checked>NaiveBayesUnigram &nbsp; -->
<!-- 			 <input type="radio" name="classificationMethod" value="NaiveBayesBigram">NaiveBayesBigram &nbsp;  -->
<!-- 			 <input type="radio" name="classificationMethod" value="SVM">SVM<br> -->
<!-- 			 <input type="submit" value="Submit" name="submit"> -->
<!-- 	</div> -->
<!-- </form> -->

<div id = "first">
	<script src="js/d3.v3.min.js"></script>
	<script>
		var width = 960, height = 500;

		var color = d3.scale.category20();

		var force = d3.layout.force().charge(-120).linkDistance(30).size(
				[ width, height ]);

		var svg = d3.select(document.getElementById("first")).append("svg").attr("width", width).attr(
				"height", height);

		d3.json("http://localhost:8080/DirenajToolkitService/d3LibJsonUtilizer", function(error, graph) {
			force.nodes(graph.nodes).links(graph.links).start();

			var link = svg.selectAll(".link").data(graph.links).enter().append(
					"line").attr("class", "link").style("stroke-width",
					function(d) {
						return Math.sqrt(d.value);
					});

			var node = svg.selectAll(".node").data(graph.nodes).enter().append(
					"circle").attr("class", "node").attr("r", 5).style("fill",
					function(d) {
						return color(d.group);
					}).call(force.drag);

			node.append("title").text(function(d) {
				return d.name;
			});

			force.on("tick", function() {
				link.attr("x1", function(d) {
					return d.source.x;
				}).attr("y1", function(d) {
					return d.source.y;
				}).attr("x2", function(d) {
					return d.target.x;
				}).attr("y2", function(d) {
					return d.target.y;
				});

				node.attr("cx", function(d) {
					return d.x;
				}).attr("cy", function(d) {
					return d.y;
				});
			});
		});
	</script>
</div>
</body>
</html>