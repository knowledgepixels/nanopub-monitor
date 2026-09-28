package ch.tkuhn.nanopub.monitor;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.message.BasicHttpResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerScannerTest {

    private static HttpResponse response(int code, String testInstanceHeader) {
        HttpResponse resp = new BasicHttpResponse(HttpVersion.HTTP_1_1, code, null);
        if (testInstanceHeader != null) {
            resp.addHeader("Nanopub-Registry-Test-Instance", testInstanceHeader);
        }
        return resp;
    }

    @Test
    void aRegistryCallingItselfATestInstanceIsOne() {
        assertTrue(ServerScanner.saysTestInstance(response(200, "true")));
        assertTrue(ServerScanner.saysTestInstance(response(200, "TRUE")), "The header is not case-sensitive");
    }

    @Test
    void aProductionRegistryIsNotATestInstance() {
        assertFalse(ServerScanner.saysTestInstance(response(200, "false")));
        assertFalse(ServerScanner.saysTestInstance(response(200, null)),
                "An older registry that reports nothing is not assumed to be a test instance");
    }

    @Test
    void nothingUsefulAtTheCandidateUrlIsNotATestInstance() {
        // What the hosts with no test deployment actually answer: a proxy error, or a page
        // that is not a registry at all.
        assertFalse(ServerScanner.saysTestInstance(response(502, null)));
        assertFalse(ServerScanner.saysTestInstance(response(404, null)));
        assertFalse(ServerScanner.saysTestInstance(response(302, "true")),
                "A redirect is not a registry answering for itself");
    }

    private static HttpResponse queryResponse(int code, String ownHeader, String registryHeader) {
        HttpResponse resp = new BasicHttpResponse(HttpVersion.HTTP_1_1, code, null);
        if (ownHeader != null) {
            resp.addHeader("Nanopub-Query-Test-Instance", ownHeader);
        }
        if (registryHeader != null) {
            resp.addHeader("Nanopub-Query-Registry-Test-Instance", registryHeader);
        }
        return resp;
    }

    @Test
    void aQueryInstanceCallingItselfATestInstanceIsOne() {
        // The case the forwarded registry header cannot express: a staging instance that
        // mirrors a production registry (knowledgepixels/nanopub-query#200).
        assertTrue(ServerScanner.saysQueryTestInstance(queryResponse(200, "true", "false")));
        assertTrue(ServerScanner.saysQueryTestInstance(queryResponse(200, "TRUE", null)),
                "The header is not case-sensitive");
    }

    @Test
    void aQueryInstanceOfATestRegistryIsOne() {
        assertTrue(ServerScanner.saysQueryTestInstance(queryResponse(200, null, "true")));
    }

    @Test
    void aProductionQueryInstanceIsNotATestInstance() {
        assertFalse(ServerScanner.saysQueryTestInstance(queryResponse(200, "false", "false")));
        assertFalse(ServerScanner.saysQueryTestInstance(queryResponse(200, null, null)),
                "An older instance that reports neither header is not assumed to be a test instance");
        assertFalse(ServerScanner.saysQueryTestInstance(queryResponse(503, "true", "true")),
                "A response that did not succeed says nothing about the instance");
    }

}
