package com.umar.apps.rule.web.rest;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RuleAttributeDTOTest {

    @Test
    void when_attribute_name_is_null_then_exception() {
        assertThatThrownBy(()-> new RuleAttributeDTO(null,"ruleType", "display text", new HashSet<>()))
                .hasMessage("attributeName is required");
    }

    @Test
    void when_rule_type_is_null_then_exception() {
        assertThatThrownBy(()-> new RuleAttributeDTO("attributeName",null, "display text", new HashSet<>()))
                .hasMessage("ruleType is required");
    }

    @Test
    void when_display_text_is_null_then_exception() {
        assertThatThrownBy(()-> new RuleAttributeDTO("attributeName","ruleType", null , new HashSet<>()))
                .hasMessage("displayText is required");
    }

    @Test
    void when_rule_values_is_null_then_exception() {
        assertThatThrownBy(()-> new RuleAttributeDTO("attributeName","ruleType", "display text", null))
                .hasMessage("ruleValues is required");
    }

    @Test
    void when_all_required_params_then_object_is_instantiated() {
        var ruleAttribDTO = new RuleAttributeDTO("attributeName","ruleType", "display text", new HashSet<>());
        assertThat(ruleAttribDTO).isNotNull();
        assertThat(ruleAttribDTO.attributeName()).isEqualTo("attributeName");
        assertThat(ruleAttribDTO.ruleType()).isEqualTo("ruleType");
        assertThat(ruleAttribDTO.created()).isNotNull();
        assertThat(ruleAttribDTO.updated()).isNull();
        assertThat(ruleAttribDTO.displayText()).isEqualTo("display text");
        assertThat(ruleAttribDTO.ruleValues()).isNotNull();
        assertThat(ruleAttribDTO.id()).isNull();
        assertThat(ruleAttribDTO.version()).isEqualTo(0);
    }

    @Test
    void when_rule_attribute_is_amended_then_updated_is_set() {
        var ruleAttribDTO = new RuleAttributeDTO("attributeName","ruleType", "display text", new HashSet<>());
        var amendedAttrib = new RuleAttributeDTO(1L, ruleAttribDTO.attributeName(), ruleAttribDTO.ruleType(), ruleAttribDTO.displayText(), ruleAttribDTO.created(), ruleAttribDTO.version() + 1, ruleAttribDTO.ruleValues());
        assertThat(amendedAttrib).isNotNull();
        assertThat(amendedAttrib.created()).isEqualTo(ruleAttribDTO.created());
        assertThat(amendedAttrib.attributeName()).isEqualTo("attributeName");
        assertThat(amendedAttrib.updated()).isNotNull();
        assertThat(amendedAttrib.version()).isEqualTo(1);
        assertThat(amendedAttrib.displayText()).isEqualTo("display text");
        assertThat(amendedAttrib.ruleType()).isEqualTo("ruleType");
        assertThat(amendedAttrib.ruleValues()).isEmpty();

    }
}
