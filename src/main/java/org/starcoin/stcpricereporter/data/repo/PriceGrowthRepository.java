package org.starcoin.stcpricereporter.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.starcoin.stcpricereporter.data.model.PriceGrowth;

import java.util.List;

public interface PriceGrowthRepository extends JpaRepository<PriceGrowth, String> {

    List<PriceGrowth> findByPairIdIn(List<String> pairIds);

}
