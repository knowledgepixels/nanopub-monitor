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

}
