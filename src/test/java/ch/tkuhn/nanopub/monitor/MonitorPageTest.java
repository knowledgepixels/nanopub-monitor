package ch.tkuhn.nanopub.monitor;

import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Renders the monitor page and its exports in the real application, with the scanner daemon off.
 */
class MonitorPageTest {

    private static final String REGISTRY_TYPE = "https://w3id.org/np/o/service/terms/nanopub-registry-1.0";
    private static final String REGISTRY_NEXT_TYPE = "https://w3id.org/np/o/service/terms/nanopub-registry-1.1";
    private static final String QUERY_TYPE = "https://w3id.org/np/o/service/terms/nanopub-query-1.1";

    private static final String REGISTRY_ROWS = "tablecontainer:sections:0:rows:";
    private static final String QUERY_ROWS = "tablecontainer:sections:1:rows:";

    private WicketTester tester;

    /**
     * Start the application with the scanner daemon off and an empty server list.
     */
    @BeforeEach
    void setUp() {
        ServerScanner.setDaemonEnabled(false);
        TestServers.serverList();
        tester = new WicketTester(new MonitorApplication());
    }

    /**
     * Stop the application and allow the scanner daemon again.
     */
    @AfterEach
    void tearDown() {
        tester.destroy();
        ServerScanner.setDaemonEnabled(true);
    }

    /**
     * Get the server data of a tracked service.
     *
     * @param url  the service URL
     * @param type the service type IRI
     * @return its server data
     */
    private static ServerData tracked(String url, String type) {
        return ServerList.get().getServerData(TestServers.service(url, type));
    }

    /**
     * Render the page and get its markup.
     *
     * @return the rendered markup
     */
    private String render() {
        tester.startPage(MonitorPage.class);
        tester.assertRenderedPage(MonitorPage.class);
        return tester.getLastResponseAsString();
    }

    /**
     * A page with no servers renders empty counts and no sections.
     */
    @Test
    void aPageWithoutServersRendersEmpty() {
        render();

        tester.assertLabel("counts:server-count", "0");
        tester.assertLabel("counts:server-ip-count", "0");
        tester.assertVisible("map");
        assertEquals(0, ((ListView<?>) tester.getComponentFromLastRenderedPage("tablecontainer:sections")).size());
    }

    /**
     * Registries are summarized, ordered by attention and shown with their trust state and peers.
     */
    @Test
    void registriesShowTheirTrustStateAndPeers() {
        TestServers.serverList(
                TestServers.service("https://a.example.org/", REGISTRY_TYPE),
                TestServers.service("https://b.example.org/", REGISTRY_TYPE),
                TestServers.service("https://c.example.org/", REGISTRY_NEXT_TYPE));
        for (String url : new String[]{"https://a.example.org/", "https://b.example.org/"}) {
            ServerData sd = tracked(url, REGISTRY_TYPE);
            sd.setCurrentSetting("RAcurrent0123");
            sd.setTrustStateHash("hash-majority-0123");
            sd.setVersion("1.4.0");
            sd.reportTestSuccess(20);
        }
        ServerData outlier = tracked("https://c.example.org/", REGISTRY_NEXT_TYPE);
        outlier.setOriginalSetting("RAoriginal0123");
        outlier.setCurrentSetting("RAcurrent0123");
        outlier.setTrustStateHash("hash-other");
        outlier.setTestInstance(true);
        outlier.reportTestSuccess(20);

        String markup = render();

        tester.assertLabel("tablecontainer:summary:0:title", "Registry");
        tester.assertLabel("tablecontainer:summary:0:ok", "3");
        tester.assertLabel("tablecontainer:summary:0:total", "3");
        tester.assertLabel("tablecontainer:summary:0:attention:attention-count", "1 need attention");
        tester.assertLabel("tablecontainer:summary-bar:0:tally", "3/3");
        tester.assertLabel("tablecontainer:sections:0:instances", "3 instances");
        tester.assertLabel(REGISTRY_ROWS + "0:urllink:url", "c.example.org");
        tester.assertVisible(REGISTRY_ROWS + "0:testchip");
        tester.assertVisible(REGISTRY_ROWS + "0:row-type");
        tester.assertLabel(REGISTRY_ROWS + "0:trust:setting", "RAoriginal → RAcurrent0");
        tester.assertLabel(REGISTRY_ROWS + "0:trust:trust-group", "outlier");
        tester.assertLabel(REGISTRY_ROWS + "1:trust:trust-group", "consensus");
        tester.assertLabel(REGISTRY_ROWS + "1:trust:trust-hash", "hash-majorit");
        tester.assertLabel(REGISTRY_ROWS + "1:version", "v1.4.0");
        tester.assertInvisible(REGISTRY_ROWS + "1:testchip");
        tester.assertInvisible(REGISTRY_ROWS + "1:checksum");
        tester.assertVisible("tablecontainer:sections:0:trust-header");
        tester.assertInvisible("tablecontainer:sections:0:sync-header");
        assertTrue(markup.contains("href=\"#nanopub-registry\""), "The summary card links to its section");
    }

