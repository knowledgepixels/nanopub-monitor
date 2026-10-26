package ch.tkuhn.nanopub.monitor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceSectionTest {

    @Test
    void aHealthyInstanceNeedsNoAttention() {
        assertFalse(ServiceSection.needsAttention("OK", "consensus", false));
        assertFalse(ServiceSection.needsAttention("OK", null, false));
    }

    @Test
    void anInstanceThatIsNotOkNeedsAttention() {
        assertTrue(ServiceSection.needsAttention("DOWN", null, false));
        assertTrue(ServiceSection.needsAttention("NOT SEEN", null, false));
    }

    @Test
    void anOutlierNeedsAttentionEvenWhenOk() {
        assertTrue(ServiceSection.needsAttention("OK", "outlier", false));
    }

    @Test
    void anInstanceBehindItsRegistryNeedsAttention() {
        assertTrue(ServiceSection.needsAttention("OK", "consensus", true));
    }

    @Test
    void anchorsAreLowerCaseWithHyphens() {
        assertEquals("np-ldf-server", ServiceSection.toAnchor("np-ldf-server"));
        assertEquals("service", ServiceSection.toAnchor("#Service"));
        assertEquals("grlc-np-api", ServiceSection.toAnchor("grlc np_api"));
    }

    @Test
    void titlesDropTheGenericServiceFragment() {
        assertEquals("nanopub-server", ServiceSection.toTitle("nanopub-server#service"));
        assertEquals("signed-nanopub-services / np-sparql-api", ServiceSection.toTitle("signed-nanopub-services#np-sparql-api"));
    }

}
