package ch.tkuhn.nanopub.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.function.Function;

/**
 * Configuration for the nanopub-monitor application.
 * <p>
 * Each setting has a name as used in {@code conf.properties}, and is resolved from these sources,
 * the first one that provides a value winning:
 * <ol>
 *   <li>an environment variable, named after the property as {@link #envNameFor} describes it
 *       ({@code scan-freq} becomes {@code NANOPUB_MONITOR_SCAN_FREQ})</li>
 *   <li>{@code local.conf.properties} on the classpath, if present</li>
 *   <li>{@code conf.properties}, which ships with the application and defines every default</li>
 * </ol>
 * The environment variables are what a Docker Compose deployment sets, since the two properties
 * files live inside the packaged web application and cannot be edited without rebuilding it.
 */
public class MonitorConf {

    private static final Logger logger = LoggerFactory.getLogger(MonitorConf.class);
    private static final MonitorConf obj = new MonitorConf(System::getenv);

    /**
     * Prefix of the environment variable names this configuration reads.
     */
    static final String ENV_PREFIX = "NANOPUB_MONITOR_";

    /**
     * Get the singleton instance of the configuration.
     *
     * @return the MonitorConf instance
     */
    public static MonitorConf get() {
        return obj;
    }

    private final Properties conf;
    private final Function<String, String> getEnv;

    /**
     * @param getEnv how to look up an environment variable; {@code System::getenv} outside of tests
     */
    MonitorConf(Function<String, String> getEnv) {
        this.getEnv = getEnv;
        conf = new Properties();

        String mainConfFile = "conf.properties";
        InputStream in = MonitorConf.class.getResourceAsStream(mainConfFile);
        if (in == null) {
            logger.error("Configuration file '{}' not found on classpath", mainConfFile);
            throw new IllegalStateException("Configuration file '" + mainConfFile + "' not found on classpath");
        }
        try {
            conf.load(in);
            logger.info("Loaded configuration from '{}'", mainConfFile);
        } catch (IOException ex) {
            logger.error("Could not load configuration file '{}'", mainConfFile, ex);
            throw new IllegalStateException("Could not load configuration file '" + mainConfFile + "'", ex);
        }

        String localConfFile = "local.conf.properties";
        in = MonitorConf.class.getResourceAsStream(localConfFile);
        if (in != null) {
            try {
                conf.load(in);
                logger.info("Loaded local configuration overrides from '{}'", localConfFile);
            } catch (IOException ex) {
                logger.error("Could not load local configuration file '{}'", localConfFile, ex);
                throw new IllegalStateException("Could not load local configuration file '" + localConfFile + "'", ex);
            }
        } else {
            logger.debug("No local configuration file '{}' found, using defaults only", localConfFile);
        }
    }

    /**
     * The environment variable that overrides the given property: the property name in upper case
     * with hyphens turned into underscores, behind {@link #ENV_PREFIX}. So {@code scan-freq} is
     * overridden by {@code NANOPUB_MONITOR_SCAN_FREQ}.
     *
     * @param property the property name as used in conf.properties
     * @return the name of the environment variable
     */
    static String envNameFor(String property) {
        return ENV_PREFIX + property.toUpperCase().replace('-', '_');
    }

    /**
     * Resolve a property from the environment or the properties files, as described in the class
     * comment. Values are trimmed, and an environment variable set to nothing but whitespace counts
     * as not set: that is how an unset variable arrives when Docker Compose interpolates a value
     * that is missing from the .env file.
     *
     * @param property the property name as used in conf.properties
     * @return the configured value, never null and never empty
     */
    private String getProperty(String property) {
        String envName = envNameFor(property);
        String fromEnv = getEnv.apply(envName);
        if (fromEnv != null && !fromEnv.trim().isEmpty()) {
            logger.info("Configuration '{}' set to '{}' by environment variable {}", property, fromEnv.trim(), envName);
            return fromEnv.trim();
        }
        String value = conf.getProperty(property);
        if (value == null || value.trim().isEmpty()) {
            // Only reachable if conf.properties, which ships with the application, lost the entry.
            throw new IllegalStateException("Configuration '" + property + "' has no value; set "
                    + envName + " or restore the entry in conf.properties");
        }
        return value.trim();
    }

    /**
     * Resolve a property that has to be a positive number. A scan frequency or thread count of zero
     * or less makes the scanner spin or refuse to run, so it is rejected here, where the message can
     * still name the setting that is wrong.
     *
     * @param property the property name as used in conf.properties
     * @return the configured value
     */
    private int getPositiveInt(String property) {
        String value = getProperty(property);
        int number;
        try {
            number = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalStateException("Configuration '" + property + "' has to be a whole number, but is '"
                    + value + "' (set via " + envNameFor(property) + " or conf.properties)", ex);
        }
        if (number <= 0) {
            throw new IllegalStateException("Configuration '" + property + "' has to be greater than 0, but is "
                    + number + " (set via " + envNameFor(property) + " or conf.properties)");
        }
        return number;
    }

    /**
     * Resolve a property that has to be a boolean. Unlike {@link Boolean#parseBoolean}, anything
     * other than "true" or "false" is rejected rather than read as false, so that a mistyped
     * environment variable is reported instead of silently switching a feature off.
     *
     * @param property the property name as used in conf.properties
     * @return the configured value
     */
    private boolean getBoolean(String property) {
        String value = getProperty(property);
        if (value.equalsIgnoreCase("true")) return true;
        if (value.equalsIgnoreCase("false")) return false;
        throw new IllegalStateException("Configuration '" + property + "' has to be 'true' or 'false', but is '"
                + value + "' (set via " + envNameFor(property) + " or conf.properties)");
    }

    /**
     * Read every setting, so that a value this configuration rejects is reported when the
     * application starts rather than when something first happens to need it. Called from
     * {@link MonitorApplication#init()}; the settings are otherwise resolved lazily, and a typo in
     * an environment variable would then leave the instance running but broken.
     *
     * @throws IllegalStateException if any setting is missing or cannot be parsed
     */
    void validate() {
        getScanFreq();
        getScanThreads();
        showMap();
        isGeoIpInfoEnabled();
    }

    /**
     * Get the frequency (in seconds) at which the monitor scans the nanopub servers.
     *
     * @return the scan frequency in seconds
     */
    public int getScanFreq() {
        return getPositiveInt("scan-freq");
    }

    /**
     * Get the number of parallel workers used to test servers in each scan.
     *
     * @return the number of scanner worker threads
     */
    public int getScanThreads() {
        return getPositiveInt("scan-threads");
    }

    /**
     * Whether the monitor should show a map with server locations.
     *
     * @return true if the map should be shown, false otherwise
     */
    public boolean showMap() {
        return getBoolean("show-map");
    }

    /**
     * Whether the monitor should try to get geoip info for servers.
     *
     * @return true if geoip info should be retrieved, false otherwise
     */
    public boolean isGeoIpInfoEnabled() {
        return getBoolean("get-geoip-info");
    }

}
