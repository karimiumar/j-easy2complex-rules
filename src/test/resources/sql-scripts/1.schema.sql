CREATE DATABASE `rules_test_db`
    CHARACTER SET 'utf8'
    COLLATE 'utf8_general_ci';


use `rules_test_db`;

CREATE TABLE `rules` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `active` BIT(1) DEFAULT NULL,
  `desc` VARCHAR(100) COLLATE utf8_general_ci DEFAULT NULL,
  `priority` INTEGER(11) DEFAULT NULL,
  `rule_name` VARCHAR(100) COLLATE utf8_general_ci DEFAULT NULL,
  `rule_type` VARCHAR(30) COLLATE utf8_general_ci DEFAULT NULL,
  `version` INTEGER(11) DEFAULT NULL,
  PRIMARY KEY USING BTREE (`id`),
  UNIQUE KEY `UKeh9io4kysb6m13u5yno4yut71` USING BTREE (`rule_name`, `rule_type`)
) ENGINE=InnoDB
AUTO_INCREMENT=16 ROW_FORMAT=DYNAMIC CHARACTER SET 'utf8' COLLATE 'utf8_general_ci'
;

CREATE TABLE `attributes` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `attribute_name` VARCHAR(30) COLLATE utf8_general_ci DEFAULT NULL,
  `display_name` VARCHAR(60) COLLATE utf8_general_ci DEFAULT NULL,
  `rule_type` VARCHAR(30) COLLATE utf8_general_ci DEFAULT NULL,
  `version` INTEGER(11) DEFAULT NULL,
  PRIMARY KEY USING BTREE (`id`),
  UNIQUE KEY `UKk8tf5kogn6j871y8qp1ib5c2k` USING BTREE (`attribute_name`, `rule_type`)
) ENGINE=InnoDB
AUTO_INCREMENT=18 ROW_FORMAT=DYNAMIC CHARACTER SET 'utf8' COLLATE 'utf8_general_ci'
;

CREATE TABLE `values` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `operand` VARCHAR(150) COLLATE utf8_general_ci DEFAULT NULL,
  `version` INTEGER(11) DEFAULT NULL,
  PRIMARY KEY USING BTREE (`id`),
  UNIQUE KEY `UK_m8fpspnouccl4qb8eu5rtkwsk` USING BTREE (`operand`)
) ENGINE=InnoDB
AUTO_INCREMENT=19 ROW_FORMAT=DYNAMIC CHARACTER SET 'utf8' COLLATE 'utf8_general_ci'
;

CREATE TABLE `rule_attribute` (
  `rule_id` BIGINT(20) DEFAULT NULL,
  `attribute_id` BIGINT(20) NOT NULL,
  PRIMARY KEY USING BTREE (`attribute_id`),
  UNIQUE KEY `UKkkrq9qjermmo20kbbmtonkavi` USING BTREE (`attribute_id`, `rule_id`),
  KEY `FKpl2ouilo68l94hbi123hj5l9u` USING BTREE (`rule_id`),
  CONSTRAINT `FK86hr6myn292eilhkkru4w2df7` FOREIGN KEY (`attribute_id`) REFERENCES `attributes` (`id`),
  CONSTRAINT `FKpl2ouilo68l94hbi123hj5l9u` FOREIGN KEY (`rule_id`) REFERENCES `rules` (`id`)
) ENGINE=InnoDB
ROW_FORMAT=DYNAMIC CHARACTER SET 'utf8' COLLATE 'utf8_general_ci'
;

CREATE TABLE `attribute_values` (
  `created_on` DATETIME DEFAULT NULL,
  `ruleAttribute_id` BIGINT(20) NOT NULL,
  `ruleValue_id` BIGINT(20) NOT NULL,
  PRIMARY KEY USING BTREE (`ruleAttribute_id`, `ruleValue_id`),
  KEY `FK2wbm20hohs0raaqdf5vr1i594` USING BTREE (`ruleValue_id`),
  CONSTRAINT `FK2wbm20hohs0raaqdf5vr1i594` FOREIGN KEY (`ruleValue_id`) REFERENCES `values` (`id`),
  CONSTRAINT `FK9588uy5cfeksg7o76j8p32cvh` FOREIGN KEY (`ruleAttribute_id`) REFERENCES `attributes` (`id`)
) ENGINE=InnoDB
ROW_FORMAT=DYNAMIC CHARACTER SET 'utf8' COLLATE 'utf8_general_ci'
;


CREATE TABLE `cashflows` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `amount` DOUBLE DEFAULT NULL,
  `counter_party` VARCHAR(255) COLLATE utf8_general_ci DEFAULT NULL,
  `created_on` DATETIME DEFAULT NULL,
  `currency` VARCHAR(255) COLLATE utf8_general_ci DEFAULT NULL,
  `notes` VARCHAR(255) COLLATE utf8_general_ci DEFAULT NULL,
  `settlement_date` DATE DEFAULT NULL,
  `stp_allowed` TINYINT(1) DEFAULT 1,
  `version` INTEGER(11) DEFAULT 0,
  PRIMARY KEY USING BTREE (`id`)
) ENGINE=InnoDB
AUTO_INCREMENT=398 ROW_FORMAT=DYNAMIC CHARACTER SET 'utf8' COLLATE 'utf8_general_ci'
;




