package org.starcoin.stcpricereporter.taskservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.starcoin.stcpricereporter.utils.DateTimeUtils;

import java.math.BigDecimal;
import java.util.Arrays;

@Component
public class CoinExTaskService {
    private Logger LOG = LoggerFactory.getLogger(CoinExTaskService.class);

    public static final String STC_USDT_TOKEN_PAIR = "STCUSDT";

    private static final String GET_DEALS_URL_FORMAT = "https://api.coinex.com/v1/market/deals?market=%1$s&limit=1";

    @Autowired
    private RestTemplate restTemplate;

    @Scheduled(cron = "${starcoin.stc-price-reporter.coinex-task-cron}")
    public void task() {
//        System.out.println("Thread Name : "
//                + Thread.currentThread().getName() + "  i am a task : date ->  "
//                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        String url = String.format(GET_DEALS_URL_FORMAT, STC_USDT_TOKEN_PAIR);
        LatestTransactionsDataResponse latestTransactionsDataResponse = restTemplate.getForObject(url, LatestTransactionsDataResponse.class);

        if (!LatestTransactionsDataResponse.MESSAGE_OK.equals(latestTransactionsDataResponse.message)) {
            LOG.error("Message is not OK.", latestTransactionsDataResponse);
            return;
        }
        if (latestTransactionsDataResponse.data.length != 1) {
            LOG.error("latestTransactionsDataResponse.data.length != 1");
            return;
        }
        System.out.println("-------- get latest transactions from CoinEx ---------");
        System.out.println(latestTransactionsDataResponse);
        String price = latestTransactionsDataResponse.data[0].price;
        BigDecimal decimalPrice = new BigDecimal(price);
        System.out.println(decimalPrice);
        long dateInMilliseconds = latestTransactionsDataResponse.data[0].dateInMilliseconds;
        System.out.println(DateTimeUtils.toDefaultZonedDateTime(dateInMilliseconds));
        //todo

    }

    public static class LatestTransactionsDataResponse {

        public static final String MESSAGE_OK = "OK";

        private String code;

        private LatestTransactionData[] data;

        private String message;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public LatestTransactionData[] getData() {
            return data;
        }

        public void setData(LatestTransactionData[] data) {
            this.data = data;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return "LatestTransactionsDataResponse{" +
                    "code='" + code + '\'' +
                    ", data=" + Arrays.toString(data) +
                    ", message='" + message + '\'' +
                    '}';
        }
    }

    /**
     * Return value description:
     *
     * name	type	description
     * id	Integer	Transaction No
     * date	Integer	Transaction time
     * date_ms	Integer	Transaction time(ms)
     * amount	String	Transaction amount
     * price	String	Transaction price
     * type	String	buy; sell;
     */
    public static class  LatestTransactionData {

        private Long id;

        @JsonProperty("date")
        private Long dateInSeconds;

        @JsonProperty("date_ms")
        private Long dateInMilliseconds;

        private String amount;

        private String price;

        private String type;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getDateInSeconds() {
            return dateInSeconds;
        }

        public void setDateInSeconds(Long dateInSeconds) {
            this.dateInSeconds = dateInSeconds;
        }

        public Long getDateInMilliseconds() {
            return dateInMilliseconds;
        }

        public void setDateInMilliseconds(Long dateInMilliseconds) {
            this.dateInMilliseconds = dateInMilliseconds;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "LatestTransactionData{" +
                    "id=" + id +
                    ", dateInSeconds=" + dateInSeconds +
                    ", dateInMilliseconds=" + dateInMilliseconds +
                    ", amount='" + amount + '\'' +
                    ", price='" + price + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }


}
