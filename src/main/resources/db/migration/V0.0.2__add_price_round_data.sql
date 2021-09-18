-- DROP TABLE IF EXISTS `price_round`;
CREATE TABLE `price_round` (
  `pair_id` varchar(50) NOT NULL,
  `round_id` decimal(50,0) NOT NULL,
  `created_at` bigint(20) NOT NULL,
  `created_by` varchar(70) NOT NULL,
  `price` decimal(50,0) NOT NULL,
  `updated_at` bigint(20) NOT NULL,
  `updated_by` varchar(70) NOT NULL,
  `version` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`pair_id`,`round_id`),
  KEY `IdxPairIdUpdatedAt` (`pair_id`,`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
