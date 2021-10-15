package com.umar.apps.rule.dao.api;

import com.umar.apps.infra.dao.api.GenericDao;
import com.umar.apps.rule.domain.RuleAttribute;
import com.umar.apps.rule.domain.RuleValue;

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
}
