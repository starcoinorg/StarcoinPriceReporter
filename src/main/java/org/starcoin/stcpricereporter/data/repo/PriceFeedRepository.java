package org.starcoin.stcpricereporter.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.starcoin.stcpricereporter.data.model.PriceFeed;

import java.util.List;

public interface PriceFeedRepository extends JpaRepository<PriceFeed, String> {

    List<PriceFeed> findByOnChainStatusEqualsAndUpdatedAtLessThan(String status, Long updatedBefore);

    int countByOnChainStatusIn(String[] statuses);

    List<PriceFeed> findByOnChainStatusInAndUpdatedAtLessThanOrderByUpdatedAt(String[] statuses, Long updatedBefore);

    List<PriceFeed> findByPairIdIn(List<String> tokenPairIds);

}
