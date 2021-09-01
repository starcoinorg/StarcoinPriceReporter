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

import static org.starcoin.stcpricereporter.taskservice.StcPriceAggregateOnChainHelper.tryUpdateStcPriceOnChain;

@Component
public class GateIoTaskService {
    private Logger LOG = LoggerFactory.getLogger(GateIoTaskService.class);

    public static final String DATASOURCE_KEY = "Gate.io";

    public static final String STC_USDT_TOKEN_PAIR = "STC_USDT";

    @Autowired
    StcPriceAggregator stcPriceAggregator;

    @Autowired
    private OnChainManager onChainManager;

    //@Scheduled(cron = "${starcoin.stc-price-reporter.gateio-task-cron}")
    @Scheduled(fixedDelayString = "${starcoin.stc-price-reporter.gateio-task-fixed-delay}")
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
        LOG.debug("------------ Get spot tickers from Gate.io -------------");
        //System.out.println(tickers);
        LOG.debug(lastPriceString);
        BigDecimal price = new BigDecimal(lastPriceString);
        //System.out.println(decimalPrice);
        //System.out.println(DateTimeUtils.toDefaultZonedDateTime(dateTimeInMillis));

        tryUpdateStcPriceOnChain(DATASOURCE_KEY, price, dateTimeInMillis / 1000,
                this.stcPriceAggregator, this.onChainManager);

    }

}
