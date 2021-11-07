package com.umar.apps.rule.domain;

import com.umar.apps.util.GenericBuilder;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class RuleValueUnitTest {

    @Test
    void given_params_a_ruleValue_is_instantiated() {
        var value1 = GenericBuilder.of(RuleValue::new)
                .with(RuleValue::setOperand, "test").build();
        assertThat(value1).isNotNull();
        assertThat(value1.getOperand()).isEqualTo("test");
        assertThat(value1.getId()).isNull();
        assertThat(value1.getCreated()).isNull();
        assertThat(value1.getUpdated()).isNull();
        assertThat(value1.getVersion()).isEqualTo(0);
    }

    @Test
    void given_params_when_ruleValue_is_instantiated_with_id_then_id_is_populated() {
        var value1 = GenericBuilder.of(RuleValue::new)
                .with(RuleValue::setOperand, "test")
                .with(RuleValue::setId, 2L).build();
        assertThat(value1).isNotNull();
        assertThat(value1.getOperand()).isEqualTo("test");
        assertThat(value1.getId()).isEqualTo(2);
        assertThat(value1.getCreated()).isNull();
        assertThat(value1.getUpdated()).isNull();
        assertThat(value1.getVersion()).isEqualTo(0);
    }

    @Test
    void given_params_when_ruleValue_is_instantiated_with_created_then_created_is_populated() {
        var value1 = GenericBuilder.of(RuleValue::new)
                .with(RuleValue::setOperand, "test")
                .with(RuleValue::setId, 2L)
                .with(RuleValue::setCreated, LocalDateTime.now())
                .build();
        assertThat(value1).isNotNull();
        assertThat(value1.getOperand()).isEqualTo("test");
        assertThat(value1.getId()).isEqualTo(2);
        assertThat(value1.getCreated()).isNotNull();
        assertThat(value1.getUpdated()).isNull();
        assertThat(value1.getVersion()).isEqualTo(0);
    }

    @Test
    void given_params_when_ruleValue_is_instantiated_with_updated_then_updated_is_populated() {
        var value1 = GenericBuilder.of(RuleValue::new)
                .with(RuleValue::setOperand, "test")
                .with(RuleValue::setId, 2L)
                .with(RuleValue::setCreated, LocalDateTime.now())
                .with(RuleValue::setUpdated, LocalDateTime.now().plusDays(5))
                .build();
        assertThat(value1).isNotNull();
        assertThat(value1.getOperand()).isEqualTo("test");
        assertThat(value1.getId()).isEqualTo(2);
        assertThat(value1.getCreated()).isNotNull();
        assertThat(value1.getUpdated()).isNotNull();
        assertThat(value1.getVersion()).isEqualTo(0);
    }
}
