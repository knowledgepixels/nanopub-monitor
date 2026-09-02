package ch.tkuhn.nanopub.monitor;

import com.google.common.collect.ImmutableList;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.nanopub.extra.services.ApiResponse;
import org.nanopub.extra.services.ApiResponseEntry;
import org.nanopub.extra.services.QueryAccess;
import org.nanopub.extra.services.QueryRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * A list of nanopublication servers, fetched from a central registry API.
 */
public class ServerList implements Serializable {

    private static final long serialVersionUID = -6272932136983159574L;
    private static final Logger logger = LoggerFactory.getLogger(ServerList.class);

    private static ServerList serverList;
    private static ServerIpInfo monitorIpInfo;

    /**
     * Get the singleton instance of the ServerList.
     *
     * @return the ServerList instance
     */
    public static ServerList get() {
        if (serverList == null) {
            serverList = new ServerList();
        }
        return serverList;
    }

    private static final Map<NanopubService, ServerData> servers = new HashMap<NanopubService, ServerData>();

    private ServerList() {
        refresh();
    }

    /**
     * Get an immutable list of all server data entries.
     *
     * @return list of ServerData
     */
    public List<ServerData> getServerData() {
        return ImmutableList.copyOf(servers.values());
    }

    /**
     * Get a sorted list of all server data entries.
     * Sorting is done by service type IRI (descending), then by IP address (ascending), then by service IRI (ascending).
     *
     * @return sorted list of ServerData
     */
    public List<ServerData> getSortedServerData() {
        List<ServerData> s = new ArrayList<ServerData>(servers.values());
        Collections.sort(s, new Comparator<ServerData>() {
            @Override
            public int compare(ServerData o1, ServerData o2) {
                if (!o1.getService().getTypeIri().equals(o2.getService().getTypeIri())) {
                    return o2.getService().getTypeIri().toString().compareTo(o1.getService().getTypeIri().toString());
                }
                if (o1.getIpInfo() == null || o1.getIpInfo().getIp() == null) return -1;
                if (o2.getIpInfo() == null || o2.getIpInfo().getIp() == null) return 1;
                if (o1.getIpInfo().getIp().equals(o2.getIpInfo().getIp())) {
                    return o1.getServiceId().compareTo(o2.getServiceId());
                }
                return o1.getIpInfo().getIp().compareTo(o2.getIpInfo().getIp());
            }
        });
        return s;
    }

    /**
     * Get the number of servers in the list.
     *
     * @return the server count
     */
    public int getServerCount() {
        return servers.size();
    }

    /**
     * Get the trust state hash reported by the largest number of registries running the
     * given setting. Used as the consensus value within a setting cohort: registries with
     * the same setting but a different hash are flagged as outliers. Comparing hashes
     * across different settings is not meaningful, so the calculation is scoped per setting.
     *
     * @param setting the registry setting (trusty URI); if null, the result is null
     * @return the majority trust state hash for the cohort, or null if the cohort has none
     */
    public String getMajorityTrustStateHashForSetting(String setting) {
        if (setting == null) return null;
        Map<String, Integer> counts = new HashMap<>();
        for (ServerData sd : servers.values()) {
            if (!sd.hasServiceTypePrefix(NanopubService.NANOPUB_REGISTRY_TYPE_IRI)) continue;
            if (!setting.equals(sd.getCurrentSetting())) continue;
            String h = sd.getTrustStateHash();
            if (h == null) continue;
            counts.merge(h, 1, Integer::sum);
        }
        return majorityKey(counts);
    }

    /**
     * Get the loaded-nanopub checksum reported by the largest number of query instances in the
     * given test cohort. Used as the consensus value for query instances: those with the same
     * test-instance status but a different checksum are flagged as outliers. Query instances do
     * not expose a setting, so consensus is scoped by test-instance status instead — test and
     * non-test instances load from different networks, so comparing their checksums across that
     * boundary is not meaningful.
     *
     * @param testInstance whether to consider the test cohort (true) or the non-test cohort (false)
     * @return the majority loaded-nanopub checksum for the cohort, or null if the cohort has none
     */
    public String getMajorityLoadedNanopubChecksum(boolean testInstance) {
        Map<String, Integer> counts = new HashMap<>();
        for (ServerData sd : servers.values()) {
            if (!sd.hasServiceTypePrefix(NanopubService.NANOPUB_QUERY_TYPE_IRI)) continue;
            if (sd.isTestInstance() != testInstance) continue;
            String h = sd.getLoadedNanopubChecksum();
            if (h == null) continue;
            counts.merge(h, 1, Integer::sum);
        }
        return majorityKey(counts);
    }

