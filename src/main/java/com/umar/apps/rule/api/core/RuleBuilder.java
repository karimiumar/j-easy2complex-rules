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
package com.umar.apps.rule.api.core;

import com.umar.apps.rule.api.Action;
import com.umar.apps.rule.api.Condition;
import com.umar.apps.rule.api.Rule;

import java.util.*;

/**
 * Builder to create {@link com.umar.apps.rule.api.Rule} instances.
 */
public class RuleBuilder {
    private String name = Rule.DEFAULT_NAME;
    private int priority = Rule.DEFAULT_PRIORITY;
    private final Set<Condition> conditions = new LinkedHashSet<>();
    private final List<Action> actions = new ArrayList<>();
    private final Comparator<Rule> comparator;

    public RuleBuilder(Comparator<Rule> comparator) {
        this.comparator = comparator;
    }

    /**
     * Sets the Rule name
     *
     * @param name The rule name
     * @return Returns the instance
     */
    public RuleBuilder name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the Rule priority
     *
     * @param priority The priority to set
     * @return Returns the instance
     */
    public RuleBuilder priority(int priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Set the rule condition
     *
     * @param condition The condition applicable to the Rule
     * @return Returns this instance
     */
    public RuleBuilder when(Condition condition) {
        this.conditions.add(condition);
        return this;
    }

    public RuleBuilder and(Condition condition) {
        this.conditions.add(condition);
        return this;
    }

    /**
     * The action to be taken by the Rule.
     *
     * @param action The action applicable
     * @return Returns this instance
     */
    public RuleBuilder then(Action action) {
        this.actions.add(action);
        return this;
    }

    /**
     * Create a new {@link Rule}
     *
     * @return Returns an instance of Rule
     */
    public Rule build() {
        return new DefaultRule(name, priority, comparator, conditions, actions);
    }
}
