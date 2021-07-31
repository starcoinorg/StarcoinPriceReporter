package org.starcoin.stcpricereporter.chainlink;


import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Locale;

public class PriceFeedRecord {
    private String pair;
    private BigDecimal deviationPercentage;
    private BigDecimal heartbeatHours;
    private Integer decimals;
    private String proxy;
    private Boolean enabled;

    public PriceFeedRecord(String pair, BigDecimal deviationPercentage, BigDecimal heartbeatHours, Integer decimals, String proxy, Boolean enabled) {
        this.pair = pair;
        this.deviationPercentage = deviationPercentage;
        this.heartbeatHours = heartbeatHours;
        this.decimals = decimals;
        this.proxy = proxy;
        this.enabled = enabled;
    }

    public String getMoveTokenPairName() {
        String[] ts = getPair().split("\\/|\\s");
        ts = Arrays.stream(ts).filter(s -> !s.isEmpty()).toArray(i -> new String[i]);
//        if (ts.length < 2) {
//            System.out.println(ts.length);
//            throw new RuntimeException(getPair());
//        }
        //return ts[0] + "_" + ts[1];
        String p = Arrays.stream(ts).reduce((a, b) -> {
            char firstChar = a.substring(0, 1).charAt(0);
            if (firstChar >= '0' && firstChar <= '9') {
                return numberNames[firstChar - '0'].toUpperCase() + "_"
                        + a.substring(1).toUpperCase() + "_" + b.toUpperCase();
            } else {
                return a.toUpperCase() + "_" + b.toUpperCase();
            }
        }).get();
        return p;
    }

    private static final String[] numberNames = {
            "zero",
            " one",
            " two",
            " three",
            " four",
            " five",
            " six",
            " seven",
            " eight",
            " nine"//,
//            " ten",
//            " eleven",
//            " twelve",
//            " thirteen",
//            " fourteen",
//            " fifteen",
//            " sixteen",
//            " seventeen",
//            " eighteen",
//            " nineteen"
    };


    public String getPair() {
        return pair;
    }

    public void setPair(String pair) {
        this.pair = pair;
    }

    public BigDecimal getDeviationPercentage() {
        return deviationPercentage;
    }

    public void setDeviationPercentage(BigDecimal deviationPercentage) {
        this.deviationPercentage = deviationPercentage;
    }

    public BigDecimal getHeartbeatHours() {
        return heartbeatHours;
    }

    public void setHeartbeatHours(BigDecimal heartbeatHours) {
        this.heartbeatHours = heartbeatHours;
    }

    public Integer getDecimals() {
        return decimals;
    }

    public void setDecimals(Integer decimals) {
        this.decimals = decimals;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "PriceFeedRecord{" +
                "pair='" + pair + '\'' +
                ", deviationPercentage=" + deviationPercentage +
                ", heartbeatHours=" + heartbeatHours +
                ", decimals=" + decimals +
                ", proxy='" + proxy + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
