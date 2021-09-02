package org.starcoin.stcpricereporter.data.model;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.math.BigDecimal;
import java.math.BigInteger;

@Entity
public class PriceFeed {
    public static final String ON_CHAIN_STATUS_CREATED = "CREATED";
    public static final String ON_CHAIN_STATUS_UPDATING = "UPDATING";
    public static final String ON_CHAIN_STATUS_SUBMITTED = "SUBMITTED";
    public static final String ON_CHAIN_STATUS_CONFIRMED = "CONFIRMED";

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

    @Column(length = 20)
    private String onChainStatus = ON_CHAIN_STATUS_CREATED;

    @Column(length = 66)
    private String onChainTransactionHash;

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

    public String getOnChainStatus() {
        return onChainStatus;
    }

    protected void setOnChainStatus(String onChainStatus) {
        this.onChainStatus = onChainStatus;
    }

    public String getOnChainTransactionHash() {
        return onChainTransactionHash;
    }

    public void setOnChainTransactionHash(String onChainTransactionHash) {
        this.onChainTransactionHash = onChainTransactionHash;
    }

    public void onChainStatusUpdating() {
        this.onChainStatus = ON_CHAIN_STATUS_UPDATING;
        this.onChainTransactionHash = null;
    }

    public void onChainStatusSubmitted(String onChainTransactionHash) {
        this.onChainTransactionHash = onChainTransactionHash;
        this.onChainStatus = ON_CHAIN_STATUS_SUBMITTED;
    }

    public void onChainStatusConfirmed() {
        this.onChainStatus = ON_CHAIN_STATUS_CONFIRMED;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
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
