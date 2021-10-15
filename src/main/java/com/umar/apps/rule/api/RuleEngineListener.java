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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A listener for rules engine execution events.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public interface RuleEngineListener {

    Logger LOG = LoggerFactory.getLogger(RuleEngineListener.class.getName());

    /**
     * Triggered before evaluating the rule set.
     * <strong>When this listener is used with {@link com.umar.apps.rule.api.core.InferenceRuleEngine},
     * this method will be triggered before the evaluation of each candidate rule set in each iteration
     * </strong>
     *
     * @param rules Rules to fire
     * @param facts Facts present before firing rules
     */
    default void beforeEvaluate(Rules rules, Facts facts) {
        //LOG.warn("beforeEvaluate() Rules: " + rules + " is to fire for facts: " + facts);
    }

    /**
     * Triggered after evaluating the rule set.
     * <strong>When this listener is used with {@link com.umar.apps.rule.api.core.InferenceRuleEngine},
     * this method will be triggered after the evaluation of each candidate rule set in each iteration
     * </strong>
     *
     * @param rules Rules fired
     * @param facts Facts present after firing rules
     */
    default void afterEvaluate(Rules rules, Facts facts) {
        //LOG.warn("afterEvaluate() Rules: " + rules + " fired for facts: " + facts);
    }
}
