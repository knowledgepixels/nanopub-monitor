package ch.tkuhn.nanopub.monitor;

import com.google.gson.Gson;
import org.eclipse.rdf4j.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Data about a nanopublication server, including its service description, IP-based geolocation info,
 * and monitoring statistics.
 */
public class ServerData implements Serializable {

    private static final long serialVersionUID = 1383338443824756632L;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private NanopubService service;
    private Object info;
    private ServerIpInfo ipInfo;
    private long lastIpInfoRetrieval;
    private Date lastSeenOk;
    private String status = "NOT SEEN";
    private String distanceString = null;
    private long totalResponseTime = 0;

    int countSuccess = 0;
    int countFailure = 0;

    /**
     * Creates a new ServerData instance for the given nanopublication service and optional server info.
     *
     * @param service the nanopublication service
     * @param info    optional additional server info (may be null)
     */
    public ServerData(NanopubService service, Object info) {
        this.service = service;
        update(info);
        getIpInfo();
    }

    /**
     * Update the server info and ensure IP info is loaded.
     *
     * @param info new server info (may be null)
     */
    public void update(Object info) {
        if (info != null) {
            this.info = info;
        }
        ensureIpInfoLoaded();
    }

    private void ensureIpInfoLoaded() {
        if (ipInfo != null && ipInfo != ServerIpInfo.empty) {
            // already loaded
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastIpInfoRetrieval > 1000 * 60 * 10) {
            // retry every 10 minutes
            loadIpInfo();
        }
    }

    /**
     * Get the nanopublication service associated with this server data.
     *
     * @return the nanopublication service
     */
    public NanopubService getService() {
        return service;
    }

    /**
     * Get the service ID (IRI string) of the nanopublication service.
     *
     * @return the service ID as a string
     */
    public String getServiceId() {
        return service.getServiceIri().stringValue();
    }

    /**
     * Check if the service has the specified type.
     *
     * @param type the IRI of the service type to check
     * @return true if the service has the specified type, false otherwise
     */
    public boolean hasServiceType(IRI type) {
        return service.getTypeIri().equals(type);
    }

    /**
     * Get additional server info.
     *
     * @return the server info object (may be null)
     */
    public Object getServerInfo() {
        return info;
    }

    /**
     * Get the IP-based geolocation info of the server.
     *
     * @return the ServerIpInfo object (never null; may be empty)
     */
    public ServerIpInfo getIpInfo() {
        if (ipInfo == null) {
            loadIpInfo();
        }
        return ipInfo;
    }

    private void loadIpInfo() {
        lastIpInfoRetrieval = System.currentTimeMillis();
        try {
            ipInfo = fetchIpInfo(new URI(service.getServiceIri().stringValue()).getHost());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            if (ipInfo == null) {
                ipInfo = ServerIpInfo.empty;
            }
        }
    }

    /**
     * Get the date when the server was last seen as OK.
     *
     * @return the last seen OK date (may be null if never seen OK)
     */
    public Date getLastSeenDate() {
        return lastSeenOk;
    }

    /**
     * Report a test failure with the given message.
     *
     * @param message the failure message
     */
    public void reportTestFailure(String message) {
        status = message;
        countFailure++;
        logger.info("Test result: {} {}", service.getServiceIri(), getStatusString());
    }

    /**
     * Report a test success with the given response time in milliseconds.
     *
     * @param responseTime the response time in milliseconds
     */
    public void reportTestSuccess(long responseTime) {
        lastSeenOk = new Date();
        status = "OK";
        totalResponseTime += responseTime;
        countSuccess++;
        logger.info("Test result: {} {} {}ms", service.getServiceIri(), getStatusString(), responseTime);
    }

    /**
     * Get the current status string of the server.
     *
     * @return the status string
     */
    public String getStatusString() {
        return status;
    }

    /**
     * Get the average response time in milliseconds, or null if there are no successful tests.
     *
     * @return the average response time in milliseconds, or null
     */
    public Integer getAvgResponseTimeInMs() {
        if (countSuccess == 0) return null;
        return (int) (totalResponseTime / (float) countSuccess);
    }

    /**
     * Get the average response time as a formatted string, or "?" if unknown.
     *
     * @return the average response time string
     */
    public String getAvgResponseTimeString() {
        Integer respTime = getAvgResponseTimeInMs();
        if (respTime == null) {
            return "?";
        } else {
            return respTime + " ms";
        }
    }

    /**
     * Get the success ratio as a float between 0 and 1, or null if there are no tests.
     *
     * @return the success ratio, or null
     */
    public Float getSuccessRatio() {
        if (countSuccess + countFailure > 0) {
            return (float) countSuccess / (countSuccess + countFailure);
        } else {
            return null;
        }
    }

    /**
     * Get the success ratio as a percentage string, or "?" if unknown.
     *
     * @return the success ratio string
     */
    public String getSuccessRatioString() {
        if (countSuccess + countFailure > 0) {
            return (((float) countSuccess / (countSuccess + countFailure)) * 100) + "%";
        } else {
            return "?";
        }
    }

    /**
     * Get the distance from the monitor to the server as a formatted string, or "?" if unknown.
     *
     * @return the distance string
     */
    public String getDistanceString() {
        if (distanceString == null) {
            Integer distKm = getDistanceInKm();
            if (distKm == null) {
                distanceString = "?";
            } else {
                distanceString = distKm + " km";
            }
        }
        return distanceString;
    }

    /**
     * Get the distance from the monitor to the server in kilometers, or null if unknown.
     *
     * @return the distance in kilometers, or null
     */
    public Integer getDistanceInKm() {
        ServerIpInfo monitorIpInfo = ServerList.get().getMonitorIpInfo();
        if (monitorIpInfo == null) return null;
        ServerIpInfo serverIpInfo = getIpInfo();
        Double sLat = serverIpInfo.getLatitude();
        Double sLng = serverIpInfo.getLongitude();
        if (sLat == null || sLng == null) return null;
        return (int) calculateDistance(sLat, sLng, monitorIpInfo.getLatitude(), monitorIpInfo.getLongitude());
    }

    private static Map<String, ServerIpInfo> ipInfoMap = new HashMap<>();

    /**
     * Fetch IP-based geolocation info for the given host using the ip-api.com service.
     *
     * @param host the hostname or IP address to look up
     * @return the ServerIpInfo object with geolocation data
     * @throws IOException        if an I/O error occurs
     * @throws URISyntaxException if the constructed URI is invalid
     */
    public static ServerIpInfo fetchIpInfo(String host) throws IOException, URISyntaxException {
        if (!MonitorConf.get().isGeoIpInfoEnabled()) return ServerIpInfo.empty;
        if (ipInfoMap.containsKey(host)) return ipInfoMap.get(host);
        ServerIpInfo serverIpInfo = null;
        URL geoipUrl = new URI("http://ip-api.com/json/" + host).toURL();
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) geoipUrl.openConnection();
            con.setConnectTimeout(10000);
            con.setReadTimeout(10000);
            serverIpInfo = new Gson().fromJson(new InputStreamReader(con.getInputStream()), ServerIpInfo.class);
            ipInfoMap.put(host, serverIpInfo);
        } finally {
            if (con != null) con.disconnect();
        }
        return serverIpInfo;
    }

    private static double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371.0;
        double latD = Math.toRadians(lat2 - lat1);
        double lngD = Math.toRadians(lng2 - lng1);
        double sinLatD = Math.sin(latD / 2);
        double sinLngD = Math.sin(lngD / 2);
        double a = Math.pow(sinLatD, 2) + Math.pow(sinLngD, 2) * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

}
