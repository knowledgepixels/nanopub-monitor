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
    // The part of the world a server sits in is ip-api's "continent" field. Its "region" and
    // "regionName" are the subdivision within a country ("VA", "Virginia"), which is finer than
    // the country and answers a different question. Field names have to match the JSON keys,
    // since these objects are deserialized straight from the ip-api response.
    private String continent = "unknown region";

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
     * Retrieves the region of the world the server is in, as a continent name: "Europe",
     * "North America", "Asia", and so on.
     *
     * @return the region name
     */
    public String getRegionName() {
        return continent;
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
