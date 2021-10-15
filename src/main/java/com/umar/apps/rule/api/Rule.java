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

/**
 * Abstraction for a rule that can be fired by a rules engine.
 *
 * Rules are registered in a namespace of a rule of type {@link Rules}
 * in which they must have a <strong>unique</strong> name.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public interface Rule extends Comparable<Rule>{
    String DEFAULT_NAME = "rule";
    String DEFAULT_DESC = "A Rule";
    Integer DEFAULT_PRIORITY = Integer.MAX_VALUE - 1;

    Long getId();
    String getName();
    String getDescription();
    int getPriority();

    /**
     * This method implements the rule's condition(s).
     * <strong>Implementations should handle any runtime exception and return {@code Result.valid()} or {@code Result.invalid()}  accordingly</strong>
     *
     * @return {@code Result.valid()} if the rule should be applied given the provided facts, {@code Result.invalid()} otherwise
     */
    Result evaluate(Facts facts);

    /**
     * This method implements the rule's action(s).
     */
    void execute(Facts facts);
}
