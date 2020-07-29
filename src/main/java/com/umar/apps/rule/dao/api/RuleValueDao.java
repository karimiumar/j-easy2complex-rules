package com.umar.apps.rule.dao.api;

import com.umar.apps.rule.RuleAttribute;
import com.umar.apps.rule.RuleValue;
import com.umar.apps.rule.infra.dao.api.GenericDao;

import java.util.Optional;

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
}
