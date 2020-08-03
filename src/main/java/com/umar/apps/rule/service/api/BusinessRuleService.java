package com.umar.apps.rule.service.api;

import com.umar.apps.rule.BusinessRule;
import com.umar.apps.rule.RuleAttribute;

import java.util.List;
import java.util.Map;

public interface BusinessRuleService {
    /**
     * Creates a {@link BusinessRule} for the given ruleName, ruleType. It first searches the db for the given ruleName and ruleType.
     * If one exists then returns it. Otherwise it creates a new {@link BusinessRule}.
     * While creating a new {@link BusinessRule}, it looksup db for {@link RuleAttribute}.
     * If no {@link RuleAttribute} exists for the given attributeName, attributeType and ruleType, then it creates a new instance of the
     * {@link RuleAttribute} and links it with {@link BusinessRule} instance. Otherwise uses the existing instance to link.
     *
     * It further looksup db for {@link com.umar.apps.rule.RuleValue} for the given operand. If none exists then it creates a new
     * {@link com.umar.apps.rule.RuleValue} and links with the parent {@link BusinessRule}. Otherwise, links the existing one with the parent.
     *
     *
     * @param ruleName The name of the Rule
     * @param ruleType The type of the Rule
     * @param priority The priority of the Rule
     */
    void createRule(String ruleName, String ruleType, int priority);

    /**
     * Creates a {@link RuleAttribute} for the given parameters and attaches it to the given {@link BusinessRule}
     * @param businessRule The {@link BusinessRule} to attach
     * @param attributeName The name of the {@link com.umar.apps.rule.engine.WorkflowItem} attribute
     * @param ruleType The type of the {@link BusinessRule}
     * @param displayName The canonical display name of the attribute
     */
    void createAttribute(BusinessRule businessRule, String attributeName, String ruleType, String displayName);

    /**
     * Creates a {@link com.umar.apps.rule.RuleValue} for the given params and attaches it to {@link RuleAttribute}
     * @param ruleAttribute The {@link RuleAttribute} to attach
     * @param operand The name of the operand.
     */
    void createValue(RuleAttribute ruleAttribute, String operand);
}
