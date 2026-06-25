package ch.tkuhn.nanopub.monitor;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The main page of the nanopub-monitor web application, displaying the status of nanopublication servers.
 */
public class MonitorPage extends WebPage {

    private static final long serialVersionUID = -2069078890268133150L;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final Logger logger = LoggerFactory.getLogger(MonitorPage.class);
    private String points = "";

    /**
     * Constructs a MonitorPage with the given parameters.
     *
     * @param parameters the page parameters
     */
    public MonitorPage(final PageParameters parameters) {
        super(parameters);
        logger.debug("Constructing MonitorPage");
        if (MonitorConf.get().showMap()) {
            for (ServerData sd : ServerList.get().getServerData()) {
                try {
                    ServerIpInfo ipInfo = sd.getIpInfo();
                    NanopubService s = sd.getService();
                    points += "[\"" + ipInfo.getLatitude() + "," + ipInfo.getLongitude() + "\",\"" + s.getMapColor() + "\",[" + s.getMapOffsetX() + "," + s.getMapOffsetY() + "]],";
                } catch (Exception ex) {
                    logger.error("Could not get map coordinates for server '{}'", sd.getServiceId(), ex);
                }
            }
            points = points.replaceFirst(",$", "");
        }

        // The counts and the table are re-read from the live ServerList on every
        // render, so an Ajax timer can repaint just these without a full page reload.
        final WebMarkupContainer counts = new WebMarkupContainer("counts");
        counts.setOutputMarkupId(true);
        counts.add(new Label("server-count", new IModel<String>() {
            private static final long serialVersionUID = 1L;

            public String getObject() {
                return ServerList.get().getServerCount() + "";
            }
        }));
        counts.add(new Label("server-ip-count", new IModel<String>() {
            private static final long serialVersionUID = 1L;

            public String getObject() {
                return distinctServerIpCount() + "";
            }
        }));
        add(counts);

        final WebMarkupContainer tableContainer = new WebMarkupContainer("tablecontainer");
        tableContainer.setOutputMarkupId(true);
        add(tableContainer);

        tableContainer.add(new DataView<ServerData>("rows", new ListDataProvider<ServerData>() {

            private static final long serialVersionUID = 8901243870718834567L;

            @Override
            protected List<ServerData> getData() {
                return ServerList.get().getSortedServerData();
            }

        }) {

            private static final long serialVersionUID = 4703849210371741467L;

            public void populateItem(final Item<ServerData> item) {
                ServerList sl = ServerList.get();
                ServerData d = item.getModelObject();
                ServerIpInfo i = d.getIpInfo();
                ExternalLink urlLink = new ExternalLink("urllink", d.getServiceId());
                urlLink.add(new Label("url", d.getServiceId()));
                item.add(urlLink);
                ExternalLink typeLink = new ExternalLink("typelink", d.getService().getTypeIri().stringValue());
                typeLink.add(new Label("type", d.getService().getTypeLabel()));
                typeLink.add(new AttributeModifier("style", "background: " + d.getService().getMapColor()));
                item.add(typeLink);
                Label testChip = new Label("testchip", "TEST");
                testChip.setVisible(d.isTestInstance());
                item.add(testChip);
                item.add(new Label("version", d.getVersion() == null ? "" : d.getVersion()));
                Label statusLabel = new Label("status", d.getStatusString());
                if (!d.getStatusString().equals("OK")) {
                    statusLabel.add(new AttributeModifier("style", "color: red"));
                }
                item.add(statusLabel);
                String groupLabel = sl.getHashGroupLabel(d);
                String hashCell = formatTrustHashCell(d, groupLabel);
                Label trustHashLabel = new Label("trusthash", hashCell);
                if ("outlier".equals(groupLabel)) {
                    trustHashLabel.add(new AttributeModifier("style", "color: red"));
                }
                item.add(trustHashLabel);
                item.add(new Label("nanopubcount", d.getNanopubCountString()));
                item.add(new Label("successratio", d.getSuccessRatioString()));
                item.add(new Label("resptime", d.getAvgResponseTimeString() + " (" + d.getDistanceString() + ")"));
                item.add(new Label("lastseen", formatDate(d.getLastSeenDate())));
                if (i == null) {
                    item.add(new Label("ip", "unknown"));
                    item.add(new Label("location", "unknown"));
                } else {
                    item.add(new Label("ip", i.getIp()));
                    item.add(new Label("location", i.getCity() + ", " + i.getCountryName()));
                }
            }

        });

        // Poll every 10 seconds and repaint only the counts and the table.
        tableContainer.add(new AbstractAjaxTimerBehavior(Duration.ofSeconds(10)) {

            private static final long serialVersionUID = 6657894561239871045L;

            @Override
            protected void onTimer(AjaxRequestTarget target) {
                target.add(counts);
                target.add(tableContainer);
            }

        });

        ServerScanner.initDaemon();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptReferenceHeaderItem.forScript("var points = [" + points + "];", null));
    }

    /**
     * Formats a Date object into a string representation.
     *
     * @param date the Date object to format
     * @return the formatted date string, or an empty string if the date is null
     */
    static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return dateFormat.format(date);
    }

    /**
     * Count the distinct (non-null) IP addresses across all known servers, used as the
     * approximate "distinct servers" figure. Computed from the live ServerList on each call
     * so it stays current across Ajax refreshes.
     *
     * @return the number of distinct server IP addresses
     */
    private static int distinctServerIpCount() {
        Set<String> ipAddresses = new HashSet<String>();
        for (ServerData sd : ServerList.get().getServerData()) {
            try {
                ServerIpInfo ipInfo = sd.getIpInfo();
                if (ipInfo != null && ipInfo.getIp() != null) {
                    ipAddresses.add(ipInfo.getIp());
                }
            } catch (Exception ex) {
                logger.error("Could not get IP info for server '{}'", sd.getServiceId(), ex);
            }
        }
        return ipAddresses.size();
    }

    /**
     * Format the setting/checksum table cell, combining setting and hash:
     * "<setting> / <hash> (<group>)"
     * with "<origSetting> → <currentSetting> / ..." when the setting was changed at runtime.
     * The hash is the registry trust state hash, falling back to the query instance's
     * loaded-nanopub checksum (query instances have no setting and no consensus group).
     * Returns "" if neither a setting nor a hash is known.
     */
    static String formatTrustHashCell(ServerData d, String groupLabel) {
        String hashShort = d.getTrustStateHashShort();
        if (hashShort.isEmpty()) {
            hashShort = d.getLoadedNanopubChecksumShort();
        }
        String current = d.getCurrentSetting();
        String original = d.getOriginalSetting();
        if (hashShort.isEmpty() && current == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (current != null) {
            if (original != null && !original.equals(current)) {
                sb.append(ServerData.shortSetting(original)).append(" → ");
            }
            sb.append(ServerData.shortSetting(current));
        }
        if (!hashShort.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" / ");
            }
            sb.append(hashShort);
            if (groupLabel != null) {
                sb.append(" (").append(groupLabel).append(")");
            }
        }
        return sb.toString();
    }

}
