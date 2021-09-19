alter table price_round add column started_at bigint;
alter table price_round add column answered_in_round decimal(50,0);

-- DROP TABLE IF EXISTS `price_growth`;
CREATE TABLE `price_growth` (
   `pair_id` varchar(50) NOT NULL,
   `created_at` bigint(20) NOT NULL,
   `created_by` varchar(70) NOT NULL,
   `day_over_day_percentage` decimal(15,7) DEFAULT NULL,
   `updated_at` bigint(20) NOT NULL,
   `updated_by` varchar(70) NOT NULL,
   `version` bigint(20) DEFAULT NULL,
   PRIMARY KEY (`pair_id`)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

