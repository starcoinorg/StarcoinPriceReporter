package org.starcoin.stcpricereporter.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.starcoin.stcpricereporter.data.model.PriceGrowth;
import org.starcoin.stcpricereporter.data.model.PriceRound;
import org.starcoin.stcpricereporter.data.repo.PriceGrowthRepository;
import org.starcoin.stcpricereporter.data.repo.PriceRoundRepository;
import org.starcoin.stcpricereporter.vo.PriceAverage;
import org.starcoin.stcpricereporter.vo.PriceRoundView;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
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

    public List<PriceAverage> getPriceAverages(List<String> pairIds, Long afterTimestamp, Long beforeTimestamp) {
        List<PriceRound> priceRounds = priceRoundRepository.findByPairIdInAndUpdatedAtGreaterThanEqualAndUpdatedAtLessThanOrderByPairId(
                pairIds, afterTimestamp, beforeTimestamp);
        List<PriceAverage> priceAverages = new ArrayList<>();
        String pId = null;
        List<PriceRound> prs = new ArrayList<>();
        for (int i = 0; i < priceRounds.size(); i++) {
            PriceRound pr = priceRounds.get(i);
            if (!pr.getPairId().equals(pId) && prs.size() > 0) {
                priceAverages.add(getPriceAverage(prs));
                prs.clear();
            }
            prs.add(pr);
            pId = pr.getPairId();
            if (i == priceRounds.size() - 1) {
                priceAverages.add(getPriceAverage(prs));
            }
        }
        return priceAverages;
    }

    private PriceAverage getPriceAverage(List<PriceRound> priceRounds) {
        priceRounds.sort(Comparator.comparing(PriceRound::getPrice));
        int skip = Math.max(0, ((priceRounds.size() + 1) / 2) - 1);
        int limit = 1 + (1 + priceRounds.size()) % 2;
        BigInteger sum = BigInteger.ZERO;
        BigInteger median = BigInteger.ZERO;
        for (int i = 0; i < priceRounds.size(); i++) {
            PriceRound pr = priceRounds.get(i);
            sum = sum.add(pr.getPrice());
            if (i >= skip && i < skip + limit) {
                median = median.add(pr.getPrice());
            }
        }
        PriceAverage priceAverage = new PriceAverage();
        priceAverage.setPairId(priceRounds.get(0).getPairId());
        priceAverage.setMean(sum.divide(BigInteger.valueOf(priceRounds.size())));
        priceAverage.setMedian(median.divide(BigInteger.valueOf(limit)));
        return priceAverage;
    }

}
