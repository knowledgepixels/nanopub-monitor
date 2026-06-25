package ch.tkuhn.nanopub.monitor;

import com.opencsv.CSVWriter;
import org.apache.wicket.request.resource.ByteArrayResource;
import org.apache.wicket.request.resource.IResource;
import org.danekja.java.util.function.serializable.SerializableSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Provides a CSV representation of the current server data.
 */
public class CsvTable implements SerializableSupplier<IResource> {

    private static final long serialVersionUID = 7196507056520414804L;

    private static final CsvTable instance = new CsvTable();
    private static final Logger logger = LoggerFactory.getLogger(CsvTable.class);

    /**
     * Get the singleton instance of CsvTable.
     *
     * @return the CsvTable instance
     */
    public static CsvTable instance() {
        return instance;
    }

    private CsvTable() {
    }

    /**
     * Generates a CSV resource containing the current server data.
     *
     * @return an IResource representing the CSV data
     */
    @Override
    public IResource get() {
        logger.debug("Generating CSV export of current server data");
        StringWriter sw = new StringWriter();
        CSVWriter w = new CSVWriter(sw);
        w.writeNext(new String[]{"URL", "Type", "Version", "Test Instance", "Status", "Current Setting", "Original Setting", "Trust State Hash", "Hash Group", "Loaded Nanopub Checksum", "Nanopub Count", "OK Ratio", "Resp Time", "Dist", "Last Seen OK", "IP Address", "Server Location"});
        ServerList sl = ServerList.get();
        int rowCount = 0;
        for (ServerData sd : sl.getSortedServerData()) {
            Float sr = sd.getSuccessRatio();
            Integer rt = sd.getAvgResponseTimeInMs();
            Integer dist = sd.getDistanceInKm();
            ServerIpInfo i = sd.getIpInfo();
            String hash = sd.getTrustStateHash();
            String group = sl.getHashGroupLabel(sd);
            String checksum = sd.getLoadedNanopubChecksum();
            Long npc = sd.getNanopubCount();
            w.writeNext(new String[]{
                    sd.getServiceId(),
                    sd.getService().getTypeIri().stringValue(),
                    (sd.getVersion() == null ? "" : sd.getVersion()),
                    sd.isTestInstance() ? "true" : "",
                    sd.getStatusString(),
                    (sd.getCurrentSetting() == null ? "" : sd.getCurrentSetting()),
                    (sd.getOriginalSetting() == null ? "" : sd.getOriginalSetting()),
                    (hash == null ? "" : hash),
                    (group == null ? "" : group),
                    (checksum == null ? "" : checksum),
                    (npc == null ? "" : npc.toString()),
                    (sr == null ? "" : sr + ""),
                    (rt == null ? "" : rt + ""),
                    (dist == null ? "" : dist + ""),
                    MonitorPage.formatDate(sd.getLastSeenDate()),
                    (i == null ? "" : i.getIp()),
                    (i == null ? "" : i.getCity() + ", " + i.getCountryName()),
            });
            rowCount++;
        }
        try {
            w.close();
            sw.close();
        } catch (IOException ex) {
            logger.error("Failed to close CSV writer after writing {} server rows", rowCount, ex);
        }
        logger.debug("CSV export complete: wrote {} server rows", rowCount);
        return new ByteArrayResource("text/csv", sw.getBuffer().toString().getBytes());
    }

}
