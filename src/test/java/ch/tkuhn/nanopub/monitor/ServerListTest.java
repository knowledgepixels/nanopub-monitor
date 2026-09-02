package ch.tkuhn.nanopub.monitor;

import org.eclipse.rdf4j.model.util.Values;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerListTest {

    private static final String REGISTRY_TYPE = "https://w3id.org/np/o/service/terms/nanopub-registry-1.0";

    private static NanopubService service(String url, String type) {
        return new NanopubService(Values.iri(url), Values.iri(type));
    }

    @Test
    void noCohortHasNoMajority() {
        assertNull(ServerList.majorityKey(Map.of()));
    }

    @Test
    void aSingleReportIsItsOwnMajority() {
        assertEquals("hash-a", ServerList.majorityKey(Map.of("hash-a", 1)));
    }

    @Test
    void theMostReportedValueWins() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("hash-a", 1);
        counts.put("hash-b", 3);
        counts.put("hash-c", 2);

        assertEquals("hash-b", ServerList.majorityKey(counts));
    }

    @Test
    void anEvenSplitHasNoMajority() {
        // Two registries in a cohort, disagreeing: neither is the consensus, and calling
        // either one of them the outlier would come down to map iteration order.
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("hash-a", 1);
        counts.put("hash-b", 1);

        assertNull(ServerList.majorityKey(counts));
    }

    @Test
    void aTieBelowTheLeaderDoesNotHideTheMajority() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("hash-a", 1);
        counts.put("hash-b", 1);
        counts.put("hash-c", 4);

        assertEquals("hash-c", ServerList.majorityKey(counts));
    }

    @Test
    void aTestInstanceLivesUnderTheSameHostWithATestPrefix() {
        assertEquals("https://test.registry.knowledgepixels.com/",
                ServerList.testInstanceUrl("https://registry.knowledgepixels.com/"));
        assertEquals("http://test.example.org:8080/registry/",
                ServerList.testInstanceUrl("http://example.org:8080/registry/"),
                "Scheme, port and path should be carried over");
    }

    @Test
    void aTestInstanceHasNoTestInstanceOfItsOwn() {
        assertNull(ServerList.testInstanceUrl("https://test.registry.knowledgepixels.com/"));
    }

    @Test
    void aUrlWithNoHostHasNoCandidate() {
        assertNull(ServerList.testInstanceUrl("urn:example:registry"));
        assertNull(ServerList.testInstanceUrl("not a url at all"));
    }

    @Test
    void onlyRegistriesGetACandidate() {
        List<NanopubService> candidates = ServerList.testInstanceCandidates(List.of(
                service("https://registry.example.org/", REGISTRY_TYPE),
                service("https://query.example.org/", "https://w3id.org/np/o/service/terms/nanopub-query-1.1"),
                service("https://nanodash.example.org/", "https://w3id.org/np/o/service/terms/nanodash-2.x"),
                service("https://np.example.org/", "https://github.com/tkuhn/nanopub-server#service")));

        assertEquals(1, candidates.size(), "Only the registry should be probed, got " + candidates);
        assertEquals("https://test.registry.example.org/", candidates.getFirst().getServiceIri().stringValue());
    }

    @Test
    void aCandidateKeepsTheServiceTypeOfTheRegistryItMirrors() {
        List<NanopubService> candidates = ServerList.testInstanceCandidates(
                List.of(service("https://registry.example.org/", REGISTRY_TYPE)));

        assertEquals(REGISTRY_TYPE, candidates.getFirst().getTypeIri().stringValue(),
                "The scanner picks its probe by service type, so the candidate needs the registry type");
    }

    @Test
    void aTestInstanceAlreadyInTheListIsNotProposedAgain() {
        List<NanopubService> candidates = ServerList.testInstanceCandidates(List.of(
                service("https://registry.example.org/", REGISTRY_TYPE),
                service("https://test.registry.example.org/", REGISTRY_TYPE)));

        assertTrue(candidates.isEmpty(),
                "The test instance is already monitored, and is itself no candidate, got " + candidates);
    }

    @Test
    void twoRegistriesOnTheSameHostProposeOneCandidate() {
        // Same host under two service type versions: the derived URL is the same, and probing
        // it twice in one pass would be wasted.
        List<NanopubService> candidates = ServerList.testInstanceCandidates(List.of(
                service("https://registry.example.org/", REGISTRY_TYPE),
                service("https://registry.example.org/", "https://w3id.org/np/o/service/terms/nanopub-registry-1.1")));

        assertEquals(1, candidates.size(), "Expected one candidate, got " + candidates);
    }

}
