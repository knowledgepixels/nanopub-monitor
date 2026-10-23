(function () {
  "use strict";

  var SCROLL_THRESHOLD_PX = 4;
  var root = document.documentElement;
  var lastScrollY = window.scrollY;

  function summaryCardsScrolledPast() {
    var summary = document.getElementById("summary");
    return summary !== null && summary.getBoundingClientRect().bottom < 0;
  }

  function setBarShown(shown) {
    if (shown) {
      root.setAttribute("data-summary-bar", "shown");
    } else {
      root.removeAttribute("data-summary-bar");
    }
  }

  function onScroll() {
    var scrollY = window.scrollY;
    var delta = scrollY - lastScrollY;
    if (Math.abs(delta) < SCROLL_THRESHOLD_PX) {
      return;
    }
    lastScrollY = scrollY;
    setBarShown(delta > 0 && summaryCardsScrolledPast());
  }

  window.addEventListener("scroll", onScroll, {passive: true});
})();
