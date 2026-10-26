package ch.tkuhn.nanopub.monitor;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceCellsTest {

    @Test
    void shortUrlDropsSchemeAndTrailingSlash() {
        assertEquals("registry.knowledgepixels.com", ServiceCells.shortUrl("https://registry.knowledgepixels.com/"));
        assertEquals("example.org/api", ServiceCells.shortUrl("http://example.org/api"));
    }

    @Test
    void statusClassDistinguishesStalledAndUnseen() {
        assertEquals("ok", ServiceCells.statusClass("OK"));
        assertEquals("warn", ServiceCells.statusClass("STALLED"));
        assertEquals("idle", ServiceCells.statusClass("NOT SEEN"));
        assertEquals("bad", ServiceCells.statusClass("DOWN"));
    }

    @Test
    void okRatioIsRoundedToOneDecimal() {
        assertEquals("99.8% OK", ServiceCells.formatOkRatio(0.99833f));
        assertEquals("100% OK", ServiceCells.formatOkRatio(1f));
        assertEquals("not tested yet", ServiceCells.formatOkRatio(null));
    }

    @Test
    void okRatioBelowNinetyPercentIsLow() {
        assertTrue(ServiceCells.isLowOkRatio(0.625f));
        assertFalse(ServiceCells.isLowOkRatio(0.95f));
        assertFalse(ServiceCells.isLowOkRatio(null));
    }

    @Test
    void lastSeenIsRelativeToNow() {
        Date now = new Date(1_000_000_000L);
        assertEquals("seen OK 8s ago", ServiceCells.formatLastSeen(new Date(now.getTime() - 8_000L), now));
        assertEquals("seen OK 1h 26m ago", ServiceCells.formatLastSeen(new Date(now.getTime() - 5_160_000L), now));
        assertEquals("never seen OK", ServiceCells.formatLastSeen(null, now));
    }

    @Test
    void settingShowsARuntimeChange() {
        assertEquals("RAkYh4UPJr", ServiceCells.formatSetting("RAkYh4UPJrXYZ", "RAkYh4UPJrXYZ"));
        assertEquals("RAkYh4UPJr → RAoX12bTwq", ServiceCells.formatSetting("RAkYh4UPJrXYZ", "RAoX12bTwqXYZ"));
        assertEquals("", ServiceCells.formatSetting(null, null));
    }

}
