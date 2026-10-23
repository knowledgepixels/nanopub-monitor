package ch.tkuhn.nanopub.monitor;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
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
 * The main page of the nanopub-monitor web application, displaying the status of nanopublication servers
 * in one section per kind of service.
 */
public class MonitorPage extends WebPage {

    private static final long serialVersionUID = -2069078890268133150L;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final Logger logger = LoggerFactory.getLogger(MonitorPage.class);

    /**
     * Constructs a MonitorPage with the given parameters.
     *
     * @param parameters the page parameters
     */
    public MonitorPage(final PageParameters parameters) {
        super(parameters);
        logger.debug("Constructing MonitorPage");
        WebMarkupContainer map = new WebMarkupContainer("map");
        map.setVisible(MonitorConf.get().showMap());
        add(map);

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

        IModel<List<ServiceSection>> sections = new LoadableDetachableModel<>() {
            private static final long serialVersionUID = 1L;

            @Override
            protected List<ServiceSection> load() {
                ServerList serverList = ServerList.get();
                return ServiceSection.of(serverList.getSortedServerData(), serverList);
            }
        };
        tableContainer.add(summaryCards(sections));
        tableContainer.add(summaryBar(sections));
        tableContainer.add(sectionTables(sections));

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

    /**
     * Build the summary cards, one per section, each counting the OK instances and linking to its section.
     *
     * @param sections the sections to summarize
     * @return the list of cards
     */
    private static ListView<ServiceSection> summaryCards(IModel<List<ServiceSection>> sections) {
        return new ListView<>("summary", sections) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<ServiceSection> item) {
                ServiceSection section = item.getModelObject();
                item.add(AttributeModifier.replace("href", "#" + section.getAnchor()));
                item.add(swatch(section));
                item.add(new Label("title", section.getTitle()));
                item.add(new Label("ok", section.getOkCount()));
                item.add(new Label("total", section.getServers().size()));
                WebMarkupContainer attention = new WebMarkupContainer("attention");
                attention.add(new Label("attention-count", section.getAttentionCount() + " need attention"));
                attention.setVisible(section.getAttentionCount() > 0);
                item.add(attention);
            }
        };
    }

    /**
     * Build the compact summary bar, which stays at the top of the window while scrolling down the sections.
     *
     * @param sections the sections to summarize
     * @return the list of chips, one per section
     */
    private static ListView<ServiceSection> summaryBar(IModel<List<ServiceSection>> sections) {
        return new ListView<>("summary-bar", sections) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<ServiceSection> item) {
                ServiceSection section = item.getModelObject();
                item.add(AttributeModifier.replace("href", "#" + section.getAnchor()));
                item.add(swatch(section));
                item.add(new Label("title", section.getTitle()));
                item.add(new Label("tally", section.getOkCount() + "/" + section.getServers().size()));
                Label attention = new Label("attention", section.getAttentionCount());
                attention.add(AttributeModifier.replace("title", section.getAttentionCount() + " need attention"));
                attention.setVisible(section.getAttentionCount() > 0);
                item.add(attention);
            }
        };
    }

    /**
     * Build one table per section, showing the columns its kind of service reports.
     *
     * @param sections the sections to show
     * @return the list of section tables
     */
    private static ListView<ServiceSection> sectionTables(IModel<List<ServiceSection>> sections) {
        return new ListView<>("sections", sections) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<ServiceSection> item) {
                ServiceSection section = item.getModelObject();
                item.add(AttributeModifier.replace("id", section.getAnchor()));
                item.add(swatch(section));
                item.add(new Label("title", section.getTitle()));
                int instances = section.getServers().size();
                item.add(new Label("instances", instances + (instances == 1 ? " instance" : " instances")));
                item.add(typeLinks(section));
                item.add(columnHeader("trust-header", section, ServiceKind.Column.TRUST_STATE));
                item.add(columnHeader("checksum-header", section, ServiceKind.Column.CHECKSUM));
                item.add(columnHeader("count-header", section, ServiceKind.Column.NANOPUB_COUNT));
                item.add(columnHeader("sync-header", section, ServiceKind.Column.SYNC));
                item.add(columnHeader("loader-header", section, ServiceKind.Column.LOADER));
                item.add(serviceRows(section));
            }
        };
    }

    /**
     * Build the links naming the exact service types of a section, each pointing to its type IRI.
     *
     * @param section the section
     * @return the list of type links
     */
    private static ListView<NanopubService> typeLinks(ServiceSection section) {
        return new ListView<>("types", section.getTypes()) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<NanopubService> item) {
                NanopubService type = item.getModelObject();
                item.add(AttributeModifier.replace("href", type.getTypeIri().stringValue()));
                item.add(new Label("type-label", type.getTypeLabel()));
            }
        };
    }

    /**
     * Build the rows of one section's table.
     *
     * @param section the section whose instances to list
     * @return the list of rows
     */
    private static ListView<ServerData> serviceRows(ServiceSection section) {
        return new ListView<>("rows", section.getServers()) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<ServerData> item) {
                ServerData d = item.getModelObject();
                String groupLabel = ServerList.get().getHashGroupLabel(d);
                item.add(AttributeModifier.replace("data-service", d.getServiceId()));
                addServiceCell(item, d);
                item.add(new Label("row-type", d.getService().getTypeLabel()).setVisible(section.hasSeveralTypes()));
                addStatusCell(item, d);
                item.add(trustStateCell(d, groupLabel).setVisible(section.shows(ServiceKind.Column.TRUST_STATE)));
                item.add(checksumCell(d, groupLabel).setVisible(section.shows(ServiceKind.Column.CHECKSUM)));
                item.add(new Label("count", d.getNanopubCountString())
                        .setVisible(section.shows(ServiceKind.Column.NANOPUB_COUNT)));
                item.add(syncCell(d).setVisible(section.shows(ServiceKind.Column.SYNC)));
                item.add(loaderCell(d).setVisible(section.shows(ServiceKind.Column.LOADER)));
                addHealthCell(item, d);
                addLocationCell(item, d);
            }
        };
    }

    /**
     * Build the colour swatch of a section's kind of service, matching its map markers.
     *
     * @param section the section
     * @return the swatch
     */
    private static Component swatch(ServiceSection section) {
        return new WebMarkupContainer("swatch")
                .add(AttributeModifier.replace("style", "background: " + section.getColor()));
    }

    /**
     * Build the header of an optional column, shown only when the section reports that column.
     *
     * @param id      the Wicket id of the header cell
     * @param section the section
     * @param column  the column the header belongs to
     * @return the header cell
     */
    private static Component columnHeader(String id, ServiceSection section, ServiceKind.Column column) {
        return new WebMarkupContainer(id).setVisible(section.shows(column));
    }

    /**
     * Add the service cell: the shortened URL linking to the service, the test chip and the version.
     *
     * @param item the row
     * @param d    the instance
     */
    private static void addServiceCell(ListItem<ServerData> item, ServerData d) {
        ExternalLink urlLink = new ExternalLink("urllink", d.getServiceId());
        urlLink.add(AttributeModifier.replace("title", d.getServiceId()));
        urlLink.add(new Label("url", ServiceCells.shortUrl(d.getServiceId())));
        item.add(urlLink);
        item.add(new Label("testchip", "TEST").setVisible(d.isTestInstance()));
        item.add(new Label("version", "v" + d.getVersion()).setVisible(d.getVersion() != null));
    }

    /**
     * Add the status cell as a badge coloured by status.
     *
     * @param item the row
     * @param d    the instance
     */
    private static void addStatusCell(ListItem<ServerData> item, ServerData d) {
        Label status = new Label("status", d.getStatusString());
        status.add(AttributeModifier.append("class", ServiceCells.statusClass(d.getStatusString())));
        item.add(status);
    }

    /**
     * Build a registry's setting and trust state cell.
     *
     * @param d          the instance
     * @param groupLabel "consensus", "outlier", or null if not compared with peers
     * @return the cell
     */
    private static Component trustStateCell(ServerData d, String groupLabel) {
        WebMarkupContainer cell = new WebMarkupContainer("trust");
        cell.add(new Label("setting", ServiceCells.formatSetting(d.getOriginalSetting(), d.getCurrentSetting())));
        String hash = d.getTrustStateHashShort();
        Label hashLabel = new Label("trust-hash", hash.isEmpty() ? "no trust state" : hash);
        if (!hash.isEmpty()) {
            hashLabel.add(AttributeModifier.append("class", "mono"));
        }
        cell.add(hashLabel);
        cell.add(groupPill("trust-group", groupLabel));
        return cell;
    }

    /**
     * Build a query instance's loaded-nanopub checksum cell.
     *
     * @param d          the instance
     * @param groupLabel "consensus", "outlier", or null if not compared with peers
     * @return the cell
     */
    private static Component checksumCell(ServerData d, String groupLabel) {
        WebMarkupContainer cell = new WebMarkupContainer("checksum");
        cell.add(new Label("checksum-hash", d.getLoadedNanopubChecksumShort()));
        cell.add(groupPill("checksum-group", groupLabel));
        return cell;
    }

    /**
     * Build the pill telling whether an instance agrees with its peers.
     *
     * @param id         the Wicket id of the pill
     * @param groupLabel "consensus", "outlier", or null, in which case the pill is hidden
     * @return the pill
     */
    private static Component groupPill(String id, String groupLabel) {
        Label pill = new Label(id, groupLabel);
        pill.add(AttributeModifier.append("class", "outlier".equals(groupLabel) ? "bad" : "ok"));
        pill.setVisible(groupLabel != null);
        return pill;
    }

    /**
     * Build the sync cell. Any non-zero lag is shown as a problem: unlike the checksum outlier,
     * it is measured against the instance's own registry, so the instance is genuinely missing
     * nanopubs rather than merely disagreeing with its peers.
     *
     * @param d the instance
     * @return the cell
     */
    private static Component syncCell(ServerData d) {
        Label sync = new Label("sync", d.getSyncLagString());
        if (d.isBehind()) {
            sync.add(AttributeModifier.append("class", "text-bad"));
        }
        return sync;
    }

    /**
     * Build the loader cell, shown as a problem once the loader has stalled.
     *
     * @param d the instance
     * @return the cell
     */
    private static Component loaderCell(ServerData d) {
        Label loader = new Label("loader", d.getLoaderAgeString());
        Long loaderAge = d.getLoaderLastSuccessAgeSeconds();
        if (loaderAge != null && loaderAge > SyncHealth.LOADER_STALL_SECONDS) {
            loader.add(AttributeModifier.append("class", "text-bad"));
        }
        return loader;
    }

    /**
     * Add the health cell: OK ratio and response time, with when the instance was last seen OK.
     * The exact time and the distance from the monitor are in the tooltip.
     *
     * @param item the row
     * @param d    the instance
     */
    private static void addHealthCell(ListItem<ServerData> item, ServerData d) {
        Float okRatio = d.getSuccessRatio();
        Label ratio = new Label("ok-ratio", ServiceCells.formatOkRatio(okRatio));
        if (okRatio == null) {
            ratio.add(AttributeModifier.append("class", "sub"));
        } else if (ServiceCells.isLowOkRatio(okRatio)) {
            ratio.add(AttributeModifier.append("class", "text-bad"));
        }
        item.add(ratio);
        item.add(new Label("resp-time", " · " + d.getAvgResponseTimeString())
                .setVisible(d.getAvgResponseTimeInMs() != null));
        Label lastSeen = new Label("last-seen", ServiceCells.formatLastSeen(d.getLastSeenDate(), new Date()));
        String exactTime = d.getLastSeenDate() == null ? "never" : formatDate(d.getLastSeenDate());
        String distance = d.getDistanceInKm() == null ? "" : " · " + d.getDistanceString() + " from monitor";
        lastSeen.add(AttributeModifier.replace("title", "Last seen OK " + exactTime + distance));
        lastSeen.setVisible(okRatio != null);
        item.add(lastSeen);
    }

    /**
     * Add the location cell: city and country with the region below, and the IP address in the tooltip.
     *
     * @param item the row
     * @param d    the instance
     */
    private static void addLocationCell(ListItem<ServerData> item, ServerData d) {
        ServerIpInfo i = d.getIpInfo();
        Label place = new Label("place", i == null ? "unknown" : i.getCity() + ", " + i.getCountryName());
        place.add(AttributeModifier.replace("title", i == null ? "unknown IP" : i.getIp()));
        item.add(place);
        item.add(new Label("region", i == null ? "" : i.getRegionName()));
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

}
