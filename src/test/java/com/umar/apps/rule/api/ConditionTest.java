package com.umar.apps.rule.api;

import org.junit.jupiter.api.Test;

import static com.umar.apps.rule.api.Condition.holds;
import static org.assertj.core.api.Assertions.assertThat;

public class ConditionTest {

    @Test
    void when_both_conditions_met_then_and_returns_true() {
        var str = "A String";
        var c1 = holds(fact -> !((String) fact.value()).isBlank(), "Incoming String is blank");
        var c2 = holds(fact -> fact.value().equals("A String"), "The text is not same as `A String`");
        var result = c1.and(c2).evaluate(new Fact<>("Test", str));
        assertThat(result.isValid()).isTrue();
        assertThat(result.getReason()).isEmpty();
    }

    @Test
    void when_only_one_condition_then_and_returns_false() {
        var str = "A tring";
        var c1 = holds(fact -> !((String) fact.value()).isBlank(), "Incoming String is blank");
        var c2 = holds(fact -> fact.value().equals("A String"),"The text is not same as `A String`");
        var result = c1.and(c2).evaluate(new Fact<>("Test", str));
        assertThat(result.isValid()).isFalse();
        assertThat(result.getReason()).as(() -> "The text is not same as `A String`");
    }

    @Test
    void whenEitherThenTrue() {
        var str = "A Ring";
        var a = holds(fact -> ((String) fact.value()).isBlank(), "Incoming String is blank");
        var b = holds(fact -> fact.value().equals("A Ring"), "The text is not same as `A Ring`");
        var result = a.or(b).evaluate(new Fact<>("fact", str));
        assertThat(result.isValid()).isTrue();
        assertThat(result.getReason()).isEmpty();
    }

    @Test
    void whenNeitherThenFalse() {
        var str = "A String";
        var a = holds(fact -> ((String) fact.value()).startsWith("Zeb"), "String doesn't start with Zeb");
        var b = holds(fact -> ((String) fact.value()).endsWith("Abc"), "String doesn't end with Abc" );
        var result = a.or(b).evaluate(new Fact<>("fact", str));
        assertThat(result.isValid()).isFalse();
        assertThat(result.getReason()).as(() -> "String doesn't end with Abc");
    }

    @Test
    void whenNotThenComplement() {
        var str = "A String";
        var a = holds(fact -> ((String) fact.value()).startsWith("Str"), "Doesn't matter....");
        var result = a.not().evaluate(new Fact<>("fact", str));
        assertThat(result.isValid()).isTrue();//result complemented
        assertThat(result.getReason()).isEmpty();
    }

    @Test
    void whenNotThenComplement2() {
        var str = "A String";
        var a = holds(fact -> fact.value().equals("A String"), "Doesn't matter....");
        var result = a.not().evaluate(new Fact<>("fact", str));
        assertThat(result.isValid()).isFalse();//result complemented
        assertThat(result.getReason()).as(() -> "Complemented the result");
    }
}
