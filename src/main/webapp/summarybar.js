(function () {
  "use strict";

  var SCROLL_THRESHOLD_PX = 4;
  var CHIP_JUMP_TIMEOUT_MS = 500;
  var root = document.documentElement;
  var lastScrollY = window.scrollY;
  var jumpingFromChip = false;
  var chipJumpTimer = null;

  function summaryCardsScrolledPast() {
    var summary = document.getElementById("summary");
    var bar = document.getElementById("summary-bar");
    var barHeight = bar === null ? 0 : bar.offsetHeight;
    return summary !== null && summary.getBoundingClientRect().bottom < barHeight;
  }

  function setBarShown(shown) {
    if (shown) {
      root.setAttribute("data-summary-bar", "shown");
    } else {
      root.removeAttribute("data-summary-bar");
    }
  }

  function endChipJump() {
    jumpingFromChip = false;
    clearTimeout(chipJumpTimer);
  }

  function onChipJump() {
    lastScrollY = window.scrollY;
    setBarShown(summaryCardsScrolledPast());
    endChipJump();
  }

  function onScroll() {
    if (jumpingFromChip) {
      onChipJump();
      return;
    }
    var scrollY = window.scrollY;
    var delta = scrollY - lastScrollY;
    if (Math.abs(delta) < SCROLL_THRESHOLD_PX) {
      return;
    }
    lastScrollY = scrollY;
    setBarShown(delta > 0 && summaryCardsScrolledPast());
  }

  function onClick(event) {
    var chip = event.target instanceof Element ? event.target.closest(".summary-chip") : null;
    if (!chip) {
      return;
    }
    jumpingFromChip = true;
    clearTimeout(chipJumpTimer);
    chipJumpTimer = setTimeout(endChipJump, CHIP_JUMP_TIMEOUT_MS);
  }

  window.addEventListener("scroll", onScroll, {passive: true});
  document.addEventListener("click", onClick);
})();
