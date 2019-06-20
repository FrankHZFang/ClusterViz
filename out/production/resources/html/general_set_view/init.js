function resizeWidth(w) {
  d3.select("svg").attr('width', w);
}

function resizeHeight(h) {
  d3.select("svg").attr('height', h);
}

var simulation;

function loadData(fileName) {
  var svg = d3.select("svg");
  svg.selectAll('*').remove();

  var w = svg.attr('width'),
    h = svg.attr('height');

  var color = d3.schemeCategory20;

  var set_radius = 2;
  var node_small_radius = 5;
  var node_large_radius = 10;
  var node_non_dependency_fill = "none"
  var node_dependency_fill = "black"

  var forceStrength = 0.2;

  var forceCenter = d3.forceRadial(h / 2, w / 2, h / 2);

  function isolate(force, n) {
    var initialize = force.initialize;
    force.initialize = function () {
      initialize.call(force, n);
    };
    return force;
  }

  var forceX = d3.forceX().x(function (d) {
      if (d.hasCenter)
        return d.forceX
      else return d.x;
    })
    .strength(forceStrength);

  var forceY = d3.forceY().y(function (d) {
      if (d.hasCenter)
        return d.forceY
      else return d.y;
    })
    .strength(forceStrength);

  var forceLink = d3.forceLink().id(function (d) {
    return d.id;
  });

  d3.json(fileName, function (error, graph) {
    if (error) throw error;

    if (simulation != null) {
      simulation.stop();
    }

    graph.sets.forEach(function (d) {
      d.r = set_radius;
      d.x = w / 2;
      d.y = h / 2;
      d.hasCenter = false;
    })

    hull_g = svg.append("g")
      .attr("class", "hull_g");

    link_g = svg.append("g")
      .attr("class", "link_g");

    set_g = svg.append("g")
      .attr("class", "set_g");

    node_g = svg.append("g")
      .attr("class", "node_g");

    set_g.selectAll("circle")
      .data(graph.sets)
      .enter()
      .append("circle")
      .attr("class", "set_center")
      .attr("id", function (d) {
        return d.id;
      })
      .attr("r", function (d) {
        return d.r;
      })
      .attr("cx", function (d) {
        return d.x;
      })
      .attr("cy", function (d) {
        return d.y;
      })
      .call(d3.drag()
        .on("start", dragstarted)
        .on("drag", dragged)
        .on("end", dragended));

    function calcForceX(d) {
      xSum = 0;
      d.set.forEach(function (s) {
        xSum += set_g.select("#" + s).datum().x;
      });

      d.forceX = xSum / d.set.length;
    }

    function calcForceY(d) {
      ySum = 0;
      d.set.forEach(function (s) {
        ySum += set_g.select("#" + s).datum().y;
      });
      d.forceY = ySum / d.set.length;
    }

    graph.nodes.forEach(function (d) {
      d.r = d.set.length > 1 ? node_large_radius : node_small_radius;
      d.x = w / 2;
      d.y = h / 2;
      d.hasCenter = true;
      calcForceX(d);
      calcForceY(d);
    })

    node_g.selectAll("circle")
      .data(graph.nodes)
      .enter()
      .append("circle")
      .attr("r", function (d) {
        return d.r;
      })
      .attr("cx", function (d) {
        return d.x;
      })
      .attr("cy", function (d) {
        return d.y;
      })
      .attr("stroke", "black")
      .attr("fill", node_non_dependency_fill)
      .call(d3.drag()
        .on("start", dragstarted)
        .on("drag", dragged)
        .on("end", dragended));

    graph.links.forEach(function (d) {
      d.source = graph.nodes.find(function (e) {
        return d.source_id == e.id
      });
      d.target = graph.nodes.find(function (e) {
        return d.target_id == e.id
      });
      node_g.select('#' + d.source_id).attr("fill", node_dependency_fill);
      node_g.select('#' + d.target_id).attr("fill", node_dependency_fill);
    });

    links = link_g
      .selectAll("line")
      .data(graph.links)
      .enter()
      .append("line")
      .attr("class", "link");

    setNodes = [];
    graph.sets.forEach(function (d) {
      setNodes.push(graph.nodes.filter(function (e) {
        return e.set.includes(d.id);
      }));
    });

    hulls = hull_g.selectAll("path")
      .data(setNodes)
      .enter()
      .append("path")
      .attr("class", "hull");

    setCenters = set_g.selectAll("circle");
    nodes = node_g.selectAll("circle");

    function ticked() {
      graph.nodes.forEach(function (d) {
        calcForceX(d);
        calcForceY(d);
      });

      hulls.attr("d", function (d) {
          switch (d.length) {
            case 0:
              return "";
            case 1:
              return "M" + d.map(function (d) {
                return (d.x - 0.01) + "," + d.y + "L" + (d.x + 0.01) + "," + d.y +
                  "Z";
              });
            case 2:
              return "M" + d.map(function (d) {
                return [d.x + "," + d.y];
              }).join("L") + "Z";
            default:
              return "M" + d3.polygonHull(d.map(function (d) {
                return [d.x, d.y];
              })).join("L") + "Z";
          }
        })
        .style("fill", function (d, i) {
          return color[i % 10];
        })
        .style("stroke", function (d, i) {
          return color[i % 10];
        });

      links
        .attr("x1", function (d) {
          return d.source.x;
        })
        .attr("y1", function (d) {
          return d.source.y;
        })
        .attr("x2", function (d) {
          return d.target.x;
        })
        .attr("y2", function (d) {
          return d.target.y;
        });

      setCenters.attr("cx", function (d) {
          return d.x;
        })
        .attr("cy", function (d) {
          return d.y;
        });

      nodes.attr("cx", function (d) {
          return d.x;
        })
        .attr("cy", function (d) {
          return d.y;
        })
        .attr("forceX", function (d) {
          return d.forceX
        });
      simulation.force("x", isolate(forceX, graph.nodes))
        .force("y", isolate(forceY, graph.nodes));
    }

    simulation = d3.forceSimulation(graph.sets.concat(graph.nodes))
      .force("charge", d3.forceManyBody())
      .force("center", isolate(forceCenter, graph.sets))
      .force("x", isolate(forceX, graph.nodes))
      .force("y", isolate(forceY, graph.nodes))
      //.force("link", forceLink.links(graph.links))
      .on("tick", ticked);

    function dragstarted(d, i) {
      //console.log("dragstarted " + i)
      if (!d3.event.active) {
        simulation.alpha(1).restart();
      }
      d.fx = d.x;
      d.fy = d.y;
    }

    function dragged(d, i) {
      //console.log("dragged " + i)
      d.fx = d3.event.x;
      d.fy = d3.event.y;
    }

    function dragended(d, i) {
      //console.log("dragended " + i)
      if (!d3.event.active) {
        simulation.alphaTarget(0);
      }
      d.fx = null;
      d.fy = null;
    }
  });
}