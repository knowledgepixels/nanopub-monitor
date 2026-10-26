package ch.tkuhn.nanopub.monitor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.apache.http.client.HttpClient;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Probes each kind of service against a local HTTP server and checks what the scanner records.
 */
class ServerScannerProbeTest {

    private static final IRI REGISTRY_TYPE = Values.iri("https://w3id.org/np/o/service/terms/nanopub-registry-1.0");
    private static final IRI QUERY_TYPE = Values.iri("https://w3id.org/np/o/service/terms/nanopub-query-1.1");
    private static final IRI NANODASH_TYPE = Values.iri("https://w3id.org/np/o/service/terms/nanodash-2.x");
    private static final IRI ROUTER_TYPE = Values.iri("https://w3id.org/np/o/service/terms/nanopub-router-0.x");
    private static final IRI UNKNOWN_TYPE = Values.iri("https://example.org/services#something-else");

    /**
     * What the local server answers on one path.
     *
     * @param status  the HTTP status code
     * @param headers the response headers
     * @param body    the response body, empty for none
     */
    private record Reply(int status, Map<String, String> headers, String body) {
    }

    private final Map<String, Reply> replies = new ConcurrentHashMap<>();
    private final List<ServerSocket> cutOffServers = new CopyOnWriteArrayList<>();
    private HttpServer server;
    private final ServerScanner scanner = new ServerScanner();
    private final HttpClient client = ServerScanner.newHttpClient();

