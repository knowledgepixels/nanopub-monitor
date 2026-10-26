package ch.tkuhn.nanopub.monitor;

import com.opencsv.CSVReader;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Checks the CSV export of the server table.
 */
class CsvTableTest {

    private static final String REGISTRY_TYPE = "https://w3id.org/np/o/service/terms/nanopub-registry-1.0";
    private static final String NANODASH_TYPE = "https://w3id.org/np/o/service/terms/nanodash-2.x";

    /**
     * Export the server list and read the CSV back.
     *
     * @param sl the server list
     * @return the rows, the header first
     * @throws Exception if the CSV cannot be read
     */
    private static List<String[]> export(ServerList sl) throws Exception {
        try (CSVReader reader = new CSVReader(new StringReader(CsvTable.toCsv(sl)))) {
            return reader.readAll();
        }
    }

    /**
     * An empty list exports the header alone.
     *
     * @throws Exception if the CSV cannot be read
     */
    @Test
    void anEmptyListExportsTheHeaderAlone() throws Exception {
        List<String[]> rows = export(TestServers.serverList());

        assertEquals(1, rows.size());
        assertArrayEquals(new String[]{"URL", "Type", "Version", "Test Instance", "Status", "Current Setting",
                "Original Setting", "Trust State Hash", "Hash Group", "Loaded Nanopub Checksum", "Nanopub Count",
                "Registry Nanopub Count", "Sync Lag", "Loader Age (s)", "OK Ratio", "Resp Time", "Dist",
                "Last Seen OK", "IP Address", "Region", "Server Location"}, rows.get(0));
    }

    /**
     * A tested registry exports everything it reported.
     *
     * @throws Exception if the CSV cannot be read
     */
    @Test
    void aTestedRegistryExportsWhatItReported() throws Exception {
        TestServers.locate("https://registry.helsinki.example.org/", TestServers.HELSINKI);
        NanopubService service = TestServers.service("https://registry.helsinki.example.org/", REGISTRY_TYPE);
        ServerList sl = TestServers.serverList(service);
        ServerData sd = sl.getServerData(service);
        sd.setVersion("1.4.0");
        sd.setTestInstance(true);
        sd.setCurrentSetting("RAcurrent");
        sd.setOriginalSetting("RAoriginal");
        sd.setTrustStateHash("hash-1");
        sd.setNanopubCount(42L);
        sd.reportTestSuccess(30);

        String[] row = export(sl).get(1);

        assertArrayEquals(new String[]{"https://registry.helsinki.example.org/", REGISTRY_TYPE, "1.4.0", "true",
                "OK", "RAcurrent", "RAoriginal", "hash-1", "consensus", "", "42", "", "", "", "1.0", "30",
                String.valueOf(sd.getDistanceInKm()), MonitorPage.formatDate(sd.getLastSeenDate()), "192.0.2.20",
                "Europe", "Helsinki, Finland"}, row);
    }

    /**
     * A service never tested exports empty cells where it reported nothing.
     *
     * @throws Exception if the CSV cannot be read
     */
    @Test
    void aServiceNeverTestedExportsEmptyCells() throws Exception {
        ServerList sl = TestServers.serverList(TestServers.service("https://nanodash.example.org/", NANODASH_TYPE));

        String[] row = export(sl).get(1);

        assertArrayEquals(new String[]{"https://nanodash.example.org/", NANODASH_TYPE, "", "", "NOT SEEN", "", "",
                "", "", "", "", "", "", "", "", "", "", "", "unknown IP", "unknown region",
                "unknown city, unknown country"}, row);
    }

    /**
     * The singleton serves the export as a resource.
     */
    @Test
    void theSingletonServesTheExport() {
        TestServers.serverList();

        assertSame(CsvTable.instance(), CsvTable.instance());
        assertNotNull(CsvTable.instance().get());
    }

}
