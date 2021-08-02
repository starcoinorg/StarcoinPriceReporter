package org.starcoin.stcpricereporter.taskservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import static org.starcoin.stcpricereporter.utils.DateTimeUtils.toDefaultZonedDateTime;

@Component
public class BixinTaskService {
    private Logger LOG = LoggerFactory.getLogger(BixinTaskService.class);

    public static final String STC_USDT_TOKEN_PAIR = "STC_USDT";

    private static final String GET_TICKS_URL_FORMAT = "https://uniapi.876ex.com/v1/market/ticks/%1$s?limit=1";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    OnChainManager onChainManager;

    @Scheduled(cron = "${starcoin.stc-price-reporter.bixin-task-cron}")
    public void task() {

        String url = String.format(GET_TICKS_URL_FORMAT, STC_USDT_TOKEN_PAIR);

        GetTicksResponse getTicksResponse = restTemplate.getForObject(url, GetTicksResponse.class);
        System.out.println("------------ GET Ticks from Bixin -------------");
        System.out.println(getTicksResponse);
        //assert getTicksResponse.getResults().length == 1;
        if (!(getTicksResponse.getResults().length == 1)) {
            LOG.error("!(getTicksResponse.getResults().length == 1)");
            return;
        }
        //assert getTicksResponse.getResults()[0].data.length == 1;
        if (!(getTicksResponse.getResults()[0].data.length == 1)) {
            LOG.error("!(getTicksResponse.getResults()[0].data.length == 1)");
            return;
        }
        //assert getTicksResponse.getResults()[0].data[0].size() > GetTicksResponse.TicksResult.TICK_PRICE_INDEX;
        if (!(getTicksResponse.getResults()[0].data[0].size() > GetTicksResponse.TicksResult.TICK_PRICE_INDEX)) {
            LOG.error("!(getTicksResponse.getResults()[0].data[0].size() > GetTicksResponse.TicksResult.TICK_PRICE_INDEX)");
            return;
        }
        GetTicksResponse.DecimalList decimals = getTicksResponse.getResults()[0].data[0];
        Long dateInMillis = decimals.get(GetTicksResponse.TicksResult.TICK_DATE_IN_MILLISECONDS_INDEX).longValue();
        BigDecimal price = decimals.get(GetTicksResponse.TicksResult.TICK_PRICE_INDEX);
        //System.out.println(dateInMillis);
        //ZonedDateTime zdt = toDefaultZonedDateTime(dateInMillis);
        //System.out.println(zdt);
        //System.out.println(price);

        onChainManager.reportOnChain(StcUsdtOracleType.INSTANCE, StcUsdtOracleType.toOracleIntegerPrice(price));

    }


    static class GetTicksResponse {
        //
        //{"results":[{"sequenceId":1083572602,"data":[[1626925510037,1,0.108800,3.908096,35.920000,0]]}]}
        //[1626925510037,1,0.108800,3.908096,35.920000,0]
        //交易时间，买入/卖出，成交价，成交额，成交量，成交类型
        //

        private TicksResult[] results;

        public TicksResult[] getResults() {
            return results;
        }

        public void setResults(TicksResult[] results) {
            this.results = results;
        }

        @Override
        public String toString() {
            return "GetTicksResponse{" +
                    "results=" + Arrays.toString(results) +
                    '}';
        }

        static class TicksResult {

            static final int TICK_DATE_IN_MILLISECONDS_INDEX = 0;

            static final int TICK_PRICE_INDEX = 2;

            private Long sequenceId;

            private DecimalList[] data;

            public Long getSequenceId() {
                return sequenceId;
            }

            public void setSequenceId(Long sequenceId) {
                this.sequenceId = sequenceId;
            }

            public DecimalList[] getData() {
                return data;
            }

            public void setData(DecimalList[] data) {
                this.data = data;
            }

            @Override
            public String toString() {
                return "TickResult{" +
                        "sequenceId=" + sequenceId +
                        ", data=" + Arrays.toString(data) +
                        '}';
            }
        }

        static class DecimalList extends ArrayList<BigDecimal> {
            //
        }

    }
}
