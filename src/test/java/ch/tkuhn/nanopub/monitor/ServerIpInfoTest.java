package ch.tkuhn.nanopub.monitor;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ServerIpInfoTest {


    private final String testJsonString = "{\n" +
                                          "        \"lat\": 12.3456,\n" +
                                          "            \"lon\": -78.9012,\n" +
                                          "            \"country\": \"Country42\",\n" +
                                          "            \"city\": \"City17\",\n" +
                                          "            \"query\": \"192.168.45.123\"\n" +
                                          "    }";

    @Test
    void empty() {
        ServerIpInfo ipInfo = ServerIpInfo.empty;
        assertNull(ipInfo.getLatitude());
        assertNull(ipInfo.getLongitude());
        assertEquals("unknown country", ipInfo.getCountryName());
        assertEquals("unknown city", ipInfo.getCity());
        assertEquals("unknown IP", ipInfo.getIp());
    }

    @Test
    void fromJson() {
        ServerIpInfo serverIpInfo = new Gson().fromJson(testJsonString, ServerIpInfo.class);
        assertEquals(12.3456, serverIpInfo.getLatitude());
        assertEquals(-78.9012, serverIpInfo.getLongitude());
        assertEquals("Country42", serverIpInfo.getCountryName());
        assertEquals("City17", serverIpInfo.getCity());
        assertEquals("192.168.45.123", serverIpInfo.getIp());
    }

}