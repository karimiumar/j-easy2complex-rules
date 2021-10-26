package com.umar.apps.rule.dao.api;

import com.umar.apps.infra.dao.api.GenericDao;
import com.umar.apps.rule.domain.RuleAttribute;
import com.umar.apps.rule.domain.RuleValue;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * A RuleValueDao interface
 * 
 * @author Mohammad Umar Ali Karimi (karimiumar@gmail.com)
 */
public interface RuleValueDao extends GenericDao<RuleValue, Long> {
    /**
     * Finds a {@link RuleValue} for the given operand
     *
     * @param operand The name of the operand to lookup
     * @return Returns an Optional.
     */
    Optional<RuleValue> findByOperand(String operand);

    /**
     * Finds a {@link RuleValue} for the given {@link RuleAttribute} and operand
     * @param ruleAttribute The RuleAttribute ruleAttribute instance
     * @param operand The operand
     * @return Returns an Optional
     */
    Optional<RuleValue> findByRuleAttributeAndValue(RuleAttribute ruleAttribute, String operand);

    /**
     * Finds a {@link RuleValue} for the given {@link RuleAttribute} and operand
     * @param ruleAttribute The RuleAttribute ruleAttribute instance
     * @return Returns an Optional
     */
    List<RuleValue> findByRuleAttribute(RuleAttribute ruleAttribute);

    /**
     * Finds {@link RuleValue} for the given id of {@link RuleAttribute}
     *
     * @param attributeId The id of the {@link RuleAttribute} to lookup
     * @return Returns a {@link List} of {@link RuleValue}
     */
    List<RuleValue> findValuesOf(long attributeId);

    /**
     * Finds a {@link RuleValue} for the given set of params
     *
     * @param ruleName The rule name
     * @param ruleType The rule type
     * @param ruleAttribute The {@link RuleAttribute} rule attribute
     * @param isActive Whether the rule being searched is active or not
     * @return Returns an optional
     */
    Collection<RuleValue> findByNameAndAttribute(String ruleName, String ruleType, RuleAttribute ruleAttribute, boolean isActive);

    /**
     * Updates a given {@link RuleValue}
     *
     * @param ruleValue The {@link RuleValue} to update
     */
    void update(RuleValue ruleValue);

    /**
     * Deletes a {@link RuleValue} for the given id
     *
     * @param id The id of the {@link RuleValue} to lookup and delete
     */
    void deleteById(long id);
}
