package org.starcoin.stcpricereporter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.starcoin.stcpricereporter.data.model.StarcoinAccount;
import org.starcoin.stcpricereporter.data.repo.StarcoinAccountRepository;

import javax.transaction.Transactional;
import java.math.BigInteger;


@Service
public class StarcoinAccountService {

    @Autowired
    private StarcoinAccountRepository starcoinAccountRepository;

    /**
     * Reset account sequence number in database. If account is not existed in database, create it.
     */
    @Transactional
    public void resetSequenceNumber(String address, BigInteger seqNumber) {
        StarcoinAccount starcoinAccount = starcoinAccountRepository.findById(address).orElse(null);
        if (starcoinAccount == null) {
            starcoinAccount = new StarcoinAccount();
            starcoinAccount.setAddress(address);
            starcoinAccount.setCreatedBy("ADMIN");
            starcoinAccount.setCreatedAt(System.currentTimeMillis());
        }
        starcoinAccount.setConfirmedSequenceNumber(seqNumber.subtract(BigInteger.ONE));
        starcoinAccount.setSequenceNumber(seqNumber);
        starcoinAccount.setUpdatedBy("ADMIN");
        starcoinAccount.setUpdatedAt(System.currentTimeMillis());
        starcoinAccountRepository.save(starcoinAccount);
    }

    @Transactional
    public BigInteger getSequenceNumberAndIncrease(String address) {
        StarcoinAccount starcoinAccount = assertAccountAddress(address);
        BigInteger transactionCount = starcoinAccount.getSequenceNumber();
        starcoinAccount.setSequenceNumber(starcoinAccount.getSequenceNumber().add(BigInteger.ONE));
        starcoinAccountRepository.save(starcoinAccount);
        return transactionCount;
    }

    @Transactional
    public BigInteger getSequenceNumber(String address) {
        StarcoinAccount starcoinAccount = assertAccountAddress(address);
        return starcoinAccount.getSequenceNumber();
    }

    /**
     * Get confirmed(on-chained) account sequence number.
     *
     * @param address account address
     */
    @Transactional
    public BigInteger getConfirmedSequenceNumber(String address) {
        StarcoinAccount starcoinAccount = assertAccountAddress(address);
        return starcoinAccount.getConfirmedSequenceNumber();
    }

    @Transactional
    public void confirmSequenceNumberOnChain(String address, BigInteger count) {
        StarcoinAccount starcoinAccount = assertAccountAddress(address);
        if (starcoinAccount.getConfirmedSequenceNumber().compareTo(count) < 0) {
            starcoinAccount.setConfirmedSequenceNumber(count);
            if (starcoinAccount.getSequenceNumber().compareTo(count.add(BigInteger.ONE)) < 0) {
                starcoinAccount.setSequenceNumber(count.add(BigInteger.ONE));
            }
            starcoinAccountRepository.save(starcoinAccount);
        }
    }

    public StarcoinAccount getStarcoinAccountOrElseNull(String address) {
        return starcoinAccountRepository.findById(address).orElse(null);
    }

    private StarcoinAccount assertAccountAddress(String address) {
        StarcoinAccount starcoinAccount = starcoinAccountRepository.findById(address).orElse(null);
        if (starcoinAccount == null) {
            throw new RuntimeException("CANNOT find account by address: " + address);
        }
        return starcoinAccount;
    }


}
