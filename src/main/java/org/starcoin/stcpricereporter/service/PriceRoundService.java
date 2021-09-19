package org.starcoin.stcpricereporter.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.starcoin.stcpricereporter.data.model.PriceRound;
import org.starcoin.stcpricereporter.data.model.PriceRoundView;
import org.starcoin.stcpricereporter.data.repo.PriceRoundRepository;

import java.math.BigInteger;
import java.util.List;

@Service
public class PriceRoundService {
    private static final Logger LOG = LoggerFactory.getLogger(PriceFeedService.class);

    @Autowired
    private PriceRoundRepository priceRoundRepository;

    @Async
    public void asyncAddPriceRound(String pairId, BigInteger price, BigInteger roundId, Long updatedAt, Long startedAt, BigInteger answeredInRound) {
        //LOG.debug("To add price round in thread: " + Thread.currentThread().getName());
        PriceRound priceRound = new PriceRound();
        priceRound.setPairId(pairId);
        priceRound.setRoundId(roundId);
        priceRound.setPrice(price);
        priceRound.setCreatedAt(updatedAt);
        priceRound.setUpdatedAt(updatedAt);
        priceRound.setStartedAt(startedAt);
        priceRound.setAnsweredInRound(answeredInRound);
        priceRound.setCreatedBy("ADMIN");
        priceRound.setUpdatedBy("ADMIN");
        priceRoundRepository.save(priceRound);
        priceRoundRepository.flush();
        //LOG.debug("Added price round in thread: " + Thread.currentThread().getName());
    }


    public PriceRoundView getProximatePriceRound(String pairId, Long timestamp) {
        List<PriceRoundView> priceRounds = priceRoundRepository.findProximateRounds(pairId, timestamp);
        if (priceRounds.size() == 0) {
            return null;
        }
        if (priceRounds.size() == 1) {
            return priceRounds.get(0);
        }
        return Math.abs(priceRounds.get(0).getUpdatedAt().longValue() - timestamp)
                < Math.abs(priceRounds.get(1).getUpdatedAt().longValue() - timestamp)
                ? priceRounds.get(0) : priceRounds.get(1);
    }
}
