package org.starcoin.stcpricereporter.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.starcoin.stcpricereporter.data.model.PriceRound;
import org.starcoin.stcpricereporter.data.model.PriceRoundId;
import org.starcoin.stcpricereporter.vo.PriceRoundView;

import java.util.List;

public interface PriceRoundRepository extends JpaRepository<PriceRound, PriceRoundId> {

    PriceRound findFirstByPairIdAndUpdatedAtGreaterThanOrderByUpdatedAt(String pairId, Long afterTimestamp);

    PriceRound findFirstByPairIdAndUpdatedAtLessThanOrderByUpdatedAtDesc(String pairId, Long beforeTimestamp);

    List<PriceRound> findByPairIdInAndUpdatedAtGreaterThanEqualAndUpdatedAtLessThanOrderByPairId(List<String> pairIds, Long afterTimestamp, Long beforeTimestamp);

    @Query(value = "(SELECT \n" +
            "    r.pair_id as pairId, r.round_id as roundId, r.price as price, r.updated_at as updatedAt, f.pair_name as pairName, f.decimals as decimals \n" +
            "FROM\n" +
            "    price_round r\n" +
            "        LEFT JOIN\n" +
            "    price_feed f ON r.pair_id = f.pair_id\n" +
            "WHERE\n" +
            "    r.pair_id = :pairId \n" +
            "        AND r.updated_at > :timestamp \n" +
            "ORDER BY r.updated_at\n" +
            "LIMIT 1) UNION (SELECT \n" +
            "    r.pair_id as pairId, r.round_id as roundId, r.price as price, r.updated_at as updatedAt, f.pair_name as pairName, f.decimals as decimals \n" +
            "FROM\n" +
            "    price_round r\n" +
            "        LEFT JOIN\n" +
            "    price_feed f ON r.pair_id = f.pair_id\n" +
            "WHERE\n" +
            "    r.pair_id = :pairId \n" +
            "        AND r.updated_at <= :timestamp \n" +
            "ORDER BY r.updated_at DESC\n" +
            "LIMIT 1)", nativeQuery = true)
    List<PriceRoundView> findProximateRounds(@Param("pairId") String pairId, @Param("timestamp") Long timestamp);
}
