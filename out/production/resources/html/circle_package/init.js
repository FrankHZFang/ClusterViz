var lastInput = null

function resizeWidth(w) {
  d3.select("svg").attr('width', w);
}

function resizeHeight(h) {
  d3.select("svg").attr('height', h);
}

function loadData(fileName) {
  var typeColors = d3.schemeCategory10;
  var linkTypeMap = new Map();
  var typeCount = 0;

  var svg = d3.select("svg");

  var depColorG = svg.select('#dep-type-colors');
  depColorG.selectAll('*').remove();

  var margin = 0,
    width = +svg.attr('width'),
    height = +svg.attr('height'),
    diameter = width > height ? height : width,
    g = svg.select("#graph");

  g.selectAll('*').remove();
  g.attr("transform", "translate(" + diameter / 2 + "," + diameter / 2 + ")");

  var color = d3.scaleLinear()
    .domain([-1, 5])
    .range(["hsl(152,80%,80%)", "hsl(228,30%,40%)"])
    .interpolate(d3.interpolateHcl);

  var pack = d3.pack()
    .size([diameter - margin, diameter - margin])
    .padding(2);

  d3.json(fileName, function (error, root) {
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

    var linkG = g.append('g');
    var links = linkG
      .selectAll('line')
      .data(jsonLinks)
      .enter().append('line');

    function refreshLink() {
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
          return getTranslateX(getCircleTransform(d.source_id));
        })
        .attr("y1", function (d) {
          return getTranslateY(getCircleTransform(d.source_id));
        })
        .attr("x2", function (d) {
          return getTranslateX(getCircleTransform(d.target_id));
        })
        .attr("y2", function (d) {
          return getTranslateY(getCircleTransform(d.target_id));
        })
        .style('stroke', function (d) {
          if (linkTypeMap.get(d.type) == undefined) {
            addTypeColor(d.type, typeColors[typeCount % 10])
          }
          return linkTypeMap.get(d.type);
        });
    }

    svg
      .style("background", color(-1))
      .on("click", function () {
        zoom(root);
      });

    zoomTo([root.x, root.y, root.r * 2 + margin]);

    function addTypeColor(type, color) {
      linkTypeMap.set(type, color);
      depColorG.append("line")
        .attr('x1', 0)
        .attr('x2', 24)
        .attr('y1', 10 * typeCount)
        .attr('y2', 10 * typeCount)
        .style('stroke', color);
      depColorG.append("text")
        .attr("class", "type")
        .text(type)
        .attr('x', 28)
        .attr('y', 10 * typeCount + 4);
      typeCount++;
    }

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
}