package com.umar.apps.rule.service.api;

import com.umar.apps.rule.api.Condition;

import java.util.Set;

public interface NettingService {
    <T> Set<Condition> getNettingConditions(T workflowItem, String ruleName, String ruleType);
}
