CREATE DATABASE `rules_test_db`
    CHARACTER SET 'utf8'
    COLLATE 'utf8_general_ci';


use `rules_test_db`;

CREATE TABLE `rules` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `rule_name` VARCHAR(50) COLLATE utf8_general_ci NOT NULL,
  `rule_type` VARCHAR(50) COLLATE utf8_general_ci NOT NULL,
  `priority` INTEGER(11) NOT NULL,
  `active` TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY USING BTREE (`id`),
  UNIQUE KEY `rules_uniq_name_type` USING BTREE (`rule_name`, `rule_type`)
) ENGINE=InnoDB
AUTO_INCREMENT=1 ROW_FORMAT=DYNAMIC CHARACTER SET 'utf8' COLLATE 'utf8_general_ci'
;

CREATE TABLE `attributes` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `attribute_name` VARCHAR(100) COLLATE utf8_general_ci NOT NULL,
  `type` VARCHAR(255) COLLATE utf8_general_ci NOT NULL,
  `rule_type` VARCHAR(50) COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY USING BTREE (`id`),
  UNIQUE KEY `uniq_attrib_name_type_rule_type` USING BTREE (`attribute_name`, `type`, `rule_type`)
) ENGINE=InnoDB
AUTO_INCREMENT=1 ROW_FORMAT=DYNAMIC CHARACTER SET 'utf8' COLLATE 'utf8_general_ci'
;

CREATE TABLE `operands` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `operand` VARCHAR(255) COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY USING BTREE (`id`),
  UNIQUE KEY `operand` USING BTREE (`operand`)
) ENGINE=InnoDB
AUTO_INCREMENT=1 ROW_FORMAT=DYNAMIC CHARACTER SET 'utf8' COLLATE 'utf8_general_ci'
;

CREATE TABLE `rule_attribute` (
  `attribute_id` BIGINT(20) NOT NULL,
  `rule_id` BIGINT(20) NOT NULL,
  KEY `rule_attribute_fk_attribute_id` USING BTREE (`attribute_id`),
  KEY `rule_attribute_fk_rule_id` USING BTREE (`rule_id`),
  CONSTRAINT `rule_attribute_fk_attribute_id` FOREIGN KEY (`attribute_id`) REFERENCES `attributes` (`id`),
  CONSTRAINT `rule_attribute_fk_rule_id` FOREIGN KEY (`rule_id`) REFERENCES `rules` (`id`)
) ENGINE=InnoDB
ROW_FORMAT=DYNAMIC CHARACTER SET 'utf8' COLLATE 'utf8_general_ci'
;

CREATE TABLE `rule_operands` (
  `value_id` BIGINT(20) NOT NULL,
  `rule_id` BIGINT(20) NOT NULL,
  KEY `rule_operands_fk_rule_id` USING BTREE (`rule_id`),
  KEY `rule_operands_fk1` USING BTREE (`value_id`),
  CONSTRAINT `rule_operands_fk1` FOREIGN KEY (`value_id`) REFERENCES `operands` (`id`),
  CONSTRAINT `rule_operands_fk_rule_id` FOREIGN KEY (`rule_id`) REFERENCES `rules` (`id`)
) ENGINE=InnoDB
ROW_FORMAT=DYNAMIC CHARACTER SET 'utf8' COLLATE 'utf8_general_ci'
;


