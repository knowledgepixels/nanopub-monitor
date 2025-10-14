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

    private static CsvTable instance = new CsvTable();
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
        StringWriter sw = new StringWriter();
        CSVWriter w = new CSVWriter(sw);
        w.writeNext(new String[]{"URL", "Type", "Status", "OK Ratio", "Resp Time", "Dist", "Last Seen OK", "IP Address", "Server Location"});
        for (ServerData sd : ServerList.get().getSortedServerData()) {
            Float sr = sd.getSuccessRatio();
            Integer rt = sd.getAvgResponseTimeInMs();
            Integer dist = sd.getDistanceInKm();
            ServerIpInfo i = sd.getIpInfo();
            w.writeNext(new String[]{
                    sd.getServiceId(),
                    sd.getService().getTypeIri().stringValue(),
                    sd.getStatusString(),
                    (sr == null ? "" : sr + ""),
                    (rt == null ? "" : rt + ""),
                    (dist == null ? "" : dist + ""),
                    MonitorPage.formatDate(sd.getLastSeenDate()),
                    (i == null ? "" : i.getIp()),
                    (i == null ? "" : i.getCity() + ", " + i.getCountryName()),
            });
        }
        try {
            w.close();
            sw.close();
        } catch (IOException ex) {
            logger.error("Error closing CSV writer", ex);
        }
        return new ByteArrayResource("text/csv", sw.getBuffer().toString().getBytes());
    }

}
