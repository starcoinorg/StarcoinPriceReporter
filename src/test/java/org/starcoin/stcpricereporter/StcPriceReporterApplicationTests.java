package org.starcoin.stcpricereporter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.starcoin.stcpricereporter.service.OnChainManager;
import org.starcoin.stcpricereporter.taskservice.PriceOracleType;

import java.math.BigInteger;

@SpringBootTest
class StcPriceReporterApplicationTests {

    @Autowired
    OnChainManager onChainManager;

    @Test
    void contextLoads() throws InterruptedException {

        // ---------------------------------------
        // read STC price...
//        BigInteger stcPrice = onChainManager.priceOracleRead(StcUsdOracleType.INSTANCE);
//        System.out.println(stcPrice);

        String[] tokenPairIds = new String[]{"AAVE_USD", "BTC_USD", "COMP_USD", "ETH_USD", "SUSHI_USD", "YFII_ETH"};
        //String[] tokenPairIds = new String[]{"BTC_USD"};
        //byte[] tokenPricePrecisions = new byte[]{8};
        int i = 0;
        for (String tokenPairId : tokenPairIds) {
            String oracleTypeAddress = "0x07fa08a855753f0ff7292fdcbe871216";
            // -----------------------------------------
            // register oracle...
//            onChainManager.registerOracle(new PriceOracleType(oracleTypeAddress, tokenPairId, tokenPairId), tokenPricePrecisions[i]);
//            Thread.sleep(20000);
            // ------------------------------------------
            // init datasource...
//            onChainManager.initDataSource(new PriceOracleType(oracleTypeAddress, tokenPairId, tokenPairId), new BigInteger("4748375847941"));
//            Thread.sleep(20000);
//            if (true) return;
            // ------------------------------------------
            // read price...
            try {
                BigInteger tokenPrice = onChainManager.priceOracleRead(new PriceOracleType(
                        "0x07fa08a855753f0ff7292fdcbe871216", tokenPairId, tokenPairId));
                System.out.println("Get price of " + tokenPairId + ": " + tokenPrice);
            } catch (RuntimeException e) {
                e.printStackTrace();//continue
                System.out.println("Get price error: " + tokenPairId);
            }
            i++;
        }
        //if (true) return;

        // ---------------------------------------
        // read ETH price...
//        BigInteger ethPrice = onChainManager.priceOracleRead(new PriceOracleType(
//                "0x07fa08a855753f0ff7292fdcbe871216", "ETH_USD", "ETH_USD"));
//        System.out.println(ethPrice);
    }

}
