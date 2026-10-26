package ch.tkuhn.nanopub.monitor;

import org.eclipse.rdf4j.model.util.Values;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceKindTest {

    @Test
    void versionedTypesBelongToTheirKind() {
        assertEquals(ServiceKind.REGISTRY, ServiceKind.of(Values.iri("https://w3id.org/np/o/service/terms/nanopub-registry-1.0")));
        assertEquals(ServiceKind.QUERY, ServiceKind.of(Values.iri("https://w3id.org/np/o/service/terms/nanopub-query-1.1")));
    }

    @Test
    void eachKnownTypeHasItsKind() {
        assertEquals(ServiceKind.NANODASH, ServiceKind.of(NanopubService.NANODASH_TYPE_IRI));
        assertEquals(ServiceKind.ROUTER, ServiceKind.of(NanopubService.NANOPUB_ROUTER_TYPE_IRI));
        assertEquals(ServiceKind.MONITOR, ServiceKind.of(NanopubService.NANOPUB_MONITOR_TYPE_IRI));
    }

    @Test
    void legacyTypesAreOther() {
        assertEquals(ServiceKind.OTHER, ServiceKind.of(NanopubService.NANOPUB_SERVER_TYPE_IRI));
        assertEquals(ServiceKind.OTHER, ServiceKind.of(NanopubService.LDF_SERVICE_TYPE_IRI));
    }

    @Test
    void registriesAndQueriesReportTheirOwnColumns() {
        assertTrue(ServiceKind.REGISTRY.getColumns().contains(ServiceKind.Column.TRUST_STATE));
        assertTrue(ServiceKind.QUERY.getColumns().containsAll(
                Set.of(ServiceKind.Column.CHECKSUM, ServiceKind.Column.SYNC, ServiceKind.Column.LOADER)));
        assertTrue(ServiceKind.NANODASH.getColumns().isEmpty());
    }

}
