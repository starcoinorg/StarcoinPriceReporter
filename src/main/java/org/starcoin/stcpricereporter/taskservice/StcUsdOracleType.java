package org.starcoin.stcpricereporter.taskservice;

import java.math.BigDecimal;
import java.math.BigInteger;

public class StcUsdOracleType extends PriceOracleType {

    public static final int PRICE_PRECISION = 6;

    public static final String STC_USD_ORALCE_TYPE_MODULE_ADDRESS = "0x00000000000000000000000000000001";
    public static final String STC_USD_ORALCE_TYPE_MODULE_NAME = "STCUSDOracle";
    public static final String STC_USD_ORALCE_TYPE_STRUCT_NAME = "STCUSD";

    public static StcUsdOracleType INSTANCE = new StcUsdOracleType();

    private StcUsdOracleType() {
        super(STC_USD_ORALCE_TYPE_MODULE_ADDRESS, STC_USD_ORALCE_TYPE_MODULE_NAME, STC_USD_ORALCE_TYPE_STRUCT_NAME);
    }

    public static BigInteger toOracleIntegerPrice(BigDecimal decimalPrice) {
        return decimalPrice.multiply(BigDecimal.TEN.pow(PRICE_PRECISION)).toBigInteger();
    }
}
