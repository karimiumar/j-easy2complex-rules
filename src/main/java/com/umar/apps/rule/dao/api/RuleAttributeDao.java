package com.umar.apps.rule.dao.api;

import com.umar.apps.infra.dao.api.GenericDao;
import com.umar.apps.rule.domain.RuleAttribute;

import java.util.Optional;

/**
 * A RuleAttributeDao interface
 * 
 * @author Mohammad Umar Ali Karimi (karimiumar@gmail.com)
 */
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