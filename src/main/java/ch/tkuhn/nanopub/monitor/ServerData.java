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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data about a nanopublication server, including its service description, IP-based geolocation info,
 * and monitoring statistics.
 */
public class ServerData implements Serializable {

    private static final long serialVersionUID = 1383338443824756632L;

    private static final Logger logger = LoggerFactory.getLogger(ServerData.class);

    private NanopubService service;
    private Object info;
    private ServerIpInfo ipInfo;
    private long lastIpInfoRetrieval;
    private Date lastSeenOk;
    private String status = "NOT SEEN";
    private String distanceString = null;
    private long totalResponseTime = 0;
    private String trustStateHash;
    private String loadedNanopubChecksum;
    private String currentSetting;
    private String originalSetting;
    private Long nanopubCount;
    private Long registryNanopubCount;
    private Long loaderLastSuccessAgeSeconds;
    private Long behindSinceMs;
    private boolean testInstance;
    private String version;

    int countSuccess = 0;
    int countFailure = 0;

    /**
     * Query ip-api.com for a given host at most once per this interval (10 minutes).
     */
    private static final long IP_INFO_TTL_MS = 10 * 60 * 1000;

    /**
     * Creates a new ServerData instance for the given nanopublication service and optional server info.
     *
     * @param service the nanopublication service
     * @param info    optional additional server info (might be null)
     */
    public ServerData(NanopubService service, Object info) {
        this.service = service;
        update(info);
    }

    /**
     * Update the server info and ensure IP info is loaded.
     *
     * @param info new server info (might be null)
     */
    public void update(Object info) {
        if (info != null) {
            this.info = info;
        }
        ensureIpInfoLoaded();
    }

