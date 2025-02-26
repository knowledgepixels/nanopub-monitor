package ch.tkuhn.nanopub.monitor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.nanopub.extra.server.ServerInfo;
import org.nanopub.extra.server.ServerIterator;
import org.nanopub.extra.services.ApiResponse;
import org.nanopub.extra.services.ApiResponseEntry;
import org.nanopub.extra.services.QueryAccess;

import com.google.common.collect.ImmutableList;

public class ServerList implements Serializable {

	private static final long serialVersionUID = -6272932136983159574L;

	private static ServerList serverList;
	private static ServerIpInfo monitorIpInfo;

	public static ServerList get() {
		if (serverList == null) {
			serverList = new ServerList();
		}
		return serverList;
	}

	private static Map<NanopubService,ServerData> servers = new HashMap<NanopubService,ServerData>();

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
				ex.printStackTrace();
			}
		}
		return monitorIpInfo;
	}

	public void refresh() {
		refreshFromNanopubServerPeers();
		refreshFromApi();
    }

	private static final ValueFactory vf = SimpleValueFactory.getInstance();

	private void refreshFromNanopubServerPeers() {
		ServerIterator serverIterator = new ServerIterator();
		while (serverIterator.hasNext()) {
			ServerInfo si = serverIterator.next();
			NanopubService s = new NanopubService(vf.createIRI(si.getPublicUrl()), NanopubService.NANOPUB_SERVER_TYPE_IRI);
			try {
				if (servers.containsKey(s)) {
					servers.get(s).update(si);
				} else {
					servers.put(s, new ServerData(s, si));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				if (servers.containsKey(s)) {
					servers.get(s).update(null);
				}
			}
		}
	}

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
			ex.printStackTrace();
		}
	}

}
