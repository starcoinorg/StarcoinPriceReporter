package org.starcoin.stcpricereporter.data.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.math.BigDecimal;

@Entity
public class PriceGrowth {

    @Id
    @Column(length = 50)
    private String pairId;

    @Column(precision = 15, scale = 7)
    private BigDecimal dayOverDayPercentage;

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

    public BigDecimal getDayOverDayPercentage() {
        return dayOverDayPercentage;
    }

    public void setDayOverDayPercentage(BigDecimal dayOverDayPercentage) {
        this.dayOverDayPercentage = dayOverDayPercentage;
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

}
