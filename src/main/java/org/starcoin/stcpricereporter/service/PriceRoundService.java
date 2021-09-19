package org.starcoin.stcpricereporter.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.starcoin.stcpricereporter.data.model.PriceGrowth;
import org.starcoin.stcpricereporter.data.model.PriceRound;
import org.starcoin.stcpricereporter.data.model.PriceRoundView;
import org.starcoin.stcpricereporter.data.repo.PriceGrowthRepository;
import org.starcoin.stcpricereporter.data.repo.PriceRoundRepository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PriceRoundService {
    private static final Logger LOG = LoggerFactory.getLogger(PriceFeedService.class);
    private static final long ONE_DAY_MILLISECONDS = 24L * 60 * 60 * 1000;
    @Autowired
    private PriceRoundRepository priceRoundRepository;
    @Autowired
    private PriceGrowthRepository priceGrowthRepository;

    @Async
    public void asyncAddPriceRound(String pairId, BigInteger price, BigInteger roundId, Long updatedAt, Long startedAt, BigInteger answeredInRound) {
        //LOG.debug("To add price round in thread: " + Thread.currentThread().getName());
        try {
            doAddPriceRound(pairId, price, roundId, updatedAt, startedAt, answeredInRound);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            LOG.info("Data violation exception caught when add price round.", e);
        }
        //LOG.debug("Updating price growth info in thread: " + Thread.currentThread().getName());
        try {
            updatePriceGrowth(pairId, price, updatedAt);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            LOG.info("Data violation exception caught when update price growth info.", e);
        }
    }

    private void updatePriceGrowth(String pairId, BigInteger price, Long updatedAt) {
        PriceGrowth priceGrowth = priceGrowthRepository.findById(pairId).orElse(null);
        if (priceGrowth == null) {
            priceGrowth = new PriceGrowth();
            priceGrowth.setPairId(pairId);
            priceGrowth.setCreatedAt(updatedAt);
            priceGrowth.setCreatedBy("ADMIN");
        }
        priceGrowth.setDayOverDayPercentage(getDayOnDayPercentage(pairId, price, updatedAt));
        priceGrowth.setUpdatedAt(updatedAt);
        priceGrowth.setUpdatedBy("ADMIN");
        priceGrowthRepository.save(priceGrowth);
        priceGrowthRepository.flush();
    }

    private BigDecimal getDayOnDayPercentage(String pairId, BigInteger price, Long updatedAt) {
        PriceRoundView proximatePriceRound = getProximatePriceRound(pairId, updatedAt - ONE_DAY_MILLISECONDS);
        if (proximatePriceRound == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal priceOneDayAgo = proximatePriceRound.getPrice();
        return new BigDecimal(price).subtract(priceOneDayAgo).multiply(BigDecimal.valueOf(100))
                .divide(priceOneDayAgo, 7, RoundingMode.HALF_UP);
    }

    private void doAddPriceRound(String pairId, BigInteger price, BigInteger roundId, Long updatedAt, Long startedAt, BigInteger answeredInRound) {
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
