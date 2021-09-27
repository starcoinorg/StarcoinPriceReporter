package org.starcoin.stcpricereporter.data.model;

import javax.persistence.*;

@Entity
public class PricePair {

    @Id
    @Column(length = 50)
    private String pairId;

    @Column(length = 200)
    private String pairName;

    @Embedded
    @AttributeOverride(name = "address", column = @Column(name = "on_chain_struct_address", length = 34, nullable = false))
    @AttributeOverride(name = "module", column = @Column(name = "on_chain_struct_module", nullable = false))
    @AttributeOverride(name = "name", column = @Column(name = "on_chain_struct_name", nullable = false))
    private MoveStructType onChainStructType;

    @Column
    private Integer decimals;

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

    public MoveStructType getOnChainStructType() {
        return onChainStructType;
    }

    public void setOnChainStructType(MoveStructType onChainStructType) {
        this.onChainStructType = onChainStructType;
    }

    public Integer getDecimals() {
        return decimals;
    }

    public void setDecimals(Integer decimals) {
        this.decimals = decimals;
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
