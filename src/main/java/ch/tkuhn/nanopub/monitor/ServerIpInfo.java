package ch.tkuhn.nanopub.monitor;

import java.io.Serializable;

/**
 * Geolocation information about a server based on its IP address.
 */
public class ServerIpInfo implements Serializable {

    private static final long serialVersionUID = 4805668042976093282L;

    private Double lat = null;
    private Double lon = null;
    private String country = "unknown country";
    private String city = "unknown city";
    private String query = "unknown IP";

    /**
     * An empty ServerIpInfo instance representing unknown or unavailable data.
     */
    public static ServerIpInfo empty = new ServerIpInfo();

    private ServerIpInfo() {
    }

    /**
     * Retrieves the latitude of the server's location.
     *
     * @return the latitude, or null if unknown
     */
    public Double getLatitude() {
        return lat;
    }

    /**
     * Retrieves the longitude of the server's location.
     *
     * @return the longitude, or null if unknown
     */
    public Double getLongitude() {
        return lon;
    }

    /**
     * Retrieves the name of the country where the server is located.
     *
     * @return the country name
     */
    public String getCountryName() {
        return country;
    }

    /**
     * Retrieves the name of the city where the server is located.
     *
     * @return the city name
     */
    public String getCity() {
        return city;
    }

    /**
     * Retrieves the IP address of the server.
     *
     * @return the IP address
     */
    public String getIp() {
        return query;
    }

}
