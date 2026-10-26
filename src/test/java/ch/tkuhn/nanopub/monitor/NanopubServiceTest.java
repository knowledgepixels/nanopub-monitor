package ch.tkuhn.nanopub.monitor;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class NanopubServiceTest {

    @Test
    void constructor() {
        IRI serviceIri = Values.iri("https://example.org/service");
        IRI typeIri = Values.iri("https://example.org/type");
        NanopubService nanopubService = new NanopubService(serviceIri, typeIri);
        assertEquals(serviceIri, nanopubService.getServiceIri());
        assertEquals(typeIri, nanopubService.getTypeIri());
    }

    @Test
    void getServiceIri() {
        IRI serviceIri = Values.iri("https://example.org/service");
        IRI typeIri = Values.iri("https://example.org/type");
        NanopubService nanopubService = new NanopubService(serviceIri, typeIri);
        assertEquals(serviceIri, nanopubService.getServiceIri());
    }

    @Test
    void getTypeIri() {
        IRI serviceIri = Values.iri("https://example.org/service");
        IRI typeIri = Values.iri("https://example.org/type");
        NanopubService nanopubService = new NanopubService(serviceIri, typeIri);
        assertEquals(typeIri, nanopubService.getTypeIri());
    }

    @Test
    void getTypeLabel() {
        String type = "type";
        IRI serviceIri = Values.iri("https://example.org/service");
        IRI typeIri = Values.iri("https://example.org/" + type);
        NanopubService nanopubService = new NanopubService(serviceIri, typeIri);
        assertEquals(type, nanopubService.getTypeLabel());
    }

    @Test
    void testToString() {
        String type = "type";
        IRI serviceIri = Values.iri("https://example.org/service");
        IRI typeIri = Values.iri("https://example.org/" + type);
        NanopubService nanopubService = new NanopubService(serviceIri, typeIri);
        assertEquals(serviceIri.stringValue() + " (" + typeIri + ")", nanopubService.toString());
    }

    @Test
    void getMapOffsetXWithUnlistedType() {
        String type = "type";
        IRI serviceIri = Values.iri("https://example.org/service");
        IRI typeIri = Values.iri("https://example.org/" + type);
        NanopubService nanopubService = new NanopubService(serviceIri, typeIri);
        assertEquals(0, nanopubService.getMapOffsetX());
    }

    @Test
    void getMapOffsetYWithUnlistedType() {
        String type = "type";
        IRI serviceIri = Values.iri("https://example.org/service");
        IRI typeIri = Values.iri("https://example.org/" + type);
        NanopubService nanopubService = new NanopubService(serviceIri, typeIri);
        assertEquals(0, nanopubService.getMapOffsetY());
    }

    @Test
    void getMapColorWithUnlistedType() {
        String type = "type";
        IRI serviceIri = Values.iri("https://example.org/service");
        IRI typeIri = Values.iri("https://example.org/" + type);
        NanopubService nanopubService = new NanopubService(serviceIri, typeIri);
        assertEquals("gray", nanopubService.getMapColor());
    }

    // Announced with a version suffix, like the registry and query types, so the router must be
    // recognised by type prefix rather than by an exact match.
    @Test
    void routerIsRecognisedWithAVersionSuffix() {
        NanopubService router = new NanopubService(
                Values.iri("https://router.example.org/"),
                Values.iri("https://w3id.org/np/o/service/terms/nanopub-router-1.0"));
        assertEquals("#dab40b", router.getMapColor());
        assertEquals(0, router.getMapOffsetX());
        assertEquals(-3, router.getMapOffsetY());
    }

    @Test
    void routerDoesNotShareItsMapSpotWithAnotherService() {
        NanopubService router = new NanopubService(
                Values.iri("https://example.org/"),
                Values.iri("https://w3id.org/np/o/service/terms/nanopub-router-1.0"));
        for (IRI otherType : new IRI[]{
                NanopubService.NANOPUB_QUERY_TYPE_IRI,
                NanopubService.NANOPUB_REGISTRY_TYPE_IRI,
                NanopubService.NANODASH_TYPE_IRI,
                NanopubService.NANOPUB_MONITOR_TYPE_IRI,
                NanopubService.NANOPUB_SERVER_TYPE_IRI}) {
            NanopubService other = new NanopubService(Values.iri("https://example.org/"), otherType);
            boolean sameSpot = other.getMapOffsetX() == router.getMapOffsetX()
                    && other.getMapOffsetY() == router.getMapOffsetY();
            assertEquals(false, sameSpot, "router overlaps " + other.getTypeLabel() + " on the map");
        }
    }

    @Test
    void routerTypeLabelIsReadable() {
        NanopubService router = new NanopubService(
                Values.iri("https://router.example.org/"),
                Values.iri("https://w3id.org/np/o/service/terms/nanopub-router-1.0"));
        assertEquals("nanopub-router-1.0", router.getTypeLabel());
    }

    @Test
    void equalsWithNull() {
        String type = "type";
        IRI serviceIri = Values.iri("https://example.org/service");
        IRI typeIri = Values.iri("https://example.org/" + type);
        NanopubService nanopubService = new NanopubService(serviceIri, typeIri);
        assertNotEquals(null, nanopubService);
    }

    @Test
    void equalsWithSameServiceAndType() {
        String type = "type";
        IRI serviceIri = Values.iri("https://example.org/service");
        IRI typeIri = Values.iri("https://example.org/" + type);
        NanopubService nanopubService1 = new NanopubService(serviceIri, typeIri);
        NanopubService nanopubService2 = new NanopubService(serviceIri, typeIri);
        assertEquals(nanopubService1, nanopubService2);
    }

    /**
     * Wicket serializes every page it stores, and the pages showing server data hold one of these
     * through {@link ServerData}. A non-serializable field here fails that, on every render.
     */
    @Test
    void survivesSerialization() throws Exception {
        NanopubService nanopubService = new NanopubService(
                Values.iri("https://example.org/service"),
                Values.iri("https://example.org/type"));

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(nanopubService);
        }
        NanopubService restored;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            restored = (NanopubService) in.readObject();
        }

        assertEquals(nanopubService, restored);
        assertEquals(nanopubService.getServiceIri(), restored.getServiceIri());
        assertEquals(nanopubService.getTypeIri(), restored.getTypeIri());
    }

    /**
     * Each known service type has its own colour and spot around a server's map marker.
     *
     * @param type    the service type IRI
     * @param offsetX the expected horizontal offset
     * @param offsetY the expected vertical offset
     * @param color   the expected colour
     */
    @ParameterizedTest
    @CsvSource({
            "https://github.com/tkuhn/nanopub-server#service, 0, 0, orange",
            "https://github.com/tkuhn/nanopub-monitor#service, 3, -3, lightcoral",
            "https://github.com/peta-pico/nanopub-services#np-ldf-server, 3, 0, skyblue",
            "https://github.com/peta-pico/nanopub-services#grlc-np-api, -3, 0, darkseagreen",
            "https://github.com/peta-pico/signed-nanopub-services#np-ldf-server, 3, 0, #4682B4",
            "https://github.com/peta-pico/signed-nanopub-services#grlc-np-api, -3, 0, lightseagreen",
            "https://github.com/peta-pico/signed-nanopub-services#np-sparql-api, 3, 0, mediumpurple",
            "https://w3id.org/np/o/service/terms/nanopub-query-1.1, 3, 3, #da0b84",
            "https://w3id.org/np/o/service/terms/nanopub-registry-1.0, -3, 3, #0bc9da",
            "https://w3id.org/np/o/service/terms/nanodash-2.x, -3, -3, #0b73da"
    })
    void eachKnownTypeHasItsOwnMapStyle(String type, int offsetX, int offsetY, String color) {
        NanopubService service = new NanopubService(Values.iri("https://example.org/"), Values.iri(type));

        assertEquals(offsetX, service.getMapOffsetX());
        assertEquals(offsetY, service.getMapOffsetY());
        assertEquals(color, service.getMapColor());
    }

}
