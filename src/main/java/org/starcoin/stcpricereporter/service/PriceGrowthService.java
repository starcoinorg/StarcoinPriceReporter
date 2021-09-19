package org.starcoin.stcpricereporter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.starcoin.stcpricereporter.data.model.PriceGrowth;
import org.starcoin.stcpricereporter.data.repo.PriceGrowthRepository;

import java.util.List;

@Service
public class PriceGrowthService {

    @Autowired
    private PriceGrowthRepository priceGrowthRepository;

    public List<PriceGrowth> findByPairIdIn(List<String> pairIds) {
        return priceGrowthRepository.findByPairIdIn(pairIds);
    }
}
