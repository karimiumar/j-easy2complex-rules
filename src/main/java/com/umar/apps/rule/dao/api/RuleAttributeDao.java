package com.umar.apps.rule.dao.api;

import com.umar.apps.rule.RuleAttribute;
import com.umar.apps.rule.infra.dao.api.GenericDao;

import java.util.Optional;

public interface RuleAttributeDao extends GenericDao<RuleAttribute, Long> {

    /**
     * Finds a {@link RuleAttribute} for the given attribute name and rule type.
     *
     * @param attributeName The name of the attribute of the Entity
     * @param ruleType The rule type
     *
     * @return Returns an Optional
     */
    Optional<RuleAttribute> findRuleAttribute(String attributeName, String ruleType);
}
