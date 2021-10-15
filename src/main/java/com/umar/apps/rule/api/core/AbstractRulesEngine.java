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

import com.umar.apps.rule.api.RuleEngineListener;
import com.umar.apps.rule.api.RuleListener;
import com.umar.apps.rule.api.RulesEngine;
import com.umar.apps.rule.api.RulesEngineParameters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *  Base class for {@link RulesEngine} implementations
 *
 *  @author: Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public abstract class AbstractRulesEngine implements RulesEngine {

    RulesEngineParameters parameters;
    List<RuleListener> ruleListeners;
    List<RuleEngineListener> ruleEngineListeners;

    AbstractRulesEngine() {
        this(new RulesEngineParameters());
    }

    AbstractRulesEngine(RulesEngineParameters rulesEngineParameters) {
        this.parameters = rulesEngineParameters;
        this.ruleListeners = new ArrayList<>();
        this.ruleEngineListeners = new ArrayList<>();
    }

    /**
     * Return a copy of the rules engine parameters.
     *
     * @return copy of the rules engine parameters
     */
    @Override
    public RulesEngineParameters getParameters() {
        return new RulesEngineParameters(
                parameters.isSkipOnFirstAppliedRule(),
                parameters.isSkipOnFirstFailedRule(),
                parameters.isSkipOnFirstNonTriggeredRule(),
                parameters.getPriorityThreshold()
        );
    }

    /**
     * Return an unmodifiable list of the registered rule listeners.
     * @return an unmodifiable list of the registered rule listeners
     */
    @Override
    public List<RuleListener> getRuleListeners() {
        return Collections.unmodifiableList(ruleListeners);
    }

    /**
     * Returns an unmodifiable list of the registered engine rule listeners.
     * @return an unmodifiable list of the registered engine rule listeners
     */
    @Override
    public List<RuleEngineListener> getRuleEngineListeners() {
        return Collections.unmodifiableList(ruleEngineListeners);
    }

    public void registerRuleListener(RuleListener ruleListener) {
        ruleListeners.add(ruleListener);
    }

    public void registerRulesEngineListener(RuleEngineListener ruleEngineListener) {
        ruleEngineListeners.add(ruleEngineListener);
    }

    public void registerRuleListeners(List<RuleListener> ruleListeners) {
        this.ruleListeners.addAll(ruleListeners);
    }

    public void registerRulesEngineListeners(List<RuleEngineListener> ruleEngineListeners) {
        this.ruleEngineListeners.addAll(ruleEngineListeners);
    }
}
