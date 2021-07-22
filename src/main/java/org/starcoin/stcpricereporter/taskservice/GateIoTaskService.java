package org.starcoin.stcpricereporter.taskservice;

import io.gate.gateapi.ApiClient;
import io.gate.gateapi.ApiException;
import io.gate.gateapi.Configuration;
import io.gate.gateapi.api.SpotApi;
import io.gate.gateapi.models.*;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class GateIoTaskService {

    @Scheduled(fixedDelay = 1000)
    public void task() {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        //defaultClient.setBasePath("https://api.gateio.ws/api/v4");

        String currencyPair = "STC_USDT";
        SpotApi spotApi = new SpotApi(defaultClient);
//        CurrencyPair pair = spotApi.getCurrencyPair(currencyPair);
//        System.out.println("testing against currency pair: " + currencyPair);
//        String minAmount = pair.getMinQuoteAmount();

        // get last price
        List<Ticker> tickers = null;
        try {
            tickers = spotApi.listTickers().currencyPair(currencyPair).execute();
        } catch (ApiException e) {
            e.printStackTrace();
            //todo
            return;
        }
        assert tickers.size() == 1;
        String lastPrice = tickers.get(0).getLast();
        assert lastPrice != null;
        System.out.println(tickers);
        System.out.println(lastPrice);
    }

}
