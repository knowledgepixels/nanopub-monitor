package ch.tkuhn.nanopub.monitor;

import com.google.gson.Gson;
import org.eclipse.rdf4j.model.util.Values;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapDataTest {

    private static final String HELSINKI_JSON = "{\"lat\": 60.1699, \"lon\": 24.9384, \"continent\": \"Europe\", "
                                                + "\"country\": \"Finland\", \"city\": \"Helsinki\", \"query\": \"95.217.140.6\"}";

    private static ServerIpInfo helsinki() {
        return new Gson().fromJson(HELSINKI_JSON, ServerIpInfo.class);
    }

    private static NanopubService router() {
        return new NanopubService(Values.iri("https://router.example.org/"), NanopubService.NANOPUB_ROUTER_TYPE_IRI);
    }

    @Test
    void hasCoordinatesWhenLatitudeAndLongitudeAreKnown() {
        assertTrue(MapData.hasCoordinates(helsinki()));
    }

    @Test
    void hasNoCoordinatesForEmptyOrMissingLocation() {
        assertFalse(MapData.hasCoordinates(ServerIpInfo.empty));
        assertFalse(MapData.hasCoordinates(null));
    }

    @Test
    void toPointCarriesWhatTheMapDrawsAndDescribes() {
        Map<String, Object> point = MapData.toPoint(router(), "OK", "0.3.0", true, helsinki());

        assertEquals("https://router.example.org/", point.get("url"));
        assertEquals("nanopub-router", point.get("typeLabel"));
        assertEquals("#dab40b", point.get("color"));
        assertEquals(0, point.get("offsetX"));
        assertEquals(-3, point.get("offsetY"));
        assertEquals("OK", point.get("status"));
        assertEquals("0.3.0", point.get("version"));
        assertEquals(true, point.get("testInstance"));
        assertEquals(60.1699, point.get("lat"));
        assertEquals(24.9384, point.get("lon"));
        assertEquals("Helsinki", point.get("city"));
        assertEquals("Finland", point.get("country"));
        assertEquals("Europe", point.get("region"));
    }

    @Test
    void toPointKeepsAnUnknownVersion() {
        Map<String, Object> point = MapData.toPoint(router(), "DOWN", null, false, helsinki());

        assertTrue(point.containsKey("version"));
        assertEquals(null, point.get("version"));
    }

    @Test
    void collectPointsOfNoServersIsEmpty() {
        assertEquals(List.of(), MapData.collectPoints(List.of()));
    }

    /**
     * Only the servers whose location is known become points.
     */
    @Test
    void collectPointsSkipsServersWithoutALocation() {
        TestServers.locate("https://router.helsinki.example.org/", TestServers.HELSINKI);
        NanopubService located = new NanopubService(Values.iri("https://router.helsinki.example.org/"),
                NanopubService.NANOPUB_ROUTER_TYPE_IRI);
        ServerList sl = TestServers.serverList(located, router());

        List<Map<String, Object>> points = MapData.collectPoints(sl.getSortedServerData());

        assertEquals(1, points.size());
        assertEquals("https://router.helsinki.example.org/", points.get(0).get("url"));
    }

    /**
     * The singleton serves the points as a resource.
     */
    @Test
    void theSingletonServesThePoints() {
        TestServers.serverList(router());

        assertSame(MapData.instance(), MapData.instance());
        assertNotNull(MapData.instance().get());
    }

}