    private void ensureIpInfoLoaded() {
        if (ipInfo == null || System.currentTimeMillis() - lastIpInfoRetrieval >= IP_INFO_TTL_MS) {
            // (re)load at most every 10 minutes; the actual ip-api.com call is throttled in fetchIpInfo
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
     * Check if the service type IRI starts with the given prefix IRI.
     * Used to match versioned service type IRIs (e.g. {@code nanopub-registry-1.0}
     * against the unversioned {@code nanopub-registry} prefix).
     *
     * @param prefix the prefix IRI
     * @return true if the service type starts with the prefix, false otherwise
     */
    public boolean hasServiceTypePrefix(IRI prefix) {
        return service.getTypeIri().stringValue().startsWith(prefix.stringValue());
    }

    /**
     * Get additional server info.
     *
     * @return the server info object (might be null)
     */
    public Object getServerInfo() {
        return info;
    }

    /**
     * Get the IP-based geolocation info of the server.
     *
     * @return the ServerIpInfo object (never null; might be empty)
     */
    public ServerIpInfo getIpInfo() {
        ensureIpInfoLoaded();
        return ipInfo;
    }

    private void loadIpInfo() {
        lastIpInfoRetrieval = System.currentTimeMillis();
        try {
            ipInfo = fetchIpInfo(new URI(service.getServiceIri().stringValue()).getHost());
        } catch (Exception ex) {
            logger.error("Could not load IP info for server '{}'", getServiceId(), ex);
            if (ipInfo == null) {
                ipInfo = ServerIpInfo.empty;
            }
        }
    }

    /**
     * Get the date when the server was last seen as OK.
     *
     * @return the last seen OK date (might be null if never seen OK)
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
        if (countSuccess == 0) {
            return null;
        }
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
     * Get the Nanopub-Registry trust state hash last reported by this server, or null if unknown
     * (e.g. for non-registry types, or if the server has not yet been scanned).
     *
     * @return the trust state hash, or null
     */
    public String getTrustStateHash() {
        return trustStateHash;
    }

    /**
     * Set the Nanopub-Registry trust state hash for this server.
     *
     * @param hash the trust state hash (might be null)
     */
    public void setTrustStateHash(String hash) {
        this.trustStateHash = hash;
    }

    /**
     * Get a short prefix of the trust state hash suitable for display, or "" if unknown.
     *
     * @return the short hash, or ""
     */
    public String getTrustStateHashShort() {
        return shortHash(trustStateHash);
    }

    /**
     * Get the checksum of loaded nanopubs last reported by this server, or null if unknown
     * (e.g. for non-query types, or if the server has not yet been scanned). Reported by
     * nanopub-query instances via the {@code Nanopub-Query-Loaded-Nanopub-Checksum} header.
     *
     * @return the loaded-nanopub checksum, or null
     */
    public String getLoadedNanopubChecksum() {
        return loadedNanopubChecksum;
    }

    /**
     * Set the checksum of loaded nanopubs for this server.
     *
     * @param checksum the loaded-nanopub checksum (might be null)
     */
    public void setLoadedNanopubChecksum(String checksum) {
        this.loadedNanopubChecksum = checksum;
    }

    /**
     * Get a short prefix of the loaded-nanopub checksum suitable for display, or "" if unknown.
     *
     * @return the short checksum, or ""
     */
    public String getLoadedNanopubChecksumShort() {
        return shortHash(loadedNanopubChecksum);
    }

    /**
     * Short prefix of a hash/checksum string for compact display, or "" if null.
     *
     * @param hash the hash or checksum
     * @return the first 12 characters, the whole value if shorter, or ""
     */
    private static String shortHash(String hash) {
        if (hash == null) {
            return "";
        }
        return hash.length() < 12 ? hash : hash.substring(0, 12);
    }

    /**
     * Get the trusty URI of the registry's current setting, or null if unknown.
     *
     * @return the current setting, or null
     */
    public String getCurrentSetting() {
        return currentSetting;
    }

    /**
     * Set the current setting trusty URI.
     *
     * @param setting the setting URI (might be null)
     */
    public void setCurrentSetting(String setting) {
        this.currentSetting = setting;
    }

    /**
     * Get the trusty URI of the registry's original setting (the one it was initialized with),
     * or null if unknown.
     *
     * @return the original setting, or null
     */
    public String getOriginalSetting() {
        return originalSetting;
    }

    /**
     * Set the original setting trusty URI.
     *
     * @param setting the setting URI (might be null)
     */
    public void setOriginalSetting(String setting) {
        this.originalSetting = setting;
    }

    /**
     * Short prefix of a setting URI for compact display, or "" if null.
     *
     * @param setting the setting URI
     * @return the short prefix, or ""
     */
    public static String shortSetting(String setting) {
        if (setting == null) {
            return "";
        }
        return setting.length() > 10 ? setting.substring(0, 10) : setting;
    }

    /**
     * Get the nanopub count last reported by this server, or null if unknown.
     *
     * @return the nanopub count, or null
     */
    public Long getNanopubCount() {
        return nanopubCount;
    }

    /**
     * Set the nanopub count for this server.
     *
     * @param count the nanopub count (might be null)
     */
    public void setNanopubCount(Long count) {
        this.nanopubCount = count;
    }

    /**
     * Get the nanopub count formatted with thousand separators, or "" if unknown.
     *
     * @return the formatted count, or ""
     */
    public String getNanopubCountString() {
        if (nanopubCount == null) {
            return "";
        }
        return String.format("%,d", nanopubCount);
    }

    /**
     * Record the sync-health headers from a nanopub-query scan.
     *
     * <p>Also maintains the "behind since" mark that {@link #isSyncStalled()} reads, so a
     * momentary lag between an instance's registry poll and its batch landing is not
     * reported as a stall. The mark clears as soon as the instance is level again.
     *
     * @param registryCount    nanopubs the instance's own registry reports holding, or null
     * @param loaderAgeSeconds seconds since the instance's loader last completed a tick, or
     *                         null when the instance does not report it
     */
    public void updateSyncHealth(Long registryCount, Long loaderAgeSeconds) {
        this.registryNanopubCount = registryCount;
        this.loaderLastSuccessAgeSeconds = loaderAgeSeconds;
        Long lag = getSyncLag();
        if (lag == null || lag == 0L) {
            behindSinceMs = null;
        } else if (behindSinceMs == null) {
            behindSinceMs = System.currentTimeMillis();
        }
    }

    /**
     * Get the nanopub count reported by this instance's own registry, or null if unknown
     * (non-query types, or not yet scanned). Reported via the
     * {@code Nanopub-Query-Registry-Nanopub-Count} header.
     *
     * @return the registry's nanopub count, or null
     */
    public Long getRegistryNanopubCount() {
        return registryNanopubCount;
    }

    /**
     * Get the seconds since this instance's loader last completed a tick, or null if
     * unknown. Reported via the {@code Nanopub-Query-Loader-Last-Success-Age-Seconds}
     * header; instances predating that header simply leave this empty.
     *
     * @return the loader age in seconds, or null
     */
    public Long getLoaderLastSuccessAgeSeconds() {
        return loaderLastSuccessAgeSeconds;
    }

    /**
     * How far this instance trails its own registry, or null if either count is unknown.
     *
     * @return the lag in nanopubs, or null
     */
    public Long getSyncLag() {
        return SyncHealth.lag(registryNanopubCount, nanopubCount);
    }

    /**
     * Get the sync lag as a display string, or "" if unknown.
     *
     * @return "in sync", "N behind", or ""
     */
    public String getSyncLagString() {
        return SyncHealth.formatLag(getSyncLag());
    }

    /**
     * Get the loader age as a display string, or "" if unknown.
     *
     * @return e.g. "2s ago", "2h 04m ago", or ""
     */
    public String getLoaderAgeString() {
        return SyncHealth.formatAge(loaderLastSuccessAgeSeconds);
    }

    /**
     * Whether this instance is behind its registry at all, however briefly.
     *
     * @return true if the lag is known and non-zero
     */
    public boolean isBehind() {
        Long lag = getSyncLag();
        return lag != null && lag > 0L;
    }

    /**
     * Whether this instance is reachable but has stopped keeping up with its registry.
     *
     * @return true if it has been out of sync long enough to report
     */
    public boolean isSyncStalled() {
        return SyncHealth.isStalled(behindSinceMs, System.currentTimeMillis(), loaderLastSuccessAgeSeconds);
    }

    /**
     * Report that this instance answers requests but is no longer ingesting.
     *
     * <p>Deliberately does not touch the success/failure counters: the instance really is
     * up, and during the 2026-07-31 stall it served queries in ~0.1 s throughout. Folding
     * this into the failure count would corrupt the OK ratio, which measures availability.
     * Call after {@link #reportTestSuccess(long)}, which resets the status to OK.
     */
    public void reportStalled() {
        status = "STALLED";
        logger.info("Test result: {} STALLED (lag {}, loader {})",
                service.getServiceIri(), getSyncLagString(), getLoaderAgeString());
    }

    /**
     * Get the server software version reported by this server, or null if unknown.
     *
     * @return the version, or null
     */
    public String getVersion() {
        return version;
    }

    /**
     * Set the server software version.
     *
     * @param version the version string (might be null)
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Whether this server is flagged as a test instance.
     *
     * @return true if it is a test instance
     */
    public boolean isTestInstance() {
        return testInstance;
    }

    /**
     * Set whether this server is a test instance.
     *
     * @param testInstance true if it is a test instance
     */
    public void setTestInstance(boolean testInstance) {
        this.testInstance = testInstance;
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
        if (monitorIpInfo == null) {
            return null;
        }
        ServerIpInfo serverIpInfo = getIpInfo();
        Double sLat = serverIpInfo.getLatitude();
        Double sLng = serverIpInfo.getLongitude();
        if (sLat == null || sLng == null) {
            return null;
        }
        return (int) calculateDistance(sLat, sLng, monitorIpInfo.getLatitude(), monitorIpInfo.getLongitude());
    }

    /**
     * The ip-api.com fields to ask for. The parameter is needed for "continent", which ip-api
     * leaves out of its default response; asking for anything at all then means asking for
     * everything {@link ServerIpInfo} reads, so the rest of the list has to stay in step with
     * that class.
     */
    private static final String IP_API_FIELDS = "status,message,continent,country,city,lat,lon,query";

    /**
     * The ip-api.com request for a host, asking for exactly the fields {@link ServerIpInfo}
     * reads.
     *
     * @param host the hostname or IP address to look up
     * @return the request URL
     */
    static String ipApiUrl(String host) {
        return "http://ip-api.com/json/" + host + "?fields=" + IP_API_FIELDS;
    }

    private static final Map<String, ServerIpInfo> ipInfoMap = new ConcurrentHashMap<>();
    private static final Map<String, Long> ipInfoFetchedAt = new ConcurrentHashMap<>();

    /**
     * Fetch IP-based geolocation info for the given host using the ip-api.com service.
     * <p>
     * The result is cached per host and the remote service is queried at most once per host
     * per {@link #IP_INFO_TTL_MS} (10 minutes). The attempt timestamp is recorded up front, so
     * a failed lookup (e.g. when ip-api.com rate-limits us) is throttled the same way and is not
     * retried on every scan; within the throttle window the last known value is returned, or
     * {@link ServerIpInfo#empty} if none was obtained yet.
     *
     * @param host the hostname or IP address to look up
     * @return the ServerIpInfo object with geolocation data (never null)
     * @throws IOException        if an I/O error occurs
     * @throws URISyntaxException if the constructed URI is invalid
     */
    public static ServerIpInfo fetchIpInfo(String host) throws IOException, URISyntaxException {
        if (!MonitorConf.get().isGeoIpInfoEnabled()) {
            return ServerIpInfo.empty;
        }
        Long fetchedAt = ipInfoFetchedAt.get(host);
        if (fetchedAt != null && System.currentTimeMillis() - fetchedAt < IP_INFO_TTL_MS) {
            ServerIpInfo cached = ipInfoMap.get(host);
            logger.debug("Using cached IP info for host '{}' (last fetched {} ms ago)", host, System.currentTimeMillis() - fetchedAt);
            return cached == null ? ServerIpInfo.empty : cached;
        }
        ipInfoFetchedAt.put(host, System.currentTimeMillis());
        ServerIpInfo serverIpInfo;
        URL geoipUrl = new URI(ipApiUrl(host)).toURL();
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) geoipUrl.openConnection();
            con.setConnectTimeout(10000);
            con.setReadTimeout(10000);
            serverIpInfo = new Gson().fromJson(new InputStreamReader(con.getInputStream()), ServerIpInfo.class);
            ipInfoMap.put(host, serverIpInfo);
            logger.debug("Fetched fresh IP info for host '{}'", host);
        } finally {
            if (con != null) {
                con.disconnect();
            }
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
