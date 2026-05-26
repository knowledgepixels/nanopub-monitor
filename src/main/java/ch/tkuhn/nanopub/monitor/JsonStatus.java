package ch.tkuhn.nanopub.monitor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.wicket.request.resource.ByteArrayResource;
import org.apache.wicket.request.resource.IResource;
import org.danekja.java.util.function.serializable.SerializableSupplier;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides a JSON representation of the current server data, intended for
 * machine consumption (e.g. by the nanopub-router).
 */
public class JsonStatus implements SerializableSupplier<IResource> {

    private static final long serialVersionUID = 1L;

    private static final JsonStatus instance = new JsonStatus();
    private static final Gson gson = new GsonBuilder().serializeNulls().create();

    public static JsonStatus instance() {
        return instance;
    }

    private JsonStatus() {
    }

    @Override
    public IResource get() {
        ServerList sl = ServerList.get();

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("monitorVersion", MonitorVersion.get());
        root.put("generatedAt", java.time.Instant.now().toString());

        List<Map<String, Object>> servers = new ArrayList<>();
        for (ServerData sd : sl.getSortedServerData()) {
            Map<String, Object> s = new LinkedHashMap<>();
            s.put("url", sd.getServiceId());
            s.put("type", sd.getService().getTypeIri().stringValue());
            s.put("typeLabel", sd.getService().getTypeLabel());
            s.put("version", sd.getVersion());
            s.put("testInstance", sd.isTestInstance());
            s.put("status", sd.getStatusString());
            s.put("currentSetting", sd.getCurrentSetting());
            s.put("originalSetting", sd.getOriginalSetting());
            s.put("trustStateHash", sd.getTrustStateHash());
            s.put("hashGroup", sl.getHashGroupLabel(sd));
            s.put("loadedNanopubChecksum", sd.getLoadedNanopubChecksum());
            s.put("nanopubCount", sd.getNanopubCount());
            s.put("okRatio", sd.getSuccessRatio());
            s.put("respTimeMs", sd.getAvgResponseTimeInMs());
            s.put("distanceKm", sd.getDistanceInKm());
            s.put("lastSeenOk", sd.getLastSeenDate() == null ? null : sd.getLastSeenDate().toInstant().toString());
            ServerIpInfo i = sd.getIpInfo();
            s.put("ip", i == null ? null : i.getIp());
            s.put("country", i == null ? null : i.getCountryName());
            s.put("city", i == null ? null : i.getCity());
            servers.add(s);
        }
        root.put("servers", servers);

        byte[] body = gson.toJson(root).getBytes(StandardCharsets.UTF_8);
        return new ByteArrayResource("application/json", body);
    }

}
