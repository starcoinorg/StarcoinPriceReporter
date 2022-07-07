package org.starcoin.stcpricereporter.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.starcoin.stcpricereporter.data.model.PriceFeed;
import org.starcoin.stcpricereporter.data.repo.PriceFeedRepository;
import org.starcoin.stcpricereporter.vo.StcUsdOracleType;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Price feed service using database.
 */
@Service
public class PriceFeedService {
    public static final String PAIR_ID_STC_USD = StcUsdOracleType.STC_USD_ORALCE_TYPE_STRUCT_NAME;
    public static final String PAIR_ID_ETH_USD = "ETH_USD";
    public static final String PAIR_NAME_STC_USD = "STC / USD";
    private static final BigDecimal ETH_TO_WEI = BigDecimal.TEN.pow(18);
    private static final BigDecimal STC_TO_NANOSTC = BigDecimal.TEN.pow(9);
    private static final int MAX_NODE_TIME_DIFFERENCE_SECONDS = 60;
    private static final Logger LOG = LoggerFactory.getLogger(PriceFeedService.class);

    @Autowired
    private PriceFeedRepository priceFeedRepository;

    @Autowired
    private PriceRoundService priceRoundService;

    /**
     * Try update price in database, swallow unexpected error.
     * This method implemented a sync lock using database, only the node updated DB successfully do On-Chain updating.
     *
     * @param pairId token pair Id.
     * @param price  current price.
     * @return If price in DB updated, return ture, else return false. If UNEXPECTED runtime ERROR CAUGHT, RETURN TRUE!
     */
    public static boolean tryUpdatePriceInDatabase(PriceFeedService priceFeedService, String pairId, BigInteger price,
                                                   BigInteger roundId, Long updatedAt,
                                                   Long startedAt, BigInteger answeredInRound) {
        try {
            if (!priceFeedService.tryUpdatePrice(pairId, price, roundId, updatedAt, startedAt, answeredInRound)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Trying update database return false. Maybe no need to update or another process have updated it. " + pairId + ": " + price);
                }
                return false;
            } else {
                return true;
            }
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException optimisticLockingFailureException) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Try update database failed, cause of ObjectOptimisticLockingFailureException." + pairId + ": " + price);
            }
            return false;
        } catch (RuntimeException exception) {
            LOG.info("Update price in database caught runtime error. " + pairId + ": " + price, exception);
            return true;// continue update on-chain.
        }
    }

    /**
     * Check if update DB is needed.
     *
     * @param priceFeed        priceFeed in database
     * @param currentPrice     current price
     * @param currentTimestamp current timestamp
     * @return true if update is needed
     */
    private static boolean needUpdateLatestPrice(PriceFeed priceFeed, BigInteger currentPrice, Long currentTimestamp) {
        return priceFeed.getLatestPrice() == null
                || priceFeed.getLatestPrice().compareTo(currentPrice) != 0
                // latestPrice in DB == current price, maybe another node updated it.
                || priceFeed.getUpdatedAt() != null && currentTimestamp - priceFeed.getUpdatedAt() > 1000 * MAX_NODE_TIME_DIFFERENCE_SECONDS;
    }

    public List<PriceFeed> getPriceFeeds() {
        return priceFeedRepository.findAll();
    }

    public PriceFeed getPriceFeed(String pairId) {
        return priceFeedRepository.findById(pairId).orElse(null);
    }

    public List<PriceFeed> findToUsdPriceFeedsByTokenIdIn(List<String> tokenIds) {
        List<String> tokenPairIds = new ArrayList<>();
        for (String t : tokenIds) {
            tokenPairIds.add(getToUsdPairId(t));
        }
        return priceFeedRepository.findByPairIdIn(tokenPairIds);
    }

    private String getToUsdPairId(String tokenId) {
        if ("STC".equals(tokenId)) {//todo confg?
            return StcUsdOracleType.STC_USD_ORALCE_TYPE_STRUCT_NAME;
        }
        return tokenId + "_USD";
    }

    /**
     * Try update price in database.
     *
     * @param pairId token pair Id.
     * @param price  current price
     * @return If database not need to update, return false, else update it and return true.
     */
    @Transactional
    public boolean tryUpdatePrice(String pairId, BigInteger price, BigInteger roundId, Long updatedAt,
                                  Long startedAt, BigInteger answeredInRound) {
        PriceFeed priceFeed = assertPriceFeed(pairId);
        if (needUpdateLatestPrice(priceFeed, price, updatedAt)) {
            priceFeed.setLatestPrice(price);
            priceFeed.onChainStatusUpdating();
            priceFeed.setUpdatedAt(updatedAt);
            priceFeed.setUpdatedBy("ADMIN");
            priceFeedRepository.save(priceFeed);
            priceFeedRepository.flush();
            //LOG.debug("Updated latest price in thread: " + Thread.currentThread().getName());
            priceRoundService.asyncAddPriceRound(pairId, price, roundId, updatedAt, startedAt, answeredInRound);
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

    /**
     * Create price feed in database if not existed. If existed, just log and ignore it.
     *
     * @param pairId              Token pair Id.(also struct name in Move lang contract)
     * @param name                Token pair name.
     * @param decimals            decimals.
     * @param deviationPercentage If price change greater than this deviation, update on-chain.
     * @param heartbeatHours      heartbeat interval in hours.
     * @param chainlinkProxy      Chainlink proxy contract address.
     */
    public void createPriceFeedIfNotExisted(String pairId, String name, Integer decimals, BigDecimal deviationPercentage,
                                            BigDecimal heartbeatHours, String chainlinkProxy) {
        PriceFeed priceFeed = priceFeedRepository.findById(pairId).orElse(null);
        if (priceFeed == null) {
            priceFeed = new PriceFeed();
            priceFeed.setPairId(pairId);
            priceFeed.setPairName(name);
            priceFeed.setDecimals(decimals);
            priceFeed.setDeviationPercentage(deviationPercentage);
            priceFeed.setHeartbeatHours(heartbeatHours);
            priceFeed.setChainlinkProxy(chainlinkProxy);
            priceFeed.setCreatedAt(System.currentTimeMillis());
            priceFeed.setCreatedBy("ADMIN");
            priceFeed.setUpdatedAt(priceFeed.getCreatedAt());
            priceFeed.setUpdatedBy(priceFeed.getCreatedBy());
            //priceFeed.setLatestPrice(price);
            priceFeedRepository.save(priceFeed);
        } else {
            LOG.debug("Price feed '" + pairId + "' existed, settings in database is not updated really.");
////            if (decimals != null && !Objects.equals(priceFeed.getDecimals(), decimals))
////                LOG.info("Try update decimals, but failed.");
////            if (deviationPercentage != null && !Objects.equals(deviationPercentage, priceFeed.getDeviationPercentage()))
////                LOG.info("Try update decimals, but failed.");
////            //priceFeed.setHeartbeatHours(heartbeatHours);
////            if (chainlinkProxy != null && !Objects.equals(chainlinkProxy, priceFeed.getChainlinkProxy()))
////                LOG.info("ry update chainlink proxy, but failed.");
//            priceFeed.setUpdatedAt(System.currentTimeMillis());
//            priceFeed.setUpdatedBy("ADMIN");
//            //priceFeed.setLatestPrice(price);
//            priceFeedRepository.save(priceFeed);
        }

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
        return ethToUsd.divide(stcToUsd, 18, RoundingMode.HALF_UP);
    }


}
