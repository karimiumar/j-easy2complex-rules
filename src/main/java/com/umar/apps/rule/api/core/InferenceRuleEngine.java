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

import com.umar.apps.rule.api.*;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * A {@link RulesEngine} implementation. It evaluates {@link Facts}
 * for a given {@link Rule} and delegates the selected rules to 
 * {@link DefaultRulesEngine} for evaluating a {@link Rule}
 * 
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class InferenceRuleEngine extends AbstractRulesEngine {

    private final DefaultRulesEngine delegate;

    public InferenceRuleEngine() {
        delegate = new DefaultRulesEngine();
    }

    @Override
    public void fire(Rules rules, Facts facts) {
        Set<Rule> selectedRules = selectCandidates(rules, facts);
        selectedRules.forEach(rule -> delegate.fire(new Rules(rule), facts));
    }

    private Set<Rule> selectCandidates(Rules rules, Facts facts) {
        final Set<Rule> candidates = new TreeSet<>();
        for (Rule rule: rules.getRules()) {
            if(rule.evaluate(facts).isValid()){
                candidates.add(rule);
            }
        }
        return candidates;
    }
    @Override
    public Map<Rule, Result> check(Rules rules, Facts facts) {
        return delegate.check(rules, facts);
    }
}
