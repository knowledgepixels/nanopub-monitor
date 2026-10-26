package ch.tkuhn.nanopub.monitor;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks what a server's data reports about its health, its hashes, its sync state and its location.
 */
class ServerDataTest {

    private static final IRI REGISTRY_TYPE = Values.iri("https://w3id.org/np/o/service/terms/nanopub-registry-1.0");

    private ServerData registry;

    /**
     * Start every test with an empty server list and a registry at an unknown location.
     */
    @BeforeEach
    void setUp() {
        TestServers.serverList();
        registry = TestServers.serverData("https://registry.example.org/", REGISTRY_TYPE);
    }

    /**
     * A server that was never tested reports nothing about its health.
     */
    @Test
    void aServerNeverTestedReportsNoHealth() {
        assertEquals("NOT SEEN", registry.getStatusString());
        assertNull(registry.getLastSeenDate());
        assertNull(registry.getAvgResponseTimeInMs());
        assertEquals("?", registry.getAvgResponseTimeString());
        assertNull(registry.getSuccessRatio());
        assertEquals("?", registry.getSuccessRatioString());
    }

    /**
     * Successes and failures add up to the OK ratio, and successes alone to the response time.
     */
    @Test
    void testResultsAddUpToRatioAndResponseTime() {
        registry.reportTestSuccess(10);
        registry.reportTestSuccess(20);
        registry.reportTestFailure("DOWN");
        registry.reportTestFailure("DOWN");

        assertEquals("DOWN", registry.getStatusString());
        assertNotNull(registry.getLastSeenDate());
        assertEquals(15, registry.getAvgResponseTimeInMs());
        assertEquals("15 ms", registry.getAvgResponseTimeString());
        assertEquals(0.5f, registry.getSuccessRatio());
        assertEquals("50.0%", registry.getSuccessRatioString());
    }

    /**
     * A success after a failure sets the status back to OK.
     */
    @Test
    void aSuccessSetsTheStatusBackToOk() {
        registry.reportTestFailure("INACCESSIBLE");
        registry.reportTestSuccess(5);

        assertEquals("OK", registry.getStatusString());
    }

    /**
     * A stall overrides the status without counting as a failure.
     */
    @Test
    void aStallIsNoFailure() {
        registry.reportTestSuccess(5);
        registry.reportStalled();

        assertEquals("STALLED", registry.getStatusString());
        assertEquals(1.0f, registry.getSuccessRatio());
    }

    /**
     * Hashes and checksums are shortened to twelve characters for display, and unknown ones to nothing.
     */
    @Test
    void hashesAreShortenedForDisplay() {
        assertEquals("", registry.getTrustStateHashShort());
        assertEquals("", registry.getLoadedNanopubChecksumShort());

        registry.setTrustStateHash("0123456789abcdef");
        registry.setLoadedNanopubChecksum("short");

        assertEquals("0123456789abcdef", registry.getTrustStateHash());
        assertEquals("0123456789ab", registry.getTrustStateHashShort());
        assertEquals("short", registry.getLoadedNanopubChecksum());
        assertEquals("short", registry.getLoadedNanopubChecksumShort());
    }

    /**
     * Settings are shortened to ten characters for display, and unknown ones to nothing.
     */
    @Test
    void settingsAreShortenedForDisplay() {
        assertEquals("", ServerData.shortSetting(null));
        assertEquals("RAshort", ServerData.shortSetting("RAshort"));
        assertEquals("RA01234567", ServerData.shortSetting("RA0123456789abcdef"));
    }

    /**
     * The settings, the version and the test flag are kept as set.
     */
    @Test
    void reportedDetailsAreKept() {
        registry.setCurrentSetting("RAcurrent");
        registry.setOriginalSetting("RAoriginal");
        registry.setVersion("1.4.0");
        registry.setTestInstance(true);

        assertEquals("RAcurrent", registry.getCurrentSetting());
        assertEquals("RAoriginal", registry.getOriginalSetting());
        assertEquals("1.4.0", registry.getVersion());
        assertTrue(registry.isTestInstance());
    }

    /**
     * The nanopub count is shown with thousand separators, and an unknown one as nothing.
     */
    @Test
    void theNanopubCountIsShownWithSeparators() {
        assertEquals("", registry.getNanopubCountString());

        registry.setNanopubCount(1234567L);

        assertEquals(String.format("%,d", 1234567L), registry.getNanopubCountString());
    }

