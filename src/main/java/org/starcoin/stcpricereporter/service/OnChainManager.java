package org.starcoin.stcpricereporter.service;

import org.starcoin.stcpricereporter.taskservice.PriceOracleType;

import java.math.BigInteger;
import java.util.Map;

public interface OnChainManager {
    void initDataSourceOrUpdateOnChain(PriceOracleType priceOracleType, BigInteger price, BigInteger roundId,
                                       Long updatedAt, Long startedAt, BigInteger answeredInRound);

    BigInteger priceOracleRead(PriceOracleType priceOracleType);

    Object resolveModule(String moduleTag);

    Map<String, Object> getTransactionInfo(String transactionHash);

    Map<String, Object> getTransaction(String transactionHash);

    String getSenderAddress();

    void resetByOnChainSequenceNumber(String address);

    void createSenderAccountIfNoExists();
}
