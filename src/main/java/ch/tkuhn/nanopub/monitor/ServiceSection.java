package ch.tkuhn.nanopub.monitor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * One section of the monitor table: the instances of one kind of service, with the columns that kind
 * reports. Instances that need attention come first.
 */
public class ServiceSection implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ServiceKind kind;
    private final String title;
    private final String anchor;
    private final String color;
    private final List<ServerData> servers;
    private final Set<ServiceKind.Column> columns;
    private final int okCount;
    private final int attentionCount;

    /**
     * Creates a section of the given servers, all of one kind and, for {@link ServiceKind#OTHER}, of one type.
     *
     * @param kind       the kind of service
     * @param typeLabel  the type label, which titles a section of {@link ServiceKind#OTHER}
     * @param servers    the servers, at least one
     * @param serverList the server list, used to tell consensus from outliers
     */
    private ServiceSection(ServiceKind kind, String typeLabel, List<ServerData> servers, ServerList serverList) {
        this.kind = kind;
        this.title = kind == ServiceKind.OTHER ? toTitle(typeLabel) : kind.getTitle();
        this.anchor = kind == ServiceKind.OTHER ? toAnchor(typeLabel) : kind.getAnchor();
        this.color = servers.get(0).getService().getMapColor();
        this.columns = columnsFor(kind, servers);
        this.servers = sortedByAttention(servers, serverList);
        this.okCount = (int) servers.stream().filter(sd -> "OK".equals(sd.getStatusString())).count();
        this.attentionCount = (int) servers.stream().filter(sd -> needsAttention(sd, serverList)).count();
    }

    /**
     * Split the given servers into sections, one per kind of service and one per other service type,
     * in the order of {@link ServiceKind}, the other types last and by label.
     *
     * @param servers    the servers to show
     * @param serverList the server list, used to tell consensus from outliers
     * @return the sections, none of them empty
     */
    public static List<ServiceSection> of(Collection<ServerData> servers, ServerList serverList) {
        Map<String, List<ServerData>> byType = new LinkedHashMap<>();
        Map<String, ServiceKind> kindOfKey = new LinkedHashMap<>();
        for (ServerData sd : servers) {
            ServiceKind kind = ServiceKind.of(sd.getService().getTypeIri());
            String key = kind == ServiceKind.OTHER ? sd.getService().getTypeLabel() : kind.name();
            byType.computeIfAbsent(key, k -> new ArrayList<>()).add(sd);
            kindOfKey.put(key, kind);
        }
        List<ServiceSection> sections = new ArrayList<>();
        for (Map.Entry<String, List<ServerData>> entry : byType.entrySet()) {
            sections.add(new ServiceSection(kindOfKey.get(entry.getKey()), entry.getKey(), entry.getValue(), serverList));
        }
        sections.sort(Comparator.comparing((ServiceSection s) -> s.kind).thenComparing(s -> s.title));
        return sections;
    }

    /**
     * Whether an instance should be looked at: it is not OK, disagrees with its peers, or trails its registry.
     *
     * @param status     the instance's status, e.g. "OK" or "DOWN"
     * @param groupLabel "consensus", "outlier", or null if the instance is not compared with peers
     * @param behind     whether the instance trails its own registry
     * @return true if the instance needs attention
     */
    static boolean needsAttention(String status, String groupLabel, boolean behind) {
        return !"OK".equals(status) || "outlier".equals(groupLabel) || behind;
    }

    /**
     * Turn a type label into a fragment identifier.
     *
     * @param typeLabel the type label, e.g. "np-ldf-server"
     * @return the label in lower case with every run of other characters replaced by a hyphen
     */
    static String toAnchor(String typeLabel) {
        return typeLabel.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }

    /**
     * Turn a type label into a section title, dropping the generic "#service" fragment.
     *
     * @param typeLabel the type label, e.g. "nanopub-server#service" or "signed-nanopub-services#np-sparql-api"
     * @return e.g. "nanopub-server" or "signed-nanopub-services / np-sparql-api"
     */
    static String toTitle(String typeLabel) {
        return typeLabel.replaceFirst("#service$", "").replace("#", " / ");
    }

    /**
     * Whether an instance needs attention.
     *
     * @param sd         the instance
     * @param serverList the server list, used to tell consensus from outliers
     * @return true if the instance needs attention
     */
    private static boolean needsAttention(ServerData sd, ServerList serverList) {
        return needsAttention(sd.getStatusString(), serverList.getHashGroupLabel(sd), sd.isBehind());
    }

    /**
     * Sort instances so that those needing attention come first, then by URL.
     *
     * @param servers    the instances
     * @param serverList the server list, used to tell consensus from outliers
     * @return a sorted copy
     */
    private static List<ServerData> sortedByAttention(List<ServerData> servers, ServerList serverList) {
        List<ServerData> sorted = new ArrayList<>(servers);
        sorted.sort(Comparator.comparing((ServerData sd) -> !needsAttention(sd, serverList))
                .thenComparing(ServerData::getServiceId));
        return sorted;
    }

    /**
     * Get the optional columns a section shows: those its kind reports, plus the nanopub count
     * whenever an instance of another type reports one.
     *
     * @param kind    the kind of service
     * @param servers the instances
     * @return the columns
     */
    private static Set<ServiceKind.Column> columnsFor(ServiceKind kind, List<ServerData> servers) {
        Set<ServiceKind.Column> columns = EnumSet.noneOf(ServiceKind.Column.class);
        columns.addAll(kind.getColumns());
        if (servers.stream().anyMatch(sd -> sd.getNanopubCount() != null)) {
            columns.add(ServiceKind.Column.NANOPUB_COUNT);
        }
        return columns;
    }

    /**
     * Get the section title.
     *
     * @return the title, e.g. "Registry"
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the fragment identifier the summary card links to.
     *
     * @return the anchor, e.g. "nanopub-registry"
     */
    public String getAnchor() {
        return anchor;
    }

    /**
     * Get the colour the map uses for this kind of service.
     *
     * @return a CSS colour
     */
    public String getColor() {
        return color;
    }

    /**
     * Get the instances in this section, those needing attention first, then by URL.
     *
     * @return the instances
     */
    public List<ServerData> getServers() {
        return servers;
    }

    /**
     * Whether this section shows the given optional column.
     *
     * @param column the column
     * @return true if the column is shown
     */
    public boolean shows(ServiceKind.Column column) {
        return columns.contains(column);
    }

    /**
     * Get the number of instances whose status is OK.
     *
     * @return the OK count
     */
    public int getOkCount() {
        return okCount;
    }

    /**
     * Get the number of instances that need attention.
     *
     * @return the attention count
     * @see #needsAttention(String, String, boolean)
     */
    public int getAttentionCount() {
        return attentionCount;
    }

}
