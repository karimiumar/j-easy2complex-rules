package com.umar.apps.rule.service.api;

import com.umar.apps.rule.domain.BusinessRule;
import com.umar.apps.rule.domain.RuleAttribute;

public interface BusinessRuleService {
    /**
     * Creates a {@link com.umar.apps.rule.domain.BusinessRule} for the given ruleName, ruleType. It first searches the db for the given ruleName and ruleType.
     * If one exists then returns it. Otherwise it creates a new {@link com.umar.apps.rule.domain.BusinessRule}.
     * While creating a new {@link com.umar.apps.rule.domain.BusinessRule}, it looksup db for {@link com.umar.apps.rule.domain.RuleAttribute}.
     * If no {@link com.umar.apps.rule.domain.RuleAttribute} exists for the given attributeName, attributeType and ruleType, then it creates a new instance of the
     * {@link com.umar.apps.rule.domain.RuleAttribute} and links it with {@link com.umar.apps.rule.domain.BusinessRule} instance. Otherwise uses the existing instance to link.
     *
     * It further looks up db for {@link com.umar.apps.rule.domain.RuleValue} for the given operand. If none exists then it creates a new
     * {@link com.umar.apps.rule.domain.RuleValue} and links with the parent {@link com.umar.apps.rule.domain.BusinessRule}. Otherwise, links the existing one with the parent.
     *
     *
     * @param ruleName The name of the Rule
     * @param ruleType The type of the Rule
     * @param priority The priority of the Rule
     */
    void createRule(String ruleName, String ruleType, int priority);

    /**
     * Creates a {@link com.umar.apps.rule.domain.RuleAttribute} for the given parameters and attaches it to the given {@link BusinessRule}
     * @param businessRule The {@link com.umar.apps.rule.domain.BusinessRule} to attach
     * @param attributeName The name of the {@link com.umar.apps.infra.dao.api.WorkflowItem} attribute
     * @param ruleType The type of the {@link com.umar.apps.rule.domain.BusinessRule}
     * @param displayName The canonical display name of the attribute
     */
    void createAttribute(BusinessRule businessRule, String attributeName, String ruleType, String displayName);

    /**
     * Creates a {@link com.umar.apps.rule.domain.RuleValue} for the given params and attaches it to {@link RuleAttribute}
     * @param ruleAttribute The {@link RuleAttribute} to attach
     * @param operand The name of the operand.
     */
    void createValue(RuleAttribute ruleAttribute, String operand);
}