    /**
     * Return the key with the strictly highest count in the given map, or null if the map is
     * empty or its highest count is shared. A cohort that splits evenly has no majority to
     * measure its members against, and picking one of the tied values would label the others
     * outliers on nothing more than map iteration order.
     */
    static String majorityKey(Map<String, Integer> counts) {
        String majority = null;
        int best = 0;
        boolean tied = false;
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            if (e.getValue() > best) {
                best = e.getValue();
                majority = e.getKey();
                tied = false;
            } else if (e.getValue() == best) {
                tied = true;
            }
        }
        return tied ? null : majority;
    }

    /**
     * Classify a server's reported hash against the consensus of its cohort. For registries, the
     * hash is the trust state hash and the cohort is registries running the same setting; for query
     * instances, the hash is the loaded-nanopub checksum and the cohort is query instances with the
     * same test-instance status. Returns "consensus" if it matches the cohort majority, "outlier"
     * if it differs, or null if the server reports no comparable hash (or, for registries, no
     * setting).
     *
     * @param sd the server data
     * @return "consensus", "outlier", or null
     */
    public String getHashGroupLabel(ServerData sd) {
        if (sd.hasServiceTypePrefix(NanopubService.NANOPUB_REGISTRY_TYPE_IRI)) {
            String h = sd.getTrustStateHash();
            if (h == null) return null;
            String setting = sd.getCurrentSetting();
            if (setting == null) return null;
            String majority = getMajorityTrustStateHashForSetting(setting);
            if (majority == null) return null;
            return h.equals(majority) ? "consensus" : "outlier";
        }
        if (sd.hasServiceTypePrefix(NanopubService.NANOPUB_QUERY_TYPE_IRI)) {
            String h = sd.getLoadedNanopubChecksum();
            if (h == null) return null;
            String majority = getMajorityLoadedNanopubChecksum(sd.isTestInstance());
            if (majority == null) return null;
            return h.equals(majority) ? "consensus" : "outlier";
        }
        return null;
    }

    /**
     * Get the IP information of the monitor server.
     * This method fetches the information only once and caches it for future calls.
     *
     * @return the ServerIpInfo of the monitor server, or null if it could not be fetched
     */
    public ServerIpInfo getMonitorIpInfo() {
        if (monitorIpInfo == null) {
            try {
                monitorIpInfo = ServerData.fetchIpInfo("");
            } catch (Exception ex) {
                logger.error("Could not fetch monitor IP info", ex);
            }
        }
        return monitorIpInfo;
    }

    /**
     * Refresh the server list by fetching data from the central registry API.
     * This method updates the internal list of servers.
     */
    public void refresh() {
        refreshFromApi();
    }

    /**
     * Asks whether a nanopub-registry is running at a candidate URL and says it is a test
     * instance. Implemented by the scanner, which owns the HTTP client.
     */
    @FunctionalInterface
    interface RegistryProbe {

        /**
         * @param serviceUrl the candidate registry URL
         * @return true if a registry answers there and reports itself as a test instance
         */
        boolean isTestInstance(String serviceUrl);
    }

    /**
     * How long to leave a candidate alone after a probe found nothing there. A discovered test
     * instance joins the server list for good, so this only paces the misses: most registries
     * have no test counterpart, and their candidate URL would otherwise be probed every scan.
     */
    private static final long TEST_INSTANCE_PROBE_INTERVAL_MS = 10 * 60 * 1000;

    private static final Map<String, Long> testInstanceProbedAt = new HashMap<>();

    /**
     * Add the test instances of the registries the network announces. A test deployment is
     * deliberately not announced in a service intro nanopublication — it would then be
     * indistinguishable from a production service to everything reading that list — so it
     * cannot be found by the query above. It is reachable by convention, at the announced
     * registry's URL with "test." in front of the host, and the probe keeps only those
     * candidates where a registry answers and reports itself as a test instance. The
     * convention only proposes where to look; the registry itself decides what is added.
     *
     * @param probe the probe to confirm a candidate with
     * @return the number of test instances newly added to the list
     */
    int addTestInstances(RegistryProbe probe) {
        int newCount = 0;
        for (NanopubService candidate : testInstanceCandidates(servers.keySet())) {
            String url = candidate.getServiceIri().stringValue();
            Long probedAt = testInstanceProbedAt.get(url);
            if (probedAt != null && System.currentTimeMillis() - probedAt < TEST_INSTANCE_PROBE_INTERVAL_MS) {
                continue;
            }
            testInstanceProbedAt.put(url, System.currentTimeMillis());
            if (!probe.isTestInstance(url)) {
                continue;
            }
            if (addIfNew(candidate)) {
                newCount++;
                logger.info("Monitoring test instance {}, found from the announced registry it mirrors", url);
            }
        }
        return newCount;
    }

    /**
     * The test-instance URLs worth probing, given the services already known: one per announced
     * registry, minus the ones already in the list and minus the registries that are themselves
     * test instances. Each candidate keeps the service type of the registry it was derived from.
     *
     * @param known the services already in the list
     * @return the candidates to probe, without duplicates
     */
    static List<NanopubService> testInstanceCandidates(Collection<NanopubService> known) {
        Set<String> knownUrls = new HashSet<>();
        for (NanopubService ns : known) {
            knownUrls.add(ns.getServiceIri().stringValue());
        }
        List<NanopubService> candidates = new ArrayList<>();
        Set<String> proposed = new HashSet<>();
        for (NanopubService ns : known) {
            if (!ns.getTypeIri().stringValue().startsWith(NanopubService.NANOPUB_REGISTRY_TYPE_IRI.stringValue())) {
                continue;
            }
            String url = testInstanceUrl(ns.getServiceIri().stringValue());
            // Candidates are deduplicated by URL rather than by service: registries announced
            // under two versions of the type IRI share one host, and so one test instance.
            if (url == null || knownUrls.contains(url) || !proposed.add(url)) {
                continue;
            }
            candidates.add(new NanopubService(vf.createIRI(url), ns.getTypeIri()));
        }
        return candidates;
    }

    /**
     * Where a service's test counterpart is deployed by convention: the same URL with "test." in
     * front of the host.
     *
     * @param serviceUrl the announced service URL
     * @return the candidate URL, or null if the service is already a test instance or its URL
     * names no host to prefix
     */
    static String testInstanceUrl(String serviceUrl) {
        try {
            URI uri = new URI(serviceUrl);
            String host = uri.getHost();
            if (host == null || host.startsWith("test.")) {
                return null;
            }
            return new URI(uri.getScheme(), uri.getUserInfo(), "test." + host, uri.getPort(),
                    uri.getPath(), uri.getQuery(), uri.getFragment()).toString();
        } catch (URISyntaxException ex) {
            logger.warn("Could not derive a test-instance URL from '{}'", serviceUrl, ex);
            return null;
        }
    }

    private static final ValueFactory vf = SimpleValueFactory.getInstance();

    /**
     * Start tracking the given service unless it is already known.
     *
     * @param ns the service
     * @return true if it was added, false if it was already in the list
     */
    private static boolean addIfNew(NanopubService ns) {
        if (servers.containsKey(ns)) {
            return false;
        }
        servers.put(ns, new ServerData(ns, null));
        return true;
    }

    private void refreshFromApi() {
        String queryId = "RAorkjih6fAwpfjDtvCaIyIkqGNHqBOqukILXGbWfhMpI/get-services";
        try {
            ApiResponse resp = QueryAccess.get(new QueryRef(queryId));
            int newCount = 0;
            for (ApiResponseEntry e : resp.getData()) {
                NanopubService ns = new NanopubService(vf.createIRI(e.get("service")), vf.createIRI(e.get("serviceType")));
                if (addIfNew(ns)) {
                    newCount++;
                }
            }
            logger.debug("Server list refreshed from '{}': {} new servers found, {} total", queryId, newCount, servers.size());
        } catch (Exception ex) {
            logger.error("Could not refresh server list from API query '{}'", queryId, ex);
        }
    }

}
