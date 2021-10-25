package com.umar.apps.rule.dao.api;

import com.umar.apps.infra.dao.api.GenericDao;
import com.umar.apps.rule.domain.RuleAttribute;

import java.util.List;
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

    /**
     * Finds {@link RuleAttribute} for the given id of {@link com.umar.apps.rule.domain.BusinessRule}
     *
     * @param ruleId The id of {@link com.umar.apps.rule.domain.BusinessRule}
     * @return Returns a {@link List} of {@link RuleAttribute}
     */
    List<RuleAttribute> findAttributesOfRule(long ruleId);

    /**
     * Finds {@link RuleAttribute} for the given id
     *
     * @param id The id to lookup
     * @return Returns an {@link Optional} of {@link RuleAttribute}
     */
    Optional<RuleAttribute> findAttributeById(long id);

    /**
     * Deletes a {@link RuleAttribute} for the given id
     *
     * @param id The id to lookup and delete
     */
    void deleteRuleAttributeById(long id);

    /**
     * Updates the given {@link RuleAttribute}
     *
     * @param ruleAttribute The {@link RuleAttribute} to update
     */
    void update(RuleAttribute ruleAttribute);
}