package org.starcoin.stcpricereporter.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.starcoin.stcpricereporter.taskservice.PriceOracleType;

import java.math.BigInteger;
import java.util.Map;

@ConditionalOnProperty(prefix = "starcoin", value = "on-chain-disabled", havingValue = "true")
@Component
public class NoOnChainManagerImpl implements OnChainManager {
    private final Logger LOG = LoggerFactory.getLogger(OnChainManager.class);

    private final String senderAddress = "0x00";

    @Autowired
    private PriceFeedService priceFeedService;

    @Autowired
    private StarcoinAccountService starcoinAccountService;

    @Override
    public void initDataSourceOrUpdateOnChain(PriceOracleType priceOracleType, BigInteger price) {
        String pairId = priceOracleType.getStructName(); // Pair Id. in database!
        try {
            priceFeedService.setOnChainStatusNoOnChain(pairId);
        } catch (RuntimeException runtimeException) {
            LOG.info("Update on-chain status in database caught runtime error. PairId: " + pairId, runtimeException);
        }
    }

    @Override
    public BigInteger priceOracleRead(PriceOracleType priceOracleType) {
        return null;
    }

    @Override
    public Object resolveModule(String moduleTag) {
        return null;
    }

    @Override
    public Map<String, Object> getTransactionInfo(String transactionHash) {
        return null;
    }

    @Override
    public Map<String, Object> getTransaction(String transactionHash) {
        return null;
    }

    @Override
    public String getSenderAddress() {
        return senderAddress;
    }

    @Override
    public void resetByOnChainSequenceNumber(String address) {
        starcoinAccountService.resetSequenceNumber(address, BigInteger.ZERO);
    }

    @Override
    public void createSenderAccountIfNoExists() {
        String address = getSenderAddress();
        if (starcoinAccountService.getStarcoinAccountOrElseNull(address) == null) {
            this.resetByOnChainSequenceNumber(address);
        }
    }

}
