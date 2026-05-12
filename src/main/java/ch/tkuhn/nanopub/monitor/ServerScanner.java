package ch.tkuhn.nanopub.monitor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opencsv.CSVReader;
import org.apache.commons.lang.time.StopWatch;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.wicket.util.thread.ICode;
import org.apache.wicket.util.thread.Task;
import org.nanopub.extra.server.NanopubServerUtils;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.Duration;
import java.util.Random;

/**
 * A daemon task that periodically scans all registered nanopublication servers to check their status.
 */
public class ServerScanner implements ICode {

    private static ServerScanner singleton;
    private static Task scanTask;
    private static Random random = new Random();

    /**
     * Initialize and start the server scanner daemon if not already running.
     * If the existing daemon has not shown signs of life for 10 minutes, it will be restarted.
     */
    public static void initDaemon() {
        if (singleton != null) {
            if (singleton.aliveAtTime + 10 * 60 * 1000 < System.currentTimeMillis()) {
                singleton.logger.info("No sign of life of the daemon for 10 minutes. Starting new one.");
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

    private Logger logger;
    private long aliveAtTime;

    private ServerScanner() {
        stillAlive();
    }

    @Override
    public void run(Logger logger) {
        this.logger = logger;
        logger.info("Scan servers...");
        ServerList.get().refresh();
        stillAlive();
        testServers();
    }

    private void testServers() {
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10 * 1000).build();
        HttpClient c = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
        for (ServerData d : ServerList.get().getServerData()) {
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
                    HttpResponse resp = c.execute(get);
                    watch.stop();
                    if (!wasSuccessful(resp)) {
                        logger.info("Test failed. HTTP code {}", resp.getStatusLine().getStatusCode());
                        d.reportTestFailure("DOWN");
                    } else {
                        CSVReader csvReader = null;
                        try {
                            csvReader = new CSVReader(new BufferedReader(new InputStreamReader(resp.getEntity().getContent())));
                            String[] line = null;
                            int n = 0;
                            while ((line = csvReader.readNext()) != null) {
                                n++;
                                if (n == 1) {
                                    // ignore header line
                                } else {
                                    if (line[0].matches("[0-9]{5,}")) {
                                        d.reportTestSuccess(watch.getTime());
                                    } else {
                                        d.reportTestFailure("BROKEN");
                                    }
                                    break;
                                }
                            }
                        } catch (Exception ex) {
                            logger.info("Test failed. Exception: {}", ex.getMessage());
                            d.reportTestFailure("BROKEN");
                        } finally {
                            if (csvReader != null) csvReader.close();
                        }
                    }
                } catch (Exception ex) {
                    logger.error("Test failed. Exception: {}", ex.getMessage());
                    d.reportTestFailure("INACCESSIBLE");
                }
            } else if (d.hasServiceType(NanopubService.LDF_SERVICE_TYPE_IRI) || d.hasServiceType(NanopubService.SIGNED_LDF_SERVICE_TYPE_IRI)) {
                logger.info("Trying to access {}?object=http%3A%2F%2Fwww.nanopub.org%2Fnschema%23Nanopublication...", d.getServiceId());
                try {
                    HttpGet get = new HttpGet(d.getServiceId() + "?object=http%3A%2F%2Fwww.nanopub.org%2Fnschema%23Nanopublication");
                    get.setHeader("Accept", "application/n-quads");
                    StopWatch watch = new StopWatch();
                    watch.start();
                    HttpResponse resp = c.execute(get);
                    watch.stop();
                    if (!wasSuccessful(resp)) {
                        logger.info("Test failed. HTTP code {}", resp.getStatusLine().getStatusCode());
                        d.reportTestFailure("DOWN");
                    } else {
                        BufferedReader reader = null;
                        try {
                            reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
                            String line = null;
                            int count = 0;
                            while ((line = reader.readLine()) != null) {
                                if (line.contains(" <http://www.nanopub.org/nschema#Nanopublication> "))
                                    count = count + 1;
                            }
                            if (count >= 100) {
                                d.reportTestSuccess(watch.getTime());
                            } else {
                                d.reportTestFailure("BROKEN");
                            }
                        } catch (Exception ex) {
                            logger.info("Test failed. Exception: {}", ex.getMessage());
                            d.reportTestFailure("BROKEN");
                        } finally {
                            if (reader != null) reader.close();
                        }
                    }
                } catch (Exception ex) {
                    logger.error("Test failed. Exception: {}", ex.getMessage());
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
                    HttpResponse resp = c.execute(get);
                    watch.stop();
                    if (!wasSuccessful(resp)) {
                        logger.info("Test failed. HTTP code {}", resp.getStatusLine().getStatusCode());
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
                            logger.error("Test failed. Exception: {}", ex.getMessage());
                            d.reportTestFailure("BROKEN");
                        } finally {
                            if (csvReader != null) csvReader.close();
                        }
                    }
                } catch (Exception ex) {
                    logger.error("Test failed. Exception: {}", ex.getMessage());
                    d.reportTestFailure("INACCESSIBLE");
                }
            } else if (d.hasServiceType(NanopubService.NANOPUB_MONITOR_TYPE_IRI)) {
                logger.info("Trying to access {}.csv...", d.getServiceId());
                try {
                    HttpGet get = new HttpGet(d.getServiceId() + ".csv");
                    get.setHeader("Accept", "text/csv");
                    StopWatch watch = new StopWatch();
                    watch.start();
                    HttpResponse resp = c.execute(get);
                    watch.stop();
                    if (!wasSuccessful(resp)) {
                        logger.info("Test failed. HTTP code {}", resp.getStatusLine().getStatusCode());
                        d.reportTestFailure("DOWN");
                    } else {
                        d.setVersion(headerValue(resp, "Nanopub-Monitor-Version"));
                        CSVReader csvReader = null;
                        try {
                            csvReader = new CSVReader(new BufferedReader(new InputStreamReader(resp.getEntity().getContent())));
                            String[] line = null;
                            int n = 0;
                            while ((line = csvReader.readNext()) != null) {
                                n++;
                                if (n == 1) {
                                    // ignore header line
                                } else {
                                    if (line[0].startsWith("http")) {
                                        d.reportTestSuccess(watch.getTime());
                                    } else {
                                        d.reportTestFailure("BROKEN");
                                    }
                                    break;
                                }
                            }
                        } catch (Exception ex) {
                            logger.info("Test failed. Exception: {}", ex.getMessage());
                            d.reportTestFailure("BROKEN");
                        } finally {
                            if (csvReader != null) csvReader.close();
                        }
                    }
                } catch (Exception ex) {
                    logger.error("Test failed. Exception: {}", ex.getMessage());
                    d.reportTestFailure("INACCESSIBLE");
                }
            } else if (d.hasServiceTypePrefix(NanopubService.NANOPUB_REGISTRY_TYPE_IRI)) {
                logger.info("Probing registry status at {}.json...", d.getServiceId());
                try {
                    HttpGet get = new HttpGet(d.getServiceId() + ".json");
                    StopWatch watch = new StopWatch();
                    watch.start();
                    HttpResponse resp = c.execute(get);
                    watch.stop();
                    if (!wasSuccessful(resp)) {
                        logger.info("Test failed. HTTP code {}", resp.getStatusLine().getStatusCode());
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
                            logger.info("Could not parse registry JSON body: {}", ex.getMessage());
                        }
                        String headerStatus = headerValue(resp, "Nanopub-Registry-Status");
                        if (!NanopubServerUtils.isReadyRegistryStatus(headerStatus)) {
                            d.reportTestFailure("STATUS: " + (headerStatus == null ? "missing" : headerStatus));
                        } else {
                            d.reportTestSuccess(watch.getTime());
                        }
                    }
                } catch (Exception ex) {
                    logger.error("Test failed. Exception: {}", ex.getMessage());
                    d.reportTestFailure("INACCESSIBLE");
                }
            } else if (d.hasServiceTypePrefix(NanopubService.NANOPUB_QUERY_TYPE_IRI)) {
                logger.info("Probing query status at {}...", d.getServiceId());
                try {
                    HttpGet get = new HttpGet(d.getServiceId());
                    StopWatch watch = new StopWatch();
                    watch.start();
                    HttpResponse resp = c.execute(get);
                    watch.stop();
                    if (!wasSuccessful(resp)) {
                        logger.info("Test failed. HTTP code {}", resp.getStatusLine().getStatusCode());
                        d.reportTestFailure("DOWN");
                    } else {
                        d.setVersion(headerValue(resp, "Nanopub-Query-Version"));
                        d.setNanopubCount(parseLongOrNull(headerValue(resp, "Nanopub-Query-Registry-Nanopub-Count")));
                        d.setTestInstance("true".equalsIgnoreCase(headerValue(resp, "Nanopub-Query-Registry-Test-Instance")));
                        String headerStatus = headerValue(resp, "Nanopub-Query-Status");
                        if (headerStatus == null || !headerStatus.equalsIgnoreCase("READY")) {
                            d.reportTestFailure("STATUS: " + (headerStatus == null ? "missing" : headerStatus));
                        } else {
                            d.reportTestSuccess(watch.getTime());
                        }
                    }
                } catch (Exception ex) {
                    logger.error("Test failed. Exception: {}", ex.getMessage());
                    d.reportTestFailure("INACCESSIBLE");
                }
            } else if (d.hasServiceType(NanopubService.NANOPUB_SERVER_TYPE_IRI)) {
                logger.info("Probing nanopub-server info at {}...", d.getServiceId());
                try {
                    HttpGet get = new HttpGet(d.getServiceId());
                    get.setHeader("Accept", "application/json");
                    StopWatch watch = new StopWatch();
                    watch.start();
                    HttpResponse resp = c.execute(get);
                    watch.stop();
                    if (!wasSuccessful(resp)) {
                        logger.info("Test failed. HTTP code {}", resp.getStatusLine().getStatusCode());
                        d.reportTestFailure("DOWN");
                    } else {
                        try (Reader r = new InputStreamReader(resp.getEntity().getContent())) {
                            JsonObject body = JsonParser.parseReader(r).getAsJsonObject();
                            if (body.has("nextNanopubNo") && !body.get("nextNanopubNo").isJsonNull()) {
                                d.setNanopubCount(body.get("nextNanopubNo").getAsLong());
                            }
                        } catch (Exception ex) {
                            logger.info("Could not parse nanopub-server JSON body: {}", ex.getMessage());
                        }
                        d.reportTestSuccess(watch.getTime());
                    }
                } catch (Exception ex) {
                    logger.error("Test failed. Exception: {}", ex.getMessage());
                    d.reportTestFailure("INACCESSIBLE");
                }
            } else {
                logger.info("Trying to access {}...", d.getServiceId());
                try {
                    HttpGet get = new HttpGet(d.getServiceId());
                    StopWatch watch = new StopWatch();
                    watch.start();
                    HttpResponse resp = c.execute(get);
                    watch.stop();
                    if (!wasSuccessful(resp)) {
                        logger.info("Test failed. HTTP code {}", resp.getStatusLine().getStatusCode());
                        d.reportTestFailure("DOWN");
                    } else {
                        d.reportTestSuccess(watch.getTime());
                    }
                } catch (Exception ex) {
                    logger.error("Test failed. Exception: {}", ex.getMessage());
                    d.reportTestFailure("INACCESSIBLE");
                }
            }
        }
    }

    private boolean wasSuccessful(HttpResponse resp) {
        int c = resp.getStatusLine().getStatusCode();
        return c >= 200 && c < 300;
    }

    private static String headerValue(HttpResponse resp, String name) {
        Header h = resp.getFirstHeader(name);
        return h == null ? null : h.getValue();
    }

    private static Long parseLongOrNull(String s) {
        if (s == null) return null;
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String jsonString(JsonObject obj, String key) {
        if (!obj.has(key)) return null;
        JsonElement el = obj.get(key);
        return el.isJsonNull() ? null : el.getAsString();
    }

    private void stillAlive() {
        aliveAtTime = System.currentTimeMillis();
    }

}
