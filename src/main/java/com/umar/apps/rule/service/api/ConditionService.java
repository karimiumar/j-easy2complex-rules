package com.umar.apps.rule.service.api;

import com.umar.apps.rule.api.Condition;

public interface ConditionService {
    /**
     * Uses Java reflection API to get {@link Condition} based on given parameters.
     *
     * @param workflowItem The workflowItem instance
     * @param ruleName The rule name to be used for condition
     * @param ruleType The rule type to use
     * @param isActive Whether the rule being searched is active or not
     * @return Returns a {@link Condition}
     */
    <T> Condition getCondition(T workflowItem, String ruleName, String ruleType, boolean isActive);
}

