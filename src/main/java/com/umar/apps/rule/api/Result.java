package com.umar.apps.rule.api;

import java.util.Optional;

/**
 * A {@link Result} holds the reason for it. 
 * A {@link Result} can either be an instance of {@link Valid}
 * or {@link Invalid}.
 * 
 * @author Mohammad Umar Ali Karimi (karimiumar@gmail.com)
 */
public sealed interface Result permits Result.Invalid, Result.Valid {

    static Result valid() {
        return Valid.valid();
    }

    static Result invalid(String reason) {
        return new Invalid(reason);
    }

    Optional<String> getReason();
    boolean isValid();

    record Invalid(String reason) implements Result {

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public Optional<String> getReason() {
            return Optional.of(reason);
        }
    }

    record Valid() implements Result {
        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public Optional<String> getReason() {
            return Optional.empty();
        }

        static Result valid() {
            return new Valid();
        }
    }
}
