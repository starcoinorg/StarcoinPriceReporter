package org.starcoin.stcpricereporter.taskservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Component
public class CoinExTaskService {
    private Logger LOG = LoggerFactory.getLogger(CoinExTaskService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Scheduled(fixedDelay = 5000)
    public void task() {
//        System.out.println("Thread Name : "
//                + Thread.currentThread().getName() + "  i am a task : date ->  "
//                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        String url = "https://api.coinex.com/v1/market/deals?market=STCUSDT&limit=1";
        LatestTransactionsDataResponse latestTransactionsDataResponse = restTemplate.getForObject(url, LatestTransactionsDataResponse.class);
        System.out.println(latestTransactionsDataResponse);
        //todo

    }

    public static class LatestTransactionsDataResponse {
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
