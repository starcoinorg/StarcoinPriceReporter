package org.starcoin.stcpricereporter.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.starcoin.stcpricereporter.data.model.PricePair;
import org.starcoin.stcpricereporter.data.repo.PricePairRepository;
import org.starcoin.stcpricereporter.vo.PriceOracleType;

import java.util.List;
import java.util.Objects;

@Service
public class PricePairService {
    private static final Logger LOG = LoggerFactory.getLogger(PricePairService.class);

    @Autowired
    private PricePairRepository pricePairRepository;

    public void createOrUpdatePricePair(String pairId, String pairName, PriceOracleType priceOracleType, Integer decimals) {
        PricePair pricePair = pricePairRepository.findById(pairId).orElse(null);
        if (pricePair == null) {
            pricePair = new PricePair();
            pricePair.setPairId(pairId);
            pricePair.setPairName(pairName);
            pricePair.setOnChainStructType(priceOracleType.toMoveStructType());
            pricePair.setDecimals(decimals);
            pricePair.setCreatedAt(System.currentTimeMillis());
            pricePair.setCreatedBy("admin");
            pricePair.setUpdatedAt(pricePair.getCreatedAt());
            pricePair.setUpdatedBy(pricePair.getCreatedBy());
        } else if (!Objects.equals(pricePair.getOnChainStructType(), priceOracleType.toMoveStructType())
                || !Objects.equals(pricePair.getPairName(), pairName)
                || !Objects.equals(pricePair.getDecimals(), decimals)) {
            if (LOG.isInfoEnabled()) LOG.info("Update price pair info. Pair Id: " + pairId);
            pricePair.setPairName(pairName);
            pricePair.setOnChainStructType(priceOracleType.toMoveStructType());
            pricePair.setDecimals(decimals);
            pricePair.setUpdatedAt(System.currentTimeMillis());
            pricePair.setUpdatedBy("admin");
        }
        pricePairRepository.save(pricePair);
    }


    public boolean createPricePairIfNotExisted(String pairId, String pairName, PriceOracleType priceOracleType, Integer decimals) {
        PricePair pricePair = pricePairRepository.findById(pairId).orElse(null);
        if (pricePair == null) {
            pricePair = new PricePair();
            pricePair.setPairId(pairId);
            pricePair.setPairName(pairName);
            pricePair.setOnChainStructType(priceOracleType.toMoveStructType());
            pricePair.setDecimals(decimals);
            pricePair.setCreatedAt(System.currentTimeMillis());
            pricePair.setCreatedBy("admin");
            pricePair.setUpdatedAt(pricePair.getCreatedAt());
            pricePair.setUpdatedBy(pricePair.getCreatedBy());
        } else {
            return false;
        }
        pricePairRepository.save(pricePair);
        return true;
    }

    public List<PricePair> getPricePairs() {
        return pricePairRepository.findAll();
    }
}
