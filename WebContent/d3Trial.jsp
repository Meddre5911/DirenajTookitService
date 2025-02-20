<!DOCTYPE html>
<meta charset="utf-8">
<style>
circle {
	fill: rgb(31, 119, 180);
	fill-opacity: .25;
	stroke: rgb(31, 119, 180);
	stroke-width: 1px;
}

.leaf circle {
	fill: #ff7f0e;
	fill-opacity: 1;
}

text {
	font: 10px sans-serif;
}
</style>
<body>
	<script src="http://d3js.org/d3.v3.min.js"></script>
	<script>
		var diameter = 960, format = d3.format(",d");

		var pack = d3.layout.pack().size([ diameter - 4, diameter - 4 ]).value(
				function(d) {
					return d.size;
				});

		var svg = d3.select("body").append("svg").attr("width", diameter).attr(
				"height", diameter).append("g").attr("transform",
				"translate(2,2)");

		d3.json("http://localhost:8080/DirenajToolkitService/d3LibJsonUtilizer", function(error, root) {
			var node = svg.datum(root).selectAll(".node").data(pack.nodes)
					.enter().append("g").attr("class", function(d) {
						return d.children ? "node" : "leaf node";
					}).attr("transform", function(d) {
						return "translate(" + d.x + "," + d.y + ")";
					});

			node.append("title").text(function(d) {
				return d.name + (d.children ? "" : ": " + format(d.size));
			});

			node.append("circle").attr("r", function(d) {
				return d.r;
			});

			node.filter(function(d) {
				return !d.children;
			}).append("text").attr("dy", ".3em").style("text-anchor", "middle")
					.text(function(d) {
						return d.name.substring(0, d.r / 3);
					});
		});

		d3.select(self.frameElement).style("height", diameter + "px");
	</script>
</body>
</html>