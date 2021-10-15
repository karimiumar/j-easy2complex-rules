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

import com.umar.apps.rule.api.core.RuleProxy;

import java.util.*;

/**
 * Registers a set of {@link Rule} through {@link RuleProxy}
 * 
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class Rules {

    private final Set<Rule> rules = new TreeSet<>();

    /**
     * Creates a new {@link Rules} object.
     *
     * @param rules Rules to register
     */
    public Rules(Rule ... rules) {
        Collections.addAll(this.rules, rules);
    }

    /**
     * Creates a new {@link Rules} object.
     *
     * @param rules Rules to register
     */
    public Rules(Object ... rules) {
        this.register(rules);
    }

    /**
     * Register one or more new rules.
     *
     * @param rules to register, must not be null
     */
    public void register(Object ... rules){
        Objects.requireNonNull(rules, "rules cannot be null");
        for(Object rule: rules) {
            Objects.requireNonNull(rule, "Rule cannot be null");
            this.rules.add(RuleProxy.asRule(rule));
        }
    }

    /**
     * Check if the rule set is empty.
     *
     * @return true if the rule set is empty, false otherwise
     */
    public boolean isEmpty() {
        return rules.isEmpty();
    }

    /**
     * Clear rules.
     */
    public void clear() {
        rules.clear();
    }

    /**
     * Return how many rules are currently registered.
     *
     * @return the number of rules currently registered
     */
    public int size() {
        return rules.size();
    }

    public Iterator<Rule> iterator() {
        return rules.iterator();
    }

    private Optional<Rule> findByName(String ruleName){
        return rules.stream().filter(rule -> rule.getName().equalsIgnoreCase(ruleName)).findFirst();
    }

    public Set<Rule> getRules() {
        return rules;
    }

    @Override
    public String toString() {
        return "Rules{" +
                "rules=" + rules +
                '}';
    }
}
