package ch.tkuhn.nanopub.monitor;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ServerListTest {

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

}
