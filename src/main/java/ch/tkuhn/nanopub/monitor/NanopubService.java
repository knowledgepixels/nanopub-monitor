package ch.tkuhn.nanopub.monitor;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * A nanopublication service, identified by its IRI and type.
 */
public class NanopubService {

    private static final ValueFactory vf = SimpleValueFactory.getInstance();

    public static final IRI NANOPUB_SERVER_TYPE_IRI = vf.createIRI("https://github.com/tkuhn/nanopub-server#service");
    public static final IRI NANOPUB_MONITOR_TYPE_IRI = vf.createIRI("https://github.com/tkuhn/nanopub-monitor#service");
    public static final IRI LDF_SERVICE_TYPE_IRI = vf.createIRI("https://github.com/peta-pico/nanopub-services#np-ldf-server");
    public static final IRI GRLC_SERVICE_TYPE_IRI = vf.createIRI("https://github.com/peta-pico/nanopub-services#grlc-np-api");
    public static final IRI SIGNED_LDF_SERVICE_TYPE_IRI = vf.createIRI("https://github.com/peta-pico/signed-nanopub-services#np-ldf-server");
    public static final IRI SIGNED_GRLC_SERVICE_TYPE_IRI = vf.createIRI("https://github.com/peta-pico/signed-nanopub-services#grlc-np-api");
    public static final IRI SIGNED_SPARQL_SERVICE_TYPE_IRI = vf.createIRI("https://github.com/peta-pico/signed-nanopub-services#np-sparql-api");
    public static final IRI NANOPUB_QUERY_TYPE_IRI = vf.createIRI("https://w3id.org/np/o/service/terms/nanopub-query");
    public static final IRI NANOPUB_REGISTRY_TYPE_IRI = vf.createIRI("https://w3id.org/np/o/service/terms/nanopub-registry");
    public static final IRI NANODASH_TYPE_IRI = vf.createIRI("https://w3id.org/np/o/service/terms/nanodash");

    private final IRI serviceIri;
    private final IRI typeIri;

    /**
     * Create a new NanopubService with the given service IRI and type IRI.
     *
     * @param serviceIri the IRI of the service
     * @param typeIri    the IRI of the service type
     */
    public NanopubService(IRI serviceIri, IRI typeIri) {
        this.serviceIri = serviceIri;
        this.typeIri = typeIri;
    }

    /**
     * Get the IRI of the service.
     *
     * @return the service IRI
     */
    public IRI getServiceIri() {
        return serviceIri;
    }

    /**
     * Get the IRI of the service type.
     *
     * @return the service type IRI
     */
    public IRI getTypeIri() {
        return typeIri;
    }

    /**
     * Get a human-readable label for the service type, derived from the type IRI.
     *
     * @return the service type label
     */
    public String getTypeLabel() {
        return typeIri.stringValue().replaceFirst("^.*/([^/]+)$", "$1");
    }

    /**
     * Get the horizontal offset for displaying the service on a map, based on its type.
     *
     * @return the horizontal offset
     */
    public int getMapOffsetX() {
        if (typeIri.equals(NANOPUB_SERVER_TYPE_IRI)) {
            return 0;
        } else if (typeIri.equals(NANOPUB_MONITOR_TYPE_IRI)) {
            return 3;
        } else if (typeIri.equals(LDF_SERVICE_TYPE_IRI)) {
            return 3;
        } else if (typeIri.equals(GRLC_SERVICE_TYPE_IRI)) {
            return -3;
        } else if (typeIri.equals(SIGNED_LDF_SERVICE_TYPE_IRI)) {
            return 3;
        } else if (typeIri.equals(SIGNED_GRLC_SERVICE_TYPE_IRI)) {
            return -3;
        } else if (typeIri.equals(SIGNED_SPARQL_SERVICE_TYPE_IRI)) {
            return 3;
        } else if (typeIri.stringValue().startsWith(NANOPUB_QUERY_TYPE_IRI.stringValue())) {
            return 3;
        } else if (typeIri.stringValue().startsWith(NANOPUB_REGISTRY_TYPE_IRI.stringValue())) {
            return -3;
        } else if (typeIri.stringValue().startsWith(NANODASH_TYPE_IRI.stringValue())) {
            return -3;
        }
        return 0;
    }

    /**
     * Get the vertical offset for displaying the service on a map, based on its type.
     *
     * @return the vertical offset
     */
    public int getMapOffsetY() {
        if (typeIri.equals(NANOPUB_SERVER_TYPE_IRI)) {
            return 0;
        } else if (typeIri.equals(NANOPUB_MONITOR_TYPE_IRI)) {
            return -3;
        } else if (typeIri.equals(LDF_SERVICE_TYPE_IRI)) {
            return 0;
        } else if (typeIri.equals(GRLC_SERVICE_TYPE_IRI)) {
            return 0;
        } else if (typeIri.equals(SIGNED_LDF_SERVICE_TYPE_IRI)) {
            return 0;
        } else if (typeIri.equals(SIGNED_GRLC_SERVICE_TYPE_IRI)) {
            return 0;
        } else if (typeIri.equals(SIGNED_SPARQL_SERVICE_TYPE_IRI)) {
            return 0;
        } else if (typeIri.stringValue().startsWith(NANOPUB_QUERY_TYPE_IRI.stringValue())) {
            return 3;
        } else if (typeIri.stringValue().startsWith(NANOPUB_REGISTRY_TYPE_IRI.stringValue())) {
            return 3;
        } else if (typeIri.stringValue().startsWith(NANODASH_TYPE_IRI.stringValue())) {
            return -3;
        }
        return 0;
    }

    /**
     * Get the color for displaying the service on a map, based on its type.
     *
     * @return the color as a CSS color string
     */
    public String getMapColor() {
        if (typeIri.equals(NANOPUB_SERVER_TYPE_IRI)) {
            return "orange";
        } else if (typeIri.equals(NANOPUB_MONITOR_TYPE_IRI)) {
            return "lightcoral";
        } else if (typeIri.equals(LDF_SERVICE_TYPE_IRI)) {
            return "skyblue";
        } else if (typeIri.equals(GRLC_SERVICE_TYPE_IRI)) {
            return "darkseagreen";
        } else if (typeIri.equals(SIGNED_LDF_SERVICE_TYPE_IRI)) {
            return "#4682B4";
        } else if (typeIri.equals(SIGNED_GRLC_SERVICE_TYPE_IRI)) {
            return "lightseagreen";
        } else if (typeIri.equals(SIGNED_SPARQL_SERVICE_TYPE_IRI)) {
            return "mediumpurple";
        } else if (typeIri.stringValue().startsWith(NANOPUB_QUERY_TYPE_IRI.stringValue())) {
            return "#da0b84";
        } else if (typeIri.stringValue().startsWith(NANOPUB_REGISTRY_TYPE_IRI.stringValue())) {
            return "#0bc9da";
        } else if (typeIri.stringValue().startsWith(NANODASH_TYPE_IRI.stringValue())) {
            return "#0b73da";
        }
        return "gray";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NanopubService s)) return false;
        return serviceIri.equals(s.serviceIri) && typeIri.equals(s.typeIri);
    }

    @Override
    public String toString() {
        return serviceIri.stringValue() + " (" + typeIri + ")";
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

}
