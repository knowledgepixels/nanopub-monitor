package ch.tkuhn.nanopub.monitor;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerDataDistanceTest {

    private static ServerIpInfo location(double lat, double lon) {
        return new Gson().fromJson("{\"lat\": " + lat + ", \"lon\": " + lon + "}", ServerIpInfo.class);
    }

    @Test
    void distanceIsUnknownWhenTheMonitorHasNoCoordinates() {
        assertNull(ServerData.distanceInKm(location(60.17, 24.94), ServerIpInfo.empty));
        assertNull(ServerData.distanceInKm(location(60.17, 24.94), null));
    }

    @Test
    void distanceIsUnknownWhenTheServerHasNoCoordinates() {
        assertNull(ServerData.distanceInKm(ServerIpInfo.empty, location(60.17, 24.94)));
        assertNull(ServerData.distanceInKm(null, location(60.17, 24.94)));
    }

    @Test
    void distanceBetweenKnownLocations() {
        assertEquals(0, ServerData.distanceInKm(location(60.17, 24.94), location(60.17, 24.94)));
        Integer helsinkiToNuremberg = ServerData.distanceInKm(location(60.17, 24.94), location(49.45, 11.08));
        assertTrue(helsinkiToNuremberg > 1400 && helsinkiToNuremberg < 1500, "got " + helsinkiToNuremberg);
    }

}
