package com.umar.apps.rule.web.rest;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RuleValueDTOTest {

    @Test
    void when_operand_is_null_then_exception() {
        assertThatThrownBy(() -> new RuleValueDTO(null)).hasMessage("operand is required");
    }

    @Test
    void when_all_required_params_then_object_is_instantiated() {
        var op = new RuleValueDTO("op");
        assertThat(op.operand()).isEqualTo("op");
        assertThat(op.created().getYear()).isEqualTo(LocalDateTime.now().getYear());
        assertThat(op.updated()).isNull();
        assertThat(op.version()).isEqualTo(0);
        assertThat(op.id()).isNull();
    }

    @Test
    void when_rule_value_is_amended_then_updated_is_set() {
        var op = new RuleValueDTO("op");
        var amendOp = new RuleValueDTO(1L, op.operand(), op.created(), op.version() + 1);
        assertThat(amendOp).isNotNull();
        assertThat(amendOp.operand()).isEqualTo("op");
        assertThat(amendOp.created()).isEqualTo(op.created());
        assertThat(amendOp.version()).isEqualTo(1);
        assertThat(amendOp.id()).isEqualTo(1L);
        assertThat(amendOp.updated()).isNotNull();
    }
}
