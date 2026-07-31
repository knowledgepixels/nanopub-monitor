package ch.tkuhn.nanopub.monitor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SyncHealthTest {

    @Test
    void lagIsUnknownUntilBothCountsArrive() {
        // Unknown has to stay distinguishable from zero: an instance that has not been
        // scanned yet must not be reported as in sync.
        assertNull(SyncHealth.lag(null, 100L));
        assertNull(SyncHealth.lag(100L, null));
        assertNull(SyncHealth.lag(null, null));
    }

    @Test
    void lagIsTheShortfallAgainstTheRegistry() {
        assertEquals(0L, SyncHealth.lag(86605L, 86605L));
        assertEquals(7L, SyncHealth.lag(86605L, 86598L));
    }

    @Test
    void lagIsClampedWhenTheLoadedCountRunsAhead() {
        // The loaded count is bumped per nanopub while the registry count refreshes once
        // per poll, so the loaded side legitimately leads for a moment.
        assertEquals(0L, SyncHealth.lag(86600L, 86605L));
    }

    @Test
    void lagFormatting() {
        assertEquals("", SyncHealth.formatLag(null));
        assertEquals("in sync", SyncHealth.formatLag(0L));
        assertEquals("7 behind", SyncHealth.formatLag(7L));
        assertEquals("1,204 behind", SyncHealth.formatLag(1204L));
    }

    @Test
    void ageFormatting() {
        assertEquals("", SyncHealth.formatAge(null));
        assertEquals("", SyncHealth.formatAge(-1L));
        assertEquals("2s ago", SyncHealth.formatAge(2L));
        assertEquals("59s ago", SyncHealth.formatAge(59L));
        assertEquals("1m ago", SyncHealth.formatAge(60L));
        assertEquals("48m ago", SyncHealth.formatAge(2880L));
        assertEquals("1h 00m ago", SyncHealth.formatAge(3600L));
        // The 2026-07-31 stall, at the point it was noticed.
        assertEquals("2h 04m ago", SyncHealth.formatAge(7440L));
    }

    @Test
    void aHealthyInstanceIsNotStalled() {
        long now = 1_000_000L;
        assertFalse(SyncHealth.isStalled(null, now, 2L));
        // Unknown loader age must not by itself imply a stall — instances predating the
        // header would all be flagged.
        assertFalse(SyncHealth.isStalled(null, now, null));
    }

    @Test
    void briefLagIsToleratedButSustainedLagIsNot() {
        long now = 1_000_000_000L;
        long justBehind = now - 60_000L;
        assertFalse(SyncHealth.isStalled(justBehind, now, null));

        long longBehind = now - SyncHealth.BEHIND_GRACE_MS - 1L;
        assertTrue(SyncHealth.isStalled(longBehind, now, null));
    }

    @Test
    void aDeadLoaderIsStalledEvenWhileTheLagStillReadsZero() {
        // The failure mode the lag alone cannot see: when the registry poll is what
        // broke, both counts freeze together and the lag settles at zero.
        long now = 1_000_000_000L;
        assertTrue(SyncHealth.isStalled(null, now, SyncHealth.LOADER_STALL_SECONDS + 1));
        assertFalse(SyncHealth.isStalled(null, now, SyncHealth.LOADER_STALL_SECONDS));
    }

}