    /**
     * Start a local HTTP server that answers each path with the reply registered for it, or 404.
     *
     * @throws IOException if the server cannot be started
     */
    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/", this::answer);
        server.start();
    }

    /**
     * Stop the local HTTP server and any cut-off servers.
     *
     * @throws IOException if a cut-off server cannot be closed
     */
    @AfterEach
    void stopServer() throws IOException {
        server.stop(0);
        for (ServerSocket socket : cutOffServers) {
            socket.close();
        }
    }

    /**
     * Answer a request with the reply registered for its path, or 404.
     *
     * @param exchange the request to answer
     * @throws IOException if the reply cannot be sent
     */
    private void answer(HttpExchange exchange) throws IOException {
        Reply reply = replies.getOrDefault(exchange.getRequestURI().getPath(), new Reply(404, Map.of(), ""));
        reply.headers().forEach((name, value) -> exchange.getResponseHeaders().add(name, value));
        byte[] body = reply.body().getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(reply.status(), body.length == 0 ? -1 : body.length);
        if (body.length > 0) {
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        }
        exchange.close();
    }

    /**
     * Get the local server's URL for a path.
     *
     * @param path the path
     * @return the URL
     */
    private String url(String path) {
        return "http://127.0.0.1:" + server.getAddress().getPort() + path;
    }

    /**
     * Register what the local server answers on a path.
     *
     * @param path    the path
     * @param status  the HTTP status code
     * @param headers the response headers
     * @param body    the response body, empty for none
     */
    private void reply(String path, int status, Map<String, String> headers, String body) {
        replies.put(path, new Reply(status, headers, body));
    }

    /**
     * Probe a service on the local server.
     *
     * @param path the service's path
     * @param type the service type
     * @return the server data the probe updated
     */
    private ServerData probe(String path, IRI type) {
        ServerData d = TestServers.serverData(url(path), type);
        scanner.testServer(client, d);
        return d;
    }

    /**
     * Start a server that answers one request with a body cut off midway: it announces more than
     * it sends, then closes the connection.
     *
     * @param partialBody the part of the body that is sent
     * @return the server's base URL, with a trailing slash
     * @throws IOException if the server cannot be started
     */
    private String cutOffServer(String partialBody) throws IOException {
        ServerSocket serverSocket = new ServerSocket(0, 0, InetAddress.getLoopbackAddress());
        cutOffServers.add(serverSocket);
        Thread thread = new Thread(() -> answerCutOff(serverSocket, partialBody), "cut-off-server");
        thread.setDaemon(true);
        thread.start();
        return "http://127.0.0.1:" + serverSocket.getLocalPort() + "/";
    }

    /**
     * Read one request's headers, then send a response whose body stops short of its announced length.
     *
     * @param serverSocket the socket to accept the request on
     * @param partialBody  the part of the body that is sent
     */
    private static void answerCutOff(ServerSocket serverSocket, String partialBody) {
        try (Socket socket = serverSocket.accept()) {
            BufferedReader request = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            String line = request.readLine();
            while (line != null && !line.isEmpty()) {
                line = request.readLine();
            }
            byte[] body = partialBody.getBytes(StandardCharsets.UTF_8);
            OutputStream out = socket.getOutputStream();
            out.write(("HTTP/1.1 200 OK\r\nContent-Length: " + (body.length + 1000) + "\r\n\r\n").getBytes(StandardCharsets.US_ASCII));
            out.write(body);
            out.flush();
        } catch (IOException ex) {
            throw new IllegalStateException("The cut-off server failed", ex);
        }
    }

    /**
     * Repeat a line.
     *
     * @param line  the line, without its line break
     * @param count how many times to repeat it
     * @return the lines
     */
    private static String lines(String line, int count) {
        return (line + "\n").repeat(count);
    }

    /**
     * A grlc API counting at least five digits of nanopubs is OK.
     */
    @Test
    void aGrlcApiWithANanopubCountIsOk() {
        reply("/grlc/get_nanopub_count", 200, Map.of(), "count\n12345678\n");

        ServerData d = probe("/grlc/", NanopubService.GRLC_SERVICE_TYPE_IRI);

        assertEquals("OK", d.getStatusString());
        assertNotNull(d.getAvgResponseTimeInMs());
    }

    /**
     * A grlc API answering with something other than a count is broken.
     */
    @Test
    void aGrlcApiWithoutACountIsBroken() {
        reply("/grlc/get_nanopub_count", 200, Map.of(), "count\nnone\n");

        assertEquals("BROKEN", probe("/grlc/", NanopubService.SIGNED_GRLC_SERVICE_TYPE_IRI).getStatusString());
    }

    /**
     * A grlc API answering with an error status is down.
     */
    @Test
    void aGrlcApiAnsweringWithAnErrorIsDown() {
        reply("/grlc/get_nanopub_count", 500, Map.of(), "");

        assertEquals("DOWN", probe("/grlc/", NanopubService.GRLC_SERVICE_TYPE_IRI).getStatusString());
    }

    /**
     * An LDF server listing a hundred nanopublications is OK.
     */
    @Test
    void anLdfServerListingAHundredNanopubsIsOk() {
        reply("/ldf/", 200, Map.of(), lines("<x> <y> <http://www.nanopub.org/nschema#Nanopublication> .", 100));

        assertEquals("OK", probe("/ldf/", NanopubService.LDF_SERVICE_TYPE_IRI).getStatusString());
    }

    /**
     * An LDF server listing too few nanopublications is broken.
     */
    @Test
    void anLdfServerListingTooFewNanopubsIsBroken() {
        reply("/ldf/", 200, Map.of(), lines("<x> <y> <http://www.nanopub.org/nschema#Nanopublication> .", 99));

        assertEquals("BROKEN", probe("/ldf/", NanopubService.SIGNED_LDF_SERVICE_TYPE_IRI).getStatusString());
    }

    /**
     * An LDF server answering with an error status is down.
     */
    @Test
    void anLdfServerAnsweringWithAnErrorIsDown() {
        reply("/ldf/", 503, Map.of(), "");

        assertEquals("DOWN", probe("/ldf/", NanopubService.LDF_SERVICE_TYPE_IRI).getStatusString());
    }

    /**
     * A SPARQL endpoint answering with a hundred rows is OK.
     */
    @Test
    void aSparqlEndpointAnsweringAHundredRowsIsOk() {
        reply("/sparql/", 200, Map.of(), lines("http://example.org/x", 100));

        assertEquals("OK", probe("/sparql/", NanopubService.SIGNED_SPARQL_SERVICE_TYPE_IRI).getStatusString());
    }

    /**
     * A SPARQL endpoint answering with too few rows is broken.
     */
    @Test
    void aSparqlEndpointAnsweringTooFewRowsIsBroken() {
        reply("/sparql/", 200, Map.of(), lines("http://example.org/x", 3));

        assertEquals("BROKEN", probe("/sparql/", NanopubService.SIGNED_SPARQL_SERVICE_TYPE_IRI).getStatusString());
    }

    /**
     * A SPARQL endpoint answering with an error status is down.
     */
    @Test
    void aSparqlEndpointAnsweringWithAnErrorIsDown() {
        reply("/sparql/", 500, Map.of(), "");

        assertEquals("DOWN", probe("/sparql/", NanopubService.SIGNED_SPARQL_SERVICE_TYPE_IRI).getStatusString());
    }

    /**
     * A monitor exporting a server table is OK and reports its version.
     */
    @Test
    void aMonitorExportingAServerTableIsOk() {
        reply("/monitor.csv", 200, Map.of("Nanopub-Monitor-Version", "1.6.0"), "URL,Type\nhttps://registry.example.org/,x\n");

        ServerData d = probe("/monitor", NanopubService.NANOPUB_MONITOR_TYPE_IRI);

        assertEquals("OK", d.getStatusString());
        assertEquals("1.6.0", d.getVersion());
    }

    /**
     * A monitor exporting rows that are not servers is broken.
     */
    @Test
    void aMonitorExportingSomethingElseIsBroken() {
        reply("/monitor.csv", 200, Map.of(), "URL,Type\nnot-a-url,x\n");

        assertEquals("BROKEN", probe("/monitor", NanopubService.NANOPUB_MONITOR_TYPE_IRI).getStatusString());
    }

    /**
     * A monitor answering with an error status is down.
     */
    @Test
    void aMonitorAnsweringWithAnErrorIsDown() {
        reply("/monitor.csv", 404, Map.of(), "");

        assertEquals("DOWN", probe("/monitor", NanopubService.NANOPUB_MONITOR_TYPE_IRI).getStatusString());
    }

    /**
     * A ready registry is OK and reports its version, trust state, count, test status and settings.
     */
    @Test
    void aReadyRegistryReportsWhatItHolds() {
        reply("/registry.json", 200, Map.of(
                "Nanopub-Registry-Status", "ready",
                "Nanopub-Registry-Version", "1.4.0",
                "Nanopub-Registry-Trust-State-Hash", "abcdef0123456789",
                "Nanopub-Registry-Nanopub-Count", " 4200 ",
                "Nanopub-Registry-Test-Instance", "true"),
                "{\"currentSetting\": \"RAcurrent\", \"originalSetting\": null}");

        ServerData d = probe("/registry", REGISTRY_TYPE);

        assertEquals("OK", d.getStatusString());
        assertEquals("1.4.0", d.getVersion());
        assertEquals("abcdef0123456789", d.getTrustStateHash());
        assertEquals(4200L, d.getNanopubCount());
        assertTrue(d.isTestInstance());
        assertEquals("RAcurrent", d.getCurrentSetting());
        assertNull(d.getOriginalSetting());
    }

    /**
     * A registry that is still loading is reported with its status, and its unreadable body is ignored.
     */
    @Test
    void aRegistryThatIsNotReadyReportsItsStatus() {
        reply("/registry.json", 200, Map.of(
                "Nanopub-Registry-Status", "loading",
                "Nanopub-Registry-Nanopub-Count", "many"), "not json");

        ServerData d = probe("/registry", REGISTRY_TYPE);

        assertEquals("STATUS: loading", d.getStatusString());
        assertNull(d.getNanopubCount(), "A count that is not a number is left unknown");
        assertNull(d.getCurrentSetting());
    }

    /**
     * A registry reporting no status at all is taken to be an older one that is ready.
     */
    @Test
    void aRegistryWithoutAStatusIsTakenToBeReady() {
        reply("/registry.json", 200, Map.of(), "{}");

        assertEquals("OK", probe("/registry", REGISTRY_TYPE).getStatusString());
    }

    /**
     * A registry that is updating still serves, and so is OK.
     */
    @Test
    void anUpdatingRegistryIsOk() {
        reply("/registry.json", 200, Map.of("Nanopub-Registry-Status", "updating"), "{}");

        assertEquals("OK", probe("/registry", REGISTRY_TYPE).getStatusString());
    }

    /**
     * A registry answering with an error status is down.
     */
    @Test
    void aRegistryAnsweringWithAnErrorIsDown() {
        reply("/registry.json", 502, Map.of(), "");

        assertEquals("DOWN", probe("/registry", REGISTRY_TYPE).getStatusString());
    }

    /**
     * A ready query instance level with its registry is OK and reports what it loaded.
     */
    @Test
    void aReadyQueryInstanceInSyncIsOk() {
        reply("/query/", 200, Map.of(
                "Nanopub-Query-Status", "READY",
                "Nanopub-Query-Version", "1.9.0",
                "Nanopub-Query-Loaded-Nanopub-Count", "1000",
                "Nanopub-Query-Loaded-Nanopub-Checksum", "checksum0123456789",
                "Nanopub-Query-Registry-Nanopub-Count", "1000",
                "Nanopub-Query-Loader-Last-Success-Age-Seconds", "4"), "");

        ServerData d = probe("/query/", QUERY_TYPE);

        assertEquals("OK", d.getStatusString());
        assertEquals("1.9.0", d.getVersion());
        assertEquals(1000L, d.getNanopubCount());
        assertEquals("checksum0123456789", d.getLoadedNanopubChecksum());
        assertEquals(0L, d.getSyncLag());
        assertEquals(4L, d.getLoaderLastSuccessAgeSeconds());
        assertFalse(d.isTestInstance());
    }

    /**
     * A query instance that is behind and whose loader has stopped is stalled, though it answers.
     */
    @Test
    void aQueryInstanceWhoseLoaderStoppedIsStalled() {
        reply("/query/", 200, Map.of(
                "Nanopub-Query-Status", "READY",
                "Nanopub-Query-Loaded-Nanopub-Count", "900",
                "Nanopub-Query-Registry-Nanopub-Count", "1000",
                "Nanopub-Query-Loader-Last-Success-Age-Seconds", "3600",
                "Nanopub-Query-Test-Instance", "true"), "");

        ServerData d = probe("/query/", QUERY_TYPE);

        assertEquals("STALLED", d.getStatusString());
        assertEquals(1.0f, d.getSuccessRatio(), "A stalled instance is still available");
        assertTrue(d.isTestInstance());
    }

    /**
     * A query instance that is not ready is reported with its status.
     */
    @Test
    void aQueryInstanceThatIsNotReadyReportsItsStatus() {
        reply("/query/", 200, Map.of("Nanopub-Query-Status", "STARTING"), "");

        assertEquals("STATUS: STARTING", probe("/query/", QUERY_TYPE).getStatusString());
    }

    /**
     * A query instance reporting no status at all is said to be missing one.
     */
    @Test
    void aQueryInstanceWithoutAStatusSaysSo() {
        reply("/query/", 200, Map.of(), "");

        assertEquals("STATUS: missing", probe("/query/", QUERY_TYPE).getStatusString());
    }

    /**
     * A query instance answering with an error status is down.
     */
    @Test
    void aQueryInstanceAnsweringWithAnErrorIsDown() {
        reply("/query/", 500, Map.of(), "");

        assertEquals("DOWN", probe("/query/", QUERY_TYPE).getStatusString());
    }

    /**
     * A nanopub server is OK and reports its count and protocol version.
     */
    @Test
    void aNanopubServerReportsItsCountAndProtocol() {
        reply("/server/", 200, Map.of(), "{\"nextNanopubNo\": 77, \"protocolVersion\": \"0.3\"}");

        ServerData d = probe("/server/", NanopubService.NANOPUB_SERVER_TYPE_IRI);

        assertEquals("OK", d.getStatusString());
        assertEquals(77L, d.getNanopubCount());
        assertEquals("0.3", d.getVersion());
    }

    /**
     * A nanopub server with an unreadable body is still OK, as it answered.
     */
    @Test
    void aNanopubServerWithAnUnreadableBodyIsStillOk() {
        reply("/server/", 200, Map.of(), "<html>");

        ServerData d = probe("/server/", NanopubService.NANOPUB_SERVER_TYPE_IRI);

        assertEquals("OK", d.getStatusString());
        assertNull(d.getNanopubCount());
    }

    /**
     * A nanopub server answering with an error status is down.
     */
    @Test
    void aNanopubServerAnsweringWithAnErrorIsDown() {
        reply("/server/", 500, Map.of(), "");

        assertEquals("DOWN", probe("/server/", NanopubService.NANOPUB_SERVER_TYPE_IRI).getStatusString());
    }

    /**
     * A Nanodash instance is OK and reports its version.
     */
    @Test
    void aNanodashInstanceReportsItsVersion() {
        reply("/nanodash/", 200, Map.of("Nanodash-Version", "3.0"), "");

        ServerData d = probe("/nanodash/", NANODASH_TYPE);

        assertEquals("OK", d.getStatusString());
        assertEquals("3.0", d.getVersion());
    }

    /**
     * A Nanodash instance answering with an error status is down.
     */
    @Test
    void aNanodashInstanceAnsweringWithAnErrorIsDown() {
        reply("/nanodash/", 500, Map.of(), "");

        assertEquals("DOWN", probe("/nanodash/", NANODASH_TYPE).getStatusString());
    }

    /**
     * A router is probed at its status page, with or without a trailing slash in its URL.
     */
    @Test
    void aRouterIsProbedAtItsStatusPage() {
        reply("/router/status.json", 200, Map.of("Nanopub-Router-Version", "0.3.0"), "{}");

        ServerData withSlash = probe("/router/", ROUTER_TYPE);
        ServerData withoutSlash = probe("/router", ROUTER_TYPE);

        assertEquals("OK", withSlash.getStatusString());
        assertEquals("0.3.0", withSlash.getVersion());
        assertEquals("OK", withoutSlash.getStatusString());
    }

    /**
     * A failing router is down but still identified by the version it reports.
     */
    @Test
    void aFailingRouterStillReportsItsVersion() {
        reply("/router/status.json", 503, Map.of("Nanopub-Router-Version", "0.3.0"), "");

        ServerData d = probe("/router/", ROUTER_TYPE);

        assertEquals("DOWN", d.getStatusString());
        assertEquals("0.3.0", d.getVersion());
    }

    /**
     * A service of an unknown type is OK as long as it answers.
     */
    @Test
    void aServiceOfAnUnknownTypeIsOkWhenItAnswers() {
        reply("/other/", 200, Map.of(), "hello");

        assertEquals("OK", probe("/other/", UNKNOWN_TYPE).getStatusString());
    }

    /**
     * A service of an unknown type answering with an error status is down.
     */
    @Test
    void aServiceOfAnUnknownTypeAnsweringWithAnErrorIsDown() {
        assertEquals("DOWN", probe("/missing/", UNKNOWN_TYPE).getStatusString());
    }

    /**
     * A service nobody listens for is inaccessible, whatever its type.
     *
     * @throws IOException if no free port can be found
     */
    @Test
    void aServiceNobodyListensForIsInaccessible() throws IOException {
        String closed = "http://127.0.0.1:" + freePort() + "/";
        for (IRI type : List.of(NanopubService.GRLC_SERVICE_TYPE_IRI, NanopubService.LDF_SERVICE_TYPE_IRI,
                NanopubService.SIGNED_SPARQL_SERVICE_TYPE_IRI, NanopubService.NANOPUB_MONITOR_TYPE_IRI, REGISTRY_TYPE,
                QUERY_TYPE, NanopubService.NANOPUB_SERVER_TYPE_IRI, NANODASH_TYPE, ROUTER_TYPE, UNKNOWN_TYPE)) {
            ServerData d = TestServers.serverData(closed, type);

            scanner.testServer(client, d);

            assertEquals("INACCESSIBLE", d.getStatusString(), "for type " + type);
        }
    }

    /**
     * A candidate URL is a test instance only if a registry there says it is one.
     */
    @Test
    void aCandidateIsATestInstanceOnlyIfItsRegistrySaysSo() {
        reply("/test-registry.json", 200, Map.of("Nanopub-Registry-Test-Instance", "true"), "{}");
        reply("/prod-registry.json", 200, Map.of("Nanopub-Registry-Test-Instance", "false"), "{}");

        assertTrue(scanner.isTestInstance(client, TestServers.service(url("/test-registry"), REGISTRY_TYPE)));
        assertFalse(scanner.isTestInstance(client, TestServers.service(url("/prod-registry"), REGISTRY_TYPE)));
        assertFalse(scanner.isTestInstance(client, TestServers.service(url("/nothing-here"), REGISTRY_TYPE)));
    }

    /**
     * A Query candidate is a test instance if it says so itself, or if the registry it loads
     * from is one.
     */
    @Test
    void aQueryCandidateIsATestInstanceIfItOrItsRegistrySaysSo() {
        reply("/staging-query/", 200, Map.of("Nanopub-Query-Test-Instance", "true"), "");
        reply("/paired-query/", 200, Map.of("Nanopub-Query-Registry-Test-Instance", "true"), "");
        reply("/prod-query/", 200, Map.of("Nanopub-Query-Test-Instance", "false"), "");
        reply("/down-query/", 503, Map.of("Nanopub-Query-Test-Instance", "true"), "");

        assertTrue(scanner.isTestInstance(client, TestServers.service(url("/staging-query/"), QUERY_TYPE)));
        assertTrue(scanner.isTestInstance(client, TestServers.service(url("/paired-query/"), QUERY_TYPE)));
        assertFalse(scanner.isTestInstance(client, TestServers.service(url("/prod-query/"), QUERY_TYPE)));
        assertFalse(scanner.isTestInstance(client, TestServers.service(url("/down-query/"), QUERY_TYPE)));
    }

    /**
     * A Query candidate is probed at its own URL, not at a registry's status URL.
     */
    @Test
    void aQueryCandidateIsNotTakenForARegistry() {
        reply("/query/", 200, Map.of(), "");
        reply("/query/.json", 200, Map.of("Nanopub-Registry-Test-Instance", "true"), "{}");

        assertFalse(scanner.isTestInstance(client, TestServers.service(url("/query/"), QUERY_TYPE)));
    }

    /**
     * A candidate URL nobody listens at is no test instance.
     *
     * @throws IOException if no free port can be found
     */
    @Test
    void aCandidateNobodyListensAtIsNoTestInstance() throws IOException {
        String closed = "http://127.0.0.1:" + freePort() + "/";
        assertFalse(scanner.isTestInstance(client, TestServers.service(closed + "registry", REGISTRY_TYPE)));
        assertFalse(scanner.isTestInstance(client, TestServers.service(closed + "query/", QUERY_TYPE)));
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

    /**
     * An LDF server whose body is cut off midway is broken.
     *
     * @throws IOException if the cut-off server cannot be started
     */
    @Test
    void anLdfServerCutOffMidwayIsBroken() throws IOException {
        ServerData d = TestServers.serverData(cutOffServer("<x> <y> <z> .\n") + "ldf/", NanopubService.LDF_SERVICE_TYPE_IRI);

        scanner.testServer(client, d);

        assertEquals("BROKEN", d.getStatusString());
    }

    /**
     * Rows that cannot be read as CSV leave the probes reading a table broken.
     */
    @Test
    void unreadableCsvIsBroken() {
        reply("/grlc/get_nanopub_count", 200, Map.of(), "count\n\"12345");
        reply("/monitor.csv", 200, Map.of(), "URL,Type\n\"https://unclosed");
        reply("/sparql/", 200, Map.of(), "x\n\"http://unclosed");

        assertEquals("BROKEN", probe("/grlc/", NanopubService.GRLC_SERVICE_TYPE_IRI).getStatusString());
        assertEquals("BROKEN", probe("/sparql/", NanopubService.SIGNED_SPARQL_SERVICE_TYPE_IRI).getStatusString());
        assertEquals("BROKEN", probe("/monitor", NanopubService.NANOPUB_MONITOR_TYPE_IRI).getStatusString());
    }

    /**
     * A table with a header row and nothing else is broken.
     */
    @Test
    void aTableWithOnlyAHeaderIsBroken() {
        reply("/grlc/get_nanopub_count", 200, Map.of(), "count\n");
        reply("/monitor.csv", 200, Map.of(), "URL,Type\n");

        ServerData grlc = probe("/grlc/", NanopubService.GRLC_SERVICE_TYPE_IRI);
        ServerData monitor = probe("/monitor", NanopubService.NANOPUB_MONITOR_TYPE_IRI);

        assertEquals("BROKEN", grlc.getStatusString());
        assertEquals(0.0f, grlc.getSuccessRatio());
        assertEquals("BROKEN", monitor.getStatusString());
        assertEquals(0.0f, monitor.getSuccessRatio());
    }

    /**
     * An empty body has no table at all, and is broken.
     */
    @Test
    void anEmptyTableIsBroken() {
        reply("/grlc/get_nanopub_count", 200, Map.of(), "");
        reply("/monitor.csv", 200, Map.of(), "");

        assertEquals("BROKEN", probe("/grlc/", NanopubService.GRLC_SERVICE_TYPE_IRI).getStatusString());
        assertEquals("BROKEN", probe("/monitor", NanopubService.NANOPUB_MONITOR_TYPE_IRI).getStatusString());
    }

    /**
     * A server that was fine on the last scan and now answers with only a header no longer
     * shows its old status.
     */
    @Test
    void aServerThatStopsSendingRowsLosesItsOkStatus() {
        reply("/grlc/get_nanopub_count", 200, Map.of(), "count\n12345678\n");
        ServerData grlc = probe("/grlc/", NanopubService.GRLC_SERVICE_TYPE_IRI);
        assertEquals("OK", grlc.getStatusString());

        reply("/grlc/get_nanopub_count", 200, Map.of(), "count\n");
        scanner.testServer(client, grlc);

        assertEquals("BROKEN", grlc.getStatusString());
        assertEquals(0.5f, grlc.getSuccessRatio());
    }

    /**
     * A table cut off before its first data row is broken.
     *
     * @throws IOException if the cut-off servers cannot be started
     */
    @Test
    void aTableCutOffBeforeItsFirstRowIsBroken() throws IOException {
        ServerData grlc = TestServers.serverData(cutOffServer("count\n"), NanopubService.GRLC_SERVICE_TYPE_IRI);
        ServerData monitor = TestServers.serverData(cutOffServer("URL,Type\n"), NanopubService.NANOPUB_MONITOR_TYPE_IRI);

        scanner.testServer(client, grlc);
        scanner.testServer(client, monitor);

        assertEquals("BROKEN", grlc.getStatusString());
        assertEquals("BROKEN", monitor.getStatusString());
    }

    /**
     * A scan cycle refreshes the list and tests every server on it.
     */
    @Test
    void aScanCycleTestsEveryServer() {
        reply("/nanodash/", 200, Map.of("Nanodash-Version", "3.0"), "");
        reply("/router/status.json", 503, Map.of(), "");
        NanopubService nanodash = TestServers.service(url("/nanodash/"), NANODASH_TYPE);
        NanopubService router = TestServers.service(url("/router/"), ROUTER_TYPE);
        ServerList sl = TestServers.serverList(nanodash, router);

        scanner.run(LoggerFactory.getLogger(ServerScannerProbeTest.class));

        assertEquals(2, sl.getServerCount());
        assertEquals("OK", sl.getServerData(nanodash).getStatusString());
        assertEquals("3.0", sl.getServerData(nanodash).getVersion());
        assertEquals("DOWN", sl.getServerData(router).getStatusString());
    }

    /**
     * Discovery adds the test instances the probe confirms.
     */
    @Test
    void discoveryAddsConfirmedTestInstances() {
        TestServers.locate("https://test.registry.example.org/", ServerIpInfo.empty);
        ServerList sl = TestServers.serverList(TestServers.service("https://registry.example.org/", REGISTRY_TYPE));

        scanner.discoverTestInstances(candidate -> true);

        assertEquals(2, sl.getServerCount());
    }

}
