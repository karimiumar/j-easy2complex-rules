CREATE DATABASE `rulesdb`
    CHARACTER SET 'utf8'
    COLLATE 'utf8_general_ci';


use `rulesdb`;

CREATE TABLE `rules` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `active` BIT(1) DEFAULT NULL,
  `desc` VARCHAR(100) COLLATE utf8_general_ci DEFAULT NULL,
  `priority` INTEGER(11) DEFAULT NULL,
  `rule_name` VARCHAR(100) COLLATE utf8_general_ci DEFAULT NULL,
  `rule_type` VARCHAR(30) COLLATE utf8_general_ci DEFAULT NULL,
  `created` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated` TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `version` INTEGER(11) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_rules_name_type` USING BTREE (`rule_name`, `rule_type`)
) ENGINE=InnoDB
AUTO_INCREMENT=16 ROW_FORMAT=DYNAMIC CHARACTER SET 'utf8' COLLATE 'utf8_general_ci'
;

CREATE TABLE `attributes` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `attribute_name` VARCHAR(30) COLLATE utf8_general_ci DEFAULT NULL,
  `display_name` VARCHAR(60) COLLATE utf8_general_ci DEFAULT NULL,
  `rule_type` VARCHAR(30) COLLATE utf8_general_ci DEFAULT NULL,
  `created` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated` TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `version` INTEGER(11) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_attrs_name_type` USING BTREE (`attribute_name`, `rule_type`)
) ENGINE=InnoDB
AUTO_INCREMENT=18 ROW_FORMAT=DYNAMIC CHARACTER SET 'utf8' COLLATE 'utf8_general_ci'
;

CREATE TABLE `values` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `operand` VARCHAR(150) COLLATE utf8_general_ci DEFAULT NULL,
  `created` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated` TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `version` INTEGER(11) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_val_operand` USING BTREE (`operand`)
) ENGINE=InnoDB
AUTO_INCREMENT=19 ROW_FORMAT=DYNAMIC CHARACTER SET 'utf8' COLLATE 'utf8_general_ci'
;

CREATE TABLE `rule_attribute` (
  `rule_id` BIGINT(20) DEFAULT NULL,
  `attribute_id` BIGINT(20) NOT NULL,
  `created` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated` TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`attribute_id`),
  UNIQUE KEY `uniq_rule_attr_attrid_ruleid` USING BTREE (`attribute_id`, `rule_id`),
  KEY `FKpl2ouilo68l94hbi123hj5l9u` USING BTREE (`rule_id`),
  CONSTRAINT `FK_attrs_attr_id` FOREIGN KEY (`attribute_id`) REFERENCES `attributes` (`id`),
  CONSTRAINT `FK_rules_rule_id` FOREIGN KEY (`rule_id`) REFERENCES `rules` (`id`)
) ENGINE=InnoDB
ROW_FORMAT=DYNAMIC CHARACTER SET 'utf8' COLLATE 'utf8_general_ci'
;

CREATE TABLE `attribute_values` (
  `ruleAttribute_id` BIGINT(20) NOT NULL,
  `ruleValue_id` BIGINT(20) NOT NULL,
  `created` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated` TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ruleAttribute_id`, `ruleValue_id`),
  KEY `FK2wbm20hohs0raaqdf5vr1i594` USING BTREE (`ruleValue_id`),
  CONSTRAINT `FK_values_id` FOREIGN KEY (`ruleValue_id`) REFERENCES `values` (`id`),
  CONSTRAINT `FK_attrs_id` FOREIGN KEY (`ruleAttribute_id`) REFERENCES `attributes` (`id`)
) ENGINE=InnoDB
ROW_FORMAT=DYNAMIC CHARACTER SET 'utf8' COLLATE 'utf8_general_ci'
;


CREATE TABLE `cashflows` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `amount` DOUBLE DEFAULT NULL,
  `counter_party` VARCHAR(255) COLLATE utf8_general_ci DEFAULT NULL,
  `currency` VARCHAR(255) COLLATE utf8_general_ci DEFAULT NULL,
  `notes` VARCHAR(255) COLLATE utf8_general_ci DEFAULT NULL,
  `settlement_date` DATE DEFAULT NULL,
  `stp_allowed` TINYINT(1) DEFAULT 1,
  `created` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated` TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `version` INTEGER(11) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB
AUTO_INCREMENT=398 ROW_FORMAT=DYNAMIC CHARACTER SET 'utf8' COLLATE 'utf8_general_ci'
;

delimiter //
CREATE TRIGGER INC_RULES BEFORE UPDATE ON rules
for each row
begin
    SET new.version = new.version + 1;
end
//
delimiter;