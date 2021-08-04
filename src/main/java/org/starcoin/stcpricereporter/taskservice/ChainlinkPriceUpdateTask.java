package org.starcoin.stcpricereporter.taskservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.starcoin.stcpricereporter.model.AggregatorV3Interface;
import org.starcoin.stcpricereporter.utils.DateTimeUtils;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;


public class ChainlinkPriceUpdateTask implements Runnable {
    private Logger LOG = LoggerFactory.getLogger(ChainlinkPriceUpdateTask.class);

    private static final Credentials NO_CREDENTIALS = Credentials.create("0x99");

    private final String ethereumHttpServiceUrl;
    private final String contractAddress;//= "0x5f4eC3Df9cbd43714FE2740f5E3616155c5b8419";
    private final String tokenPairName;
    private final int decimals;
    private final OnChainManager onChainManager;
    private final PriceOracleType priceOracleType;

    private final ChainlinkPriceCache chainlinkPriceCache = new ChainlinkPriceCache();

    public ChainlinkPriceUpdateTask(String ethereumHttpServiceUrl,
                                    String tokenPairName,
                                    String contractAddress,
                                    int decimals,
                                    OnChainManager onChainManager, PriceOracleType priceOracleType) {
        this.ethereumHttpServiceUrl = ethereumHttpServiceUrl;
        this.tokenPairName = tokenPairName;
        this.decimals = decimals;
        this.contractAddress = contractAddress;
        this.onChainManager = onChainManager;
        this.priceOracleType = priceOracleType;
    }

    @Override
    public void run() {
        Web3j web3 = Web3j.build(new HttpService(ethereumHttpServiceUrl));
        DefaultGasProvider defaultGasProvider = new DefaultGasProvider();
        AggregatorV3Interface aggregatorV3Interface = AggregatorV3Interface.load(contractAddress, web3, NO_CREDENTIALS, defaultGasProvider);
        if (chainlinkPriceCache.isPriceEmpty()) {
            try {
                BigInteger decimals = aggregatorV3Interface.decimals().send();
                if (decimals.compareTo(BigInteger.valueOf(this.decimals)) != 0) {
                    throw new RuntimeException(this.tokenPairName + " decimals error.");
                } else {
                    //System.out.println("Decimals asserted.");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        aggregatorV3Interface.latestRoundData().flowable().subscribe(s ->
        {
            LOG.debug("Chainlink latestRoundData: " + s);
            BigInteger roundID = s.component1();
            BigInteger price = s.component2();
            BigInteger startedAt = s.component3();
            BigInteger timeStamp = s.component4();
            BigInteger answeredInRound = s.component5();
            BigInteger[] priceParts = price.divideAndRemainder(BigInteger.TEN.pow(decimals));
            LOG.debug(tokenPairName + ", price: " + priceParts[0] + "." + priceParts[1] + ", timestamp: "
                    + DateTimeUtils.toDefaultZonedDateTime(timeStamp.longValue() * 1000));

            boolean needReport = chainlinkPriceCache.tryUpdate(price, timeStamp.divide(BigInteger.valueOf(1000)).longValue());
            if (needReport) {
                LOG.debug(tokenPairName + ", report on-chain...");
                this.onChainManager.initDataSourceOrUpdateOnChain(chainlinkPriceCache, priceOracleType, price);
                markOnChainUpdated();
            }
        });

    }

    private void markOnChainUpdated() {
        this.chainlinkPriceCache.setDirty(false);
    }

    @Override
    public String toString() {
        return "ChainlinkPriceUpdateTask{" +
                "ethereumHttpServiceUrl='" + ethereumHttpServiceUrl + '\'' +
                ", contractAddress='" + contractAddress + '\'' +
                ", tokenPairName='" + tokenPairName + '\'' +
                ", decimals=" + decimals +
                //", onChainManager=" + onChainManager +
                ", priceOracleType=" + priceOracleType +
                //", chainlinkPriceCache=" + chainlinkPriceCache +
                '}';
    }

    public static class ChainlinkPriceCache implements OffChainPriceCache<BigInteger> {

        private BigInteger price;
        private Long lastUpdatedAt;
        private boolean dirty = false;
        private boolean firstUpdate = false;

        @Override
        public synchronized boolean tryUpdate(BigInteger price, Long timestamp) {
            if (this.price == null
                    || this.dirty
                    || !this.price.equals(price)
                    || !this.lastUpdatedAt.equals(timestamp)
            ) {
                if (this.price == null) {
                    this.firstUpdate = true;
                }
                this.price = price;
                this.lastUpdatedAt = timestamp;
                this.dirty = true;
            }
            return this.dirty;
        }

        @Override
        public synchronized boolean isDirty() {
            return dirty;
        }

        @Override
        public synchronized void setDirty(boolean dirty) {
            this.dirty = dirty;
            if (!dirty) {
                this.firstUpdate = false;
            }
        }

        @Override
        public synchronized boolean isFirstUpdate() {
            return firstUpdate;
        }

        public synchronized boolean isPriceEmpty() {
            return this.price == null;
        }
    }


}