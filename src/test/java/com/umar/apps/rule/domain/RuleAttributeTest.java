package com.umar.apps.rule.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RuleAttributeTest {

    @Test
    void given_params_a_ruleAttribute_is_instantiated_with_empty() {
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
}
