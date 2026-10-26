package ch.tkuhn.nanopub.monitor;

import org.junit.jupiter.api.Test;
import org.nanopub.extra.services.ApiResponse;
import org.nanopub.extra.services.ApiResponseEntry;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks the server list's ordering, its consensus labels and its discovery of test instances.
 */
class ServerListStateTest {

    private static final String REGISTRY_TYPE = "https://w3id.org/np/o/service/terms/nanopub-registry-1.0";
    private static final String QUERY_TYPE = "https://w3id.org/np/o/service/terms/nanopub-query-1.1";
    private static final String NANODASH_TYPE = "https://w3id.org/np/o/service/terms/nanodash-2.x";

    /**
     * Track registries at the given URLs.
     *
     * @param urls the registry URLs
     * @return the server list
     */
    private static ServerList registries(String... urls) {
        NanopubService[] services = new NanopubService[urls.length];
        for (int i = 0; i < urls.length; i++) {
            services[i] = TestServers.service(urls[i], REGISTRY_TYPE);
        }
        return TestServers.serverList(services);
    }

    /**
     * Report a registry's setting and trust state hash.
     *
     * @param sl      the server list
     * @param url     the registry URL
     * @param setting the setting it runs
     * @param hash    the trust state hash it reports
     * @return its server data
     */
    private static ServerData report(ServerList sl, String url, String setting, String hash) {
        ServerData sd = sl.getServerData(TestServers.service(url, REGISTRY_TYPE));
        sd.setCurrentSetting(setting);
        sd.setTrustStateHash(hash);
        return sd;
    }

    /**
     * Report a query instance's checksum and test status.
     *
     * @param sl           the server list
     * @param url          the query instance URL
     * @param checksum     the loaded-nanopub checksum it reports
     * @param testInstance whether it is a test instance
     * @return its server data
     */
    private static ServerData reportQuery(ServerList sl, String url, String checksum, boolean testInstance) {
        ServerData sd = sl.getServerData(TestServers.service(url, QUERY_TYPE));
        sd.setLoadedNanopubChecksum(checksum);
        sd.setTestInstance(testInstance);
        return sd;
    }

    /**
     * The list tracks exactly the services it was given.
     */
    @Test
    void theListTracksTheGivenServices() {
        ServerList sl = registries("https://a.example.org/", "https://b.example.org/");

        assertSame(sl, ServerList.get());
        assertEquals(2, sl.getServerCount());
        assertEquals(2, sl.getServerData().size());
        assertNull(sl.getServerData(TestServers.service("https://c.example.org/", REGISTRY_TYPE)));
    }

    /**
     * Servers are sorted by type, descending, then by IP address and then by URL.
     */
    @Test
    void serversAreSortedByTypeThenAddressThenUrl() {
        TestServers.locate("https://z.located.example.org/", TestServers.HELSINKI);
        ServerList sl = TestServers.serverList(
                TestServers.service("https://b.example.org/", REGISTRY_TYPE),
                TestServers.service("https://a.example.org/", REGISTRY_TYPE),
                TestServers.service("https://z.located.example.org/", REGISTRY_TYPE),
                TestServers.service("https://query.example.org/", QUERY_TYPE));

        List<String> order = new ArrayList<>();
        sl.getSortedServerData().forEach(sd -> order.add(sd.getServiceId()));

        assertEquals(List.of("https://z.located.example.org/", "https://a.example.org/", "https://b.example.org/",
                "https://query.example.org/"), order);
    }

    /**
     * Within a setting, the registry reporting a different hash from the majority is the outlier.
     */
    @Test
    void theRegistryDisagreeingWithItsSettingCohortIsTheOutlier() {
        ServerList sl = registries("https://a.example.org/", "https://b.example.org/", "https://c.example.org/",
                "https://d.example.org/");
        ServerData a = report(sl, "https://a.example.org/", "RAsetting", "hash-1");
        report(sl, "https://b.example.org/", "RAsetting", "hash-1");
        ServerData c = report(sl, "https://c.example.org/", "RAsetting", "hash-2");
        ServerData d = report(sl, "https://d.example.org/", "RAother", "hash-3");

        assertEquals("hash-1", sl.getMajorityTrustStateHashForSetting("RAsetting"));
        assertEquals("consensus", sl.getHashGroupLabel(a));
        assertEquals("outlier", sl.getHashGroupLabel(c));
        assertEquals("consensus", sl.getHashGroupLabel(d), "A registry alone in its setting agrees with itself");
    }

    /**
     * A registry without a hash, without a setting, or in an evenly split cohort gets no label.
     */
    @Test
    void aRegistryWithNothingToCompareGetsNoLabel() {
        ServerList sl = registries("https://a.example.org/", "https://b.example.org/", "https://c.example.org/",
                "https://d.example.org/");
        ServerData noHash = report(sl, "https://a.example.org/", "RAsetting", null);
        ServerData noSetting = report(sl, "https://b.example.org/", null, "hash-1");
        ServerData split = report(sl, "https://c.example.org/", "RAsplit", "hash-1");
        report(sl, "https://d.example.org/", "RAsplit", "hash-2");

        assertNull(sl.getHashGroupLabel(noHash));
        assertNull(sl.getHashGroupLabel(noSetting));
        assertNull(sl.getHashGroupLabel(split), "Two registries disagreeing have no majority");
        assertNull(sl.getMajorityTrustStateHashForSetting(null));
    }

