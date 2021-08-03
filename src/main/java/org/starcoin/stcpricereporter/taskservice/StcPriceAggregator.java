package org.starcoin.stcpricereporter.taskservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class StcPriceAggregator {
    private Logger LOG = LoggerFactory.getLogger(StcPriceAggregator.class);

    public static final long MAX_EXPIRATION_SECONDS = 2 * 60;

    private final ConcurrentMap<String, StcPrice> stcPriceMap = new ConcurrentHashMap<>();

    private final StcPriceCache stcPriceCache = new StcPriceCache();

    public OffChainPriceCache<BigDecimal> getStcPriceCache() {
        return stcPriceCache;
    }

    public boolean updatePrice(String datasourceKey, BigDecimal price, Long dateTimeInSeconds) {
        stcPriceMap.put(datasourceKey, new StcPrice(price, dateTimeInSeconds));
        BigDecimal aggregatePrice = aggregatePrices();
        LOG.debug("Aggregated STC price: " + aggregatePrice);
        return stcPriceCache.tryUpdate(aggregatePrice, System.currentTimeMillis() / 1000);
    }

    public void markOnChainUpdated() {
        this.stcPriceCache.setDirty(false);
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
                LOG.debug("Unavailable price, datasource key: "
                        + k + ", timestamp: " + v.getDateTimeInSeconds());
            }
        });
        if (priceCount[0] == 0) {
            throw new RuntimeException("No available prices.");
        }
        BigDecimal aggregatePrice = priceSum[0].divide(BigDecimal.valueOf(priceCount[0]), BigDecimal.ROUND_HALF_DOWN);
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

    public static class StcPriceCache implements OffChainPriceCache<BigDecimal> {
        public static final BigDecimal DEVIATION_PERCENTAGE = BigDecimal.valueOf(0.5);
        private static final int HEARTBEAT_SECONDS = 60 * 60; // One hour
        private BigDecimal price;
        private Long lastUpdatedAt;
        private boolean dirty = false;
        private boolean firstUpdate = false;

        @Override
        public synchronized boolean tryUpdate(BigDecimal price, Long dateTimeInSeconds) {
            if (this.price == null
                    || this.dirty
                    || this.price.subtract(price).abs().divide(this.price, BigDecimal.ROUND_HALF_DOWN)
                    .multiply(BigDecimal.valueOf(100L)).compareTo(DEVIATION_PERCENTAGE) > 0
                    || dateTimeInSeconds > this.lastUpdatedAt + HEARTBEAT_SECONDS
            ) {
                if (this.price == null) {
                    this.firstUpdate = true;
                }
                this.price = price;
                this.lastUpdatedAt = dateTimeInSeconds;
                this.dirty = true;
            }
            return this.dirty;
        }

        @Override
        public synchronized boolean isDirty() {
            return dirty;
        }

        @Override
        public synchronized void setDirty(boolean dirty) {
            this.dirty = dirty;
        }

        @Override
        public synchronized boolean isFirstUpdate() {
            return firstUpdate;
        }
    }


}
