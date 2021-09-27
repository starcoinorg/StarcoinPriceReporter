CREATE TABLE `price_pair` (
  `pair_id` varchar(50) NOT NULL,
  `created_at` bigint(20) NOT NULL,
  `created_by` varchar(70) NOT NULL,
  `decimals` int(11) DEFAULT NULL,
  `on_chain_struct_address` varchar(34) NOT NULL,
  `on_chain_struct_module` varchar(255) NOT NULL,
  `on_chain_struct_name` varchar(255) NOT NULL,
  `pair_name` varchar(200) DEFAULT NULL,
  `updated_at` bigint(20) NOT NULL,
  `updated_by` varchar(70) NOT NULL,
  `version` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`pair_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
