package org.starcoin.stcpricereporter.taskservice;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.starcoin.stcpricereporter.model.AggregatorV3Interface;
import org.starcoin.stcpricereporter.utils.DateTimeUtils;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;

//@Component
public class PriceUpdateTask implements Runnable {
    private static final Credentials NO_CREDENTIALS = Credentials.create("0x99");

    private String ethereumHttpServiceUrl;
    private String contractAddress;//= "0x5f4eC3Df9cbd43714FE2740f5E3616155c5b8419";
    private String tokenPairName;

    public PriceUpdateTask(String ethereumHttpServiceUrl, String tokenPairName, String contractAddress) {
        this.ethereumHttpServiceUrl = ethereumHttpServiceUrl;
        this.tokenPairName = tokenPairName;
        this.contractAddress = contractAddress;
    }

    //@Scheduled(fixedDelay = 5000)
    @Override
    public void run() {
        Web3j web3 = Web3j.build(new HttpService(ethereumHttpServiceUrl));
        DefaultGasProvider defaultGasProvider = new DefaultGasProvider();
        AggregatorV3Interface aggregatorV3Interface = AggregatorV3Interface.load(contractAddress, web3, NO_CREDENTIALS, defaultGasProvider);
        BigInteger decimals;
        try {
            decimals = aggregatorV3Interface.decimals().send();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        aggregatorV3Interface.latestRoundData().flowable().subscribe(s ->
        {
            System.out.println(s);
            BigInteger roundID = s.component1();
            BigInteger price = s.component2();
            BigInteger startedAt = s.component3();
            BigInteger timeStamp = s.component4();
            BigInteger answeredInRound = s.component5();
            BigInteger[] prices = price.divideAndRemainder(BigInteger.TEN.pow(decimals.intValue()));
            System.out.println(tokenPairName + ", price:" + prices[0] + "." + prices[1]);
            System.out.println(DateTimeUtils.toDefaultZonedDateTime(timeStamp.longValue() * 1000));
        });
    }

}