    /**
     * An instance whose registry holds more than it loaded is behind, but not stalled at first.
     */
    @Test
    void anInstanceBehindItsRegistryIsNotStalledAtFirst() {
        registry.setNanopubCount(900L);
        registry.updateSyncHealth(1000L, 2L);

        assertEquals(100L, registry.getSyncLag());
        assertEquals(1000L, registry.getRegistryNanopubCount());
        assertEquals(2L, registry.getLoaderLastSuccessAgeSeconds());
        assertEquals("100 behind", registry.getSyncLagString());
        assertEquals("2s ago", registry.getLoaderAgeString());
        assertTrue(registry.isBehind());
        assertFalse(registry.isSyncStalled());
    }

    /**
     * An instance whose loader has not finished a tick for long is stalled.
     */
    @Test
    void anInstanceWithAnOldLoaderIsStalled() {
        registry.setNanopubCount(900L);
        registry.updateSyncHealth(1000L, SyncHealth.LOADER_STALL_SECONDS + 1);

        assertTrue(registry.isSyncStalled());
    }

    /**
     * An instance that catches up is no longer behind.
     */
    @Test
    void anInstanceThatCatchesUpIsInSync() {
        registry.setNanopubCount(900L);
        registry.updateSyncHealth(1000L, 2L);
        registry.setNanopubCount(1000L);
        registry.updateSyncHealth(1000L, 2L);

        assertEquals("in sync", registry.getSyncLagString());
        assertFalse(registry.isBehind());
        assertFalse(registry.isSyncStalled());
    }

    /**
     * Without both counts there is no lag to report.
     */
    @Test
    void withoutBothCountsThereIsNoLag() {
        registry.updateSyncHealth(null, null);

        assertNull(registry.getSyncLag());
        assertEquals("", registry.getSyncLagString());
        assertEquals("", registry.getLoaderAgeString());
        assertFalse(registry.isBehind());
    }

    /**
     * A versioned service type matches its unversioned prefix, but not as an exact type.
     */
    @Test
    void aVersionedTypeMatchesItsPrefix() {
        assertTrue(registry.hasServiceTypePrefix(NanopubService.NANOPUB_REGISTRY_TYPE_IRI));
        assertFalse(registry.hasServiceType(NanopubService.NANOPUB_REGISTRY_TYPE_IRI));
        assertTrue(registry.hasServiceType(REGISTRY_TYPE));
        assertEquals("https://registry.example.org/", registry.getServiceId());
    }

    /**
     * New server info replaces the old, and missing info keeps it.
     */
    @Test
    void serverInfoIsOnlyReplacedByNewInfo() {
        assertNull(registry.getServerInfo());

        registry.update("first");
        registry.update(null);

        assertEquals("first", registry.getServerInfo());
    }

    /**
     * A located server is as far from the monitor as the two places are apart.
     */
    @Test
    void aLocatedServerHasADistance() {
        TestServers.locate("https://helsinki.example.org/", TestServers.HELSINKI);
        ServerData helsinki = TestServers.serverData("https://helsinki.example.org/", REGISTRY_TYPE);

        assertSame(TestServers.HELSINKI, helsinki.getIpInfo());
        Integer km = helsinki.getDistanceInKm();
        assertTrue(km > 1600 && km < 1900, "Zurich to Helsinki, got " + km);
        assertEquals(km + " km", helsinki.getDistanceString());
    }

    /**
     * A server at an unknown location has no distance.
     */
    @Test
    void anUnlocatedServerHasNoDistance() {
        assertSame(ServerIpInfo.empty, registry.getIpInfo());
        assertNull(registry.getDistanceInKm());
        assertEquals("?", registry.getDistanceString());
    }

    /**
     * A lookup within the throttle window answers from the cache.
     *
     * @throws Exception if the lookup fails
     */
    @Test
    void aRecentLookupIsAnsweredFromTheCache() throws Exception {
        ServerData.cacheIpInfo("cached.example.org", TestServers.HELSINKI);

        assertSame(TestServers.HELSINKI, ServerData.fetchIpInfo("cached.example.org"));
    }

    /**
     * The ip-api.com request asks for exactly the fields the location info reads.
     */
    @Test
    void theIpApiRequestAsksForTheFieldsItReads() {
        assertEquals("http://ip-api.com/json/example.org?fields=status,message,continent,country,city,lat,lon,query",
                ServerData.ipApiUrl("example.org"));
    }

}
