package org.starcoin.stcpricereporter.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.starcoin.stcpricereporter.data.model.PriceFeed;
import org.starcoin.stcpricereporter.data.repo.PriceFeedRepository;

import java.math.BigDecimal;
import java.math.BigInteger;

@Service
public class PriceFeedService {
    private final Logger LOG = LoggerFactory.getLogger(PriceFeedService.class);

    @Autowired
    private PriceFeedRepository priceFeedRepository;

    public PriceFeed getPriceFeed(String pairId) {
        return priceFeedRepository.findById(pairId).orElse(null);
    }

    public void updatePrice(String tokenPairId, BigInteger price) {
        PriceFeed priceFeed = priceFeedRepository.findById(tokenPairId).orElse(null);
        if (priceFeed == null) {
            LOG.info("CANNOT find price feed by Id: " + tokenPairId);
            return;
        }
        priceFeed.setLatestPrice(price);
        priceFeed.setUpdatedAt(System.currentTimeMillis());
        priceFeed.setUpdatedBy("ADMIN");
        priceFeedRepository.save(priceFeed);
    }

    public void createPriceFeedIfNotExists(String tokenPairId, String name, Integer decimals,
                                           BigDecimal deviationPercentage, BigDecimal heartbeatHours) {
        PriceFeed priceFeed = priceFeedRepository.findById(tokenPairId).orElse(null);
        if (priceFeed == null) {
            priceFeed = new PriceFeed();
            priceFeed.setPairId(tokenPairId);
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
}
