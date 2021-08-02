package org.starcoin.stcpricereporter.taskservice;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class StcPriceAggregator {

    public static final BigDecimal DEVIATION_PERCENTAGE = BigDecimal.valueOf(0.5);

    public static final long MAX_EXPIRATION_SECONDS = 5 * 60;

    private ConcurrentMap<String, StcPrice> stcPriceMap = new ConcurrentHashMap<>();

    public boolean updatePrice(String datasourceKey, BigDecimal price, Long dateTimeInSeconds) {
        stcPriceMap.put(datasourceKey, new StcPrice(price, dateTimeInSeconds));
        BigDecimal aggregatePrice = aggregatePrices();
        System.out.println("Aggregated price: " + aggregatePrice);
        //todo
        return true;
    }

    private BigDecimal aggregatePrices() {
        final Long[] minTimestamp = {null};
        final Long[] maxTimestamp = {null};
        stcPriceMap.forEach((k, v) -> {
            if (minTimestamp[0] == null || v.getDateTimeInSeconds() < minTimestamp[0]) {
                minTimestamp[0] = v.getDateTimeInSeconds();
            }
            if (maxTimestamp[0] == null || v.getDateTimeInSeconds() > maxTimestamp[0]) {
                maxTimestamp[0] = v.getDateTimeInSeconds();
            }
        });
        final BigDecimal[] priceSum = {BigDecimal.ZERO};
        final int[] priceCount = {0};
        stcPriceMap.forEach((k, v) -> {
            if (v.getDateTimeInSeconds().equals(maxTimestamp[0])
                    || !v.getDateTimeInSeconds().equals(minTimestamp[0])
                    || System.currentTimeMillis() / 1000 < v.getDateTimeInSeconds() + MAX_EXPIRATION_SECONDS) {
                priceCount[0] = priceCount[0] + 1;
                priceSum[0] = priceSum[0].add(v.getPrice());
            } else {
                System.out.println("Unavailable price, datasource key: "
                        + k + ", timestamp: " + v.getDateTimeInSeconds());
            }
        });
        if (priceCount[0] == 0) {
            throw new RuntimeException("No available prices.");
        }
        BigDecimal aggregatePrice = priceSum[0].divide(BigDecimal.valueOf(priceCount[0]));
        return aggregatePrice;
    }

    public static class StcPrice {
        private BigDecimal price;
        private Long dateTimeInSeconds;

        public StcPrice(BigDecimal price, Long dateTimeInSeconds) {
            this.price = price;
            this.dateTimeInSeconds = dateTimeInSeconds;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public Long getDateTimeInSeconds() {
            return dateTimeInSeconds;
        }
    }
}
