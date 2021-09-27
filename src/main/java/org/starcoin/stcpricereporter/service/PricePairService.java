package org.starcoin.stcpricereporter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.starcoin.stcpricereporter.data.model.PricePair;
import org.starcoin.stcpricereporter.data.repo.PricePairRepository;
import org.starcoin.stcpricereporter.vo.PriceOracleType;

import java.util.List;

@Service
public class PricePairService {

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
        } else {
            //todo update price pair info in database.
        }
        pricePairRepository.save(pricePair);
    }

    public List<PricePair> getPricePairs() {
        return pricePairRepository.findAll();
    }
}
