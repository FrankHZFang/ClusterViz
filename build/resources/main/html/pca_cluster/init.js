function resizeWidth(w) {
  d3.select("svg").attr('width', w);
}

function resizeHeight(h) {
  d3.select("svg").attr('height', h);
}

function loadData(fileName) {
  var svg = d3.select("svg");

  var circleG = svg.select('#circles');
  var labelG = svg.select('#labels');

  var zoom = d3.zoom()
    .on('zoom', function () {
      circleG.attr('transform', d3.event.transform);
      labelG.attr('transform', d3.event.transform);
    })
    .filter(function () {
      return event.type != 'dblclick';
    });

  d3.select('#zoom-in').on('click', function () {
    zoom.scaleBy(svg.transition().duration(200), 1.2);
  });

  d3.select('#zoom-out').on('click', function () {
    zoom.scaleBy(svg.transition().duration(200), 1 / 1.2);
  });

  svg.call(zoom);

  var margin = 20,
    width = +svg.attr('width'),
    height = +svg.attr('height'),
    diameter = width > height ? height : width

  var xyScale = d3.scaleLinear().domain([-12, 12]).range([0, diameter])
  var rScale = d3.scaleLinear().domain([0, 24]).range([0, diameter])

  d3.json(fileName, function (error, root) {
    if (error) throw error;

    var nodes = root;

    var circle = circleG.selectAll("circle")
      .data(nodes)
      .enter().append("circle")
      .attr("class", "circle")
      .attr("id", function (d) {
        return d.id;
      })
      .attr("cx", function (d) {
        return xyScale(d.x);
      })
      .attr("cy", function (d) {
        return xyScale(d.y);
      })
      .attr("r", function (d) {
        return rScale(d.r);
      })
      .attr("stroke", "black")
      .attr("stroke-width", "2")
      .attr("fill", "white")
      .attr("fill-opacity", "0");

    var label = labelG.append('g')
      .attr('class', 'labels')
      .selectAll('text')
      .data(nodes)
      .enter().append('text')
      .attr('class', 'label')
      .attr("x", function (d) {
        return xyScale(d.x);
      })
      .attr("y", function (d) {
        return xyScale(d.y);
      })
      .on("click", function () {
        alert(d3.select(this).text())
        pcaClick.clicked(d3.select(this).text())
      })
      .text(function (d) {
        return d.name
      });
  });
}