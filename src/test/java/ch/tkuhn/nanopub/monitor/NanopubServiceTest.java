package ch.tkuhn.nanopub.monitor;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;
import org.junit.jupiter.api.Test;

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

}