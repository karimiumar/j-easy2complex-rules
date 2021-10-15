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

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A RulesEngine interface
 * 
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public interface RulesEngine {

    /**
     * Return the rules engine parameters.
     *
     * @return The rules engine parameters
     */
    RulesEngineParameters getParameters();

    /**
     * Return the list of registered rule listeners.
     *
     * @return the list of registered rule listeners
     */
    default List<RuleListener> getRuleListeners() {
        return Collections.emptyList();
    }

    /**
     * Return the list of registered rule engine listeners.
     *
     * @return the list of registered rule engine listeners
     */
    default List<RuleEngineListener> getRuleEngineListeners() {
        return Collections.emptyList();
    }

    /**
     * Fire all registered rules on given facts
     * @param rules The rules registered
     * @param facts The facts
     */
    void fire(Rules rules, Facts facts);

    /**
     * Check rules without firing them.
     *
     * @param rules The rules registered.
     * @param facts The facts
     * @return a map with the result of evaluation of each result
     */
    default Map<Rule, Result> check(Rules rules, Facts facts) {
       return Collections.emptyMap();
    }
}
