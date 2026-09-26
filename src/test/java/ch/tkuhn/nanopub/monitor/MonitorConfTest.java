package ch.tkuhn.nanopub.monitor;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MonitorConfTest {

    /**
     * A configuration reading the given environment instead of the real one.
     */
    private static MonitorConf withEnv(Map<String, String> env) {
        return new MonitorConf(env::get);
    }

    @Test
    void get() {
        MonitorConf conf = MonitorConf.get();
        assertNotNull(conf);
        assertSame(conf, MonitorConf.get());
    }

    @Test
    void scanFreq() {
        assertTrue(MonitorConf.get().getScanFreq() > 0);
    }

    @Test
    void scanThreads() {
        assertTrue(MonitorConf.get().getScanThreads() > 0);
    }

    @Test
    void flagsAreReadable() {
        MonitorConf conf = MonitorConf.get();
        // No assertion on the values themselves, which are deployment choices;
        // this only pins down that the properties are present and parseable.
        conf.showMap();
        conf.isGeoIpInfoEnabled();
    }

    @Test
    void envNameFor() {
        assertEquals("NANOPUB_MONITOR_SCAN_FREQ", MonitorConf.envNameFor("scan-freq"));
        assertEquals("NANOPUB_MONITOR_SCAN_THREADS", MonitorConf.envNameFor("scan-threads"));
        assertEquals("NANOPUB_MONITOR_SHOW_MAP", MonitorConf.envNameFor("show-map"));
        assertEquals("NANOPUB_MONITOR_GET_GEOIP_INFO", MonitorConf.envNameFor("get-geoip-info"));
    }

    @Test
    void environmentOverridesConfFile() {
        MonitorConf conf = withEnv(Map.of(
                "NANOPUB_MONITOR_SCAN_FREQ", "60",
                "NANOPUB_MONITOR_SCAN_THREADS", "4",
                "NANOPUB_MONITOR_SHOW_MAP", "false",
                "NANOPUB_MONITOR_GET_GEOIP_INFO", "false"));
        assertEquals(60, conf.getScanFreq());
        assertEquals(4, conf.getScanThreads());
        assertFalse(conf.showMap());
        assertFalse(conf.isGeoIpInfoEnabled());
    }

    @Test
    void confFileValuesUsedWhenEnvironmentIsEmpty() {
        MonitorConf conf = withEnv(Map.of());
        assertEquals(MonitorConf.get().getScanFreq(), conf.getScanFreq());
        assertEquals(MonitorConf.get().getScanThreads(), conf.getScanThreads());
        assertEquals(MonitorConf.get().showMap(), conf.showMap());
        assertEquals(MonitorConf.get().isGeoIpInfoEnabled(), conf.isGeoIpInfoEnabled());
    }

    /**
     * Docker Compose interpolates a variable that the .env file does not define into an empty
     * string, so an empty value has to count as unset rather than as a broken setting.
     */
    @Test
    void blankEnvironmentValueFallsBackToConfFile() {
        MonitorConf conf = withEnv(Map.of(
                "NANOPUB_MONITOR_SCAN_FREQ", "",
                "NANOPUB_MONITOR_SHOW_MAP", "   "));
        assertEquals(MonitorConf.get().getScanFreq(), conf.getScanFreq());
        assertEquals(MonitorConf.get().showMap(), conf.showMap());
    }

    @Test
    void surroundingWhitespaceIsIgnored() {
        MonitorConf conf = withEnv(Map.of(
                "NANOPUB_MONITOR_SCAN_FREQ", " 30 ",
                "NANOPUB_MONITOR_SHOW_MAP", " FALSE "));
        assertEquals(30, conf.getScanFreq());
        assertFalse(conf.showMap());
    }

    @Test
    void nonNumericNumberIsRejected() {
        MonitorConf conf = withEnv(Map.of("NANOPUB_MONITOR_SCAN_FREQ", "ten"));
        IllegalStateException ex = assertThrows(IllegalStateException.class, conf::getScanFreq);
        assertTrue(ex.getMessage().contains("NANOPUB_MONITOR_SCAN_FREQ"),
                "the message should name the environment variable to fix, but was: " + ex.getMessage());
    }

    @Test
    void nonPositiveNumberIsRejected() {
        assertThrows(IllegalStateException.class, () -> withEnv(Map.of("NANOPUB_MONITOR_SCAN_FREQ", "0")).getScanFreq());
        assertThrows(IllegalStateException.class, () -> withEnv(Map.of("NANOPUB_MONITOR_SCAN_THREADS", "-1")).getScanThreads());
    }

    @Test
    void validateAcceptsAWorkingConfiguration() {
        withEnv(Map.of()).validate();
        withEnv(Map.of("NANOPUB_MONITOR_SCAN_THREADS", "2")).validate();
    }

    @Test
    void validateRejectsABrokenSetting() {
        // The application calls this at startup, so that a typo is reported there and not on
        // whichever request first happens to read the setting.
        assertThrows(IllegalStateException.class, () -> withEnv(Map.of("NANOPUB_MONITOR_SCAN_THREADS", "lots")).validate());
        assertThrows(IllegalStateException.class, () -> withEnv(Map.of("NANOPUB_MONITOR_GET_GEOIP_INFO", "off")).validate());
    }

    /**
     * A value that is neither "true" nor "false" is a mistake worth reporting: read as false, it
     * would silently switch the feature off instead.
     */
    @Test
    void nonBooleanFlagIsRejected() {
        MonitorConf conf = withEnv(Map.of("NANOPUB_MONITOR_SHOW_MAP", "yes"));
        IllegalStateException ex = assertThrows(IllegalStateException.class, conf::showMap);
        assertTrue(ex.getMessage().contains("NANOPUB_MONITOR_SHOW_MAP"),
                "the message should name the environment variable to fix, but was: " + ex.getMessage());
    }

}
