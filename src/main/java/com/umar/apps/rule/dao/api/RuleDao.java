package com.umar.apps.rule.dao.api;

import com.umar.apps.infra.dao.api.GenericDao;
import com.umar.apps.rule.domain.BusinessRule;
import com.umar.apps.rule.domain.RuleAttribute;
import com.umar.apps.rule.domain.RuleValue;

import java.util.Collection;
import java.util.Optional;

/**
 * A RuleDao interface
 * 
 * @author Mohammad Umar Ali Karimi (karimiumar@gmail.com)
 */
public interface RuleDao extends GenericDao<BusinessRule, Long> {

    /**
     * Finds a Collection of Business Rules by name
     *
     * @param ruleName The name of the Business Rule
     * @return Returns a Collection of Business Rules
     */
    Collection<BusinessRule> findByName(String ruleName);
    /**
     * Finds a Collection of Business Ruls by name
     *
     * @param  type The type of the Business Rule
     * @return Returns a Collection of Business Rules
     */
    Collection<BusinessRule> findByType(String type);
    /**
     * Finds a Collection of Business Rules by active flag
     *
     * @param isActive If true all active rules will be returned otherwise inactive rules
     * @return Returns a Collection of Business Rules
     */
    Collection<BusinessRule> findActiveRules(boolean isActive);

    /**
     * Finds a {@link BusinessRule} for the given name and type
     *
     * @param ruleName The rule name to lookup
     * @param ruleType The rule type to lookup
     *
     * @return Returns an Optional
     */
    Optional<BusinessRule> findByNameAndType(String ruleName, String ruleType);

    /**
     * Finds a {@link RuleValue} for the given set of params
     *
     * @param ruleName The rule name
     * @param ruleType The rule type
     * @param ruleAttribute The {@link RuleAttribute} rule attribute
     * @return Returns an optional
     */
    Collection<RuleValue> findByNameAndAttribute(String ruleName, String ruleType, RuleAttribute ruleAttribute);
}
