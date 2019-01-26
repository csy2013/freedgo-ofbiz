"use strict";

function _classCallCheck(e, t) {
  if (!(e instanceof t)) throw new TypeError("Cannot call a class as a function")
}

var _createClass = function () {
  function e(e, t) {
    for (var n = 0; n < t.length; n++) {
      var a = t[n];
      a.enumerable = a.enumerable || !1, a.configurable = !0, "value" in a && (a.writable = !0), Object.defineProperty(e, a.key, a)
    }
  }

  return function (t, n, a) {
    return n && e(t.prototype, n), a && e(t, a), t
  }
}();
!function (e) {
  function t(e, t) {
    if (null == e) throw new TypeError("Cannot convert undefined or null to object");
    for (var n = Object(e), a = 1; a < arguments.length; a++) {
      var r = arguments[a];
      if (null != r) for (var o in r) Object.prototype.hasOwnProperty.call(r, o) && (n[o] = r[o])
    }
    return n
  }

  function n(e) {
    return !isNaN(parseFloat(e)) && isFinite(e)
  }

  function a() {
    var t = e.performance || e.webkitPerformance || e.msPerformance || e.mozPerformance;
    return void 0 !== t
  }

  var r = ["navigationStart", "unloadEventStart", "unloadEventEnd", "redirectStart", "redirectEnd", "fetchStart", "domainLookupStart", "domainLookupEnd", "connectStart", "secureConnectionStart", "connectEnd", "requestStart", "responseStart", "responseEnd", "domInteractive", "domLoading", "domContentLoadedEventStart", "domContentLoadedEventEnd", "domComplete", "loadEventStart", "loadEventEnd"],
    o = ["unloadEventTime", "redirectTime", "readyStart", "appcacheTime", "lookupDomainTime", "connectTime", "requestTime", "initDomTreeTime", "domReadyTime", "firstPaintTime", "loadEventTime", "loadTime"],
    i = function () {
      function n(a) {
        _classCallCheck(this, n);
        var r = this;
        this.options = t(this.defaultOptions, a || {}), this.performance = new s, this.performanceResource = new c, e.addEventListener("load", function () {
          r.ifSend() && setTimeout(function () {
            r.sendTimingData()
          }, 1e3)
        })
      }

      return _createClass(n, [{
        key: "defaultOptions", get: function () {
          return {rate: .1, baseUrl: "https://probe.youzan.com/p/r?biz="}
        }
      }]), _createClass(n, [{
        key: "ifSend", value: function () {
          return Math.random() < this.options.rate
        }
      }, {
        key: "getExtraData", value: function () {
          return {domain: encodeURIComponent(document.domain), pathname: e.location.pathname, appid: this.options.appid, timestamp: Date.now()}
        }
      }, {
        key: "postData", value: function (e, t) {
          var n = new XMLHttpRequest;
          n.open("POST", e, !0), n.withCredentials = !0, n.send(JSON.stringify(t))
        }
      }, {
        key: "sendTimingData", value: function () {
          var e = this.performance.getTimes(), n = this.getExtraData(), a = [t(e, n)], r = this.options.baseUrl + "performance";
          this.postData(r, a)
        }
      }, {
        key: "sendResourceTimingData", value: function () {
          for (var e = this.performanceResource.getTimes(), n = this.getExtraData(), a = [], r = 0; r < e.length; r++) a.push(t(e[r], n));
          var o = this.options.baseUrl + "performance_resource";
          this.postData(o, a)
        }
      }]), n
    }(), s = function () {
      function t(e) {
        _classCallCheck(this, t)
      }

      return _createClass(t, [{
        key: "getTimes", value: function () {
          if (!a()) return {};
          var t = e.performance.timing, r = {};
          if (t) {
            for (var o in t) n(t[o]) && (0 !== t[o] ? r[o] = parseFloat(t[o] - t.navigationStart) : r[o] = parseFloat(t[o]));
            if (void 0 === r.firstPaint) {
              var i = 0;
              e.chrome && e.chrome.loadTimes ? (i = 1e3 * e.chrome.loadTimes().firstPaintTime, r.firstPaintTime = i - t.navigationStart) : "number" == typeof t.msFirstPaint && (i = t.msFirstPaint, r.firstPaintTime = i - t.navigationStart)
            }
            r.loadTime = t.loadEventEnd - t.fetchStart, r.domReadyTime = t.domComplete - t.domInteractive, r.readyStart = t.fetchStart - t.navigationStart, r.redirectTime = t.redirectEnd - t.redirectStart, r.appcacheTime = t.domainLookupStart - t.fetchStart, r.unloadEventTime = t.unloadEventEnd - t.unloadEventStart, r.lookupDomainTime = t.domainLookupEnd - t.domainLookupStart, r.connectTime = t.connectEnd - t.connectStart, r.requestTime = t.responseEnd - t.requestStart, r.initDomTreeTime = t.domInteractive - t.responseEnd, r.loadEventTime = t.loadEventEnd - t.loadEventStart
          }
          return r
        }
      }, {
        key: "printTable", value: function (e) {
          var t = this.getTimes(), n = [], a = [];
          console.log("原始性能数据：");
          for (var i = 0; i < r.length; i++) n.push({name: r[i], ms: t[r[i]]});
          console.table(n), console.log("\n计算性能数据：");
          for (var s = 0; s < o.length; s++) a.push({name: o[s], ms: t[o[s]]});
          console.table(a)
        }
      }]), t
    }(), c = function () {
      function t(e) {
        _classCallCheck(this, t)
      }

      return _createClass(t, [{
        key: "getTimes", value: function () {
          if (!a()) return [];
          for (var t = e.performance.getEntries(), n = 0; n < t.length; n++) t[n] = t[n].toJSON();
          return t
        }
      }]), t
    }();
  e.APM = i
}("undefined" != typeof window ? window : {});