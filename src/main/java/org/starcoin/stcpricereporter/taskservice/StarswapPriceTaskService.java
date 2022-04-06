package org.starcoin.stcpricereporter.taskservice;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.starcoin.stcpricereporter.service.PriceFeedService;
import org.starcoin.stcpricereporter.service.PricePairService;
import org.starcoin.stcpricereporter.vo.PriceOracleType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static org.starcoin.stcpricereporter.service.PriceFeedService.tryUpdatePriceInDatabase;

@Component
public class StarswapPriceTaskService {

    private static final Logger LOG = LoggerFactory.getLogger(StarswapPriceTaskService.class);

    private static final List<String> STARSWAP_TOKEN_ID_LIST = Arrays.asList("STAR");
    private static final String UNKNOWN_MODULE_ADDRESS = "0x99";
    private static final String UNKNOWN_MODULE_NAME = "UNKNOWN_MODULE";
    private static final int TOKEN_TO_USD_PRICE_DECIMALS = 6;
    private static final BigDecimal DEVIATION_PERCENTAGE = new BigDecimal(0.5);
    private static final BigDecimal HEARTBEAT_HOURS = new BigDecimal(0.5);
    private static final String URL_TOKEN_ID_PLACEHOLDER = "{tokenId}";

    private final StarswapPriceCache starswapPriceCache = new StarswapPriceCache();

    @Autowired
    private PriceFeedService priceFeedService;

    @Autowired
    private PricePairService pricePairService;

    @Value("${starcoin.starswap.get-token-to-usd-exchange-rate-url}")
    private String getTokenToUsdExchangeRateUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Scheduled(fixedDelayString = "${starcoin.starswap-price-task-service.fixed-delay}")
    public void task() {
        for (String tokenId : STARSWAP_TOKEN_ID_LIST) {
            String pairId = getToUsdPairIdByTokenId(tokenId);
            String pairName = getToUsdPairNameByTokenId(tokenId);
            pricePairService.createPricePairIfNotExisted(pairId, pairName, getToUsdPriceOracleTypeByTokenId(tokenId), TOKEN_TO_USD_PRICE_DECIMALS);
            priceFeedService.createPriceFeedIfNotExisted(pairId, pairName, TOKEN_TO_USD_PRICE_DECIMALS,
                    DEVIATION_PERCENTAGE, HEARTBEAT_HOURS, null);
            Long updatedAt = System.currentTimeMillis();
            BigInteger roundId = BigInteger.valueOf(updatedAt);
            BigInteger price = getTokenToUsdPrice(tokenId);
            boolean needReport = starswapPriceCache.tryUpdate(price, updatedAt);
            if (needReport) {
                try {
                    tryUpdatePriceInDatabase(priceFeedService, pairId, price, roundId, updatedAt, null, null);
                } catch (RuntimeException runtimeException) {
                    LOG.error("Update " + pairId + " price in DB error.", runtimeException);
                }
                try {
                    priceFeedService.setOnChainStatusNoOnChain(pairId);
                } catch (RuntimeException runtimeException) {
                    LOG.info("Update on-chain status in database caught runtime error. PairId: " + pairId, runtimeException);
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Not need to update {} price in DB, {} = {}", pairId, starswapPriceCache.getPrice(), price);
                }
            }
        }

    }

    private BigInteger getTokenToUsdPrice(String tokenId) {
        String url = getTokenToUsdExchangeRateUrl.replace(URL_TOKEN_ID_PLACEHOLDER, tokenId);
        BigDecimal exchangeRate = restTemplate.getForObject(url, BigDecimal.class);
        return exchangeRate.multiply(new BigDecimal(10).pow(TOKEN_TO_USD_PRICE_DECIMALS)).toBigInteger();
    }

    private PriceOracleType getToUsdPriceOracleTypeByTokenId(String tokenId) {
        return new PriceOracleType(UNKNOWN_MODULE_ADDRESS, UNKNOWN_MODULE_NAME, getToUsdPairIdByTokenId(tokenId));
    }

    private String getToUsdPairNameByTokenId(String tokenId) {
        return tokenId + " / USD";
    }

    private String getToUsdPairIdByTokenId(String tokenId) {
        return tokenId + "_USD";
    }


    public static class StarswapPriceCache implements OffChainPriceCache<BigInteger> {

        public static final int HEARTBEAT_SECONDS = HEARTBEAT_HOURS.multiply(new BigDecimal(60 * 60)).intValue();
        private BigInteger price;
        private Long lastUpdatedAt;
        private boolean dirty = false;
        private boolean firstUpdate = false;

        @Override
        public synchronized boolean tryUpdate(BigInteger price, Long timestamp) {
            if (this.price == null
                    || this.dirty
                    || new BigDecimal(this.price).subtract(new BigDecimal(price)).abs().divide(new BigDecimal(this.price), BigDecimal.ROUND_HALF_DOWN)
                    .multiply(BigDecimal.valueOf(100L)).compareTo(DEVIATION_PERCENTAGE) > 0
                    || timestamp / 1000 > this.lastUpdatedAt + HEARTBEAT_SECONDS
            ) {
                if (this.price == null) {
                    this.firstUpdate = true;
                }
                this.price = price;
                this.lastUpdatedAt = timestamp;
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

        public synchronized BigInteger getPrice() {
            return price;
        }
    }

}
