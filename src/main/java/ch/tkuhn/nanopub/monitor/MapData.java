package ch.tkuhn.nanopub.monitor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.wicket.request.resource.ByteArrayResource;
import org.apache.wicket.request.resource.IResource;
import org.danekja.java.util.function.serializable.SerializableSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides the points drawn on the server map as JSON: one point per service whose location is known,
 * with what the map needs to place, colour and describe it. The map page polls this resource to keep
 * the markers in step with the table.
 */
public class MapData implements SerializableSupplier<IResource> {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(MapData.class);
    private static final Gson gson = new GsonBuilder().serializeNulls().create();
    private static final MapData instance = new MapData();

    /**
     * Get the singleton instance of MapData.
     *
     * @return the MapData instance
     */
    public static MapData instance() {
        return instance;
    }

    private MapData() {
    }

    /**
     * Generates a JSON resource with the current map points, or with no points when the map is disabled.
     *
     * @return an IResource representing the map points
     */
    @Override
    public IResource get() {
        List<Map<String, Object>> points = MonitorConf.get().showMap()
                ? collectPoints(ServerList.get().getSortedServerData())
                : List.of();
        logger.debug("Generated map data with {} points", points.size());
        byte[] body = gson.toJson(Map.of("points", points)).getBytes(StandardCharsets.UTF_8);
        return new ByteArrayResource("application/json", body);
    }

    /**
     * Build the map points for the given servers, skipping those whose location is unknown.
     *
     * @param servers the servers to place on the map
     * @return one point per server with known coordinates, in the order given
     */
    static List<Map<String, Object>> collectPoints(Collection<ServerData> servers) {
        List<Map<String, Object>> points = new ArrayList<>();
        for (ServerData sd : servers) {
            ServerIpInfo ipInfo = sd.getIpInfo();
            if (hasCoordinates(ipInfo)) {
                points.add(toPoint(sd.getService(), sd.getStatusString(), sd.getVersion(), sd.isTestInstance(), ipInfo));
            }
        }
        return points;
    }

    /**
     * Check whether the location info carries coordinates the map can place.
     *
     * @param ipInfo the location info (might be null)
     * @return true if both latitude and longitude are known
     */
    static boolean hasCoordinates(ServerIpInfo ipInfo) {
        return ipInfo != null && ipInfo.getLatitude() != null && ipInfo.getLongitude() != null;
    }

    /**
     * Build a single map point.
     *
     * @param service      the service the point stands for
     * @param status       the service's current status, e.g. "OK" or "DOWN"
     * @param version      the service's version (might be null)
     * @param testInstance whether the service is a test instance
     * @param ipInfo       the service's location info, with coordinates
     * @return the point as an ordered map, ready to be serialized
     */
    static Map<String, Object> toPoint(NanopubService service, String status, String version, boolean testInstance, ServerIpInfo ipInfo) {
        Map<String, Object> point = new LinkedHashMap<>();
        point.put("url", service.getServiceIri().stringValue());
        point.put("typeLabel", service.getTypeLabel());
        point.put("color", service.getMapColor());
        point.put("offsetX", service.getMapOffsetX());
        point.put("offsetY", service.getMapOffsetY());
        point.put("status", status);
        point.put("version", version);
        point.put("testInstance", testInstance);
        point.put("lat", ipInfo.getLatitude());
        point.put("lon", ipInfo.getLongitude());
        point.put("city", ipInfo.getCity());
        point.put("country", ipInfo.getCountryName());
        point.put("region", ipInfo.getRegionName());
        return point;
    }

}
