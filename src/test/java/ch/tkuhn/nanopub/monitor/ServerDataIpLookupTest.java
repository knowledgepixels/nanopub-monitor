package ch.tkuhn.nanopub.monitor;

import com.sun.net.httpserver.HttpServer;
import org.eclipse.rdf4j.model.util.Values;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Checks the geolocation lookup against a local stand-in for ip-api.com.
 */
class ServerDataIpLookupTest {

    private HttpServer ipApi;
    private final List<String> lookups = new CopyOnWriteArrayList<>();

    /**
     * Start a local ip-api.com stand-in that places every host in Helsinki, and send lookups to it.
     *
     * @throws IOException if the server cannot be started
     */
    @BeforeEach
    void startIpApi() throws IOException {
        ipApi = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        ipApi.createContext("/json/", exchange -> {
            lookups.add(exchange.getRequestURI().getPath() + "?" + exchange.getRequestURI().getQuery());
            byte[] body = "{\"lat\": 60.17, \"lon\": 24.94, \"city\": \"Helsinki\", \"country\": \"Finland\"}"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        ipApi.start();
        ServerData.setIpApiBaseUrl("http://127.0.0.1:" + ipApi.getAddress().getPort() + "/json/");
    }

    /**
     * Stop the stand-in and send lookups to ip-api.com again.
     */
    @AfterEach
    void stopIpApi() {
        ServerData.setIpApiBaseUrl(null);
        ipApi.stop(0);
    }

    /**
     * A host is looked up once, and then answered from the cache.
     *
     * @throws Exception if the lookup fails
     */
    @Test
    void aHostIsLookedUpOnceThenCached() throws Exception {
        ServerIpInfo first = ServerData.fetchIpInfo("lookup-once.example.org");
        ServerIpInfo second = ServerData.fetchIpInfo("lookup-once.example.org");

        assertEquals("Helsinki", first.getCity());
        assertEquals("Finland", first.getCountryName());
        assertSame(first, second);
        assertEquals(List.of("/json/lookup-once.example.org?fields=status,message,continent,country,city,lat,lon,query"),
                lookups);
    }

    /**
     * A failed lookup is reported, and not retried within the throttle window.
     *
     * @throws IOException if no free port can be found
     */
    @Test
    void aFailedLookupIsNotRetriedSoon() throws Exception {
        ServerData.setIpApiBaseUrl("http://127.0.0.1:" + freePort() + "/json/");

        assertThrows(IOException.class, () -> ServerData.fetchIpInfo("lookup-fails.example.org"));
        assertSame(ServerIpInfo.empty, ServerData.fetchIpInfo("lookup-fails.example.org"));
    }

    /**
     * A service whose URL names no host has an unknown location.
     */
    @Test
    void aServiceWithoutAHostHasAnUnknownLocation() {
        ServerData sd = new ServerData(new NanopubService(Values.iri("urn:example:no-host"),
                NanopubService.NANOPUB_SERVER_TYPE_IRI), null);

        assertSame(ServerIpInfo.empty, sd.getIpInfo());
        assertEquals(List.of(), lookups);
    }

    /**
     * Find a local port nobody listens on.
     *
     * @return the port
     * @throws IOException if no port can be opened
     */
    private static int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0, 0, InetAddress.getLoopbackAddress())) {
            return socket.getLocalPort();
        }
    }

}
