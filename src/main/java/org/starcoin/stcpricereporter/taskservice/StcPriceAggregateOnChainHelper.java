package org.starcoin.stcpricereporter.taskservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.starcoin.stcpricereporter.service.OnChainManager;

import java.math.BigDecimal;

public class StcPriceAggregateOnChainHelper {
    private static final Logger LOG = LoggerFactory.getLogger(StcPriceAggregateOnChainHelper.class);


    public static boolean tryUpdateStcPriceOnChain(String datasourceKey, BigDecimal price, Long dateTimeInSeconds,
                                                   StcPriceAggregator stcPriceAggregator,
                                                   OnChainManager onChainManager) {
        boolean needReport = stcPriceAggregator.updatePrice(datasourceKey, price, dateTimeInSeconds);
        if (needReport) {
            LOG.debug("STC / USD, report on-chain...");
            try {
                onChainManager.initDataSourceOrUpdateOnChain(StcUsdOracleType.INSTANCE, StcUsdOracleType.toOracleIntegerPrice(price));
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
