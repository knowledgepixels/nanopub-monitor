(function () {
  "use strict";

  var SVG_NS = "http://www.w3.org/2000/svg";
  var MAP_WIDTH = 1000;
  var MAP_HEIGHT = 400;
  var MARKER_SCREEN_RADIUS = 4;
  var MAX_ZOOM = 8;
  var ZOOM_STEP = 1.6;
  var WHEEL_ZOOM_SENSITIVITY = 0.01;
  var MAX_WHEEL_DELTA = 40;
  var DRAG_THRESHOLD_PX = 4;
  var ZOOM_HINT_MS = 1500;
  var REFRESH_INTERVAL_MS = 10000;
  var POPOVER_HIDE_DELAY_MS = 250;
  var ROW_FLASH_MS = 2000;

  var mapElement;
  var svgElement;
  var markerLayer;
  var zoomInButton;
  var zoomOutButton;
  var zoomResetButton;
  var zoomHint;
  var zoomHintTimer = null;
  var view = {x: 0, y: 0, width: MAP_WIDTH, height: MAP_HEIGHT};
  var activePointers = {};
  var gesture = null;
  var suppressNextClick = false;
  var popover;
  var pointsByLocation = {};
  var openLocation = null;
  var popoverPinned = false;
  var hideTimer = null;

  function projectToMap(lat, lon) {
    return {x: lon * 2.6938 + 465.4, y: lat * -2.6938 + 227.066};
  }

  function createSvgElement(name, attributes) {
    var element = document.createElementNS(SVG_NS, name);
    for (var key in attributes) {
      element.setAttribute(key, attributes[key]);
    }
    return element;
  }

  function createElement(name, className, text) {
    var element = document.createElement(name);
    if (className) {
      element.className = className;
    }
    if (text !== undefined) {
      element.textContent = text;
    }
    return element;
  }

  function drawWorld() {
    svgElement = createSvgElement("svg", {
      viewBox: "0 0 " + MAP_WIDTH + " " + MAP_HEIGHT,
      role: "img",
      "aria-label": "World map of nanopub service locations"
    });
    svgElement.appendChild(createSvgElement("rect", {class: "map-sea", x: 0, y: 0, width: MAP_WIDTH, height: MAP_HEIGHT}));
    var land = createSvgElement("g", {class: "map-land"});
    for (var country in worldmap.shapes) {
      land.appendChild(createSvgElement("path", {d: worldmap.shapes[country]}));
    }
    svgElement.appendChild(land);
    markerLayer = createSvgElement("g", {class: "map-markers"});
    svgElement.appendChild(markerLayer);
    mapElement.appendChild(svgElement);
  }

  function locationKey(point) {
    return point.lat + "," + point.lon;
  }

  function shortUrl(url) {
    return url.replace(/^https?:\/\//, "").replace(/\/$/, "");
  }

  function groupByLocation(points) {
    var groups = {};
    points.forEach(function (point) {
      var key = locationKey(point);
      (groups[key] = groups[key] || []).push(point);
    });
    return groups;
  }

  function drawMarkers(points) {
    pointsByLocation = groupByLocation(points);
    while (markerLayer.firstChild) {
      markerLayer.removeChild(markerLayer.firstChild);
    }
    points.forEach(function (point) {
      markerLayer.appendChild(createMarker(point));
    });
    refreshOpenPopover();
  }

  function createMarker(point) {
    var position = projectToMap(point.lat, point.lon);
    var marker = createSvgElement("circle", {
      class: "map-marker",
      cx: position.x + point.offsetX,
      cy: position.y + point.offsetY,
      fill: point.color,
      tabindex: 0,
      role: "button",
      "aria-label": point.typeLabel + " " + shortUrl(point.url) + ", " + point.status
    });
    marker.dataset.service = point.url;
    marker.dataset.location = locationKey(point);
    return marker;
  }

  function markersFor(serviceUrl) {
    return Array.prototype.filter.call(markerLayer.querySelectorAll(".map-marker"), function (marker) {
      return marker.dataset.service === serviceUrl;
    });
  }

  function rowFor(serviceUrl) {
    return Array.prototype.find.call(document.querySelectorAll("tr[data-service]"), function (row) {
      return row.dataset.service === serviceUrl;
    });
  }

  function setMarkerHighlight(serviceUrl, highlighted) {
    markersFor(serviceUrl).forEach(function (marker) {
      marker.classList.toggle("highlighted", highlighted);
      if (highlighted) {
        markerLayer.appendChild(marker);
      }
    });
  }

  function setRowHighlight(serviceUrl, highlighted) {
    var row = rowFor(serviceUrl);
    if (row) {
      row.classList.toggle("row-highlight", highlighted);
    }
  }

  function prefersReducedMotion() {
    return window.matchMedia && window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  }

  function scrollToRow(serviceUrl) {
    var row = rowFor(serviceUrl);
    if (!row) {
      return;
    }
    row.scrollIntoView({behavior: prefersReducedMotion() ? "auto" : "smooth", block: "center"});
    row.classList.add("row-flash");
    setTimeout(function () {
      row.classList.remove("row-flash");
    }, ROW_FLASH_MS);
  }

  function statusClass(status) {
    return status === "OK" ? "status-ok" : "status-problem";
  }

  function buildPopoverEntry(point) {
    var entry = createElement("li");
    var button = createElement("button", "map-popover-entry");
    button.type = "button";
    var swatch = createElement("span", "map-popover-swatch");
    swatch.style.background = point.color;
    button.appendChild(swatch);
    var details = createElement("span", "map-popover-details");
    var title = createElement("span", "map-popover-url", shortUrl(point.url));
    if (point.testInstance) {
      title.appendChild(createElement("span", "test-chip", "TEST"));
    }
    details.appendChild(title);
    var meta = createElement("span", "map-popover-meta", point.typeLabel + (point.version ? " · v" + point.version : "") + " · ");
    meta.appendChild(createElement("span", statusClass(point.status), point.status));
    details.appendChild(meta);
    button.appendChild(details);
    button.addEventListener("click", function () {
      hidePopover();
      scrollToRow(point.url);
    });
    button.addEventListener("mouseenter", function () {
      setRowHighlight(point.url, true);
    });
    button.addEventListener("mouseleave", function () {
      setRowHighlight(point.url, false);
    });
    entry.appendChild(button);
    return entry;
  }

  function fillPopover(points) {
    popover.textContent = "";
    var first = points[0];
    var heading = createElement("div", "map-popover-heading", first.city + ", " + first.country);
    heading.appendChild(createElement("span", "map-popover-region", first.region));
    popover.appendChild(heading);
    var list = createElement("ul", "map-popover-list");
    points.forEach(function (point) {
      list.appendChild(buildPopoverEntry(point));
    });
    popover.appendChild(list);
  }

  function positionPopover(location) {
    var anchor = markerLayer.querySelector('.map-marker[data-location="' + location + '"]');
    if (!anchor) {
      return;
    }
    var mapBox = mapElement.getBoundingClientRect();
    var markerBox = anchor.getBoundingClientRect();
    if (!isInside(markerBox, svgElement.getBoundingClientRect())) {
      hidePopover();
      return;
    }
    var centerX = markerBox.left + markerBox.width / 2 - mapBox.left;
    var markerTop = markerBox.top - mapBox.top;
    var markerBottom = markerBox.bottom - mapBox.top;
    var left = Math.max(4, Math.min(centerX - popover.offsetWidth / 2, mapBox.width - popover.offsetWidth - 4));
    var opensAbove = markerTop - popover.offsetHeight - 10 >= 0;
    popover.style.left = left + "px";
    popover.style.top = (opensAbove ? markerTop - popover.offsetHeight - 10 : markerBottom + 10) + "px";
  }

  function isInside(innerBox, outerBox) {
    var centerX = innerBox.left + innerBox.width / 2;
    var centerY = innerBox.top + innerBox.height / 2;
    return centerX >= outerBox.left && centerX <= outerBox.right && centerY >= outerBox.top && centerY <= outerBox.bottom;
  }

  function showPopover(location) {
    var points = pointsByLocation[location];
    if (!points) {
      hidePopover();
      return;
    }
    cancelHide();
    openLocation = location;
    fillPopover(points);
    popover.hidden = false;
    positionPopover(location);
  }

  function hidePopover() {
    cancelHide();
    openLocation = null;
    popoverPinned = false;
    popover.hidden = true;
  }

  function scheduleHide() {
    if (popoverPinned) {
      return;
    }
    cancelHide();
    hideTimer = setTimeout(hidePopover, POPOVER_HIDE_DELAY_MS);
  }

  function cancelHide() {
    if (hideTimer) {
      clearTimeout(hideTimer);
      hideTimer = null;
    }
  }

  function refreshOpenPopover() {
    if (openLocation === null) {
      return;
    }
    if (pointsByLocation[openLocation]) {
      var wasPinned = popoverPinned;
      showPopover(openLocation);
      popoverPinned = wasPinned;
    } else {
      hidePopover();
    }
  }

  function pinPopover(location) {
    if (popoverPinned && openLocation === location) {
      hidePopover();
      return;
    }
    showPopover(location);
    popoverPinned = true;
  }

  function markerFromEvent(event) {
    var target = event.target;
    return target instanceof Element && target.classList.contains("map-marker") ? target : null;
  }

  function bindMarkerEvents() {
    markerLayer.addEventListener("mouseover", function (event) {
      var marker = markerFromEvent(event);
      if (marker && !popoverPinned) {
        showPopover(marker.dataset.location);
      }
      if (marker) {
        setRowHighlight(marker.dataset.service, true);
      }
    });
    markerLayer.addEventListener("mouseout", function (event) {
      var marker = markerFromEvent(event);
      if (marker) {
        setRowHighlight(marker.dataset.service, false);
        scheduleHide();
      }
    });
    markerLayer.addEventListener("click", function (event) {
      var marker = markerFromEvent(event);
      if (marker) {
        event.stopPropagation();
        if (!consumeSuppressedClick()) {
          pinPopover(marker.dataset.location);
        }
      }
    });
    markerLayer.addEventListener("keydown", function (event) {
      var marker = markerFromEvent(event);
      if (marker && (event.key === "Enter" || event.key === " ")) {
        event.preventDefault();
        pinPopover(marker.dataset.location);
        var firstEntry = popover.querySelector(".map-popover-entry");
        if (firstEntry) {
          firstEntry.focus();
        }
      }
    });
    markerLayer.addEventListener("focusin", function (event) {
      var marker = markerFromEvent(event);
      if (marker && !popoverPinned) {
        showPopover(marker.dataset.location);
      }
    });
    markerLayer.addEventListener("focusout", function (event) {
      if (markerFromEvent(event) && !popover.contains(event.relatedTarget)) {
        scheduleHide();
      }
    });
  }

  function bindPopoverEvents() {
    popover.addEventListener("mouseenter", cancelHide);
    popover.addEventListener("mouseleave", scheduleHide);
    popover.addEventListener("click", function (event) {
      event.stopPropagation();
    });
    document.addEventListener("click", function () {
      if (!consumeSuppressedClick() && !popover.hidden) {
        hidePopover();
      }
    });
    document.addEventListener("keydown", function (event) {
      if (event.key === "Escape" && !popover.hidden) {
        var location = openLocation;
        hidePopover();
        var marker = markerLayer.querySelector('.map-marker[data-location="' + location + '"]');
        if (marker) {
          marker.focus();
        }
      }
    });
    window.addEventListener("resize", function () {
      if (openLocation !== null) {
        positionPopover(openLocation);
      }
    });
  }

  function bindTableEvents() {
    document.addEventListener("mouseover", function (event) {
      var row = event.target instanceof Element ? event.target.closest("tr[data-service]") : null;
      if (row && !row.contains(event.relatedTarget)) {
        setMarkerHighlight(row.dataset.service, true);
      }
    });
    document.addEventListener("mouseout", function (event) {
      var row = event.target instanceof Element ? event.target.closest("tr[data-service]") : null;
      if (row && !row.contains(event.relatedTarget)) {
        setMarkerHighlight(row.dataset.service, false);
      }
    });
  }

  function loadPoints() {
    return fetch(".map.json", {cache: "no-store"})
      .then(function (response) {
        if (!response.ok) {
          throw new Error("Map data request failed with HTTP " + response.status);
        }
        return response.json();
      })
      .then(function (data) {
        drawMarkers(data.points || []);
      })
      .catch(function (error) {
        console.warn("Could not refresh the server map:", error);
      });
  }

  function startPolling() {
    setInterval(function () {
      if (!document.hidden) {
        loadPoints();
      }
    }, REFRESH_INTERVAL_MS);
    document.addEventListener("visibilitychange", function () {
      if (!document.hidden) {
        loadPoints();
      }
    });
  }

  function scaleMarkersToScreen() {
    var screenPixelsPerMapUnit = svgElement.getBoundingClientRect().width / view.width;
    if (screenPixelsPerMapUnit > 0) {
      mapElement.style.setProperty("--map-marker-radius", MARKER_SCREEN_RADIUS / screenPixelsPerMapUnit + "px");
    }
  }

  function clamp(value, min, max) {
    return Math.min(Math.max(value, min), max);
  }

  function currentZoom() {
    return MAP_WIDTH / view.width;
  }

  function applyView() {
    svgElement.setAttribute("viewBox", [view.x, view.y, view.width, view.height].join(" "));
    mapElement.classList.toggle("zoomed", currentZoom() > 1);
    zoomInButton.disabled = currentZoom() >= MAX_ZOOM;
    zoomOutButton.disabled = currentZoom() <= 1;
    zoomResetButton.disabled = currentZoom() <= 1;
    scaleMarkersToScreen();
    if (openLocation !== null) {
      positionPopover(openLocation);
    }
  }

  function setView(x, y, width) {
    var clampedWidth = clamp(width, MAP_WIDTH / MAX_ZOOM, MAP_WIDTH);
    var clampedHeight = clampedWidth * MAP_HEIGHT / MAP_WIDTH;
    view = {
      x: clamp(x, 0, MAP_WIDTH - clampedWidth),
      y: clamp(y, 0, MAP_HEIGHT - clampedHeight),
      width: clampedWidth,
      height: clampedHeight
    };
    applyView();
  }

  function clientToMap(clientX, clientY) {
    var box = svgElement.getBoundingClientRect();
    return {
      x: view.x + (clientX - box.left) / box.width * view.width,
      y: view.y + (clientY - box.top) / box.height * view.height
    };
  }

  function zoomAround(factor, mapX, mapY) {
    var newWidth = clamp(view.width / factor, MAP_WIDTH / MAX_ZOOM, MAP_WIDTH);
    var ratio = newWidth / view.width;
    setView(mapX - (mapX - view.x) * ratio, mapY - (mapY - view.y) * ratio, newWidth);
  }

  function zoomAroundCenter(factor) {
    zoomAround(factor, view.x + view.width / 2, view.y + view.height / 2);
  }

  function resetZoom() {
    setView(0, 0, MAP_WIDTH);
  }

  function createZoomButton(label, ariaLabel, onClick) {
    var button = createElement("button", "map-zoom-button", label);
    button.type = "button";
    button.setAttribute("aria-label", ariaLabel);
    button.title = ariaLabel;
    button.addEventListener("click", function (event) {
      event.stopPropagation();
      onClick();
    });
    return button;
  }

  function buildZoomControls() {
    var controls = createElement("div", "map-zoom-controls");
    zoomInButton = createZoomButton("+", "Zoom in", function () {
      zoomAroundCenter(ZOOM_STEP);
    });
    zoomOutButton = createZoomButton("\u2212", "Zoom out", function () {
      zoomAroundCenter(1 / ZOOM_STEP);
    });
    zoomResetButton = createZoomButton("Reset", "Show the whole world", resetZoom);
    zoomResetButton.classList.add("map-zoom-reset");
    controls.appendChild(zoomInButton);
    controls.appendChild(zoomOutButton);
    controls.appendChild(zoomResetButton);
    mapElement.appendChild(controls);
    zoomHint = createElement("div", "map-zoom-hint", "Use Ctrl or \u2318 + scroll to zoom the map");
    zoomHint.hidden = true;
    mapElement.appendChild(zoomHint);
  }

  function showZoomHint() {
    zoomHint.hidden = false;
    clearTimeout(zoomHintTimer);
    zoomHintTimer = setTimeout(function () {
      zoomHint.hidden = true;
    }, ZOOM_HINT_MS);
  }

  function onWheel(event) {
    if (!event.ctrlKey && !event.metaKey) {
      showZoomHint();
      return;
    }
    event.preventDefault();
    var point = clientToMap(event.clientX, event.clientY);
    var limitedDelta = clamp(event.deltaY, -MAX_WHEEL_DELTA, MAX_WHEEL_DELTA);
    zoomAround(Math.exp(-limitedDelta * WHEEL_ZOOM_SENSITIVITY), point.x, point.y);
  }

  function onDoubleClick(event) {
    if (markerFromEvent(event)) {
      return;
    }
    var point = clientToMap(event.clientX, event.clientY);
    zoomAround(event.shiftKey ? 1 / ZOOM_STEP : ZOOM_STEP, point.x, point.y);
  }

  function pointerList() {
    return Object.keys(activePointers).map(function (id) {
      return activePointers[id];
    });
  }

  function pinchDistance(pointers) {
    return Math.hypot(pointers[0].clientX - pointers[1].clientX, pointers[0].clientY - pointers[1].clientY);
  }

  function startGesture() {
    var pointers = pointerList();
    if (pointers.length === 1) {
      gesture = {type: "pan", startX: pointers[0].clientX, startY: pointers[0].clientY, startView: view, moved: false};
    } else if (pointers.length === 2) {
      gesture = {type: "pinch", distance: pinchDistance(pointers), moved: true};
    } else {
      gesture = null;
    }
  }

  function onPointerDown(event) {
    if (event.button !== 0) {
      return;
    }
    activePointers[event.pointerId] = {clientX: event.clientX, clientY: event.clientY};
    startGesture();
  }

  function continuePan(pointer) {
    var dx = pointer.clientX - gesture.startX;
    var dy = pointer.clientY - gesture.startY;
    if (!gesture.moved && Math.hypot(dx, dy) < DRAG_THRESHOLD_PX) {
      return;
    }
    if (!gesture.moved) {
      gesture.moved = true;
      mapElement.classList.add("panning");
    }
    var box = svgElement.getBoundingClientRect();
    var mapUnitsPerPixel = gesture.startView.width / box.width;
    setView(gesture.startView.x - dx * mapUnitsPerPixel, gesture.startView.y - dy * mapUnitsPerPixel, gesture.startView.width);
  }

  function continuePinch(pointers) {
    var distance = pinchDistance(pointers);
    var midpoint = clientToMap((pointers[0].clientX + pointers[1].clientX) / 2, (pointers[0].clientY + pointers[1].clientY) / 2);
    zoomAround(distance / gesture.distance, midpoint.x, midpoint.y);
    gesture.distance = distance;
  }

  function onPointerMove(event) {
    if (!activePointers[event.pointerId] || !gesture) {
      return;
    }
    activePointers[event.pointerId] = {clientX: event.clientX, clientY: event.clientY};
    if (gesture.type === "pinch") {
      continuePinch(pointerList());
    } else if (currentZoom() > 1) {
      continuePan(activePointers[event.pointerId]);
    }
  }

  function onPointerEnd(event) {
    if (!activePointers[event.pointerId]) {
      return;
    }
    if (gesture && gesture.moved) {
      suppressNextClick = true;
      setTimeout(function () {
        suppressNextClick = false;
      }, 0);
    }
    delete activePointers[event.pointerId];
    mapElement.classList.remove("panning");
    startGesture();
  }

  function consumeSuppressedClick() {
    var suppressed = suppressNextClick;
    suppressNextClick = false;
    return suppressed;
  }

  function bindZoomEvents() {
    svgElement.addEventListener("wheel", onWheel, {passive: false});
    svgElement.addEventListener("dblclick", onDoubleClick);
    svgElement.addEventListener("pointerdown", onPointerDown);
    window.addEventListener("pointermove", onPointerMove);
    window.addEventListener("pointerup", onPointerEnd);
    window.addEventListener("pointercancel", onPointerEnd);
  }

  function init() {
    mapElement = document.getElementById("map");
    if (!mapElement) {
      return;
    }
    drawWorld();
    buildZoomControls();
    applyView();
    window.addEventListener("resize", scaleMarkersToScreen);
    popover = createElement("div", "map-popover");
    popover.hidden = true;
    popover.setAttribute("role", "dialog");
    mapElement.appendChild(popover);
    bindMarkerEvents();
    bindPopoverEvents();
    bindTableEvents();
    bindZoomEvents();
    loadPoints();
    startPolling();
  }

  init();
})();
