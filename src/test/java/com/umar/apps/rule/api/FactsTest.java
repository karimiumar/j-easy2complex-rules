package com.umar.apps.rule.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FactsTest {

    @Test
    void when_facts_is_empty_then_true() {
        var facts = new Facts();
        assertThat(facts.isEmpty()).isTrue();
    }

    @Test
    void when_a_fact_is_added_then_size_is_1(){
        var facts = new Facts();
        facts.put("Test", 1);
        assertThat(facts.size()).isEqualTo(1);
    }

    @Test
    void when_a_fact_is_added_twice_then_size_is_1(){
        var facts = new Facts();
        facts.put("Test", 1);
        assertThat(facts.size()).isEqualTo(1);
        facts.put("Test", 1);
        assertThat(facts.size()).isEqualTo(1);
    }

    @Test
    void when_a_fact_is_added_twice_with_new_value_then_new_value_is_retained(){
        var facts = new Facts();
        facts.put("Test", 1);
        assertThat(facts.size()).isEqualTo(1);
        facts.put("Test", 2);
        assertThat(facts.size()).isEqualTo(1);
        assertThat(facts.getFact(0).name()).isEqualTo("Test");
        assertThat(facts.getFact(0).value()).isEqualTo(2);
    }

    @Test
    void when_facts_is_cleared_then_empty() {
        var facts = new Facts();
        facts.put("Test", 1);
        facts.clear();
        assertThat(facts.size()).isEqualTo(0);
    }

    @Test
    void when_invalid_index_is_passed_to_getFact_then_throws_exception() {
        var facts = new Facts();
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> facts.getFact(1));
    }
}
