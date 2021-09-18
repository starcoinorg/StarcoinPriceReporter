package org.starcoin.stcpricereporter.data.model;

import javax.persistence.*;
import java.math.BigInteger;

@Entity
@IdClass(PriceRoundId.class)
@Table(indexes = {@Index(name = "IdxPairIdUpdatedAt", columnList = "pair_id,updated_at")})
public class PriceRound {

    @Id
    @Column(name = "pair_id", length = 50)
    private String pairId;

    @Id
    @Column(precision = 50, scale = 0)
    private BigInteger roundId;

    @Column(precision = 50, scale = 0, nullable = false)
    private BigInteger price;

    @Column(nullable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    @Column(length = 70, nullable = false)
    private String createdBy;

    @Column(length = 70, nullable = false)
    private String updatedBy;

    @Version
    private Long version;

    @Transient
    private String pairName;

    @Transient
    private Integer decimals;

    public String getPairName() {
        return pairName;
    }

    public void setPairName(String pairName) {
        this.pairName = pairName;
    }

    public Integer getDecimals() {
        return decimals;
    }

    public void setDecimals(Integer decimals) {
        this.decimals = decimals;
    }

    /**
     * From chainlink solidity contract:
     * <p>
     * function getRoundData(
     * uint80 _roundId
     * )
     * external
     * view
     * returns (
     * uint80 roundId,
     * int256 answer,
     * uint256 startedAt,
     * uint256 updatedAt,
     * uint80 answeredInRound
     * );
     * roundId: The round ID.
     * <p>
     * answer: The price.
     * startedAt: Timestamp of when the round started.
     * updatedAt: Timestamp of when the round was updated.
     * answeredInRound: The round ID of the round in which the answer was computed.
     */

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

    public BigInteger getPrice() {
        return price;
    }

    public void setPrice(BigInteger price) {
        this.price = price;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "PriceRound{" +
                "pairId='" + pairId + '\'' +
                ", roundId=" + roundId +
                ", price=" + price +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", createdBy='" + createdBy + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                ", version=" + version +
                ", pairName='" + pairName + '\'' +
                ", decimals=" + decimals +
                '}';
    }
}
