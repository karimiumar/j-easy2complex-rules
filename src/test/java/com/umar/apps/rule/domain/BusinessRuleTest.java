package com.umar.apps.rule.domain;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BusinessRuleTest {

    @Test
    void given_params_a_rule_is_instantiated_with_empty_rule_attributes() {
        var rule = new BusinessRule("Test", "A Test Rule", 2, "Test Rule", true);
        assertThat(rule).isNotNull();
        assertThat(rule.getRuleAttributes()).isEmpty();
        assertThat(rule.isActive()).isTrue();
        assertThat(rule.getPriority()).isEqualTo(2);
        assertThat(rule.getRuleName()).isEqualTo("Test");
        assertThat(rule.getDescription()).isEqualTo("A Test Rule");
        assertThat(rule.getRuleType()).isEqualTo("Test Rule");
        assertThat(rule.getId()).isEqualTo(0L);
        assertThat(rule.getVersion()).isEqualTo(0);
        assertThat(rule.getCreated()).isNull();
        assertThat(rule.getUpdated()).isNull();
    }

    @Test
    void given_rule_when_attribute_is_added_then_rule_attributes_is_not_empty() {
        var rule = new BusinessRule("Test", "A Test Rule", 2, "Test Rule", true);
        var attrib = new RuleAttribute();
        rule.addRuleAttribute(attrib);
        assertThat(rule).isNotNull();
        assertThat(rule.getRuleAttributes()).isNotEmpty();
    }

    @Test
    void given_rule_when_two_attributes_added_then_rule_attributes_size_is_2() {
        var rule = new BusinessRule("Test", "A Test Rule", 2, "Test Rule", true);
        var attrib1 = new RuleAttribute(0L, "attribute1", "Test", "Attribute 1");
        var attrib2 = new RuleAttribute(0L, "attribute2", "Test", "Attribute 2");
        rule.addRuleAttribute(attrib1);
        rule.addRuleAttribute(attrib2);
        assertThat(rule).isNotNull();
        assertThat(rule.getRuleAttributes()).isNotEmpty();
        assertThat(rule.getRuleAttributes().size()).isEqualTo(2);
        assertThat(rule.getRuleAttributes()).containsAll(List.of(attrib1, attrib2));
    }

    @Test
    void given_rule_when_an_attribute_deleted_then_rule_attributes_size_is_shrinked() {
        var rule = new BusinessRule("Test", "A Test Rule", 2, "Test Rule", true);
        var attrib1 = new RuleAttribute(0L, "attribute1", "Test", "Attribute 1");
        var attrib2 = new RuleAttribute(0L, "attribute2", "Test", "Attribute 2");
        var attrib3 = new RuleAttribute(0L, "attribute3", "Test", "Attribute 3");
        rule.addRuleAttribute(attrib1);
        rule.addRuleAttribute(attrib2);
        rule.addRuleAttribute(attrib3);
        assertThat(rule).isNotNull();
        assertThat(rule.getRuleAttributes()).isNotEmpty();
        assertThat(rule.getRuleAttributes().size()).isEqualTo(3);
        assertThat(rule.getRuleAttributes()).containsAll(List.of(attrib1, attrib2, attrib3));

        rule.removeRuleAttribute(attrib2);
        assertThat(rule.getRuleAttributes().size()).isEqualTo(2);
        assertThat(rule.getRuleAttributes()).containsAll(List.of(attrib1, attrib3));
    }

    @Test
    void given_rule_when_setRuleAttributes_then_ruleAttributes_is_initializedWith_new_ruleAttributes() {
        var rule = new BusinessRule("Test", "A Test Rule", 2, "Test Rule", true);
        var attrib1 = new RuleAttribute(0L, "attribute1", "Test", "Attribute 1");
        var attrib2 = new RuleAttribute(0L, "attribute2", "Test", "Attribute 2");
        var attrib3 = new RuleAttribute(0L, "attribute3", "Test", "Attribute 3");
        rule.addRuleAttribute(attrib1);
        rule.addRuleAttribute(attrib2);
        rule.addRuleAttribute(attrib3);
        assertThat(rule).isNotNull();
        assertThat(rule.getRuleAttributes()).isNotEmpty();
        assertThat(rule.getRuleAttributes().size()).isEqualTo(3);
        assertThat(rule.getRuleAttributes()).containsAll(List.of(attrib1, attrib2, attrib3));

        rule.setRuleAttributes(new HashSet<>());
        assertThat(rule.getRuleAttributes()).isEmpty();
        assertThat(rule.getRuleAttributes()).doesNotContain(attrib1, attrib2, attrib3);
    }

    @Test
    void given_rule_with_two_attributes_then_each_ruleAttribute_businessRule_is_rule() {
        var rule = new BusinessRule("Test", "A Test Rule", 2, "Test Rule", true);
        var attrib1 = new RuleAttribute(0L, "attribute1", "Test", "Attribute 1");
        var attrib2 = new RuleAttribute(0L, "attribute2", "Test", "Attribute 2");
        rule.addRuleAttribute(attrib1);
        rule.addRuleAttribute(attrib2);
        assertThat(attrib1.getBusinessRule()).isEqualTo(rule);
        assertThat(attrib2.getBusinessRule()).isEqualTo(rule);
    }

    @Test
    void given_rule_with_attributes_when_an_ruleAttribute_is_removed_then_ruleAttributes_businessRule_is_null() {
        var rule = new BusinessRule("Test", "A Test Rule", 2, "Test Rule", true);
        var attrib1 = new RuleAttribute(0L, "attribute1", "Test", "Attribute 1");
        var attrib2 = new RuleAttribute(0L, "attribute2", "Test", "Attribute 2");
        rule.addRuleAttribute(attrib1);
        rule.addRuleAttribute(attrib2);
        assertThat(attrib1.getBusinessRule()).isEqualTo(rule);
        assertThat(attrib2.getBusinessRule()).isEqualTo(rule);

        rule.removeRuleAttribute(attrib1);
        assertThat(rule.getRuleAttributes()).containsExactly(attrib2);
        assertThat(attrib1.getBusinessRule()).isNull();
    }

    @Test
    void given_two_rules_are_equal_when_priority_active_ruleName_ruleType_is_same() {
        var rule1 = new BusinessRule("Test", "A Test Rule", 2, "Test Rule", true);
        var rule2 = new BusinessRule("Test", "A Test Rule", 2, "Test Rule", true);
        assertThat(rule1).isEqualTo(rule2);
    }

    @Test
    void given_two_rules_are_not_equal_when_priority_is_different() {
        var rule1 = new BusinessRule("Test", "A Test Rule", 1, "Test Rule", true);
        var rule2 = new BusinessRule("Test", "A Test Rule", 2, "Test Rule", true);
        assertThat(rule1).isNotEqualTo(rule2);
    }

    @Test
    void given_two_rules_are_not_equal_when_ruleName_is_different() {
        var rule1 = new BusinessRule("Test1", "A Test Rule", 1, "Test Rule", true);
        var rule2 = new BusinessRule("Test2", "A Test Rule", 1, "Test Rule", true);
        assertThat(rule1).isNotEqualTo(rule2);
    }

    @Test
    void given_two_rules_are_not_equal_when_ruleType_is_different() {
        var rule1 = new BusinessRule("Test", "A Test Rule", 1, "Test Rule1", true);
        var rule2 = new BusinessRule("Test", "A Test Rule", 1, "Test Rule2", true);
        assertThat(rule1).isNotEqualTo(rule2);
    }

    @Test
    void given_two_rules_are_not_equal_when_active_is_different() {
        var rule1 = new BusinessRule("Test", "A Test Rule", 1, "Test Rule", true);
        var rule2 = new BusinessRule("Test", "A Test Rule", 1, "Test Rule", false);
        assertThat(rule1).isNotEqualTo(rule2);
    }

    @Test
    void given_two_objects_are_not_equal_when_one_is_instanceof_rule_and_another_not_an_instance_of_rule() {
        var rule1 = new BusinessRule("Test", "A Test Rule", 1, "Test Rule", true);
        var rule2 = new Object();
        assertThat(rule1).isNotEqualTo(rule2);
    }

    @Test
    void given_two_objects_when_equals_invoked_throws_NullPointerException_for_ruleType() {
        var rule1 = new BusinessRule();
        var rule2 = new BusinessRule();
        assertThatThrownBy(()-> rule1.equals(rule2))
                .hasMessage("Cannot invoke \"String.equals(Object)\" because \"this.ruleType\" is null")
                .isInstanceOf(NullPointerException.class);
    }
}
