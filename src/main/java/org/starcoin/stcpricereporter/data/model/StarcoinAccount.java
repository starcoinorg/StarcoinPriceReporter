package org.starcoin.stcpricereporter.data.model;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.math.BigInteger;

@Entity
@DynamicInsert
@DynamicUpdate
public class StarcoinAccount {

    @Id
    @Column(length = 34) //0x76A45FBF9631F68eb09812a21452E38E
    private String address;

    /**
     * Confirmed(on-chained) account sequence number.
     */
    @Column(precision = 50, scale = 0, nullable = false)
    private BigInteger confirmedSequenceNumber;

    @Column(precision = 50, scale = 0, nullable = false)
    private BigInteger sequenceNumber;

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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigInteger getConfirmedSequenceNumber() {
        return confirmedSequenceNumber;
    }

    public void setConfirmedSequenceNumber(BigInteger confirmedSequenceNumber) {
        this.confirmedSequenceNumber = confirmedSequenceNumber;
    }

    public BigInteger getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(BigInteger sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
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