    /**
     * A registry with no trust state says so.
     */
    @Test
    void aRegistryWithoutTrustStateSaysSo() {
        TestServers.serverList(TestServers.service("https://a.example.org/", REGISTRY_TYPE));

        render();

        tester.assertLabel(REGISTRY_ROWS + "0:trust:trust-hash", "no trust state");
        tester.assertInvisible(REGISTRY_ROWS + "0:trust:trust-group");
        tester.assertInvisible(REGISTRY_ROWS + "0:row-type");
        tester.assertInvisible(REGISTRY_ROWS + "0:version");
        tester.assertLabel(REGISTRY_ROWS + "0:ok-ratio", "not tested yet");
        tester.assertInvisible(REGISTRY_ROWS + "0:resp-time");
        tester.assertInvisible(REGISTRY_ROWS + "0:last-seen");
        tester.assertLabel(REGISTRY_ROWS + "0:place", "unknown city, unknown country");
        tester.assertLabel(REGISTRY_ROWS + "0:region", "unknown region");
    }

    /**
     * A query instance behind its registry with a stalled loader is flagged in both columns.
     */
    @Test
    void aStalledQueryInstanceIsFlagged() {
        TestServers.serverList(
                TestServers.service("https://registry.example.org/", REGISTRY_TYPE),
                TestServers.service("https://query.example.org/", QUERY_TYPE));
        ServerData query = tracked("https://query.example.org/", QUERY_TYPE);
        query.setLoadedNanopubChecksum("checksum0123456789");
        query.setNanopubCount(900L);
        query.updateSyncHealth(1000L, SyncHealth.LOADER_STALL_SECONDS + 60);
        query.reportTestSuccess(10);
        query.reportTestFailure("DOWN");
        query.reportTestFailure("DOWN");

        String markup = render();

        tester.assertLabel("tablecontainer:sections:1:title", "Query");
        tester.assertLabel("tablecontainer:sections:1:instances", "1 instance");
        tester.assertLabel(QUERY_ROWS + "0:checksum:checksum-hash", "checksum0123");
        tester.assertLabel(QUERY_ROWS + "0:checksum:checksum-group", "consensus");
        tester.assertLabel(QUERY_ROWS + "0:sync", "100 behind");
        tester.assertVisible(QUERY_ROWS + "0:loader");
        tester.assertLabel(QUERY_ROWS + "0:status", "DOWN");
        tester.assertVisible(QUERY_ROWS + "0:resp-time");
        tester.assertVisible(QUERY_ROWS + "0:last-seen");
        tester.assertVisible("tablecontainer:summary-bar:1:attention");
        assertEquals(3, markup.split("text-bad", -1).length - 1,
                "The lag, the loader age and the low OK ratio are each flagged, in:\n" + markup);
    }

    /**
     * A located server shows its place, and its distance from the monitor in the tooltip.
     */
    @Test
    void aLocatedServerShowsWhereItIs() {
        TestServers.locate("https://registry.helsinki.example.org/", TestServers.HELSINKI);
        TestServers.serverList(
                TestServers.service("https://registry.helsinki.example.org/", REGISTRY_TYPE),
                TestServers.service("https://registry.example.org/", REGISTRY_TYPE));
        ServerData helsinki = tracked("https://registry.helsinki.example.org/", REGISTRY_TYPE);
        helsinki.reportTestSuccess(10);

        String markup = render();

        tester.assertLabel("counts:server-ip-count", "2");
        assertTrue(markup.contains("Helsinki, Finland"));
        assertTrue(markup.contains("title=\"192.0.2.20\""));
        assertTrue(markup.contains("Last seen OK " + MonitorPage.formatDate(helsinki.getLastSeenDate()) + " · "
                                   + helsinki.getDistanceString() + " from monitor"));
    }

    /**
     * The Ajax timer repaints the counts and the table.
     */
    @Test
    void theTimerRepaintsCountsAndTable() {
        render();
        TestServers.serverList(TestServers.service("https://a.example.org/", REGISTRY_TYPE));

        tester.executeAllTimerBehaviors(tester.getLastRenderedPage());

        tester.assertComponentOnAjaxResponse("counts");
        tester.assertComponentOnAjaxResponse("tablecontainer");
        tester.assertLabel("counts:server-count", "1");
    }

    /**
     * Every response names the monitor's version.
     */
    @Test
    void everyResponseNamesTheMonitorVersion() {
        render();

        assertEquals(MonitorVersion.get(), tester.getLastResponse().getHeader("Nanopub-Monitor-Version"));
    }

    /**
     * The exports are served at their mounted paths.
     */
    @Test
    void theExportsAreMounted() {
        TestServers.serverList(TestServers.service("https://a.example.org/", REGISTRY_TYPE));

        tester.executeUrl(".csv");
        assertTrue(tester.getLastResponseAsString().startsWith("\"URL\""));

        tester.executeUrl(".json");
        assertTrue(tester.getLastResponseAsString().contains("\"monitorVersion\""));

        tester.executeUrl(".map.json");
        assertTrue(tester.getLastResponseAsString().contains("\"points\""));
    }

    /**
     * Dates are shown to the second, and an unknown date as nothing.
     */
    @Test
    void datesAreShownToTheSecond() {
        assertEquals("", MonitorPage.formatDate(null));
        assertTrue(MonitorPage.formatDate(new Date()).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

}
