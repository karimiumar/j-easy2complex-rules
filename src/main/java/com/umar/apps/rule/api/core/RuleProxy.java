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

import com.umar.apps.rule.api.Rule;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class RuleProxy implements InvocationHandler {

    private String name;
    private Integer priority;
    private Method [] methods;
    private Method compareToMethod;
    private final Object target;

    public static Rule asRule(final Object rule) {
        Rule result;
        if(rule instanceof Rule){
            result = (Rule) rule;
        } else {
            result = (Rule) Proxy.newProxyInstance(Rule.class.getClassLoader()
                    , new Class[] {Rule.class, Comparable.class}
                    , new RuleProxy(rule)
            );
        }
        return result;
    }

    private RuleProxy(final Object target){
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        return switch (methodName) {
            case "getName" -> getRuleName();
            case "getPriority" -> getRulePriority();
            case "compareTo" -> compareToMethod(args);
            default -> null;
        };
    }

    public Object getTarget() {
        return target;
    }

    private Object compareToMethod(final Object[] args) throws Exception {
        Method compareToMethod = getCompareToMethod();
        Object otherRule = args[0]; // validated upfront
        if (compareToMethod != null && Proxy.isProxyClass(otherRule.getClass())) {
            if (compareToMethod.getParameters().length != 1) {
                throw new IllegalArgumentException("compareTo method must have a single argument");
            }
            RuleProxy ruleProxy = (RuleProxy) Proxy.getInvocationHandler(otherRule);
            return compareToMethod.invoke(target, ruleProxy.getTarget());
        } else {
            return compareTo((Rule) otherRule);
        }
    }

    private Method getCompareToMethod() {
        if (this.compareToMethod == null) {
            Method[] methods = getMethods();
            for (Method method : methods) {
                if (method.getName().equals("compareTo")) {
                    this.compareToMethod = method;
                    return this.compareToMethod;
                }
            }
        }
        return this.compareToMethod;
    }

    private Class<?> getTargetClass() {
        return target.getClass();
    }

    private Method[] getMethods() {
        if (this.methods == null) {
            this.methods = getTargetClass().getMethods();
        }
        return this.methods;
    }

    private int compareTo(final Rule otherRule) {
        int otherPriority = otherRule.getPriority();
        int priority = getRulePriority();
        if (priority < otherPriority) {
            return -1;
        } else if (priority > otherPriority) {
            return 1;
        } else {
            String otherName = otherRule.getName();
            String name = getRuleName();
            return name.compareTo(otherName);
        }
    }

    private String getRuleName() {
        if (this.name == null) {
            this.name = Rule.DEFAULT_NAME;
        }
        return this.name;
    }

    private int getRulePriority() {
        if (this.priority == null) {
            this.priority = Rule.DEFAULT_PRIORITY;
        }
        return this.priority;
    }
}
