package org.starcoin.stcpricereporter.taskservice;

import io.gate.gateapi.ApiClient;
import io.gate.gateapi.ApiException;
import io.gate.gateapi.Configuration;
import io.gate.gateapi.api.SpotApi;
import io.gate.gateapi.models.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GateIoTaskService {
    private Logger LOG = LoggerFactory.getLogger(GateIoTaskService.class);

    @Scheduled(fixedDelay = 5000)
    public void task() {
        ApiClient apiClient = Configuration.getDefaultApiClient();
        //defaultClient.setBasePath("https://api.gateio.ws/api/v4");

        String currencyPair = "STC_USDT";
        SpotApi spotApi = new SpotApi(apiClient);
//        CurrencyPair pair = spotApi.getCurrencyPair(currencyPair);
//        System.out.println("testing against currency pair: " + currencyPair);
//        String minAmount = pair.getMinQuoteAmount();

        // get last price
        List<Ticker> tickers;
        try {
            tickers = spotApi.listTickers().currencyPair(currencyPair).execute();
        } catch (ApiException e) {
           LOG.error("Gate.io ApiException", e);
           return;
        }
        assert tickers.size() == 1;
        String lastPrice = tickers.get(0).getLast();
        assert lastPrice != null;
        System.out.println(tickers);
        System.out.println(lastPrice);
        //todo
    }

}
