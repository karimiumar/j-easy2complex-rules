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
import com.umar.apps.rule.api.Result;
import com.umar.apps.rule.api.Rule;

import java.util.Objects;

/**
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class BasicRule implements Rule {

    protected Long id = 0L;
    protected String name;
    protected String description;
    protected int priority;


    public BasicRule() {
        this(Rule.DEFAULT_NAME, Rule.DEFAULT_DESC, Rule.DEFAULT_PRIORITY);
    }

    public BasicRule(String name, String description, int priority) {
        this.name = name;
        this.description = description;
        this.priority = priority;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public Result evaluate(Facts facts) {
        return Result.invalid("FALSE");
    }

    @Override
    public void execute(Facts facts) {

    }

    @Override
    public int compareTo(Rule rule) {
        if(getPriority() < rule.getPriority()) return -1;
        else if(getPriority() > rule.getPriority()) return 1;
        return getName().compareTo(rule.getName());
    }

    /*
     * Rules are unique according to their names within a rules engine registry.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasicRule basicRule = (BasicRule) o;
        return priority == basicRule.priority && Objects.equals(name, basicRule.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, priority);
    }
}
