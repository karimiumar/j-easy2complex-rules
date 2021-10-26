package com.umar.apps.rule.service.api;

import com.umar.apps.rule.domain.BusinessRule;
import com.umar.apps.rule.domain.RuleAttribute;
import com.umar.apps.rule.domain.RuleValue;

import java.util.List;
import java.util.Optional;

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
     * @param description The description of the Rule
     * @param priority The priority of the Rule
     * @param active Whether the given rule is active
     */
    void createRule(String ruleName, String ruleType, String description, int priority, boolean active);

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

    /**
     * Provides a {@link List} of persistent {@link BusinessRule}
     *
     * @return Returns a {@link List} of {@link BusinessRule}
     */
    List<BusinessRule> findAll();

    /**
     * Finds an {@link Optional} of {@link BusinessRule} for the given id
     *
     * @param id The id to lookup
     * @return Returns a {@link BusinessRule}
     */
    Optional<BusinessRule> findRuleById(long id);

    /**
     * Finds name of {@link BusinessRule} for the given id
     *
     * @param ruleId The id to lookup
     * @return Returns name of the {@link BusinessRule}
     */
    String findRuleNameById(long ruleId);

    /**
     * Updates a given {@link BusinessRule}
     *
     * @param businessRule The {@link BusinessRule} to update.
     */
    void update(BusinessRule businessRule);

    /**
     * Updates a given {@link RuleAttribute}
     *
     * @param ruleAttribute The {@link RuleAttribute} to update
     */
    void update(RuleAttribute ruleAttribute);

    /**
     * Updates a given {@link RuleValue}
     *
     * @param ruleValue The {@link RuleValue} to update
     */
    void update(RuleValue ruleValue);

    /**
     * Physically deletes a {@link BusinessRule}
     *
     * @param id The id of the {@link BusinessRule}
     */
    void deleteRuleById(long id);

    /**
     * Finds {@link RuleAttribute} for the given ruleId
     *
     * @param ruleId The id of the corresponding {@link BusinessRule}
     * @return A {@link List} of {@link RuleAttribute}
     */
    List<RuleAttribute> findAttributesOfRule(long ruleId);

    /**
     * Finds an {@link Optional} of {@link RuleAttribute} for the given id
     *
     * @param id The id to lookup
     * @return Returns an {@link Optional}
     */
    Optional<RuleAttribute> findAttributeById(long id);

    /**
     * Deletes a {@link RuleAttribute} for the given id
     *
     * @param id The id to delete
     */
    void deleteRuleAttributeById(long id);

    /**
     * Deletes a {@link com.umar.apps.rule.domain.RuleValue} for the given id
     *
     * @param id The id to delete
     */
    void deleteRuleValueById(long id);

    /**
     * Finds a unique {@link RuleAttribute} for the given ruleName and ruleType
     *
     * @param attributeName The name of the attribute
     * @param ruleType The type of the rule
     * @return Returns an {@link Optional} of {@link RuleAttribute}
     */
    Optional<RuleAttribute> findRuleAttribute(String attributeName, String ruleType);

    /**
     * Finds a unique {@link RuleValue} for the given operand
     *
     * @param operand The operand to lookup
     * @return Returns an {@link Optional} of {@link RuleValue}
     */
    Optional<RuleValue> findByOperand(String operand);

    /**
     * Finds a unique {@link BusinessRule} for the given rule name, type and active flag
     *
     * @param ruleName The name of the rule to lookup
     * @param ruleType The type of the rule to lookup
     * @param isActive Whether the rule to lookup us active
     * @return Returns an {@link Optional} of {@link BusinessRule}
     */
    Optional<BusinessRule> findByNameAndType(String ruleName, String ruleType, boolean isActive);

    /**
     * Finds a {@link List} of {@link RuleValue} for the given attributeId
     *
     * @param attributeId The id to lookup
     * @return Returns a {@link List} of {@link RuleValue}
     */
    List<RuleValue> findValuesOf(long attributeId);

    /**
     * Finds a unique {@link RuleValue} for the given id
     *
     * @param id The id to lookup
     * @return Returns an {@link Optional} of {@link RuleValue}
     */
    Optional<RuleValue> findRuleValueById(long id);

    /**
     * Finds a {@link BusinessRule} for the given id along with associated child objects
     *
     * @param id The id of the {@link BusinessRule}
     * @return Returns an {@link Optional} of {@link BusinessRule}
     */
    Optional<BusinessRule> findRuleByIdWithSubgraphs(long id);

    /**
     * Persists a new {@link BusinessRule}
     *
     * @param rule The {@link BusinessRule} to persist
     * @return The persisted {@link BusinessRule}
     */
    BusinessRule save(BusinessRule rule);
}