package com.umar.apps.rule.web.rest;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BusinessRuleDTOTest {

    @Test
    void when_rule_name_is_null_then_exception() {
        assertThatThrownBy(() -> new BusinessRuleDTO(null, "ruleType", "description", 1, new HashSet<>()))
                .hasMessage("ruleName is required");
    }

    @Test
    void when_rule_type_is_null_then_exception() {
        assertThatThrownBy(() -> new BusinessRuleDTO("ruleName", null, "description", 1, new HashSet<>()))
                .hasMessage("ruleType is required");
    }

    @Test
    void when_description_is_null_then_exception() {
        assertThatThrownBy(() -> new BusinessRuleDTO("ruleName", "ruleType", null, 1, new HashSet<>()))
                .hasMessage("description is required");
    }

    @Test
    void when_priority_is_null_then_exception() {
        assertThatThrownBy(() -> new BusinessRuleDTO("ruleName", "ruleType", "description", null, new HashSet<>()))
                .hasMessage("priority is required");
    }

    @Test
    void when_rule_attributes_is_null_then_exception() {
        assertThatThrownBy(() -> new BusinessRuleDTO("ruleName", "ruleType", "description", 1, null))
                .hasMessage("ruleAttributes is required");
    }

    @Test
    void when_all_required_params_then_object_is_instantiated() {
        var businessDTO = new BusinessRuleDTO("ruleName", "ruleType", "description", 1, new HashSet<>());
        assertThat(businessDTO).isNotNull();
        assertThat(businessDTO.created()).isNotNull();
        assertThat(businessDTO.updated()).isNull();
        assertThat(businessDTO.id()).isNull();
        assertThat(businessDTO.version()).isEqualTo(0);
        assertThat(businessDTO.active()).isTrue();
        assertThat(businessDTO.ruleName()).isEqualTo("ruleName");
        assertThat(businessDTO.ruleType()).isEqualTo("ruleType");
        assertThat(businessDTO.description()).isEqualTo("description");
        assertThat(businessDTO.ruleAttributes()).isNotNull();
        assertThat(businessDTO.ruleAttributes()).isEmpty();
        assertThat(businessDTO.priority()).isEqualTo(1);
        assertThat(businessDTO.version()).isEqualTo(0);
    }

    @Test
    void when_business_rule_is_amended_then_updated_date_is_set() {
        var businessDTO = new BusinessRuleDTO("ruleName", "ruleType", "description", 1, new HashSet<>());
        var amendedDTO = new BusinessRuleDTO(1L, businessDTO.ruleName(), businessDTO.ruleType(), businessDTO.description(), 2, false, businessDTO.created(), businessDTO.version() + 1, businessDTO.ruleAttributes());
        assertThat(amendedDTO).isNotNull();
        assertThat(amendedDTO.created()).isEqualTo(businessDTO.created());
        assertThat(amendedDTO.updated()).isNotNull();
        assertThat(amendedDTO.id()).isEqualTo(1L);
        assertThat(amendedDTO.active()).isFalse();
        assertThat(amendedDTO.ruleName()).isEqualTo("ruleName");
        assertThat(amendedDTO.ruleType()).isEqualTo("ruleType");
        assertThat(amendedDTO.description()).isEqualTo("description");
        assertThat(amendedDTO.ruleAttributes()).isNotNull();
        assertThat(amendedDTO.ruleAttributes()).isEmpty();
        assertThat(amendedDTO.priority()).isEqualTo(2);
        assertThat(amendedDTO.version()).isEqualTo(1);
    }
}
