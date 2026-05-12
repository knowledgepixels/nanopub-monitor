package ch.tkuhn.nanopub.monitor;

/**
 * Exposes the running monitor's version string, derived from the deployed JAR/WAR
 * manifest at runtime. Returns "dev" when no version is available (e.g. during
 * mvn jetty:run from target/classes, before packaging).
 */
public class MonitorVersion {

    private static final String VERSION;

    static {
        String v = MonitorVersion.class.getPackage().getImplementationVersion();
        VERSION = v != null ? v : "dev";
    }

    private MonitorVersion() {
    }

    public static String get() {
        return VERSION;
    }

}
