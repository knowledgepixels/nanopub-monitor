package ch.tkuhn.nanopub.monitor;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The main application class for the nanopub-monitor web application.
 */
public class MonitorApplication extends WebApplication {

    private static final Logger logger = LoggerFactory.getLogger(MonitorApplication.class);

    private final Map<String, Optional<String>> assetVersions = new ConcurrentHashMap<>();

    @Override
    public Class<? extends WebPage> getHomePage() {
        return MonitorPage.class;
    }

    /**
     * Initialize the application.
     */
    public void init() {
        super.init();
        logger.info("Initializing nanopub-monitor application, version {}", MonitorVersion.get());
        // Fail here rather than on the first request that happens to read the offending setting.
        MonitorConf.get().validate();
        logger.debug("Configuration validated");
        mountResource(".csv", ResourceReference.of("csv", CsvTable.instance()));
        mountResource(".json", ResourceReference.of("json", JsonStatus.instance()));
        mountResource(".map.json", ResourceReference.of("map-json", MapData.instance()));
        logger.debug("Mounted .csv, .json and .map.json resources");
        // Version every static asset a page refers to by its content, so that browsers pick up
        // a changed stylesheet or script without the references having to be bumped by hand.
        getRequestCycleSettings().addResponseFilter(
                buffer -> new AppendingStringBuffer(AssetVersions.apply(buffer, this::assetVersion)));
        getCspSettings().blocking().disabled();
        logger.debug("CSP blocking disabled");
        getRequestCycleListeners().add(new IRequestCycleListener() {
            @Override
            public void onBeginRequest(RequestCycle cycle) {
                if (cycle.getResponse() instanceof WebResponse wr) {
                    wr.setHeader("Nanopub-Monitor-Version", MonitorVersion.get());
                }
            }
        });
        logger.info("Nanopub-monitor application initialization complete");
    }

    /**
     * Get the content-derived version of a static asset of the web application. Deployed assets
     * do not change, so their versions are computed once; in development mode they are computed
     * on every request, so that an edited file is picked up right away.
     *
     * @param path the path of the asset relative to the web application root, e.g. "style.css"
     * @return the version, or null if there is no such asset
     */
    String assetVersion(String path) {
        if (usesDeploymentConfig()) {
            return assetVersions.computeIfAbsent(path, p -> Optional.ofNullable(readAssetVersion(p))).orElse(null);
        }
        return readAssetVersion(path);
    }

    private String readAssetVersion(String path) {
        try (InputStream content = getServletContext().getResourceAsStream("/" + path)) {
            return content == null ? null : AssetVersions.versionOf(content);
        } catch (IOException ex) {
            logger.warn("Could not read asset '{}' to derive its version", path, ex);
            return null;
        }
    }

}
