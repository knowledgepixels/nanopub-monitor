package ch.tkuhn.nanopub.monitor;

import com.google.common.collect.ImmutableList;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.nanopub.extra.services.ApiResponse;
import org.nanopub.extra.services.ApiResponseEntry;
import org.nanopub.extra.services.QueryAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

public class ServerList implements Serializable {

    private static final long serialVersionUID = -6272932136983159574L;
    private static final Logger logger = LoggerFactory.getLogger(ServerList.class);

    private static ServerList serverList;
    private static ServerIpInfo monitorIpInfo;

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

    public List<ServerData> getServerData() {
        return ImmutableList.copyOf(servers.values());
    }

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

    public int getServerCount() {
        return servers.size();
    }

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

    public void refresh() {
        refreshFromApi();
    }

    private static final ValueFactory vf = SimpleValueFactory.getInstance();

    private void refreshFromApi() {
        try {
            ApiResponse resp = QueryAccess.get("RAorkjih6fAwpfjDtvCaIyIkqGNHqBOqukILXGbWfhMpI/get-services", null);
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
