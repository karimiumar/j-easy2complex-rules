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
 *  This class encapsulates a set of facts and represents a facts namespace.
 *  Facts have unique names within a <code>Facts</code> object.
 */
public class Facts implements Iterable<Fact<?>> {

    private final Set<Fact<?>> facts = new HashSet<>();

    /**
     * Add a fact, replacing any fact with the same name.
     *
     * @param factName of the fact to add, must not be null
     * @param value of the fact to add, must not be null
     */
    public <T> void put(String factName, T value) {
        Objects.requireNonNull(factName, "Fact name cannot be null");
        Objects.requireNonNull(value, "Fact value cannot be null");
        Fact<?> retrievedFact = getFact(factName);
        if(null != retrievedFact) {
            remove(factName);
        }
        add(new Fact<>(factName, value));
    }

    /**
     * Add a fact, replacing any fact with the same name.
     *
     * @param fact to add, must not be null
     */
    public <T> void add(Fact<T> fact) {
        Objects.requireNonNull(fact, "Fact cannot be null");
        Fact<?> retrievedFact = getFact(fact.getName());
        if(null != retrievedFact) {
            remove(retrievedFact);
        }
        facts.add(fact);
    }

    /**
     * Remove a fact.
     *
     * @param fact to remove, must not be null
     */
    public <T> void remove(Fact<T> fact) {
        Objects.requireNonNull(fact, "Fact cannot be null");
        facts.remove(fact);
    }

    /**
     * Remove a fact by name.
     *
     * @param factName name of the fact to remove, must not be null
     */
    public void remove(String factName){
        Objects.requireNonNull(factName, "Fact name cannot be null");
        Fact<?> factToRemove = getFact(factName);
        if(null != factToRemove){
            facts.remove(factToRemove);
        }
    }

    /**
     * Get the value of a fact by its name. This is a convenience method provided
     * as a short version of {@code getFact(factName).getValue()}.
     *
     * @param factName name of the fact, must not be null
     * @param <T> type of the fact's value
     * @return the value of the fact having the given name, or null if there is
     * no fact with the given name
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(String factName) {
        Objects.requireNonNull(factName, "Fact name cannot be null");
        Fact<?> fact = getFact(factName);
        if(null != fact) {
            return (T) fact.getValue();
        }
        return null;
    }

    /**
     * Get a fact by name.
     *
     * @param factName name of the fact, must not be null
     * @return the fact having the given name, or null if there is no fact with the given name
     */
    public Fact<?> getFact(String factName) {
        Objects.requireNonNull(factName, "Fact name cannot be null");
        return facts.stream().filter(fact -> fact.getName().equalsIgnoreCase(factName)).findFirst().orElse(null);
    }

    public Fact<?> getFact(int index) {
        Object[] objects = facts.toArray();
        return (Fact<?>) objects[index];
    }

    /**
     * Return a copy of the facts as a map. It is not intended to manipulate
     * facts outside of the rules engine (aka other than manipulating them through rules).
     *
     * @return a copy of the current facts as a {@link HashMap}
     */
    public Map<String, Object> asMap() {
        Map<String, Object> map = new HashMap<>();
        for(Fact<?> fact: facts) {
            map.put(fact.getName(), fact.getValue());
        }
        return map;
    }

    public int size() {
        return facts.size();
    }

    /**
     * Return an iterator on the set of facts. It is not intended to remove
     * facts using this iterator outside of the rules engine (aka other than doing it through rules)
     *
     * @return an iterator on the set of facts
     */
    @Override
    public Iterator<Fact<?>> iterator() {
        return facts.iterator();
    }

    /**
     * Clear the facts
     */
    public void clear(){
        facts.clear();
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
