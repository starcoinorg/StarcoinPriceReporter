package org.starcoin.stcpricereporter.data.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

public class PriceRoundId implements Serializable {

    private String pairId;
    private BigInteger roundId;

    public PriceRoundId() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PriceRoundId that = (PriceRoundId) o;
        return Objects.equals(pairId, that.pairId) && Objects.equals(roundId, that.roundId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pairId, roundId);
    }

    @Override
    public String toString() {
        return "PriceRoundId{" +
                "pairId='" + pairId + '\'' +
                ", roundId=" + roundId +
                '}';
    }

    public String getPairId() {
        return pairId;
    }

    public void setPairId(String pairId) {
        this.pairId = pairId;
    }

    public BigInteger getRoundId() {
        return roundId;
    }

    public void setRoundId(BigInteger roundId) {
        this.roundId = roundId;
    }
}
