package org.starcoin.stcpricereporter.chainlink;

import java.math.BigDecimal;

public interface PriceFeedRecord {
    String getMoveTokenPairName();

    String getPair();

    BigDecimal getDeviationPercentage();

    BigDecimal getHeartbeatHours();

    Integer getDecimals();

    String getProxy();

    Boolean getEnabled();
}
