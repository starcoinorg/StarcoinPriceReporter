package org.starcoin.stcpricereporter.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.starcoin.stcpricereporter.data.model.PricePair;

public interface PricePairRepository extends JpaRepository<PricePair, String> {
    //
}
