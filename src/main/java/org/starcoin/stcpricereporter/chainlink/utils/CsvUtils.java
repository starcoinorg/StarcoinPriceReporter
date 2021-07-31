package org.starcoin.stcpricereporter.chainlink.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.starcoin.stcpricereporter.chainlink.PriceFeedRecord;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CsvUtils {
    public static final String COLUMN_PAIR = "Pair";
    public static final String COLUMN_DEVIATION = "Deviation";
    public static final String COLUMN_HEARTBEAT = "Heartbeat";
    public static final String COLUMN_DECIMALS = "Dec";
    public static final String COLUMN_PROXY = "Proxy";
    public static final String COLUMN_ENABLED = "Enabled";

    public static List<PriceFeedRecord> readCsvPriceFeedRecords(Reader in) {
        Iterable<CSVRecord> records;
        try {
            records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        List<PriceFeedRecord> priceFeedRecords = new ArrayList<>();
        for (CSVRecord record : records) {
            String pair = record.get(COLUMN_PAIR);
            String deviation = record.get(COLUMN_DEVIATION);
            //System.out.println(deviation);
            BigDecimal deviationPercentage = "N/A".equals(deviation) ? null : new BigDecimal(deviation.substring(0, deviation.lastIndexOf("%")));
            String heartbeat = record.get(COLUMN_HEARTBEAT);
            if (!heartbeat.endsWith("h")) {
                throw new RuntimeException("Heartbeat NOT ends with 'h'");
            }
            BigDecimal heartBeatHours = new BigDecimal(heartbeat.substring(0, heartbeat.lastIndexOf("h")));
            String decStr = record.get(COLUMN_DECIMALS);
            Integer decimals = Integer.parseInt(decStr);
            String proxy = record.get(COLUMN_PROXY);
            String enabledStr = record.get(COLUMN_ENABLED);
            Boolean enabled = enabledStr == null || enabledStr.isEmpty() ? null :
                    "Y".equals(enabledStr.toUpperCase()) || Boolean.parseBoolean(enabledStr) ? true : false;
            PriceFeedRecord priceFeedRecord = new PriceFeedRecord(pair, deviationPercentage, heartBeatHours, decimals, proxy, enabled);
//            if (enabled) {
//                System.out.println(priceFeedRecord);
//            }
            priceFeedRecords.add(priceFeedRecord);
        }
        return priceFeedRecords;
    }
}
