package com.umar.apps.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A GenericBuilder for Builder pattern
 * 
 * Taken from stackoverflow.com
 * 
 * @author 
 * 
 * @param <T> The type of Object to build
 */
public class GenericBuilder<T> {

    private final Supplier<T> instantiator;

    private final List<Consumer<T>> instanceModifiers = new ArrayList<>();

    public GenericBuilder(Supplier<T> instantiator) {
        this.instantiator = instantiator;
    }

    public static <T> GenericBuilder<T> of(Supplier<T> instantiator) {
        return new GenericBuilder<>(instantiator);
    }

    public <U> GenericBuilder<T> with(BiConsumer<T, U> consumer, U value) {
        Consumer<T> c = instance -> consumer.accept(instance, value);
        instanceModifiers.add(c);
        return this;
    }

    public T build() {
        T value = instantiator.get();
        instanceModifiers.forEach(modifier -> modifier.accept(value));
        instanceModifiers.clear();
        return value;
    }

    /*private void verifyPredicates(T value) {
        List<Predicate<T>> violated = predicates.stream()
                .filter(e -> !e.test(value)).collect(Collectors.toList());
        if (!violated.isEmpty()) {
            throw new IllegalStateException(value.toString()
                    + " violates predicates " + violated);
        }
    }*/
}

/**
 * Person value = GenericBuilder.of(Person::new)
 *             .with(Person::setName, "Otto").with(Person::setAge, 5).build();
 */
