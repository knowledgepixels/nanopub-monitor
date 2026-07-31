package ch.tkuhn.nanopub.monitor;

/**
 * Sync-health arithmetic for nanopub-query instances: how far one trails its own
 * registry, and whether it has stopped ingesting altogether.
 *
 * <p>These checks are deliberately <em>absolute</em>. The checksum consensus check
 * compares instances against each other, so it can only see a problem while at least
 * one of them is healthy — when every instance stalls together they agree perfectly
 * and the fleet reads as all-clear. Comparing each instance against its own registry
 * means agreement buys nothing.
 *
 * <p>The two signals are not redundant. The registry side of the lag is whatever the
 * instance's most recent successful poll returned, so if polling itself is what broke,
 * the loaded count and the registry count freeze together and the lag settles at a
 * reassuring zero; only the loader age catches that. Conversely an instance that keeps
 * polling but falls behind moves only the lag.
 *
 * <p>Static and state-free so it can be tested without constructing a
 * {@link ServerData}, whose constructor performs geo-IP lookups.
 */
final class SyncHealth {

    /**
     * Loader age beyond which an instance counts as stalled. The loader polls every
     * two seconds and stamps its timestamp even on idle "nothing to do" ticks, so a
     * healthy instance stays near zero however quiet the registry is.
     */
    static final long LOADER_STALL_SECONDS = 300;

    /**
     * How long an instance may sit behind its registry before it counts as stalled.
     * Generous because a nanopub published between the instance's registry poll and
     * its batch landing shows up here as legitimate transient lag.
     */
    static final long BEHIND_GRACE_MS = 15 * 60 * 1000L;

    private SyncHealth() {
    }

    /**
     * How far an instance trails its registry.
     *
     * <p>Clamped at zero: the loaded count is bumped as each nanopub lands while the
     * forwarded registry count only refreshes once per poll, so the loaded side can
     * legitimately run ahead for a moment. That is not something to report as a
     * negative lag.
     *
     * @param registryCount nanopubs the instance's registry reports holding, or null
     * @param loadedCount   nanopubs the instance reports having loaded, or null
     * @return the lag in nanopubs, or null if either count is unknown
     */
    static Long lag(Long registryCount, Long loadedCount) {
        if (registryCount == null || loadedCount == null) {
            return null;
        }
        return Math.max(0L, registryCount - loadedCount);
    }

    /**
     * Renders a lag for the table cell.
     *
     * @param lag the lag in nanopubs, or null if unknown
     * @return "in sync", "N behind", or "" when unknown
     */
    static String formatLag(Long lag) {
        if (lag == null) {
            return "";
        }
        if (lag == 0L) {
            return "in sync";
        }
        return String.format("%,d behind", lag);
    }

    /**
     * Renders a loader age for the table cell, at the coarsest useful precision.
     *
     * @param seconds seconds since the loader last completed a tick, or null if unknown
     * @return e.g. "2s ago", "48m ago", "2h 04m ago", or "" when unknown
     */
    static String formatAge(Long seconds) {
        if (seconds == null || seconds < 0) {
            return "";
        }
        if (seconds < 60) {
            return seconds + "s ago";
        }
        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + "m ago";
        }
        return String.format("%dh %02dm ago", minutes / 60, minutes % 60);
    }

    /**
     * Whether an instance should be reported as stalled: reachable and answering, but
     * no longer keeping up with its registry.
     *
     * @param behindSinceMs   epoch millis when the instance was first seen behind, or null if level
     * @param nowMs           current epoch millis
     * @param loaderAgeSeconds seconds since the loader last completed a tick, or null if unknown
     * @return true if the instance has been out of sync long enough to report
     */
    static boolean isStalled(Long behindSinceMs, long nowMs, Long loaderAgeSeconds) {
        if (loaderAgeSeconds != null && loaderAgeSeconds > LOADER_STALL_SECONDS) {
            return true;
        }
        return behindSinceMs != null && nowMs - behindSinceMs > BEHIND_GRACE_MS;
    }

}
