package com.umar.apps.rule.api;

import java.util.function.Predicate;

import static com.umar.apps.rule.api.Result.*;
/**
 * An interface representing a Condition
 * 
 * A {@link Condition} can be defined as given, which will then be used to evaluate a Fact.
 *
 * {@code
 *  var condition1 = holds(fact -> !((String) fact.value()).isBlank(), "Incoming string is blank");
 *  var condition2 = holds(fact -> fact.value().equals("A String"), "Incoming string is not `A String`");
 *  var result = c1.and(c2).apply(new Fact<>("Test", "A String"));
 *  assertThat(result.isValid()).isTrue();
 *  assertThat(result.getReason()).isEmpty();
 * }
 * 
 * Original @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 *
 * Modified by @author Mohammad Umar Ali Karimi (karimiumar@gmail.com)
 */
public interface Condition {

    Condition FALSE = facts -> invalid("FALSE");

    Result evaluate(Fact<?> fact);

    /**
     * Comes handy to create {@code Condition} of {@code Predicate<Fact>} from caller. For example,
     *
     * {@code
     *   var stringNotBlankCondition = holds(fact -> !((String) fact.value()).isBlank(), "Incoming String is blank");
     * }
     * @param predicate A {@code Predicate} of {@code Fact}
     * @param message A message held by {@code Result} when the {@code Fact} fails validation
     * @return Returns a {@code Condition}
     */
    static Condition holds(Predicate<Fact<?>> predicate, String message) {
        return fact -> predicate.test(fact) ? valid() : invalid(message);
    }

    /**
     * Represents the Logical AND
     *
     * @param other The other Condition to apply to Logical AND
     * @return Returns a {@code Condition}
     */
    default Condition and(Condition other) {
        return fact -> {
            var result = this.evaluate(fact);
            return result.isValid() ? other.evaluate(fact) : result;
        };

    }

    /**
     * Represents Logical OR
     * @param other The other Condition to apply to Logical OR
     * @return Returns a {@code Condition}
     */
    default Condition or(Condition other) {
        return fact -> {
            var result = this.evaluate(fact);
            return result.isValid() ? result : other.evaluate(fact);
        };
    }

    /**
     * The logical NOT. It complements the result.
     * And stores `Complemented the result` message for {@code Result.Invalid } results
     * @return Returns a {@code Condition}.
     */
    default Condition not() {
        return fact -> {
            var result = this.evaluate(fact);
            return result.isValid() ? invalid("Complemented the result"): valid() ;
        };
    }
}
