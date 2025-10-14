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

    private static Map<NanopubService, ServerData> servers = new HashMap<NanopubService, ServerData>();

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

    private static final ValueFactory vf = SimpleValueFactory.getInstance();

    private void refreshFromApi() {
        try {
            ApiResponse resp = QueryAccess.get(new QueryRef("RAorkjih6fAwpfjDtvCaIyIkqGNHqBOqukILXGbWfhMpI/get-services"));
            for (ApiResponseEntry e : resp.getData()) {
                NanopubService ns = new NanopubService(vf.createIRI(e.get("service")), vf.createIRI(e.get("serviceType")));
                if (!servers.containsKey(ns)) {
                    servers.put(ns, new ServerData(ns, null));
                }
            }
        } catch (Exception ex) {
            logger.error("Could not refresh server list from API", ex);
        }
    }

}
