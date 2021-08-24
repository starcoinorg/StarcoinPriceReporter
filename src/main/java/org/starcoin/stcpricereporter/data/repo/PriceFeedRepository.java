package org.starcoin.stcpricereporter.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.starcoin.stcpricereporter.data.model.PriceFeed;

public interface PriceFeedRepository extends JpaRepository<PriceFeed, String> {
    //
}
