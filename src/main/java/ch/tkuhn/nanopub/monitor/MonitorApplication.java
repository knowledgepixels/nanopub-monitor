package ch.tkuhn.nanopub.monitor;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
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
    }

}
