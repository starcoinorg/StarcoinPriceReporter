package org.starcoin.stcpricereporter.data.model;

import java.math.BigDecimal;
import java.math.BigInteger;


public interface PriceRoundView {

    //  "    r.pair_id, r.round_id, r.price, r.updated_at, f.pair_name, f.decimals, " +
    //  "        null as created_at, null as created_by, null as updated_by, null as version\n" +

    String getPairId();

    BigDecimal getRoundId();

    BigDecimal getPrice();

    BigInteger getUpdatedAt();

    String getPairName();

    Integer getDecimals();

//    Long getCreatedAt();
//
//    String getCreatedBy();
//
//    String getUpdatedBy();
//
//    Long getVersion();

}
