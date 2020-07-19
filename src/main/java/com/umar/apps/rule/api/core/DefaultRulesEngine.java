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

import java.util.HashMap;
import java.util.Map;

public final class DefaultRulesEngine implements RulesEngine {

    @Override
    public void fire(Rules rules, Facts facts) {
        doFire(rules, facts);
    }

    void doFire(Rules rules, Facts facts) {
        if(rules.isEmpty()) return;

        for(Rule rule: rules) {
            final String name = rule.getName();
            final int priority = rule.getPriority();
            if(!shouldBeEvaluated(rule, facts)) continue;
            boolean evaluationResult = true;
            try {
                evaluationResult = rule.evaluate(facts);
            }catch (RuntimeException e) {
                e.printStackTrace();
            }
            if(evaluationResult) {
                try {
                    rule.execute(facts);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public Map<Rule, Boolean> check(Rules rules, Facts facts) {
        return doCheck(rules, facts);
    }

    private Map<Rule, Boolean> doCheck(Rules rules, Facts facts) {
        Map<Rule, Boolean> result = new HashMap<>();
        for (Rule rule: rules) {
            if(shouldBeEvaluated(rule, facts)){
                result.put(rule, rule.evaluate(facts));
            }
        }
        return result;
    }

    private boolean shouldBeEvaluated(Rule rule, Facts facts) {
        return true;
    }
}
