package com.umar.apps.rule.service.api;

import com.umar.apps.rule.BusinessRule;
import com.umar.apps.rule.RuleAttribute;
import com.umar.apps.rule.api.Condition;
import com.umar.apps.rule.engine.WorkflowItem;

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
     * @param attributeNameTypeMap A map of attributeName and attributeType
     * @param attributeValues A List of attribute value operands
     * @return Returns a {@link BusinessRule}
     */
    BusinessRule createRule(String ruleName, String ruleType, int priority, Map<String, String> attributeNameTypeMap, List<String> attributeValues);

    /**
     * Creates a {@link BusinessRule} for the given ruleName, ruleType. It first searches the db for the given ruleName and ruleType.
     * If one exists then returns it. Otherwise it creates a new {@link BusinessRule}.
     * While creating a new {@link BusinessRule}, it looksup db for {@link RuleAttribute}.
     * If no {@link RuleAttribute} exists for the given attributeName, attributeType and ruleType, then it creates a new instance of the
     * {@link RuleAttribute} and links it with {@link BusinessRule} instance. Otherwise uses the existing instance to link.
     *
     * It further looksup db for {@link com.umar.apps.rule.RuleValue} for the given operand. If none exists then it creates a new
     *
     * {@link com.umar.apps.rule.RuleValue} and links with the parent {@link BusinessRule}. Otherwise, links the existing one with the parent.
     *
     * @param ruleName The name of the Rule
     * @param ruleType The type of the Rule
     * @param priority The priority of the Rule
     * @param operand The operand of the Rule
     * @param attributeNameTypeMap A map of attributeName and attributeType
     * @return Returns a {@link BusinessRule}
     */
    BusinessRule createRule(String ruleName, String ruleType, int priority, String operand, Map<String, String> attributeNameTypeMap);

    /**
     * Uses Java reflection API to get {@link Condition} based on given parameters.
     *
     * @param workflowItem The workflowItem instance
     * @param ruleType The rule type to use
     * @param ruleName The rule name to be used for condition
     * @return Returns a {@link Condition}
     */
    public  <T> Condition getSTPCondition(T workflowItem, String ruleType, String ruleName);

}
