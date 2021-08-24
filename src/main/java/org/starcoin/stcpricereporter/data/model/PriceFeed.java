package org.starcoin.stcpricereporter.data.model;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

@Entity
public class PriceFeed {
    @Id
    @Column(length = 50)
    private String pairId;

    @Column(length = 200)
    private String pairName;

    @Column(precision = 10, scale = 7)
    private BigDecimal deviationPercentage;

    @Column(precision = 10, scale = 5)
    private BigDecimal heartbeatHours;

    @Column
    private Integer decimals;

    @Column(precision = 50, scale = 0)
    private BigInteger latestPrice;


    @Column(length = 70, nullable = false)
    private String createdBy;

    @Column(length = 70, nullable = false)
    private String updatedBy;

    @Column(nullable = false)
    private Long createdAt;

    @Column(nullable = false)
    private Long updatedAt;

    @Version
    private Long version;
    
    public String getPairId() {
        return pairId;
    }

    public void setPairId(String pairId) {
        this.pairId = pairId;
    }

    public String getPairName() {
        return pairName;
    }

    public void setPairName(String pairName) {
        this.pairName = pairName;
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

    public BigInteger getLatestPrice() {
        return latestPrice;
    }

    public void setLatestPrice(BigInteger latestPrice) {
        this.latestPrice = latestPrice;
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
}
