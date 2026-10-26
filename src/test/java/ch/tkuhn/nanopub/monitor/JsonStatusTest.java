package ch.tkuhn.nanopub.monitor;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks the JSON status export read by the nanopub-router.
 */
class JsonStatusTest {

    private static final String QUERY_TYPE = "https://w3id.org/np/o/service/terms/nanopub-query-1.1";

    /**
     * Get the entries of the servers in a status.
     *
     * @param status the status
     * @return the server entries
     */
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> servers(Map<String, Object> status) {
        return (List<Map<String, Object>>) status.get("servers");
    }

    /**
     * The status names the monitor's version and when it was generated.
     */
    @Test
    void theStatusNamesTheMonitorAndItsTime() {
        Map<String, Object> status = JsonStatus.statusOf(TestServers.serverList());

        assertEquals(MonitorVersion.get(), status.get("monitorVersion"));
        assertNotNull(status.get("generatedAt"));
        assertTrue(servers(status).isEmpty());
    }

    /**
     * A tested query instance is described with what it reported.
     */
    @Test
    void aTestedQueryInstanceIsDescribed() {
        TestServers.locate("https://query.helsinki.example.org/", TestServers.HELSINKI);
        NanopubService service = TestServers.service("https://query.helsinki.example.org/", QUERY_TYPE);
        ServerList sl = TestServers.serverList(service);
        ServerData sd = sl.getServerData(service);
        sd.setVersion("1.9.0");
        sd.setLoadedNanopubChecksum("sum-1");
        sd.setNanopubCount(900L);
        sd.updateSyncHealth(1000L, 5L);
        sd.reportTestSuccess(40);

        Map<String, Object> entry = servers(JsonStatus.statusOf(sl)).get(0);

        assertEquals("https://query.helsinki.example.org/", entry.get("url"));
        assertEquals(QUERY_TYPE, entry.get("type"));
        assertEquals("nanopub-query-1.1", entry.get("typeLabel"));
        assertEquals("1.9.0", entry.get("version"));
        assertEquals(false, entry.get("testInstance"));
        assertEquals("OK", entry.get("status"));
        assertEquals("consensus", entry.get("hashGroup"));
        assertEquals("sum-1", entry.get("loadedNanopubChecksum"));
        assertEquals(900L, entry.get("nanopubCount"));
        assertEquals(1000L, entry.get("registryNanopubCount"));
        assertEquals(100L, entry.get("syncLag"));
        assertEquals(5L, entry.get("loaderAgeSeconds"));
        assertEquals(1.0f, entry.get("okRatio"));
        assertEquals(40, entry.get("respTimeMs"));
        assertEquals(sd.getDistanceInKm(), entry.get("distanceKm"));
        assertEquals(sd.getLastSeenDate().toInstant().toString(), entry.get("lastSeenOk"));
        assertEquals("192.0.2.20", entry.get("ip"));
        assertEquals("Europe", entry.get("region"));
        assertEquals("Finland", entry.get("country"));
        assertEquals("Helsinki", entry.get("city"));
    }

    /**
     * A service never seen OK has no last-seen time.
     */
    @Test
    void aServiceNeverSeenOkHasNoLastSeenTime() {
        ServerList sl = TestServers.serverList(TestServers.service("https://query.example.org/", QUERY_TYPE));

        Map<String, Object> entry = servers(JsonStatus.statusOf(sl)).get(0);

        assertNull(entry.get("lastSeenOk"));
        assertNull(entry.get("okRatio"));
    }

    /**
     * The singleton serves the status as a resource.
     */
    @Test
    void theSingletonServesTheStatus() {
        TestServers.serverList(TestServers.service("https://query.example.org/", QUERY_TYPE));

        assertSame(JsonStatus.instance(), JsonStatus.instance());
        assertNotNull(JsonStatus.instance().get());
    }

}
