package org.starcoin.stcpricereporter.taskservice;

import java.math.BigDecimal;
import java.math.BigInteger;

public class StcUsdtOracleType extends PriceOracleType {

    public static final int PRICE_PRECISION = 8;

    public static final String STC_USDT_ORALCE_TYPE_MODULE_ADDRESS = "0x07fa08a855753f0ff7292fdcbe871216";
    public static final String STC_USDT_ORALCE_TYPE_MODULE_NAME = "STCUSDT";
    public static final String STC_USDT_ORALCE_TYPE_STRUCT_NAME = "STCUSDT";

    public static StcUsdtOracleType INSTANCE = new StcUsdtOracleType();
    
    private StcUsdtOracleType() {
        super(STC_USDT_ORALCE_TYPE_MODULE_ADDRESS, STC_USDT_ORALCE_TYPE_MODULE_NAME, STC_USDT_ORALCE_TYPE_STRUCT_NAME);
    }

    public static BigInteger toOracleIntegerPrice(BigDecimal decimalPrice) {
        return decimalPrice.multiply(BigDecimal.TEN.pow(PRICE_PRECISION)).toBigInteger();
    }
}
