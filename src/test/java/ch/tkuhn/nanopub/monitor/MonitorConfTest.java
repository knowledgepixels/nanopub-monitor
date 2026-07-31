package ch.tkuhn.nanopub.monitor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MonitorConfTest {

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

}
