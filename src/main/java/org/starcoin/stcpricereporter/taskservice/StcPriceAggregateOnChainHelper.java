package org.starcoin.stcpricereporter.taskservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.starcoin.stcpricereporter.service.OnChainManager;
import org.starcoin.stcpricereporter.vo.StcUsdOracleType;

import java.math.BigDecimal;
import java.math.BigInteger;

public class StcPriceAggregateOnChainHelper {
    private static final Logger LOG = LoggerFactory.getLogger(StcPriceAggregateOnChainHelper.class);


    public static boolean tryUpdateStcPriceOnChain(String datasourceKey, BigDecimal price, Long dateTimeInSeconds,
                                                   StcPriceAggregator stcPriceAggregator,
                                                   OnChainManager onChainManager) {
        boolean needReport = stcPriceAggregator.updatePrice(datasourceKey, price, dateTimeInSeconds);
        if (needReport) {
            LOG.debug("STC / USD, report on-chain...");
            try {
                Long updatedAt = System.currentTimeMillis();
                BigInteger roundId = BigInteger.valueOf(updatedAt);
                onChainManager.initDataSourceOrUpdateOnChain(StcUsdOracleType.INSTANCE,
                        StcUsdOracleType.toOracleIntegerPrice(price), roundId, updatedAt, null, null); //todo null??
            } catch (RuntimeException runtimeException) {
                LOG.error("Update " + "STCUSD" + " on-chain price error.", runtimeException);
                return false;
            }
            stcPriceAggregator.markOnChainUpdated();
            return true;
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Not need to update STC / USD price. Latest updated price: {}", stcPriceAggregator.getCachePrice());
            }
        }
        return false;
    }

}
