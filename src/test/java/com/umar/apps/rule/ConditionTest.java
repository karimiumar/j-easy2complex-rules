package com.umar.apps.rule;

import com.umar.apps.rule.api.Condition;
import com.umar.apps.rule.api.Fact;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConditionTest {

    private static Boolean evaluate(Fact<?> fact) {
        if (fact.getValue() instanceof String string) {
            return string.contains("tr");
        }
        return false;
    }

    @Test
    public void whenBothThenTrue() {
        String str = "A string";
        Condition a = fact -> null != fact.getValue();
        Condition b = ConditionTest::evaluate;
        var result = a.and(b).evaluate(new Fact<>("fact", str));
        assertTrue(result);
    }

    @Test
    public void whenOneThenFalse() {
        String str = "A Ring";
        Condition a = fact -> null != fact.getValue();
        Condition b = ConditionTest::evaluate;
        var result = a.and(b).evaluate(new Fact<>("fact", str));
        assertFalse(result);
    }
}
