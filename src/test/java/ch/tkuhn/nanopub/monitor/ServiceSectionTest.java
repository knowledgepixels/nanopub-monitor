package ch.tkuhn.nanopub.monitor;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceSectionTest {

    @Test
    void aHealthyInstanceNeedsNoAttention() {
        assertFalse(ServiceSection.needsAttention("OK", "consensus", false));
        assertFalse(ServiceSection.needsAttention("OK", null, false));
    }

    @Test
    void anInstanceThatIsNotOkNeedsAttention() {
        assertTrue(ServiceSection.needsAttention("DOWN", null, false));
        assertTrue(ServiceSection.needsAttention("NOT SEEN", null, false));
    }

    @Test
    void anOutlierNeedsAttentionEvenWhenOk() {
        assertTrue(ServiceSection.needsAttention("OK", "outlier", false));
    }

    @Test
    void anInstanceBehindItsRegistryNeedsAttention() {
        assertTrue(ServiceSection.needsAttention("OK", "consensus", true));
    }

    @Test
    void anchorsAreLowerCaseWithHyphens() {
        assertEquals("np-ldf-server", ServiceSection.toAnchor("np-ldf-server"));
        assertEquals("service", ServiceSection.toAnchor("#Service"));
        assertEquals("grlc-np-api", ServiceSection.toAnchor("grlc np_api"));
    }

    @Test
    void titlesDropTheGenericServiceFragment() {
        assertEquals("nanopub-server", ServiceSection.toTitle("nanopub-server#service"));
        assertEquals("signed-nanopub-services / np-sparql-api", ServiceSection.toTitle("signed-nanopub-services#np-sparql-api"));
    }

    /**
     * Build the sections of the services the server list tracks.
     *
     * @param sl the server list
     * @return the sections
     */
    private static List<ServiceSection> sectionsOf(ServerList sl) {
        return ServiceSection.of(sl.getSortedServerData(), sl);
    }

    /**
     * Sections follow the order of the service kinds, the other types last and by label.
     */
    @Test
    void sectionsFollowTheOrderOfTheKinds() {
        ServerList sl = TestServers.serverList(
                TestServers.service("https://ldf.example.org/", NanopubService.LDF_SERVICE_TYPE_IRI),
                TestServers.service("https://nanodash.example.org/", "https://w3id.org/np/o/service/terms/nanodash-2.x"),
                TestServers.service("https://registry.example.org/", "https://w3id.org/np/o/service/terms/nanopub-registry-1.0"),
                TestServers.service("https://grlc.example.org/", NanopubService.GRLC_SERVICE_TYPE_IRI));

        List<String> titles = sectionsOf(sl).stream().map(ServiceSection::getTitle).toList();

        assertEquals(List.of("Registry", "Nanodash", "nanopub-services / grlc-np-api", "nanopub-services / np-ldf-server"), titles);
    }

    /**
     * A section of another type takes its title and anchor from the type label.
     */
    @Test
    void aSectionOfAnotherTypeIsNamedAfterIt() {
        ServerList sl = TestServers.serverList(
                TestServers.service("https://np.example.org/", NanopubService.NANOPUB_SERVER_TYPE_IRI));

        ServiceSection section = sectionsOf(sl).get(0);

        assertEquals("nanopub-server", section.getTitle());
        assertEquals("nanopub-server-service", section.getAnchor());
        assertEquals("orange", section.getColor());
    }

    /**
     * Instances needing attention come first and are counted, and the OK ones are counted too.
     */
    @Test
    void instancesNeedingAttentionComeFirst() {
        String type = "https://w3id.org/np/o/service/terms/nanopub-registry-1.0";
        NanopubService healthy = TestServers.service("https://a.example.org/", type);
        NanopubService down = TestServers.service("https://b.example.org/", type);
        ServerList sl = TestServers.serverList(healthy, down);
        sl.getServerData(healthy).reportTestSuccess(10);
        sl.getServerData(down).reportTestFailure("DOWN");

        ServiceSection section = sectionsOf(sl).get(0);

        assertEquals("Registry", section.getTitle());
        assertEquals("nanopub-registry", section.getAnchor());
        assertEquals(List.of("https://b.example.org/", "https://a.example.org/"),
                section.getServers().stream().map(ServerData::getServiceId).toList());
        assertEquals(1, section.getOkCount());
        assertEquals(1, section.getAttentionCount());
        assertTrue(section.shows(ServiceKind.Column.TRUST_STATE));
        assertFalse(section.shows(ServiceKind.Column.SYNC));
    }

    /**
     * Two versions of a type share one section, which names them both.
     */
    @Test
    void twoVersionsOfATypeShareASection() {
        ServerList sl = TestServers.serverList(
                TestServers.service("https://a.example.org/", "https://w3id.org/np/o/service/terms/nanopub-registry-1.1"),
                TestServers.service("https://b.example.org/", "https://w3id.org/np/o/service/terms/nanopub-registry-1.0"));

        List<ServiceSection> sections = sectionsOf(sl);

        assertEquals(1, sections.size());
        assertTrue(sections.get(0).hasSeveralTypes());
        assertEquals(List.of("nanopub-registry-1.0", "nanopub-registry-1.1"),
                sections.get(0).getTypes().stream().map(NanopubService::getTypeLabel).toList());
    }

    /**
     * A section of a kind without a nanopub count shows one once an instance reports it.
     */
    @Test
    void aReportedNanopubCountAddsItsColumn() {
        NanopubService server = TestServers.service("https://np.example.org/", NanopubService.NANOPUB_SERVER_TYPE_IRI);
        ServerList sl = TestServers.serverList(server);

        assertFalse(sectionsOf(sl).get(0).shows(ServiceKind.Column.NANOPUB_COUNT));
        assertFalse(sectionsOf(sl).get(0).hasSeveralTypes());

        sl.getServerData(server).setNanopubCount(5L);

        assertTrue(sectionsOf(sl).get(0).shows(ServiceKind.Column.NANOPUB_COUNT));
    }

}
