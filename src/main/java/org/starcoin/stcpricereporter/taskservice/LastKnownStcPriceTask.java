package org.starcoin.stcpricereporter.taskservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.starcoin.stcpricereporter.data.model.PriceFeed;
import org.starcoin.stcpricereporter.service.OnChainManager;
import org.starcoin.stcpricereporter.service.PriceFeedService;
import org.starcoin.stcpricereporter.vo.StcUsdOracleType;

import java.math.BigDecimal;

/**
 * Keeps the STC / USD feed alive at the last price this reporter published.
 * <p>
 * Every exchange source is gone: Gate.io delisted STC in 2024-05 and the CoinEx v1 API stopped
 * answering on 2026-10-12, which froze the on-chain round. With no market source left, the last
 * stored price is republished; the aggregator cache re-reports it on its one-hour heartbeat.
 */
@Component
public class LastKnownStcPriceTask {
    private static final Logger LOG = LoggerFactory.getLogger(LastKnownStcPriceTask.class);

    public static final String DATASOURCE_KEY = "LastKnown";

    private final BigDecimal price;
    private final StcPriceAggregator stcPriceAggregator;
    private final StcPriceAggregateOnChainHelper stcPriceAggregateOnChainHelper;
    private final OnChainManager onChainManager;

    public LastKnownStcPriceTask(PriceFeedService priceFeedService,
                                 StcPriceAggregator stcPriceAggregator,
                                 StcPriceAggregateOnChainHelper stcPriceAggregateOnChainHelper,
                                 OnChainManager onChainManager) {
        PriceFeed feed = priceFeedService.getPriceFeed(PriceFeedService.PAIR_ID_STC_USD);
        if (feed == null || feed.getLatestPrice() == null) {
            throw new IllegalStateException("No stored STC / USD price to republish, pair id: "
                    + PriceFeedService.PAIR_ID_STC_USD);
        }
        this.price = new BigDecimal(feed.getLatestPrice(), StcUsdOracleType.PRICE_PRECISION);
        this.stcPriceAggregator = stcPriceAggregator;
        this.stcPriceAggregateOnChainHelper = stcPriceAggregateOnChainHelper;
        this.onChainManager = onChainManager;
        LOG.info("Republishing last known STC / USD price: {}", this.price);
    }

    @Scheduled(fixedDelay = 60_000)
    public void task() {
        stcPriceAggregateOnChainHelper.tryUpdateStcPriceOnChain(DATASOURCE_KEY, price,
                System.currentTimeMillis() / 1000, stcPriceAggregator, onChainManager);
    }
}
