-- DROP TABLE IF EXISTS `price_feed`;

CREATE TABLE `price_feed` (
  `pair_id` varchar(50) NOT NULL,
  `created_at` bigint(20) NOT NULL,
  `created_by` varchar(70) NOT NULL,
  `decimals` int(11) DEFAULT NULL,
  `deviation_percentage` decimal(10,7) DEFAULT NULL,
  `heartbeat_hours` decimal(10,5) DEFAULT NULL,
  `latest_price` decimal(50,0) DEFAULT NULL,
  `pair_name` varchar(200) DEFAULT NULL,
  `updated_at` bigint(20) NOT NULL,
  `updated_by` varchar(70) NOT NULL,
  `version` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`pair_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
