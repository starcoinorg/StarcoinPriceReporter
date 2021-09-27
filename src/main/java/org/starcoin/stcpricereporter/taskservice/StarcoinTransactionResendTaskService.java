package org.starcoin.stcpricereporter.taskservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.starcoin.stcpricereporter.data.model.PriceFeed;
import org.starcoin.stcpricereporter.data.repo.PriceFeedRepository;
import org.starcoin.stcpricereporter.service.OnChainManager;
import org.starcoin.stcpricereporter.vo.PriceOracleType;
import org.starcoin.stcpricereporter.vo.StcUsdOracleType;

import java.math.BigInteger;
import java.util.List;

@Component
public class StarcoinTransactionResendTaskService {
    private static final Logger LOG = LoggerFactory.getLogger(StarcoinTransactionResendTaskService.class);

    @Value("${starcoin.transaction-resend-task-service.resend-updated-before-seconds}")
    private Long resendUpdatedBeforeSeconds;// = 5L;

    @Autowired
    private PriceFeedRepository priceFeedRepository;

    @Autowired
    private OnChainManager onChainManager;

    @Autowired
    private ChainlinkTaskScheduler chainlinkTaskScheduler;

    @Scheduled(fixedDelayString = "${starcoin.transaction-resend-task-service.fixed-delay}")
    public void task() {
        Long updatedBefore = System.currentTimeMillis() - resendUpdatedBeforeSeconds * 1000;
        List<PriceFeed> transactions = priceFeedRepository
                .findByOnChainStatusInAndUpdatedAtLessThanOrderByUpdatedAt(new String[]{
                        PriceFeed.ON_CHAIN_STATUS_UPDATING,
                        PriceFeed.ON_CHAIN_STATUS_SUBMITTED
                }, updatedBefore);
        if (transactions == null || transactions.isEmpty()) {
            return;
        }
        for (PriceFeed t : transactions) {
            LOG.debug("Find updating or submitted transaction which without receipt and updated before "
                    + updatedBefore + " seconds: " + t.getOnChainTransactionHash());
            //if (null != t.getOnChainTransactionHash() && !t.getOnChainTransactionHash().isEmpty()) {
            try {
                BigInteger roundId = BigInteger.valueOf(System.currentTimeMillis());
                onChainManager.initDataSourceOrUpdateOnChain(getPriceOracleType(t.getPairId()), t.getLatestPrice(),
                        roundId, t.getUpdatedAt(), null, null); // todo null??
            } catch (RuntimeException exception) {
                LOG.error("Update on-chain price error.", exception);
                continue;
            }
            //}
        }
    }

    private PriceOracleType getPriceOracleType(String pairId) {
        //todo is this ok?
        if (pairId.equals(StcUsdOracleType.STC_USD_ORALCE_TYPE_STRUCT_NAME)) {
            return StcUsdOracleType.INSTANCE;
        }
        return chainlinkTaskScheduler.getPriceOracleType(pairId);
    }

}
