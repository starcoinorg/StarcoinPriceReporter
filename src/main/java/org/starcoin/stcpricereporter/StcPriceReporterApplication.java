package org.starcoin.stcpricereporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.starcoin.stcpricereporter.service.OnChainManager;
import org.starcoin.stcpricereporter.service.PriceFeedService;
import org.starcoin.stcpricereporter.service.PricePairService;
import org.starcoin.stcpricereporter.taskservice.StcPriceAggregator;
import org.starcoin.stcpricereporter.vo.StcUsdOracleType;
import springfox.documentation.oas.annotations.EnableOpenApi;

import java.math.BigDecimal;
import java.math.RoundingMode;

@SpringBootApplication
@EnableOpenApi
@EnableScheduling
@EnableAsync
public class StcPriceReporterApplication {
    private static final Logger LOG = LoggerFactory.getLogger(StcPriceReporterApplication.class);

    @Autowired
    private PriceFeedService priceFeedService;

    @Autowired
    private OnChainManager onChainManager;

    @Autowired
    private PricePairService pricePairService;

    public static void main(String[] args) {
        SpringApplication.run(StcPriceReporterApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    void initStcUsdPriceFeed() {
        String pairId = PriceFeedService.PAIR_ID_STC_USD;
        String pairName = PriceFeedService.PAIR_NAME_STC_USD;
        BigDecimal deviationPercentage = StcPriceAggregator.StcPriceCache.DEVIATION_PERCENTAGE;
        BigDecimal heartbeatHours = BigDecimal.valueOf(StcPriceAggregator.StcPriceCache.HEARTBEAT_SECONDS)
                .divide(BigDecimal.valueOf(60 * 60), 3, RoundingMode.HALF_UP);
        priceFeedService.createPriceFeedIfNotExisted(pairId, pairName, StcUsdOracleType.PRICE_PRECISION,
                deviationPercentage, heartbeatHours, null);
        //		System.out.println(priceFeedService.getEthToStcExchangeRate());
        //		System.out.println(priceFeedService.getWeiToNanoStcExchangeRate());
        pricePairService.createOrUpdatePricePair(pairId, pairName, StcUsdOracleType.INSTANCE, StcUsdOracleType.PRICE_PRECISION);
    }

    @EventListener(ApplicationReadyEvent.class)
    void initStarcoinTransactionSenderAccount() {
        try {
            onChainManager.createSenderAccountIfNoExists();
        } catch (RuntimeException e) {
            LOG.error("Create starcoin sender account error.", e);
        }
    }

}
