package ch.tkuhn.nanopub.monitor;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;

/**
 * Text and styling of the monitor table's cells, kept apart from the page so it can be tested
 * without Wicket.
 */
final class ServiceCells {

    /**
     * OK ratio below which the ratio is shown as a problem.
     */
    static final float LOW_OK_RATIO = 0.9f;

    private ServiceCells() {
    }

    /**
     * Shorten a service URL for display by dropping the scheme and the trailing slash.
     *
     * @param url the service URL
     * @return e.g. "registry.knowledgepixels.com" for "https://registry.knowledgepixels.com/"
     */
    static String shortUrl(String url) {
        return url.replaceFirst("^https?://", "").replaceFirst("/$", "");
    }

    /**
     * Get the CSS class of the status badge.
     *
     * @param status the status, e.g. "OK" or "DOWN"
     * @return "ok", "warn" for a stalled instance, "idle" for one not seen yet, or "bad"
     */
    static String statusClass(String status) {
        return switch (status) {
            case "OK" -> "ok";
            case "STALLED" -> "warn";
            case "NOT SEEN" -> "idle";
            default -> "bad";
        };
    }

    /**
     * Render an OK ratio.
     *
     * @param ratio the share of successful tests, between 0 and 1, or null if untested
     * @return e.g. "99.8% OK" or "100% OK", or "not tested yet"
     */
    static String formatOkRatio(Float ratio) {
        if (ratio == null) {
            return "not tested yet";
        }
        DecimalFormat percent = new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.ROOT));
        return percent.format(ratio * 100) + "% OK";
    }

    /**
     * Whether an OK ratio is low enough to show as a problem.
     *
     * @param ratio the share of successful tests, or null if untested
     * @return true if the ratio is known and below {@link #LOW_OK_RATIO}
     */
    static boolean isLowOkRatio(Float ratio) {
        return ratio != null && ratio < LOW_OK_RATIO;
    }

    /**
     * Say how long ago a service was last seen OK.
     *
     * @param lastSeenOk when the service was last seen OK, or null if never
     * @param now        the current time
     * @return e.g. "seen OK 8s ago", or "never seen OK"
     */
    static String formatLastSeen(Date lastSeenOk, Date now) {
        if (lastSeenOk == null) {
            return "never seen OK";
        }
        long seconds = Math.max(0L, (now.getTime() - lastSeenOk.getTime()) / 1000);
        return "seen OK " + SyncHealth.formatAge(seconds);
    }

    /**
     * Render the setting of a registry, showing the change when it was switched at runtime.
     *
     * @param original the setting the registry started with, or null
     * @param current  the registry's current setting, or null
     * @return e.g. "RAkYh4UPJr" or "RAkYh4UPJr → RAoX12bTwq", or "" when unknown
     */
    static String formatSetting(String original, String current) {
        if (current == null) {
            return "";
        }
        if (original != null && !original.equals(current)) {
            return ServerData.shortSetting(original) + " → " + ServerData.shortSetting(current);
        }
        return ServerData.shortSetting(current);
    }

}
