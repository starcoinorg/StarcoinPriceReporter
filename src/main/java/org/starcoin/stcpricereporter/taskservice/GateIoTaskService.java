package org.starcoin.stcpricereporter.taskservice;

import io.gate.gateapi.ApiClient;
import io.gate.gateapi.ApiException;
import io.gate.gateapi.Configuration;
import io.gate.gateapi.api.SpotApi;
import io.gate.gateapi.models.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class GateIoTaskService {
    private Logger LOG = LoggerFactory.getLogger(GateIoTaskService.class);

    public static final String STC_USDT_TOKEN_PAIR = "STC_USDT";

    @Autowired
    private OnChainManager onChainManager;

    @Scheduled(cron = "${starcoin.stc-price-reporter.gateio-task-cron}")
    public void task() {
        ApiClient apiClient = Configuration.getDefaultApiClient();
        //defaultClient.setBasePath("https://api.gateio.ws/api/v4");

        SpotApi spotApi = new SpotApi(apiClient);
//        CurrencyPair pair = spotApi.getCurrencyPair(currencyPair);
//        System.out.println("testing against currency pair: " + currencyPair);
//        String minAmount = pair.getMinQuoteAmount();

        long dateTimeInMillis = System.currentTimeMillis();
        // get last price
        List<Ticker> tickers;
        try {
            tickers = spotApi.listTickers().currencyPair(STC_USDT_TOKEN_PAIR).execute();
        } catch (ApiException e) {
           LOG.error("Gate.io ApiException", e);
           return;
        }
        //assert tickers.size() == 1;
        if (!(tickers.size() == 1)) {
            LOG.error("!(tickers.size() == 1)");
            return;
        }
        String lastPriceString = tickers.get(0).getLast();
        //assert lastPrice != null;
        if (lastPriceString == null) {
            LOG.error("lastPrice == null");
            return;
        }
        System.out.println("------------ Get spot tickers from Gate.io -------------");
        //System.out.println(tickers);
        System.out.println(lastPriceString);
        BigDecimal price = new BigDecimal(lastPriceString);
        //System.out.println(decimalPrice);
        //System.out.println(DateTimeUtils.toDefaultZonedDateTime(dateTimeInMillis));

        onChainManager.reportOnChain(StcUsdtOracleType.INSTANCE, StcUsdtOracleType.toOracleIntegerPrice(price));

    }

}
