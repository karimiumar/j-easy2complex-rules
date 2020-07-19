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

import com.umar.apps.rule.api.Action;
import com.umar.apps.rule.api.Condition;
import com.umar.apps.rule.api.Facts;

import java.util.List;
import java.util.Set;

public class DefaultRule extends BasicRule {

    private final Set<Condition> conditions;
    private final List<Action> actions;

    DefaultRule(String name, String description, int priority, Set<Condition> conditions, List<Action> actions) {
        super(name,description, priority);
        this.actions = actions;
        this.conditions = conditions;
    }

    @Override
    public boolean evaluate(Facts facts) {
        boolean result = false;
        int index = 0;
        int size = facts.size();
        for (Condition condition: conditions) {
            if(index >=size) break;
            result = condition.evaluate(facts.getFact(index++));
            if(!result) break;
        }
        return result;
    }

    @Override
    public void execute(Facts facts) throws Exception {
        for(Action action: actions) {
            action.execute(facts);
        }
    }
}
