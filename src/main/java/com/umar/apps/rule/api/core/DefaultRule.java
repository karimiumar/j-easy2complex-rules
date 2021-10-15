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
 * Original @author: Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
package com.umar.apps.rule.api.core;

import com.umar.apps.rule.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * An implementation of {@link Rule}
 *
 * Original @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 * 
 * Modified By @author: Mohammad Umar Ali Karimi(karimiumar@gmail.com)
 */
public class DefaultRule extends BasicRule {

    private final Set<Condition> conditions;
    private final List<Action> actions;
    private final Comparator<Rule> comparator;

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRule.class.getName());

    DefaultRule(String name, String description, int priority, Comparator<Rule> comparator, Set<Condition> conditions, List<Action> actions) {
        super(name, description, priority);
        this.actions = actions;
        this.conditions = conditions;
        this.comparator = comparator;
    }

    @Override
    public Result evaluate(Facts facts) {
        var result = Result.invalid("FALSE");
        for (Condition condition: conditions) {
            for(var fact: facts.getFacts()) {
                result = evaluate(condition, fact);
            }
            if(!result.isValid()) {
                break;
            }
        }
        return result;
    }

    private Result evaluate(Condition condition, Fact<?> fact) {
        var result = condition.evaluate(fact);
        LOGGER.debug("Evaluation Result isValid = {}, reason='{}' for Fact '{}'", result.isValid(), result.getReason(), fact);
        return result;
    }

    @Override
    public void execute(Facts facts) {
        actions.forEach(action -> action.execute(facts));
    }

    @Override
    public int compareTo(Rule rule) {
        return comparator.compare(this, rule);
    }

    @Override
    public String toString() {
        return "DefaultRule{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", priority=" + priority +
                '}';
    }
}
