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

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * A listener for rule execution events
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public interface RuleListener {

    Logger LOG = LoggerFactory.getLogger(RuleListener.class.getName());
    /**
     * Triggered before the evaluation of rule.
     *
     * @param rule Rule being evaluated.
     * @param facts Facts evaluating the rule
     * @return true if the rule should be evaluated, false otherwise
     */
    default boolean beforeEvaluate(Rule rule, Facts facts) {
        //LOG.warn("beforeEvaluate() Rule: " + rule + " is to be evaluated for facts: " + facts);
        return true;
    }

    /**
     * Triggered after the evaluation of rule.
     *
     * @param rule Rule that has been evaluated.
     * @param facts Facts known after the evaluation of rule
     */
    default void afterEvaluate(Rule rule, Facts facts) {
        //LOG.warn("afterEvaluate() Rule: " + rule + " evaluated for facts: " + facts);
    }


    /**
     * Triggered before execution of rule.
     *
     * @param rule Rule that is to be executed.
     * @param facts The Facts on which rule is to be executed.
     */
    default void beforeExecute(Rule rule, Facts facts) {
        //LOG.warn("beforeExecute() Rule: " + rule + " to be executed for facts: " + facts);
    }

    /**
     * Triggered after successful execution of rule.
     *
     * @param rule Rule that was successfully executed.
     * @param facts The Facts on which rule was executed.
     */
    default void onSuccess(Rule rule, Facts facts) {
        //LOG.warn("onSuccess() Rule: " + rule + " successfully executed for facts: " + facts);
    }

    /**
     * Triggered after a rule failed.
     *
     * @param rule Rule that failed to execute.
     * @param facts The Facts on which rule was executed.
     */
    default void onFailure(Rule rule, Facts facts) {
        //LOG.warn("onFailure() Rule: " + rule + " failed to execute for facts: " + facts);
    }
}
