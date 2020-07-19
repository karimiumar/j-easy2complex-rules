package com.umar.apps.rule.api;

import com.umar.apps.rule.engine.WorkflowItem;

@FunctionalInterface
public interface RuleAction {
    void apply(WorkflowItem workflowItem);
}
