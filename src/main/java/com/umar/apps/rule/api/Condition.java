package com.umar.apps.rule.api;

import java.util.Objects;

/**
 * An interface representing a Rule Condition
 *
 * @author Mohammad Umar Ali Karimi (karimiumar@gmail.com)
 */
public interface Condition {

    Condition FALSE = facts->false;

    Boolean evaluate(Fact<?> fact);
    /**
     * The AND operation applied on facts. Rule will only be applicable
     * when both facts are met.
     *
     * @param other The other fact to compare to
     * @return Returns an && {@link Condition}
     */
    default Condition and(Condition other) {
        Objects.requireNonNull(other, "Condition to be compared to is null");
        return fact-> this.evaluate(fact) && other.evaluate(fact);
    }

    /**
     * The OR operation applied on facts. Rule will be applied if any of the
     * facts is met.
     *
     * @param other The other fact to compare to
     * @return Returns an || {@link Condition}
     */
    default Condition or(Condition other) {
        Objects.requireNonNull(other, "Condition to be compared to is null");
        return fact-> this.evaluate(fact) || other.evaluate(fact);
    }

    /**
     * The NOT operation applied on a fact. Reverses the result of the fact.
     *
     * @return Returns a ! {@link Condition}
     */
    default Condition not() {
        return fact -> !this.evaluate(fact);
    }
}
