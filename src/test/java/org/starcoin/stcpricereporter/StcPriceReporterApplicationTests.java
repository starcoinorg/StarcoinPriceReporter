package org.starcoin.stcpricereporter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.starcoin.stcpricereporter.taskservice.OnChainManager;
import org.starcoin.stcpricereporter.taskservice.PriceOracleType;
import org.starcoin.stcpricereporter.taskservice.StcUsdOracleType;

import java.math.BigInteger;

@SpringBootTest
class StcPriceReporterApplicationTests {

	@Autowired
	OnChainManager onChainManager;

	@Test
	void contextLoads() {
		BigInteger stcPrice = onChainManager.priceOracleRead(StcUsdOracleType.INSTANCE);
		System.out.println(stcPrice);

		BigInteger ethPrice = onChainManager.priceOracleRead(new PriceOracleType(
				"0x07fa08a855753f0ff7292fdcbe871216", "ETH_USD", "ETH_USD"));
		System.out.println(ethPrice);
	}

}
