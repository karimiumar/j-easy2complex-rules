package com.umar.apps.rule.api;

import java.util.function.Function;
import java.util.function.Predicate;

import static com.umar.apps.rule.api.Result.*;

/**
 * A {@link Condition2} can be defined as given, which will then be used to evaluate a Fact.
 *
 * {@code
 *  var condition1 = holds(fact -> !((String) fact.value()).isBlank(), "Incoming string is blank");
 *  var condition2 = holds(fact -> fact.value().equals("A String"), "Incoming string is not `A String`");
 *  var result = c1.and(c2).apply(new Fact<>("Test", "A String"));
 *  assertThat(result.isValid()).isTrue();
 *  assertThat(result.getReason()).isEmpty();
 * }
 *
 */
public interface Condition2 extends Function<Fact<?>, Result> {

    Condition2 FALSE = fact -> invalid("FALSE");

    Condition2 TRUE = fact -> valid();

    /**
     * Comes handy to create {@code Condition2} of {@code Predicate<Fact>} from caller. For example,
     *
     * {@code
     *   var stringNotBlankCondition = holds(fact -> !((String) fact.value()).isBlank(), "Incoming String is blank");
     * }
     * @param predicate A {@code Predicate} of {@code Fact}
     * @param message A message held by {@code Result} when the {@code Fact} fails validation
     * @return Returns a {@code Condition2}
     */
    static Condition2 holds(Predicate<Fact<?>> predicate, String message) {
        return fact -> predicate.test(fact) ? valid() : invalid(message);
    }

    /**
     * Represents the Logical AND
     *
     * @param other The other Condition to apply to Logical AND
     * @return Returns a {@code Condition2}
     */
    default Condition2 and(Condition2 other) {
        return fact -> {
            var result = this.apply(fact);
            return result.isValid() ? other.apply(fact) : result;
        };
    }

    /**
     * Represents Logical OR
     * @param other The other Condition to apply to Logical OR
     * @return Returns a {@code Condition2}
     */
    default Condition2 or(Condition2 other) {
        return fact -> {
            var result = this.apply(fact);
            return result.isValid() ? result : other.apply(fact);
        };
    }

    /**
     * The logical NOT. It complements the result.
     * And stores `Complemented the result` message for {@code Result.Invalid } results
     * @return Returns a {@code Condition2}.
     */
    default Condition2 not() {
        return fact -> {
            var result = this.apply(fact);
            return result.isValid()? invalid("Complemented the result") : valid();
        };
    }
}
