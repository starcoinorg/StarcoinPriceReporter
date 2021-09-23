package org.starcoin.stcpricereporter.vo;

import java.math.BigInteger;

public class PriceAverage {

    private String pairId;

    private BigInteger mean;

    private BigInteger median;

    public String getPairId() {
        return pairId;
    }

    public void setPairId(String pairId) {
        this.pairId = pairId;
    }

    public BigInteger getMean() {
        return mean;
    }

    public void setMean(BigInteger mean) {
        this.mean = mean;
    }

    public BigInteger getMedian() {
        return median;
    }

    public void setMedian(BigInteger median) {
        this.median = median;
    }

}
