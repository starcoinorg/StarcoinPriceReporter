package org.starcoin.stcpricereporter.taskservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.starcoin.stcpricereporter.data.model.PriceFeed;
import org.starcoin.stcpricereporter.data.model.StarcoinAccount;
import org.starcoin.stcpricereporter.data.repo.PriceFeedRepository;
import org.starcoin.stcpricereporter.service.OnChainManager;
import org.starcoin.stcpricereporter.service.StarcoinAccountService;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;


@Component
public class StarcoinTransactionConfirmTaskService {
    private static final Logger LOG = LoggerFactory.getLogger(StarcoinTransactionConfirmTaskService.class);
    private static final int ACCOUNT_SEQUENCE_NUMBER_RESET_GAP = 2;
    //private static final int NEEDED_BLOCK_CONFIRMATIONS = 1;

    @Value("${starcoin.transaction-confirm-task-service.confirm-Transaction-created-before-seconds}")
    private Long confirmTransactionCreatedBeforeSeconds;// = 5L;

    @Autowired
    private PriceFeedRepository priceFeedRepository;

    @Autowired
    private StarcoinAccountService starcoinAccountService;

    @Autowired
    private OnChainManager onChainManager;

    @Scheduled(fixedDelayString = "${starcoin.transaction-confirm-task-service.fixed-delay}")
    public void task() {
        Long updatedBefore = System.currentTimeMillis() - confirmTransactionCreatedBeforeSeconds * 1000;
        List<PriceFeed> priceFeeds = priceFeedRepository.findByOnChainStatusEqualsAndUpdatedAtLessThan(PriceFeed.ON_CHAIN_STATUS_SUBMITTED, updatedBefore);
        if (priceFeeds == null) {
            return;
        }
        for (PriceFeed t : priceFeeds) {
            Map<String, Object> resultMap = onChainManager.getTransactionInfo(t.getOnChainTransactionHash());
            if (resultMap == null || !resultMap.containsKey("block_hash")) {
                LOG.info("Get transaction info error." + resultMap);
                continue;
            }
//            BigInteger transactionBlockNumber = new BigInteger(resultMap.get("block_number").toString());
//            BigInteger latestBlockNumber;
//            try {
//                latestBlockNumber = getLatestBlockNumber(jsonRpcSession);
//            } catch (RuntimeException | JSONRPC2SessionException | JsonProcessingException e) {
//                LOG.error("Get block error.", e);
//                continue;
//            }
//            if (latestBlockNumber.compareTo(transactionBlockNumber.add(BigInteger.valueOf(NEEDED_BLOCK_CONFIRMATIONS))) < 0) {
//                LOG.debug("Transaction '" + t.getTransactionHash() + "' not confirmed yet.");
//                // ------------------------------------------
//                // Update transaction block info...
//                updateTransactionBlockAndAccountSequenceNumber(t, resultMap);
//                // ------------------------------------------
//                continue;
//            }
            if (!"Executed".equals(resultMap.get("status"))) {
                continue;//todo do something???
            }
            try {
                // Confirmed!
                t.onChainStatusConfirmed();
                updateTransactionAndAccountSequenceNumber(t);
            } catch (RuntimeException exception) {
                LOG.error("Update starcoin transaction error.", exception);
                //continue;
            }
        } // end for
        // -----------------------------------------------
        resetAccountSequenceNumberIfNecessary();
    }

    private void updateTransactionAndAccountSequenceNumber(PriceFeed t) {
//        t.setBlockHash(onChainTransactionInfo.get("block_hash").toString());
//        t.setBlockNumber(new BigInteger(onChainTransactionInfo.get("block_number").toString()));
//        t.setTransactionIndex(new BigInteger(onChainTransactionInfo.get("transaction_index").toString()));
        t.setUpdatedAt(System.currentTimeMillis());
        t.setUpdatedBy("ADMIN");
        priceFeedRepository.save(t);
        priceFeedRepository.flush();

        Map<String, Object> transactionMap = onChainManager.getTransaction(t.getOnChainTransactionHash());
        Map<String, Object> userTransaction = (Map<String, Object>) transactionMap.get("user_transaction");
        Map<String, Object> rawTransaction = (Map<String, Object>) userTransaction.get("raw_txn");
        // Update account sequence number(transaction counter) ...
        starcoinAccountService.confirmSequenceNumberOnChain((String) rawTransaction.get("sender"), new BigInteger(rawTransaction.get("sequence_number").toString()));
    }

    private void resetAccountSequenceNumberIfNecessary() {
        StarcoinAccount account = starcoinAccountService.getStarcoinAccountOrElseNull(onChainManager.getSenderAddress());
        if (account == null) {
            LOG.error("Cannot find sender account.");
            return;
        }
        try {
            int submittedTxnCount = priceFeedRepository.countByOnChainStatusIn(new String[]{PriceFeed.ON_CHAIN_STATUS_SUBMITTED});
            if (account.getConfirmedSequenceNumber().add(BigInteger.valueOf(submittedTxnCount)).add(BigInteger.ONE)
                    .add(BigInteger.valueOf(ACCOUNT_SEQUENCE_NUMBER_RESET_GAP))
                    .compareTo(account.getSequenceNumber()) < 0) {
                LOG.info("Local Account sequence number run TOO FAST! RESET it by on-chain sequence number.");
                onChainManager.resetByOnChainSequenceNumber(onChainManager.getSenderAddress());
            }
        } catch (RuntimeException runtimeException) {
            LOG.error("resetAccountSequenceNumberIfNecessary error.", runtimeException);
        }
    }
}
