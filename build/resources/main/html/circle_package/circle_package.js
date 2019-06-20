var svg = d3.select("svg");
svg.selectAll('*').remove();

var margin = 20,
    width = +svg.attr('width'),
    height = +svg.attr('height'),
    diameter = width > height ? height : width,
    g = svg.append("g").attr("transform", "translate(" + diameter / 2 + "," + diameter / 2 + ")");

var color = d3.scaleLinear()
    .domain([-1, 5])
    .range(["hsl(152,80%,80%)", "hsl(228,30%,40%)"])
    .interpolate(d3.interpolateHcl);

var pack = d3.pack()
    .size([diameter - margin, diameter - margin])
    .padding(2);

d3.json("circle_package_test.json", function (error, root) {
    if (error) throw error;

    var jsonLinks = root.links;

    root = d3.hierarchy(root.root)
        .sum(function (d) {
            return d.size;
        })
        .sort(function (a, b) {
            return b.value - a.value;
        });

    var focus = root,
        nodes = pack(root).descendants(),
        view;

    var circle = g.selectAll("circle")
        .data(nodes)
        .enter().append("circle")
        .attr("class", function (d) {
            return d.parent ? d.children ? "node" : "node node--leaf" : "node node--root";
        })
        .attr("id", function (d) {
            return d.data.id;
        })
        .style("fill", function (d) {
            return d.children ? color(d.depth) : null;
        })
        .on("click", function (d) {
            if (focus !== d) zoom(d), d3.event.stopPropagation();
        });

    var text = g.selectAll("text")
        .data(nodes)
        .enter().append("text")
        .attr("class", "label")
        .style("fill-opacity", function (d) {
            return d.parent === root ? 1 : 0;
        })
        .style("display", function (d) {
            return d.parent === root ? "inline" : "none";
        })
        .text(function (d) {
            return d.data.name;
        });

    var node = g.selectAll("circle,text");

    function refreshLink(links) {
        function getTranslateX(translateText) {
            var start = translateText.indexOf("(");
            var comma = translateText.indexOf(",");
            return parseFloat(translateText.slice(start + 1, comma));
        }

        function getTranslateY(translateText) {
            var comma = translateText.indexOf(",");
            var end = translateText.indexOf(")");
            return parseFloat(translateText.slice(comma + 1, end));
        }

        function getCircleTransform(id) {
            return d3.select("#" + id.replace(/\./g, '\\.')).attr("transform");;
        }
        links.attr("x1", function (d) {
                var test = getCircleTransform(d.source_id);
                return getTranslateX(getCircleTransform(d.source_id)) + diameter / 2;
            })
            .attr("y1", function (d) {
                return getTranslateY(getCircleTransform(d.source_id)) + diameter / 2;
            })
            .attr("x2", function (d) {
                return getTranslateX(getCircleTransform(d.target_id)) + diameter / 2;
            })
            .attr("y2", function (d) {
                return getTranslateY(getCircleTransform(d.target_id)) + diameter / 2;
            });
    }

    var links = svg.append('g')
        .style('stroke', '#aaa')
        .selectAll('line')
        .data(jsonLinks)
        .enter().append('line');

    svg
        .style("background", color(-1))
        .on("click", function () {
            zoom(root);
        });

    zoomTo([root.x, root.y, root.r * 2 + margin]);

    function zoom(d) {
        var focus0 = focus;
        focus = d;

        var transition = d3.transition()
            .duration(d3.event.altKey ? 7500 : 750)
            .tween("zoom", function (d) {
                var i = d3.interpolateZoom(view, [focus.x, focus.y, focus.r * 2 + margin]);
                return function (t) {
                    zoomTo(i(t));
                };
            });

        transition.selectAll("text")
            .filter(function (d) {
                return d.parent === focus || this.style.display === "inline";
            })
            .style("fill-opacity", function (d) {
                return d.parent === focus ? 1 : 0;
            })
            .on("start", function (d) {
                if (d.parent === focus) this.style.display = "inline";
            })
            .on("end", function (d) {
                if (d.parent !== focus) this.style.display = "none";
            });
    }

    function zoomTo(v) {
        var k = diameter / v[2];
        view = v;
        node.attr("transform", function (d) {
            return "translate(" + (d.x - v[0]) * k + "," + (d.y - v[1]) * k + ")";
        });
        circle.attr("r", function (d) {
            return d.r * k;
        });
        refreshLink(links);
    }
});