package ch.tkuhn.nanopub.monitor;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * The main application class for the nanopub-monitor web application.
 */
public class MonitorApplication extends WebApplication {

    @Override
    public Class<? extends WebPage> getHomePage() {
        return MonitorPage.class;
    }

    /**
     * Initialize the application.
     */
    public void init() {
        super.init();
        mountResource(".csv", ResourceReference.of("csv", CsvTable.instance()));
        getCspSettings().blocking().disabled();
        getRequestCycleListeners().add(new IRequestCycleListener() {
            @Override
            public void onBeginRequest(RequestCycle cycle) {
                if (cycle.getResponse() instanceof WebResponse wr) {
                    wr.setHeader("Nanopub-Monitor-Version", MonitorVersion.get());
                }
            }
        });
    }

}