    /**
     * Query instances are compared with the other instances of their own test cohort.
     */
    @Test
    void queryInstancesAreComparedWithinTheirTestCohort() {
        ServerList sl = TestServers.serverList(
                TestServers.service("https://q1.example.org/", QUERY_TYPE),
                TestServers.service("https://q2.example.org/", QUERY_TYPE),
                TestServers.service("https://q3.example.org/", QUERY_TYPE),
                TestServers.service("https://test.q.example.org/", QUERY_TYPE),
                TestServers.service("https://q4.example.org/", QUERY_TYPE));
        ServerData q1 = reportQuery(sl, "https://q1.example.org/", "sum-1", false);
        reportQuery(sl, "https://q2.example.org/", "sum-1", false);
        ServerData q3 = reportQuery(sl, "https://q3.example.org/", "sum-2", false);
        ServerData test = reportQuery(sl, "https://test.q.example.org/", "sum-2", true);
        ServerData unknown = reportQuery(sl, "https://q4.example.org/", null, false);

        assertEquals("sum-1", sl.getMajorityLoadedNanopubChecksum(false));
        assertEquals("consensus", sl.getHashGroupLabel(q1));
        assertEquals("outlier", sl.getHashGroupLabel(q3));
        assertEquals("consensus", sl.getHashGroupLabel(test));
        assertNull(sl.getHashGroupLabel(unknown));
    }

    /**
     * Services that report no hashes are never labelled.
     */
    @Test
    void otherServicesAreNeverLabelled() {
        ServerList sl = TestServers.serverList(TestServers.service("https://nanodash.example.org/", NANODASH_TYPE));
        ServerData nanodash = sl.getServerData(TestServers.service("https://nanodash.example.org/", NANODASH_TYPE));
        nanodash.setTrustStateHash("hash-1");

        assertNull(sl.getHashGroupLabel(nanodash));
    }

    /**
     * A confirmed test instance joins the list, and a missed candidate is not probed again soon.
     */
    @Test
    void confirmedTestInstancesJoinAndMissesWait() {
        ServerList sl = registries("https://found.example.org/", "https://missed.example.org/");
        TestServers.locate("https://test.found.example.org/", ServerIpInfo.empty);
        List<String> probed = new ArrayList<>();
        ServerList.RegistryProbe probe = url -> {
            probed.add(url);
            return url.contains("found");
        };

        assertEquals(1, sl.addTestInstances(probe));
        assertEquals(3, sl.getServerCount());
        assertNotNull(sl.getServerData(TestServers.service("https://test.found.example.org/", REGISTRY_TYPE)));
        assertEquals(2, probed.size());

        assertEquals(0, sl.addTestInstances(probe));
        assertEquals(2, probed.size(), "Neither candidate should be probed again so soon, got " + probed);
    }

    /**
     * The monitor's own location is the one looked up for it.
     */
    @Test
    void theMonitorKnowsWhereItIs() {
        ServerList sl = TestServers.serverList();

        assertTrue(sl.getMonitorIpInfo().hasCoordinates());
    }

    /**
     * A refresh adds the services the source newly announces and keeps the known ones.
     */
    @Test
    void aRefreshAddsNewlyAnnouncedServices() {
        NanopubService first = TestServers.service("https://first.example.org/", REGISTRY_TYPE);
        NanopubService second = TestServers.service("https://second.example.org/", REGISTRY_TYPE);
        TestServers.serverList(first, second);
        List<NanopubService> announced = new ArrayList<>(List.of(first));
        ServerList sl = ServerList.resetTo(() -> announced);
        sl.getServerData(first).reportTestSuccess(5);

        announced.add(second);
        sl.refresh();

        assertEquals(2, sl.getServerCount());
        assertEquals("OK", sl.getServerData(first).getStatusString(), "A known service keeps its data");
    }

    /**
     * A source that fails leaves the list as it was.
     */
    @Test
    void aFailingSourceLeavesTheListAsItWas() {
        ServerList sl = registries("https://a.example.org/");
        ServerList failing = ServerList.resetTo(() -> {
            throw new IllegalStateException("registry API unreachable");
        });

        failing.refresh();

        assertEquals(0, failing.getServerCount());
        assertSame(failing, ServerList.get());
        assertNotNull(sl);
    }

    /**
     * Each row of the services query becomes a service of its type.
     */
    @Test
    void eachRowOfTheServicesQueryBecomesAService() {
        ApiResponse resp = new ApiResponse();
        ApiResponseEntry entry = new ApiResponseEntry();
        entry.add("service", "https://registry.example.org/");
        entry.add("serviceType", REGISTRY_TYPE);
        resp.add(entry);

        assertEquals(List.of(TestServers.service("https://registry.example.org/", REGISTRY_TYPE)),
                ServerList.toServices(resp));
    }

}
