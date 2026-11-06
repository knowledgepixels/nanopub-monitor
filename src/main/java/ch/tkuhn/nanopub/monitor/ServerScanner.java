package ch.tkuhn.nanopub.monitor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.lang.time.StopWatch;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.wicket.util.thread.ICode;
import org.apache.wicket.util.thread.Task;
import org.nanopub.extra.server.NanopubServerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * A daemon task that periodically scans all registered nanopublication servers to check their status.
 */
public class ServerScanner implements ICode {

    private static ServerScanner singleton;
    private static Task scanTask;
    private static Random random = new Random();
    private static volatile boolean daemonEnabled = true;

    /**
     * Allow or prevent {@link #initDaemon()} from starting the scanner daemon. Rendering a page
     * starts the daemon, so tests that render one switch it off to stay off the network.
     *
     * @param enabled false to make {@link #initDaemon()} do nothing
     */
    static void setDaemonEnabled(boolean enabled) {
        daemonEnabled = enabled;
    }

    /**
     * Initialize and start the server scanner daemon if not already running.
     * If the existing daemon has not shown signs of life for 10 minutes, it will be restarted.
     */
    public static void initDaemon() {
        if (!daemonEnabled) {
            return;
        }
        if (singleton != null) {
            if (singleton.aliveAtTime + 10 * 60 * 1000 < System.currentTimeMillis()) {
                singleton.logger.info("Server-scanner daemon has shown no sign of life for over 10 minutes (last alive at {}); restarting it", new Date(singleton.aliveAtTime));
                singleton = null;
                scanTask.interrupt();
            } else {
                return;
            }
        }
        scanTask = new Task("server-scanner");
        scanTask.setDaemon(true);
        singleton = new ServerScanner();
        scanTask.run(Duration.ofSeconds(MonitorConf.get().getScanFreq()), singleton);
    }

    private Logger logger = LoggerFactory.getLogger(ServerScanner.class);
    private long aliveAtTime;

    /**
     * Creates a scanner that logs to its own class logger until {@link #run(Logger)} hands it the task's.
     */
    ServerScanner() {
        stillAlive();
    }

    @Override
    public void run(Logger logger) {
        this.logger = logger;
        logger.info("Starting nanopub server scan cycle");
        ServerList.get().refresh();
        stillAlive();
        try (CloseableHttpClient c = newHttpClient()) {
            discoverTestInstances(candidate -> isTestInstance(c, candidate));
            stillAlive();
            logger.debug("Server list refreshed: {} servers known, beginning tests", ServerList.get().getServerCount());
            testServers(c);
        } catch (IOException ex) {
            logger.warn("Could not close the scan cycle's HTTP client: {}", ex.getMessage());
        }
    }

