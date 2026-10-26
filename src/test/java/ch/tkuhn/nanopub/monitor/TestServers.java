package ch.tkuhn.nanopub.monitor;

import com.google.gson.Gson;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;

import java.net.URI;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Builds services, locations and server lists for tests, with every host's geolocation answered
 * from the cache so that no test asks ip-api.com.
 */
final class TestServers {

    static final ServerIpInfo ZURICH = location(47.37, 8.54, "Zurich", "Switzerland", "Europe", "192.0.2.10");
    static final ServerIpInfo HELSINKI = location(60.17, 24.94, "Helsinki", "Finland", "Europe", "192.0.2.20");

    private static final Set<String> locatedHosts = ConcurrentHashMap.newKeySet();

    private TestServers() {
    }

    /**
     * Build the geolocation info ip-api.com would report.
     *
     * @param lat       the latitude
     * @param lon       the longitude
     * @param city      the city
     * @param country   the country
     * @param continent the continent, shown as the region
     * @param ip        the IP address
     * @return the geolocation info
     */
    static ServerIpInfo location(double lat, double lon, String city, String country, String continent, String ip) {
        String json = "{\"lat\": " + lat + ", \"lon\": " + lon + ", \"city\": \"" + city + "\", \"country\": \""
                      + country + "\", \"continent\": \"" + continent + "\", \"query\": \"" + ip + "\"}";
        return new Gson().fromJson(json, ServerIpInfo.class);
    }

    /**
     * Build a service of the given type.
     *
     * @param url  the service URL
     * @param type the service type IRI
     * @return the service
     */
    static NanopubService service(String url, IRI type) {
        return new NanopubService(Values.iri(url), type);
    }

    /**
     * Build a service of the given type.
     *
     * @param url  the service URL
     * @param type the service type IRI, as a string
     * @return the service
     */
    static NanopubService service(String url, String type) {
        return service(url, Values.iri(type));
    }

    /**
     * Answer geolocation lookups for the host of the given URL with the given info.
     *
     * @param url  the service URL whose host to locate
     * @param info the geolocation info to answer with
     */
    static void locate(String url, ServerIpInfo info) {
        String host = URI.create(url).getHost();
        ServerData.cacheIpInfo(host, info);
        locatedHosts.add(host);
    }

    /**
     * Build the server data of a service at an unknown location.
     *
     * @param url  the service URL
     * @param type the service type IRI
     * @return the server data
     */
    static ServerData serverData(String url, IRI type) {
        locateIfUnknown(url);
        return new ServerData(service(url, type), null);
    }

    /**
     * Make the server list track exactly the given services, with the monitor in Zurich and every
     * service at an unknown location unless {@link #locate} placed its host earlier.
     *
     * @param services the services to track
     * @return the server list
     */
    static ServerList serverList(NanopubService... services) {
        ServerData.cacheIpInfo("", ZURICH);
        for (NanopubService ns : services) {
            locateIfUnknown(ns.getServiceIri().stringValue());
        }
        return ServerList.resetTo(Arrays.asList(services));
    }

    /**
     * Answer geolocation lookups for the host of the given URL with an unknown location, unless
     * {@link #locate} placed that host already.
     *
     * @param url the service URL whose host to locate
     */
    private static void locateIfUnknown(String url) {
        String host = URI.create(url).getHost();
        if (!locatedHosts.contains(host)) {
            ServerData.cacheIpInfo(host, ServerIpInfo.empty);
        }
    }

}
