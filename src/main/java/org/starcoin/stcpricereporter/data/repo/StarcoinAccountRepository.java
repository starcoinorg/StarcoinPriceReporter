package org.starcoin.stcpricereporter.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.starcoin.stcpricereporter.data.model.StarcoinAccount;

public interface StarcoinAccountRepository extends JpaRepository<StarcoinAccount, String> {

}
