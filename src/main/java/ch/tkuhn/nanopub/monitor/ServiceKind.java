package ch.tkuhn.nanopub.monitor;

import org.eclipse.rdf4j.model.IRI;

import java.util.EnumSet;
import java.util.Set;

/**
 * The kinds of service the monitor table gives a section of its own, in the order the sections appear,
 * together with the optional columns each kind reports.
 */
public enum ServiceKind {

    /**
     * Nanopub Registry instances, which report a setting, a trust state and a nanopub count.
     */
    REGISTRY("Registry", "nanopub-registry", NanopubService.NANOPUB_REGISTRY_TYPE_IRI,
            EnumSet.of(Column.TRUST_STATE, Column.NANOPUB_COUNT)),

    /**
     * Nanopub Query instances, which report a loaded-nanopub checksum, a count and their sync health.
     */
    QUERY("Query", "nanopub-query", NanopubService.NANOPUB_QUERY_TYPE_IRI,
            EnumSet.of(Column.CHECKSUM, Column.NANOPUB_COUNT, Column.SYNC, Column.LOADER)),

    /**
     * Nanodash instances.
     */
    NANODASH("Nanodash", "nanodash", NanopubService.NANODASH_TYPE_IRI, EnumSet.noneOf(Column.class)),

    /**
     * Nanopub Router instances.
     */
    ROUTER("Router", "nanopub-router", NanopubService.NANOPUB_ROUTER_TYPE_IRI, EnumSet.noneOf(Column.class)),

    /**
     * Nanopub Monitor instances.
     */
    MONITOR("Monitor", "nanopub-monitor", NanopubService.NANOPUB_MONITOR_TYPE_IRI, EnumSet.noneOf(Column.class)),

    /**
     * Any other service type; each such type gets a section titled after the type itself.
     */
    OTHER(null, null, null, EnumSet.noneOf(Column.class));

    /**
     * The table columns that only some kinds of service report.
     */
    public enum Column {
        TRUST_STATE, CHECKSUM, NANOPUB_COUNT, SYNC, LOADER
    }

    private final String title;
    private final String anchor;
    private final IRI typePrefix;
    private final Set<Column> columns;

    ServiceKind(String title, String anchor, IRI typePrefix, Set<Column> columns) {
        this.title = title;
        this.anchor = anchor;
        this.typePrefix = typePrefix;
        this.columns = columns;
    }

    /**
     * Find the kind of service a type IRI stands for. Types are matched by prefix, so versioned
     * type IRIs such as {@code .../nanopub-registry-1.0} belong to their kind.
     *
     * @param typeIri the service type IRI
     * @return the matching kind, or {@link #OTHER} if no kind matches
     */
    public static ServiceKind of(IRI typeIri) {
        for (ServiceKind kind : values()) {
            if (kind.typePrefix != null && typeIri.stringValue().startsWith(kind.typePrefix.stringValue())) {
                return kind;
            }
        }
        return OTHER;
    }

    /**
     * Get the section title of this kind.
     *
     * @return the title, or null for {@link #OTHER}, whose sections take the type label
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the fragment identifier of this kind's section.
     *
     * @return the anchor, or null for {@link #OTHER}, whose sections derive one from the type label
     */
    public String getAnchor() {
        return anchor;
    }

    /**
     * Get the optional columns this kind reports.
     *
     * @return the columns, which may be empty
     */
    public Set<Column> getColumns() {
        return columns;
    }

}
