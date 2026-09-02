package ch.tkuhn.nanopub.monitor;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerIpInfoTest {


    private final String testJsonString = "{\n" +
                                          "        \"lat\": 12.3456,\n" +
                                          "            \"lon\": -78.9012,\n" +
                                          "            \"continent\": \"Continent7\",\n" +
                                          "            \"country\": \"Country42\",\n" +
                                          "            \"city\": \"City17\",\n" +
                                          "            \"query\": \"192.168.45.123\"\n" +
                                          "    }";

    // What ip-api actually answers for 8.8.8.8, including the two fields whose names suggest
    // they are the region asked for and are not.
    private final String ipApiJsonString = "{\n" +
                                           "        \"status\": \"success\",\n" +
                                           "            \"continent\": \"North America\",\n" +
                                           "            \"continentCode\": \"NA\",\n" +
                                           "            \"country\": \"United States\",\n" +
                                           "            \"region\": \"VA\",\n" +
                                           "            \"regionName\": \"Virginia\",\n" +
                                           "            \"city\": \"Ashburn\",\n" +
                                           "            \"lat\": 39.03,\n" +
                                           "            \"lon\": -77.5,\n" +
                                           "            \"query\": \"8.8.8.8\"\n" +
                                           "    }";

    @Test
    void empty() {
        ServerIpInfo ipInfo = ServerIpInfo.empty;
        assertNull(ipInfo.getLatitude());
        assertNull(ipInfo.getLongitude());
        assertEquals("unknown country", ipInfo.getCountryName());
        assertEquals("unknown city", ipInfo.getCity());
        assertEquals("unknown region", ipInfo.getRegionName());
        assertEquals("unknown IP", ipInfo.getIp());
    }

    @Test
    void fromJson() {
        ServerIpInfo serverIpInfo = new Gson().fromJson(testJsonString, ServerIpInfo.class);
        assertEquals(12.3456, serverIpInfo.getLatitude());
        assertEquals(-78.9012, serverIpInfo.getLongitude());
        assertEquals("Country42", serverIpInfo.getCountryName());
        assertEquals("City17", serverIpInfo.getCity());
        assertEquals("Continent7", serverIpInfo.getRegionName());
        assertEquals("192.168.45.123", serverIpInfo.getIp());
    }

    @Test
    void theRegionIsTheContinentAndNotTheSubdivision() {
        ServerIpInfo serverIpInfo = new Gson().fromJson(ipApiJsonString, ServerIpInfo.class);

        assertEquals("North America", serverIpInfo.getRegionName(),
                "ip-api's 'region'/'regionName' are the subdivision within the country, not the part of the world");
        assertEquals("United States", serverIpInfo.getCountryName());
        assertEquals("Ashburn", serverIpInfo.getCity());
    }

    @Test
    void aResponseWithoutAContinentLeavesTheRegionUnknown() {
        // ip-api omits the continent unless it is asked for, which is what the request in
        // ServerData does; an older cached response, or a lookup that failed, has none.
        ServerIpInfo serverIpInfo = new Gson().fromJson("{\"country\": \"Country42\"}", ServerIpInfo.class);

        assertEquals("unknown region", serverIpInfo.getRegionName());
    }

    @Test
    void theGeoipRequestAsksForEveryFieldThisClassReads() {
        String url = ServerData.ipApiUrl("example.org");

        assertTrue(url.startsWith("http://ip-api.com/json/example.org?"), "Unexpected URL: " + url);
        // Naming any field at all makes ip-api drop the ones not named, so a missing entry here
        // silently degrades to the defaults above rather than failing.
        for (String field : new String[]{"continent", "country", "city", "lat", "lon", "query"}) {
            assertTrue(url.contains(field), "The request should ask for '" + field + "': " + url);
        }
    }

}