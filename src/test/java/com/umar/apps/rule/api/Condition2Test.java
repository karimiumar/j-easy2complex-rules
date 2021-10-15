package com.umar.apps.rule.api;

import org.junit.jupiter.api.Test;

import static com.umar.apps.rule.api.Condition2.holds;
import static org.assertj.core.api.Assertions.assertThat;

public class Condition2Test {

    @Test
    void when_both_conditions_met_then_and_returns_true() {
        var str = "A String";
        var c1 = holds(fact -> !((String) fact.value()).isBlank(), "Incoming string is blank");
        var c2 = holds(fact -> fact.value().equals("A String"), "Incoming string is not `A String`");
        var result = c1.and(c2).apply(new Fact<>("Test", str));
        assertThat(result.isValid()).isTrue();
        assertThat(result.getReason()).isEmpty();
    }

    @Test
    void when_only_one_condition_then_and_returns_false() {
        var str = "A tring";
        var c1 = holds(fact -> !((String) fact.value()).isBlank(), "Incoming string is blank");
        var c2 = holds(fact -> fact.value().equals("A String"), "Incoming string is not `A String`");
        var result = c1.and(c2).apply(new Fact<>("Test", str));
        assertThat(result.isValid()).isFalse();
        assertThat(result.getReason()).as(() -> "Incoming string is not `A String`");
    }

    @Test
    void whenEitherThenTrue() {
        var str = "A Ring";
        var a = holds(fact -> ((String) fact.value()).isBlank(), "Incoming string is blank");
        var b = holds(fact -> fact.value().equals("A Ring"), "Incoming string is not `A Ring`");
        var result = a.or(b).apply(new Fact<>("fact", str));
        assertThat(result.isValid()).isTrue();
        assertThat(result.getReason()).isEmpty();
    }

    @Test
    void whenNeitherThenFalse() {
        var str = "A Zeb Abc String";
        var a = holds(fact -> ((String) fact.value()).startsWith("Zeb"), "Incoming string doesn't start with `Zeb`");
        var b = holds(fact -> ((String) fact.value()).endsWith("Abc"), "Incoming string doesn't end with `Abc`");
        var result = a.or(b).apply(new Fact<>("fact", str));
        assertThat(result.isValid()).isFalse();
        assertThat(result.getReason()).as(() -> "Incoming string doesn't end with `Abc`");
    }

    @Test
    void whenNotThenComplement() {
        var str = "A String";
        var a = holds(fact -> ((String) fact.value()).startsWith("Str"), "Incoming string doesn't start with `Str`");
        var result = a.not().apply(new Fact<>("fact", str));
        assertThat(result.isValid()).isTrue();//result complemented
        assertThat(result.getReason()).as(() -> "");
    }
}
