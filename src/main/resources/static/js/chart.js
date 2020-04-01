// ------------------------------------------------- Functions ---------------------------------------------------------

function getAllUrlParams() {
    var queryString = window.location.search.slice(1);
    var obj = {};
    if (queryString) {
        queryString = queryString.split("#")[0];
        var arr = queryString.split("&");

        for (var i = 0; i < arr.length; i++) {
            var a = arr[i].split("=");
            var paramName = a[0];
            var paramValue = typeof (a[1]) === "undefined" ? true : a[1];
            if (paramName.match(/\[(\d+)?\]$/)) {
                var key = paramName.replace(/\[(\d+)?\]/, "");
                if (!obj[key]) obj[key] = [];
                if (paramName.match(/\[\d+\]$/)) {
                    var index = /\[(\d+)\]/.exec(paramName)[1];
                    obj[key][index] = paramValue;
                } else {
                    obj[key].push(paramValue);
                }
            } else {
                if (!obj[paramName]) {
                    obj[paramName] = paramValue;
                } else if (obj[paramName] && typeof obj[paramName] === "string") {
                    obj[paramName] = [obj[paramName]];
                    obj[paramName].push(paramValue);
                } else {
                    obj[paramName].push(paramValue);
                }
            }
        }
    }
    return obj;
}

function encodeQueryData(data) {
   const ret = [];
   for (let d in data) ret.push(encodeURIComponent(d) + "=" + encodeURIComponent(data[d]));
   return ret.join("&");
}

function drawLineChart(label, categories, series, responseTime, chipId, width, height) {
    Highcharts.chart("container", {
        chart: {
            type: "line",
            width,
            height
        },
        title: {
            text: "Particulate matter data"
        },
        subtitle: {
            text: "Sensor-ID: " + chipId + " - (Response time: " + responseTime + " ms)"
        },
        xAxis: {
            categories,
            title: {
                text: "Time"
            }
        },
        yAxis: {
            title: {
                text: "PM values"
            }
        },
        tooltip: {
            formatter: function() {
                return "<strong>"+this.x+": </strong>"+ this.y;
            }
        },
        series: [{
            name: label,
            data: series
        }]
    });
}

// -------------------------------------------------- Main code --------------------------------------------------------

// Get url parameter
var params = getAllUrlParams();
var urlSuffix = encodeQueryData(params);
var chipId = params.chipId;
var width = params.width ? params.width : 800;
var height = params.height ? params.height : 500;

// Execute request for data
$.ajax({
    url: "data/chart?" + urlSuffix,
    success: (result) => {
        var field = JSON.parse(result).field;
        var time = JSON.parse(result).time;
        var values = JSON.parse(result).values;
        var responseTime = JSON.parse(result).responseTime;
        drawLineChart(field, time, values, responseTime, chipId, width, height);
    }
});