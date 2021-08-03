package org.starcoin.stcpricereporter.taskservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class StcPriceAggregateOnChainHelper {
    private static Logger LOG = LoggerFactory.getLogger(StcPriceAggregateOnChainHelper.class);


    public static boolean tryUpdateStcPriceOnChain(String datasourceKey, BigDecimal price, Long dateTimeInSeconds,
                                                   StcPriceAggregator stcPriceAggregator,
                                                   OnChainManager onChainManager) {
        boolean needReport = stcPriceAggregator.updatePrice(datasourceKey, price, dateTimeInSeconds);
        if (needReport) {
            LOG.debug("STC / USD, report on-chain...");
            onChainManager.initDataSourceOrUpdateOnChain(stcPriceAggregator.getStcPriceCache(), StcUsdOracleType.INSTANCE, StcUsdOracleType.toOracleIntegerPrice(price));
            stcPriceAggregator.markOnChainUpdated();
            return true;
        }
        return false;
    }

}
