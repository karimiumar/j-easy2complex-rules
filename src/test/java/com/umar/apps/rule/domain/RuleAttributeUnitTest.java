package com.umar.apps.rule.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RuleAttributeUnitTest {

    @Test
    void given_params_a_ruleAttribute_is_instantiated_with_no_businessRule() {
        var attrib = new RuleAttribute(1L, "testAttribute1", "Test", "Test Attribute 1");
        assertThat(attrib).isNotNull();
        assertThat(attrib.getBusinessRule()).isNull();
        assertThat(attrib.getAttributeName()).isEqualTo("testAttribute1");
        assertThat(attrib.getRuleType()).isEqualTo("Test");
        assertThat(attrib.getDisplayName()).isEqualTo("Test Attribute 1");
        assertThat(attrib.getId()).isEqualTo(1L);
        assertThat(attrib.getCreated()).isNull();
        assertThat(attrib.getUpdated()).isNull();
        assertThat(attrib.getVersion()).isEqualTo(0);
    }
    
    @Test
    void given_attribute_when_a_rule_is_set_then_businessRule_is_not_null() {
        var attrib = new RuleAttribute(1L, "testAttribute1", "Test", "Test Attribute 1");
        var rule = new BusinessRule();
        rule.setRuleName("Test Rule");
        attrib.setBusinessRule(rule);
        assertThat(attrib).isNotNull();
        assertThat(attrib.getBusinessRule()).isNotNull();
        assertThat(attrib.getBusinessRule()).isEqualTo(rule);
    }
    
    @Test
    void given_attribute_when_a_rule_is_removed_businessRule_is_null() {
        var attrib = new RuleAttribute(1L, "testAttribute1", "Test", "Test Attribute 1");
        var rule = new BusinessRule();
        rule.setRuleName("Test Rule");
        attrib.setBusinessRule(rule);
        assertThat(attrib).isNotNull();
        assertThat(attrib.getBusinessRule()).isNotNull();
        assertThat(attrib.getBusinessRule()).isEqualTo(rule);
        //Remove BusinessRule
        attrib.setBusinessRule(null);
        assertThat(attrib.getBusinessRule()).isNull();
    }

    @Test
    void given_two_objects_when_equals_invoked_then_not_equal() {
        var attribute1 = new RuleAttribute(1L, "testAttribute1", "Test", "Test Attribute 1");
        var attribute2 = new RuleAttribute();
        assertThat(attribute1).isNotEqualTo(attribute2);
    }

    @Test
    void given_two_objects_when_equals_invoked_then_equals() {
        var attribute1 = new RuleAttribute(1L, "testAttribute1", "Test", "Test Attribute 1");
        var attribute2 = new RuleAttribute(1L, "testAttribute1", "Test", "Test Attribute 1");
        assertThat(attribute1).isEqualTo(attribute2);
    }

    @Test
    void given_attribute_when_another_businessRule_assigned_then_newBusinessRule() {
        var attrib = new RuleAttribute(1L, "testAttribute1", "Test", "Test Attribute 1");
        var firstRule = new BusinessRule();
        firstRule.setRuleName("First Rule");
        attrib.setBusinessRule(firstRule);
        assertThat(attrib).isNotNull();
        assertThat(attrib.getBusinessRule()).isNotNull();
        assertThat(attrib.getBusinessRule()).isEqualTo(firstRule);

        var secondRule = new BusinessRule();
        secondRule.setRuleName("Second Rule");
        attrib.setBusinessRule(secondRule);
        assertThat(attrib.getBusinessRule()).isEqualTo(secondRule);
        assertThat(attrib.getBusinessRule()).isNotEqualTo(firstRule);
    }
}
