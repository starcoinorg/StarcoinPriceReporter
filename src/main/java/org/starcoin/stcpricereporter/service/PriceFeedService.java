package org.starcoin.stcpricereporter.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.starcoin.stcpricereporter.data.model.PriceFeed;
import org.starcoin.stcpricereporter.data.repo.PriceFeedRepository;
import org.starcoin.stcpricereporter.taskservice.StcUsdOracleType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;

/**
 * Price feed service using database.
 */
@Service
public class PriceFeedService {
    public static final String PAIR_ID_STC_USD = StcUsdOracleType.STC_USD_ORALCE_TYPE_STRUCT_NAME;
    public static final String PAIR_ID_ETH_USD = "ETH_USD";
    private static final BigDecimal ETH_TO_WEI = BigDecimal.TEN.pow(18);
    private static final BigDecimal STC_TO_NANOSTC = BigDecimal.TEN.pow(9);
    private static final int DATABASE_HEARTBEAT_SECONDS = 60;
    private final Logger LOG = LoggerFactory.getLogger(PriceFeedService.class);
    @Autowired
    private PriceFeedRepository priceFeedRepository;

    public List<PriceFeed> getPriceFeeds() {
        return priceFeedRepository.findAll();
    }

    public PriceFeed getPriceFeed(String pairId) {
        return priceFeedRepository.findById(pairId).orElse(null);
    }

    /**
     * Try update price in database.
     *
     * @param pairId
     * @param price
     * @return If database not need to update, return false. Else return true.
     */
    public boolean tryUpdatePrice(String pairId, BigInteger price) {
        PriceFeed priceFeed = assertPriceFeed(pairId);
        if (priceFeed.getLatestPrice() == null
                || priceFeed.getLatestPrice().compareTo(price) != 0
                || priceFeed.getUpdatedAt() != null && System.currentTimeMillis() - priceFeed.getUpdatedAt() > 1000 * DATABASE_HEARTBEAT_SECONDS) {
            priceFeed.setLatestPrice(price);
            priceFeed.onChainStatusUpdating();
            priceFeed.setUpdatedAt(System.currentTimeMillis());
            priceFeed.setUpdatedBy("ADMIN");
            priceFeedRepository.save(priceFeed);
            priceFeedRepository.flush();
            return true;
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Price in database NOT need to update. PairId: " + pairId + ", current price: " + price);
            }
            return false;
        }
    }

    public void setOnChainStatusSubmitted(String pairId, String onChainTransactionHash) {
        PriceFeed priceFeed = assertPriceFeed(pairId);
        priceFeed.onChainStatusSubmitted(onChainTransactionHash);
        priceFeed.setUpdatedAt(System.currentTimeMillis());
        priceFeed.setUpdatedBy("ADMIN");
        priceFeedRepository.save(priceFeed);
    }


    public void setOnChainStatusNoOnChain(String pairId) {
        PriceFeed priceFeed = assertPriceFeed(pairId);
        priceFeed.onChainStatusNoOnChain();
        priceFeed.setUpdatedAt(System.currentTimeMillis());
        priceFeed.setUpdatedBy("ADMIN");
        priceFeedRepository.save(priceFeed);
    }

    private PriceFeed assertPriceFeed(String pairId) {
        PriceFeed priceFeed = priceFeedRepository.findById(pairId).orElse(null);
        if (priceFeed == null) {
            String msg = "CANNOT find price feed in database by pairId: " + pairId;
            LOG.error(msg);
            throw new RuntimeException(msg);
        }
        return priceFeed;
    }

    public void createPriceFeedIfNotExists(String pairId, String name, Integer decimals,
                                           BigDecimal deviationPercentage, BigDecimal heartbeatHours) {
        PriceFeed priceFeed = priceFeedRepository.findById(pairId).orElse(null);
        if (priceFeed == null) {
            priceFeed = new PriceFeed();
            priceFeed.setPairId(pairId);
            priceFeed.setPairName(name);
            priceFeed.setDecimals(decimals);
            priceFeed.setDeviationPercentage(deviationPercentage);
            priceFeed.setHeartbeatHours(heartbeatHours);
            priceFeed.setCreatedAt(System.currentTimeMillis());
            priceFeed.setCreatedBy("ADMIN");
            priceFeed.setUpdatedAt(priceFeed.getCreatedAt());
            priceFeed.setUpdatedBy(priceFeed.getCreatedBy());
        } else {
            priceFeed.setUpdatedAt(System.currentTimeMillis());
            priceFeed.setUpdatedBy("ADMIN");
        }
        //priceFeed.setLatestPrice(price);
        priceFeedRepository.save(priceFeed);
    }

    /**
     * Get current WEI to NanoSTC exchange rate.
     */
    public BigDecimal getWeiToNanoStcExchangeRate() {
        BigDecimal ethToStc = getEthToStcExchangeRate();
        return ethToStc.divide(ETH_TO_WEI, 18, BigDecimal.ROUND_HALF_UP).multiply(STC_TO_NANOSTC);
    }

    public BigDecimal getEthToStcExchangeRate() {
        PriceFeed ethUsdPF = priceFeedRepository.findById(PAIR_ID_ETH_USD)
                .orElseThrow(() -> new RuntimeException("CANNOT get ETH price."));
        BigDecimal ethToUsd = new BigDecimal(ethUsdPF.getLatestPrice())
                .divide(BigDecimal.TEN.pow(ethUsdPF.getDecimals()), 18, RoundingMode.HALF_UP);
        PriceFeed stcUsdPF = priceFeedRepository.findById(PAIR_ID_STC_USD)
                .orElseThrow(() -> new RuntimeException("CANNOT get STC price."));
        BigDecimal stcToUsd = new BigDecimal(stcUsdPF.getLatestPrice())
                .divide(BigDecimal.TEN.pow(stcUsdPF.getDecimals()), 18, RoundingMode.HALF_UP);
        BigDecimal ethToStc = ethToUsd.divide(stcToUsd, 18, RoundingMode.HALF_UP);
        return ethToStc;
    }

}
