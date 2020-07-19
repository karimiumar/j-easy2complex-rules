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

import com.umar.apps.rule.api.Facts;
import com.umar.apps.rule.api.Rule;
import com.umar.apps.rule.api.Rules;
import com.umar.apps.rule.api.RulesEngine;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class InferenceRuleEngine implements RulesEngine {

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
        for (Rule rule: rules) {
            if(rule.evaluate(facts)){
                candidates.add(rule);
            }
        }
        return candidates;
    }
    @Override
    public Map<Rule, Boolean> check(Rules rules, Facts facts) {
        return delegate.check(rules, facts);
    }
}
