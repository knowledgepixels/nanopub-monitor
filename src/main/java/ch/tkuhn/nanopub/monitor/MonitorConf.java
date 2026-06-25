package ch.tkuhn.nanopub.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration for the nanopub-monitor application.
 */
public class MonitorConf {

    private static final MonitorConf obj = new MonitorConf();
    private static final Logger logger = LoggerFactory.getLogger(MonitorConf.class);

    /**
     * Get the singleton instance of the configuration.
     *
     * @return the MonitorConf instance
     */
    public static MonitorConf get() {
        return obj;
    }

    private Properties conf;

    private MonitorConf() {
        conf = new Properties();

        String mainConfFile = "conf.properties";
        InputStream in = MonitorConf.class.getResourceAsStream(mainConfFile);
        if (in == null) {
            logger.error("Configuration file '{}' not found on classpath", mainConfFile);
            System.exit(1);
        }
        try {
            conf.load(in);
            logger.info("Loaded configuration from '{}'", mainConfFile);
        } catch (IOException ex) {
            logger.error("Could not load configuration file '{}'", mainConfFile, ex);
            System.exit(1);
        }

        String localConfFile = "local.conf.properties";
        in = MonitorConf.class.getResourceAsStream(localConfFile);
        if (in != null) {
            try {
                conf.load(in);
                logger.info("Loaded local configuration overrides from '{}'", localConfFile);
            } catch (IOException ex) {
                logger.error("Could not load local configuration file '{}'", localConfFile, ex);
                System.exit(1);
            }
        } else {
            logger.debug("No local configuration file '{}' found, using defaults only", localConfFile);
        }
    }

    /**
     * Get the frequency (in seconds) at which the monitor scans the nanopub servers.
     *
     * @return the scan frequency in seconds
     */
    public int getScanFreq() {
        return Integer.parseInt(conf.getProperty("scan-freq"));
    }

    /**
     * Get the number of parallel workers used to test servers in each scan.
     *
     * @return the number of scanner worker threads
     */
    public int getScanThreads() {
        return Integer.parseInt(conf.getProperty("scan-threads"));
    }

    /**
     * Whether the monitor should show a map with server locations.
     *
     * @return true if the map should be shown, false otherwise
     */
    public boolean showMap() {
        return Boolean.parseBoolean(conf.getProperty("show-map"));
    }

    /**
     * Whether the monitor should try to get geoip info for servers.
     *
     * @return true if geoip info should be retrieved, false otherwise
     */
    public boolean isGeoIpInfoEnabled() {
        return Boolean.parseBoolean(conf.getProperty("get-geoip-info"));
    }

}
