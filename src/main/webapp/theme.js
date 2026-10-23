(function () {
  "use strict";

  var STORAGE_KEY = "nanopub-monitor-theme";
  var root = document.documentElement;
  var systemDark = window.matchMedia("(prefers-color-scheme: dark)");

  function storedTheme() {
    try {
      return localStorage.getItem(STORAGE_KEY);
    } catch (e) {
      return null;
    }
  }

  function storeTheme(theme) {
    try {
      localStorage.setItem(STORAGE_KEY, theme);
    } catch (e) {
    }
  }

  function systemTheme() {
    return systemDark.matches ? "dark" : "light";
  }

  function currentTheme() {
    return root.getAttribute("data-theme");
  }

  function labelToggle() {
    var toggle = document.getElementById("theme-toggle");
    if (!toggle) {
      return;
    }
    var label = currentTheme() === "dark" ? "Switch to light mode" : "Switch to dark mode";
    toggle.setAttribute("aria-label", label);
    toggle.title = label;
  }

  function applyTheme(theme) {
    root.setAttribute("data-theme", theme);
    labelToggle();
  }

  function toggleTheme() {
    var next = currentTheme() === "dark" ? "light" : "dark";
    storeTheme(next);
    applyTheme(next);
  }

  function followSystemUntilChosen() {
    systemDark.addEventListener("change", function () {
      if (!storedTheme()) {
        applyTheme(systemTheme());
      }
    });
  }

  applyTheme(storedTheme() || systemTheme());
  followSystemUntilChosen();

  document.addEventListener("DOMContentLoaded", function () {
    var toggle = document.getElementById("theme-toggle");
    if (toggle) {
      toggle.addEventListener("click", toggleTheme);
      labelToggle();
    }
  });
})();
