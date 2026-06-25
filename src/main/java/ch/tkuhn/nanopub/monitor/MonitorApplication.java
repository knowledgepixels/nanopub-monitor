package ch.tkuhn.nanopub.monitor;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.resource.ResourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main application class for the nanopub-monitor web application.
 */
public class MonitorApplication extends WebApplication {

    private static final Logger logger = LoggerFactory.getLogger(MonitorApplication.class);

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
        mountResource(".csv", ResourceReference.of("csv", CsvTable.instance()));
        mountResource(".json", ResourceReference.of("json", JsonStatus.instance()));
        logger.debug("Mounted .csv and .json status resources");
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

}
