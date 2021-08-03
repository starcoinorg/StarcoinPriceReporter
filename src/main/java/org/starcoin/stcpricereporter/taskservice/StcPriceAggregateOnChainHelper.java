package org.starcoin.stcpricereporter.taskservice;

import java.math.BigDecimal;

public class StcPriceAggregateOnChainHelper {


    public static boolean tryUpdateStcPriceOnChain(String datasourceKey, BigDecimal price, Long dateTimeInSeconds,
                                                   StcPriceAggregator stcPriceAggregator,
                                                   OnChainManager onChainManager) {
        boolean needReport = stcPriceAggregator.updatePrice(datasourceKey, price, dateTimeInSeconds);
        if (needReport) {
            System.out.println("STC / USDT, report on-chain...");
            onChainManager.initDataSourceOrUpdateOnChain(stcPriceAggregator.getStcPriceCache(), StcUsdOracleType.INSTANCE, StcUsdOracleType.toOracleIntegerPrice(price));
            stcPriceAggregator.markOnChainUpdated();
            return true;
        }
        return false;
    }

}
