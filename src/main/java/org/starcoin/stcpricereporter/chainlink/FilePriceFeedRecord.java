package org.starcoin.stcpricereporter.chainlink;


import java.math.BigDecimal;
import java.util.Arrays;

public class FilePriceFeedRecord implements PriceFeedRecord {
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
    private String pair;
    private BigDecimal deviationPercentage;
    private BigDecimal heartbeatHours;
    private Integer decimals;
    private String proxy;
    private Boolean enabled;

    public FilePriceFeedRecord(String pair, BigDecimal deviationPercentage, BigDecimal heartbeatHours, Integer decimals, String proxy, Boolean enabled) {
        this.pair = pair;
        this.deviationPercentage = deviationPercentage;
        this.heartbeatHours = heartbeatHours;
        this.decimals = decimals;
        this.proxy = proxy;
        this.enabled = enabled;
    }

    @Override
    public String getMoveTokenPairName() {
        String[] ts = getPair().split("\\/|\\s");
        ts = Arrays.stream(ts).filter(s -> !s.isEmpty()).toArray(i -> new String[i]);
//        if (ts.length < 2) {
//            System.out.println(ts.length);
//            throw new RuntimeException(getPair());
//        }
        //return ts[0] + "_" + ts[1];
        return Arrays.stream(ts).reduce((a, b) -> {
            char firstChar = a.substring(0, 1).charAt(0);
            if (firstChar >= '0' && firstChar <= '9') {
                return numberNames[firstChar - '0'].toUpperCase() + "_"
                        + a.substring(1).toUpperCase() + "_" + b.toUpperCase();
            } else {
                return a.toUpperCase() + "_" + b.toUpperCase();
            }
        }).orElseThrow(() -> new RuntimeException("Get move token pair name error, form value: " + getPair()));
    }

    @Override
    public String getPair() {
        return pair;
    }

    public void setPair(String pair) {
        this.pair = pair;
    }

    @Override
    public BigDecimal getDeviationPercentage() {
        return deviationPercentage;
    }

    public void setDeviationPercentage(BigDecimal deviationPercentage) {
        this.deviationPercentage = deviationPercentage;
    }

    @Override
    public BigDecimal getHeartbeatHours() {
        return heartbeatHours;
    }

    public void setHeartbeatHours(BigDecimal heartbeatHours) {
        this.heartbeatHours = heartbeatHours;
    }

    @Override
    public Integer getDecimals() {
        return decimals;
    }

    public void setDecimals(Integer decimals) {
        this.decimals = decimals;
    }

    @Override
    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    @Override
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