    /**
     * Creates the HTTP client a scan cycle probes the servers with.
     *
     * @return a client with the scanner's timeouts for leasing a connection, connecting and reading
     */
    static CloseableHttpClient newHttpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(10 * 1000)
                .setConnectTimeout(10 * 1000)
                .setSocketTimeout(15 * 1000)
                .build();
        return HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
    }

    /**
     * Look for test instances of the registries and Query instances the network announces, and
     * add the ones that answer. Test deployments are not announced in service intro
     * nanopublications, so they cannot be queried for; the list proposes where to look and the
     * services themselves confirm what is found.
     *
     * @param probe the probe to confirm a candidate with
     */
    void discoverTestInstances(ServerList.TestInstanceProbe probe) {
        int found = ServerList.get().addTestInstances(probe);
        if (found > 0) {
            logger.info("Discovered {} test instance(s) mirroring announced services", found);
        }
    }

    /**
     * Probe a candidate for a service of its type that reports itself as a test instance: a
     * registry at its status URL, a Query instance at its own URL. Anything else — nothing
     * listening, an error status, or a service that does not claim to be a test instance — is
     * not one.
     *
     * @param c         the HTTP client to probe with
     * @param candidate the candidate test instance, with the type of the service it mirrors
     * @return true if a service of that type answers there and reports itself as a test instance
     */
    boolean isTestInstance(CloseableHttpClient c, NanopubService candidate) {
        String serviceUrl = candidate.getServiceIri().stringValue();
        if (candidate.hasTypePrefix(NanopubService.NANOPUB_QUERY_TYPE_IRI)) {
            return probeForTestInstance(c, serviceUrl, ServerScanner::saysQueryTestInstance);
        }
        return probeForTestInstance(c, serviceUrl + ".json", ServerScanner::saysTestInstance);
    }

    /**
     * Request a status URL and ask the response whether it comes from a test instance.
     *
     * @param c                the HTTP client to probe with
     * @param statusUrl        the URL to request
     * @param saysTestInstance whether a response comes from a test instance
     * @return true if the request went through and the response says so
     */
    private boolean probeForTestInstance(CloseableHttpClient c, String statusUrl, Predicate<HttpResponse> saysTestInstance) {
        logger.debug("Probing {} for a test instance...", statusUrl);
        try (CloseableHttpResponse resp = c.execute(new HttpGet(statusUrl))) {
            return saysTestInstance.test(resp);
        } catch (Exception ex) {
            logger.debug("No test instance at '{}': {}", statusUrl, ex.getMessage());
            return false;
        }
    }

    /**
     * Read past a table's header row to its first data row. A table that ends before it — no
     * rows at all, only the header, or a body cut off before the first data row — has none.
     *
     * @param csvReader the reader positioned at the start of the table
     * @return the first data row, or null if the table has none
     * @throws IOException            if the table cannot be read
     * @throws CsvValidationException if a row is not valid CSV
     */
    static String[] firstDataRow(CSVReader csvReader) throws IOException, CsvValidationException {
        if (csvReader.readNext() == null) {
            return null;
        }
        return csvReader.readNext();
    }

    /**
     * Whether a response comes from a registry calling itself a test instance.
     *
     * @param resp the response to a registry status request
     * @return true if the request succeeded and the test-instance header says so
     */
    static boolean saysTestInstance(HttpResponse resp) {
        if (!wasSuccessful(resp)) {
            return false;
        }
        return "true".equalsIgnoreCase(headerValue(resp, "Nanopub-Registry-Test-Instance"));
    }

    /**
     * Whether a response comes from a Query instance that is a test instance in either
     * sense: it declares itself one via {@code Nanopub-Query-Test-Instance}, or the
     * registry it loads from declares itself one, which Query forwards as
     * {@code Nanopub-Query-Registry-Test-Instance}. Both are needed, because a staging
     * instance normally mirrors a production registry and can therefore only say so
     * itself (knowledgepixels/nanopub-query#200), while a Query instance paired with a
     * test registry only has the forwarded header.
     *
     * @param resp the response to a Query status request
     * @return true if the request succeeded and either header says so
     */
    static boolean saysQueryTestInstance(HttpResponse resp) {
        if (!wasSuccessful(resp)) {
            return false;
        }
        return "true".equalsIgnoreCase(headerValue(resp, "Nanopub-Query-Test-Instance"))
                || "true".equalsIgnoreCase(headerValue(resp, "Nanopub-Query-Registry-Test-Instance"));
    }

    private void testServers(CloseableHttpClient c) {
        int threads = Math.max(1, MonitorConf.get().getScanThreads());
        ExecutorService pool = Executors.newFixedThreadPool(threads, r -> {
            Thread t = new Thread(r, "server-scanner-worker");
            t.setDaemon(true);
            return t;
        });
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (ServerData d : ServerList.get().getServerData()) {
                futures.add(pool.submit(() -> testServer(c, d)));
            }
            for (Future<?> f : futures) {
                try {
                    f.get();
                } catch (Exception ex) {
                    logger.error("Unexpected error in server test task (this indicates a bug, since testServer() should catch its own exceptions)", ex);
                }
            }
        } finally {
            pool.shutdown();
            try {
                pool.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        logger.info("Scan cycle complete: tested {} servers", ServerList.get().getServerCount());
    }

    /**
     * Probe one server in the way its service type calls for, and record the outcome and
     * whatever it reports about itself in its server data.
     *
     * @param c the HTTP client to probe with
     * @param d the server data to probe and update
     */
    void testServer(CloseableHttpClient c, ServerData d) {
        logger.info("Testing server {}...", d.getServiceId());
        stillAlive();
//			if (d.hasServiceType(NanopubService.NANOPUB_SERVER_TYPE_IRI)) {
//				ServerInfo i = (ServerInfo) d.getServerInfo();
//				if (i == null) {
//					d.reportTestFailure("DOWN");
//					continue;
//				}
//				if (i.getNextNanopubNo() == 0) continue;
//				try {
//					long npNo = (long) (random.nextDouble() * (i.getNextNanopubNo()));
//					logger.info("Trying to retrieve nanopub number " + npNo);
//					int pageNo = (int) (npNo / i.getPageSize()) + 1;
//					int rowNo = (int) (npNo % i.getPageSize());
//					int r = 0;
//					for (String nanopubUri : NanopubServerUtils.loadNanopubUriList(i, pageNo)) {
//						if (rowNo < r) {
//							r++;
//							continue;
//						}
//						String ac = TrustyUriUtils.getArtifactCode(nanopubUri);
//						HttpGet get = new HttpGet(i.getPublicUrl() + ac);
//						get.setHeader("Accept", "application/trig");
//						StopWatch watch = new StopWatch();
//						watch.start();
//						HttpResponse resp = c.execute(get);
//						watch.stop();
//						if (!wasSuccessful(resp)) {
//							logger.info("Test failed. HTTP code " + resp.getStatusLine().getStatusCode());
//							d.reportTestFailure("DOWN");
//						} else {
//							InputStream in = resp.getEntity().getContent();
//							Nanopub np = new NanopubImpl(in, RDFFormat.TRIG);
//							if (TrustyNanopubUtils.isValidTrustyNanopub(np)) {
//								d.reportTestSuccess(watch.getTime());
//							} else {
//								logger.info("Test failed. Not a trusty nanopub: " + np.getUri());
//								d.reportTestFailure("BROKEN");
//							}
//						}
//						break;
//					}
//				} catch (Exception ex) {
//					ex.printStackTrace();
//					d.reportTestFailure("INACCESSIBLE");
//				}
//			} else
        if (d.hasServiceType(NanopubService.GRLC_SERVICE_TYPE_IRI) || d.hasServiceType(NanopubService.SIGNED_GRLC_SERVICE_TYPE_IRI)) {
            logger.info("Trying to access {}get_nanopub_count...", d.getServiceId());
            try {
                HttpGet get = new HttpGet(d.getServiceId() + "get_nanopub_count");
                get.setHeader("Accept", "text/csv");
                StopWatch watch = new StopWatch();
                watch.start();
                try (CloseableHttpResponse resp = c.execute(get)) {
                    watch.stop();
                    if (!wasSuccessful(resp)) {
                        logger.info("Test failed for server '{}'. HTTP code {}", d.getServiceId(), resp.getStatusLine().getStatusCode());
                        d.reportTestFailure("DOWN");
                    } else {
                        CSVReader csvReader = null;
                        try {
                            csvReader = new CSVReader(new BufferedReader(new InputStreamReader(resp.getEntity().getContent())));
                            String[] row = firstDataRow(csvReader);
                            if (row != null && row[0].matches("[0-9]{5,}")) {
                                d.reportTestSuccess(watch.getTime());
                            } else {
                                d.reportTestFailure("BROKEN");
                            }
                        } catch (Exception ex) {
                            logger.info("Test failed for server '{}': {}", d.getServiceId(), ex.getMessage(), ex);
                            d.reportTestFailure("BROKEN");
                        } finally {
                            if (csvReader != null) {
                                csvReader.close();
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                logger.error("Test failed for server '{}': {}", d.getServiceId(), ex.getMessage(), ex);
                d.reportTestFailure("INACCESSIBLE");
            }
        } else if (d.hasServiceType(NanopubService.LDF_SERVICE_TYPE_IRI) || d.hasServiceType(NanopubService.SIGNED_LDF_SERVICE_TYPE_IRI)) {
            logger.info("Trying to access {}?object=http%3A%2F%2Fwww.nanopub.org%2Fnschema%23Nanopublication...", d.getServiceId());
            try {
                HttpGet get = new HttpGet(d.getServiceId() + "?object=http%3A%2F%2Fwww.nanopub.org%2Fnschema%23Nanopublication");
                get.setHeader("Accept", "application/n-quads");
                StopWatch watch = new StopWatch();
                watch.start();
                try (CloseableHttpResponse resp = c.execute(get)) {
                    watch.stop();
                    if (!wasSuccessful(resp)) {
                        logger.info("Test failed for server '{}'. HTTP code {}", d.getServiceId(), resp.getStatusLine().getStatusCode());
                        d.reportTestFailure("DOWN");
                    } else {
                        BufferedReader reader = null;
                        try {
                            reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
                            String line = null;
                            int count = 0;
                            while ((line = reader.readLine()) != null) {
                                if (line.contains(" <http://www.nanopub.org/nschema#Nanopublication> ")) {
                                    count = count + 1;
                                }
                            }
                            if (count >= 100) {
                                d.reportTestSuccess(watch.getTime());
                            } else {
                                d.reportTestFailure("BROKEN");
                            }
                        } catch (Exception ex) {
                            logger.info("Test failed for server '{}': {}", d.getServiceId(), ex.getMessage(), ex);
                            d.reportTestFailure("BROKEN");
                        } finally {
                            if (reader != null) {
                                reader.close();
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                logger.error("Test failed for server '{}': {}", d.getServiceId(), ex.getMessage(), ex);
                d.reportTestFailure("INACCESSIBLE");
            }
        } else if (d.hasServiceType(NanopubService.SIGNED_SPARQL_SERVICE_TYPE_IRI)) {
            String urlSuffix = "?query=select+%3Fx+where+%7B%3Fx+a+%3Fc%7D+limit+100&format=text%2Fcsv";
            logger.info("Trying to access {}{}...", d.getServiceId(), urlSuffix);
            try {
                HttpGet get = new HttpGet(d.getServiceId() + urlSuffix);
                get.setHeader("Accept", "text/csv");
                StopWatch watch = new StopWatch();
                watch.start();
                try (CloseableHttpResponse resp = c.execute(get)) {
                    watch.stop();
                    if (!wasSuccessful(resp)) {
                        logger.info("Test failed for server '{}'. HTTP code {}", d.getServiceId(), resp.getStatusLine().getStatusCode());
                        d.reportTestFailure("DOWN");
                    } else {
                        CSVReader csvReader = null;
                        try {
                            csvReader = new CSVReader(new BufferedReader(new InputStreamReader(resp.getEntity().getContent())));
                            String[] line;
                            int count = 0;
                            while ((csvReader.readNext()) != null) {
                                count++;
                            }
                            if (count >= 100) {
                                d.reportTestSuccess(watch.getTime());
                            } else {
                                d.reportTestFailure("BROKEN");
                            }
                        } catch (Exception ex) {
                            logger.error("Test failed for server '{}': {}", d.getServiceId(), ex.getMessage(), ex);
                            d.reportTestFailure("BROKEN");
                        } finally {
                            if (csvReader != null) {
                                csvReader.close();
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                logger.error("Test failed for server '{}': {}", d.getServiceId(), ex.getMessage(), ex);
                d.reportTestFailure("INACCESSIBLE");
            }
        } else if (d.hasServiceType(NanopubService.NANOPUB_MONITOR_TYPE_IRI)) {
            logger.info("Trying to access {}.csv...", d.getServiceId());
            try {
                HttpGet get = new HttpGet(d.getServiceId() + ".csv");
                get.setHeader("Accept", "text/csv");
                StopWatch watch = new StopWatch();
                watch.start();
                try (CloseableHttpResponse resp = c.execute(get)) {
                    watch.stop();
                    if (!wasSuccessful(resp)) {
                        logger.info("Test failed for server '{}'. HTTP code {}", d.getServiceId(), resp.getStatusLine().getStatusCode());
                        d.reportTestFailure("DOWN");
                    } else {
                        d.setVersion(headerValue(resp, "Nanopub-Monitor-Version"));
                        CSVReader csvReader = null;
                        try {
                            csvReader = new CSVReader(new BufferedReader(new InputStreamReader(resp.getEntity().getContent())));
                            String[] row = firstDataRow(csvReader);
                            if (row != null && row[0].startsWith("http")) {
                                d.reportTestSuccess(watch.getTime());
                            } else {
                                d.reportTestFailure("BROKEN");
                            }
                        } catch (Exception ex) {
                            logger.info("Test failed for server '{}': {}", d.getServiceId(), ex.getMessage(), ex);
                            d.reportTestFailure("BROKEN");
                        } finally {
                            if (csvReader != null) {
                                csvReader.close();
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                logger.error("Test failed for server '{}': {}", d.getServiceId(), ex.getMessage(), ex);
                d.reportTestFailure("INACCESSIBLE");
            }
        } else if (d.hasServiceTypePrefix(NanopubService.NANOPUB_REGISTRY_TYPE_IRI)) {
            logger.info("Probing registry status at {}.json...", d.getServiceId());
            try {
                HttpGet get = new HttpGet(d.getServiceId() + ".json");
                StopWatch watch = new StopWatch();
                watch.start();
                try (CloseableHttpResponse resp = c.execute(get)) {
                    watch.stop();
                    if (!wasSuccessful(resp)) {
                        logger.info("Test failed for server '{}'. HTTP code {}", d.getServiceId(), resp.getStatusLine().getStatusCode());
                        d.reportTestFailure("DOWN");
                    } else {
                        d.setVersion(headerValue(resp, "Nanopub-Registry-Version"));
                        d.setTrustStateHash(headerValue(resp, "Nanopub-Registry-Trust-State-Hash"));
                        d.setNanopubCount(parseLongOrNull(headerValue(resp, "Nanopub-Registry-Nanopub-Count")));
                        d.setTestInstance("true".equalsIgnoreCase(headerValue(resp, "Nanopub-Registry-Test-Instance")));
                        try (Reader r = new InputStreamReader(resp.getEntity().getContent())) {
                            JsonObject body = JsonParser.parseReader(r).getAsJsonObject();
                            d.setCurrentSetting(jsonString(body, "currentSetting"));
                            d.setOriginalSetting(jsonString(body, "originalSetting"));
                        } catch (Exception ex) {
                            logger.info("Could not parse registry JSON body for server '{}': {}", d.getServiceId(), ex.getMessage());
                        }
                        String headerStatus = headerValue(resp, "Nanopub-Registry-Status");
                        if (!NanopubServerUtils.isReadyRegistryStatus(headerStatus)) {
                            d.reportTestFailure("STATUS: " + (headerStatus == null ? "missing" : headerStatus));
                        } else {
                            d.reportTestSuccess(watch.getTime());
                        }
                    }
                }
            } catch (Exception ex) {
                logger.error("Test failed for server '{}': {}", d.getServiceId(), ex.getMessage(), ex);
                d.reportTestFailure("INACCESSIBLE");
            }
        } else if (d.hasServiceTypePrefix(NanopubService.NANOPUB_QUERY_TYPE_IRI)) {
            logger.info("Probing query status at {}...", d.getServiceId());
            try {
                HttpGet get = new HttpGet(d.getServiceId());
                StopWatch watch = new StopWatch();
                watch.start();
                try (CloseableHttpResponse resp = c.execute(get)) {
                    watch.stop();
                    if (!wasSuccessful(resp)) {
                        logger.info("Test failed for server '{}'. HTTP code {}", d.getServiceId(), resp.getStatusLine().getStatusCode());
                        d.reportTestFailure("DOWN");
                    } else {
                        d.setVersion(headerValue(resp, "Nanopub-Query-Version"));
                        d.setNanopubCount(parseLongOrNull(headerValue(resp, "Nanopub-Query-Loaded-Nanopub-Count")));
                        d.setLoadedNanopubChecksum(headerValue(resp, "Nanopub-Query-Loaded-Nanopub-Checksum"));
                        // Both counts come from the same response, so one request tells us how far
                        // this instance trails its own registry — no cross-instance comparison, and
                        // so still meaningful when every instance is equally behind. The loader-age
                        // header is absent on instances predating it; the column stays empty there.
                        d.updateSyncHealth(
                                parseLongOrNull(headerValue(resp, "Nanopub-Query-Registry-Nanopub-Count")),
                                parseLongOrNull(headerValue(resp, "Nanopub-Query-Loader-Last-Success-Age-Seconds")));
                        d.setTestInstance(saysQueryTestInstance(resp));
                        String headerStatus = headerValue(resp, "Nanopub-Query-Status");
                        if (headerStatus == null || !headerStatus.equalsIgnoreCase("READY")) {
                            d.reportTestFailure("STATUS: " + (headerStatus == null ? "missing" : headerStatus));
                        } else {
                            d.reportTestSuccess(watch.getTime());
                            // A stalled instance still reports READY and still answers quickly —
                            // that is exactly why the 2026-07-31 stall went unnoticed for hours.
                            // Overrides the status only after the success is recorded, so the OK
                            // ratio keeps measuring availability.
                            if (d.isSyncStalled()) {
                                d.reportStalled();
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                logger.error("Test failed for server '{}': {}", d.getServiceId(), ex.getMessage(), ex);
                d.reportTestFailure("INACCESSIBLE");
            }
        } else if (d.hasServiceType(NanopubService.NANOPUB_SERVER_TYPE_IRI)) {
            logger.info("Probing nanopub-server info at {}...", d.getServiceId());
            try {
                HttpGet get = new HttpGet(d.getServiceId());
                get.setHeader("Accept", "application/json");
                StopWatch watch = new StopWatch();
                watch.start();
                try (CloseableHttpResponse resp = c.execute(get)) {
                    watch.stop();
                    if (!wasSuccessful(resp)) {
                        logger.info("Test failed for server '{}'. HTTP code {}", d.getServiceId(), resp.getStatusLine().getStatusCode());
                        d.reportTestFailure("DOWN");
                    } else {
                        try (Reader r = new InputStreamReader(resp.getEntity().getContent())) {
                            JsonObject body = JsonParser.parseReader(r).getAsJsonObject();
                            if (body.has("nextNanopubNo") && !body.get("nextNanopubNo").isJsonNull()) {
                                d.setNanopubCount(body.get("nextNanopubNo").getAsLong());
                            }
                            d.setVersion(jsonString(body, "protocolVersion"));
                        } catch (Exception ex) {
                            logger.info("Could not parse nanopub-server JSON body for server '{}': {}", d.getServiceId(), ex.getMessage());
                        }
                        d.reportTestSuccess(watch.getTime());
                    }
                }
            } catch (Exception ex) {
                logger.error("Test failed for server '{}': {}", d.getServiceId(), ex.getMessage(), ex);
                d.reportTestFailure("INACCESSIBLE");
            }
        } else if (d.hasServiceTypePrefix(NanopubService.NANODASH_TYPE_IRI)) {
            logger.info("Probing Nanodash status at {}...", d.getServiceId());
            try {
                HttpGet get = new HttpGet(d.getServiceId());
                StopWatch watch = new StopWatch();
                watch.start();
                try (CloseableHttpResponse resp = c.execute(get)) {
                    watch.stop();
                    if (!wasSuccessful(resp)) {
                        logger.info("Test failed for server '{}'. HTTP code {}", d.getServiceId(), resp.getStatusLine().getStatusCode());
                        d.reportTestFailure("DOWN");
                    } else {
                        d.setVersion(headerValue(resp, "Nanodash-Version"));
                        d.reportTestSuccess(watch.getTime());
                    }
                }
            } catch (Exception ex) {
                logger.error("Test failed for server '{}': {}", d.getServiceId(), ex.getMessage(), ex);
                d.reportTestFailure("INACCESSIBLE");
            }
        } else if (d.hasServiceTypePrefix(NanopubService.NANOPUB_ROUTER_TYPE_IRI)) {
            // Probed at /status.json, which answers 200 with a JSON summary for as long as the
            // process is serving, so the status column measures the router itself.
            //
            // Deliberately not /healthz: that endpoint reports whether the router still holds a
            // fresh copy of *this monitor's* feed, and turns 503 when it does not. Grading routers
            // on it would make a monitor outage look like every router failing, and would report
            // that verdict from the very service that was down.
            String statusUrl = d.getServiceId() + (d.getServiceId().endsWith("/") ? "" : "/") + "status.json";
            logger.info("Probing router status at {}...", statusUrl);
            try {
                HttpGet get = new HttpGet(statusUrl);
                StopWatch watch = new StopWatch();
                watch.start();
                try (CloseableHttpResponse resp = c.execute(get)) {
                    watch.stop();
                    // Read before the status check: the router sets the version header on every
                    // response, so a failing instance can still be identified by build.
                    d.setVersion(headerValue(resp, "Nanopub-Router-Version"));
                    if (!wasSuccessful(resp)) {
                        logger.info("Test failed for server '{}'. HTTP code {}", d.getServiceId(), resp.getStatusLine().getStatusCode());
                        d.reportTestFailure("DOWN");
                    } else {
                        d.reportTestSuccess(watch.getTime());
                    }
                }
            } catch (Exception ex) {
                logger.error("Test failed for server '{}': {}", d.getServiceId(), ex.getMessage(), ex);
                d.reportTestFailure("INACCESSIBLE");
            }
        } else {
            logger.info("Trying to access {}...", d.getServiceId());
            try {
                HttpGet get = new HttpGet(d.getServiceId());
                StopWatch watch = new StopWatch();
                watch.start();
                try (CloseableHttpResponse resp = c.execute(get)) {
                    watch.stop();
                    if (!wasSuccessful(resp)) {
                        logger.info("Test failed for server '{}'. HTTP code {}", d.getServiceId(), resp.getStatusLine().getStatusCode());
                        d.reportTestFailure("DOWN");
                    } else {
                        d.reportTestSuccess(watch.getTime());
                    }
                }
            } catch (Exception ex) {
                logger.error("Test failed for server '{}': {}", d.getServiceId(), ex.getMessage(), ex);
                d.reportTestFailure("INACCESSIBLE");
            }
        }
    }

    private static boolean wasSuccessful(HttpResponse resp) {
        int c = resp.getStatusLine().getStatusCode();
        return c >= 200 && c < 300;
    }

    private static String headerValue(HttpResponse resp, String name) {
        Header h = resp.getFirstHeader(name);
        return h == null ? null : h.getValue();
    }

    private static Long parseLongOrNull(String s) {
        if (s == null) {
            return null;
        }
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String jsonString(JsonObject obj, String key) {
        if (!obj.has(key)) {
            return null;
        }
        JsonElement el = obj.get(key);
        return el.isJsonNull() ? null : el.getAsString();
    }

    private void stillAlive() {
        aliveAtTime = System.currentTimeMillis();
    }

}
