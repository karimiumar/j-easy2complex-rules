/*
 * The MIT License
 *
 *  Copyright (c) 2020, Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 *
 *  Original @author: Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
package com.umar.apps.rule.api;

import java.util.*;

/**
 * 
 * Original @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 * 
 * Modified By: @author Mohammad Umar Ali Karimi (karimiumar@gmail.com)
 */
public class Facts {

    private final Set<Fact<?>> facts = new HashSet<>();

    /**
     * Registers a new {@link Fact}. If a {@link Fact} with the provided factName 
     * is already registered then it is first replaced with the incoming fact.
     * 
     * @param <T> The type of value of the {@link Fact}
     * @param factName The name of the fact. It cannot be null.
     * @param value The value of the fact. It cannot be null.
     */
    public <T> void put(String factName, T value) {
        Objects.requireNonNull(factName, "Fact name cannot be null");
        Objects.requireNonNull(value, "Fact value cannot be null");
        var optionalFact = getFact(factName);
        optionalFact.ifPresent(facts::remove);//remove the fact from facts set
        add(factName, value);
    }

    /**
     * Retrieves a {@link Fact} from the provided index. The method will
     * throw an {@link ArrayIndexOutOfBoundsException} for an invalid index.
     * 
     * @param index An integer value of the index position
     * @return Returns a {@link Fact}
     */
    public Fact<?> getFact(int index) {
        Object [] objects = facts.toArray();
        return (Fact<?>) objects[index];
    }
    
    /**
     * Gets a {@link Set} of {@link Fact} registered or an empty {@link Set}
     * 
     * @return Returns registered {@link Fact}
     */
    public Set<Fact<?>> getFacts() {
        return facts;
    }

    /**
     * Returns an instance of {@link Iterator}
     * 
     * @return Returns an instance of {@link Iterator}
     */
    public Iterator<Fact<?>> iterator() {
        return facts.iterator();
    }

    /**
     * Clears all the registered {@link Fact}
     */
    public void clear() {
        facts.clear();
    }

    /**
     * Checks whether the registered {@link Fact} is empty or not.
     * 
     * @return Returns whether registered {@link Fact} is empty.
     */
    public boolean isEmpty() {
        return facts.isEmpty();
    }

    /**
     * Provides the size of the registered {@link Fact}
     * 
     * @return Returns the size of the registered {@link Fact}
     */
    public int size() {
        return facts.size();
    }

    private <T> void add(String factName, T value) {
        var fact = new Fact<>(factName, value);
        facts.add(fact);
    }

    private Optional<Fact<?>> getFact(String factName) {
        return facts.stream().filter(fact -> fact.name().equals(factName)).findFirst();
    }

    @Override
    public String toString() {
        Iterator<Fact<?>> iterator = iterator();
        StringBuilder builder = new StringBuilder("[");
        while (iterator.hasNext()){
            builder.append(iterator.next().toString());
            if (iterator.hasNext()){
                builder.append(",");
            }
        }
        builder.append("]");
        return builder.toString();
    }
}
