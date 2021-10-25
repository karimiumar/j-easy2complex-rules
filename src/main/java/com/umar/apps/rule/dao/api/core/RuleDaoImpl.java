package com.umar.apps.rule.dao.api.core;

import com.umar.apps.infra.dao.api.core.GenericJpaDao;
import com.umar.apps.rule.dao.api.RuleDao;
import com.umar.apps.rule.domain.BusinessRule;
import com.umar.apps.rule.domain.RuleAttribute;
import com.umar.apps.rule.domain.RuleValue;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManagerFactory;
import java.util.*;

import static com.umar.apps.infra.dao.api.core.AbstractTxExecutor.doInJPA;

/**
 * A default implementation of {@link RuleDao} interface.
 * 
 * @author Mohammad Umar Ali Karimi (karimiumar@gmail.com)
 */
@Repository
public class RuleDaoImpl extends GenericJpaDao<BusinessRule, Long> implements RuleDao {

    private static final Logger logger = LoggerFactory.getLogger(RuleDaoImpl.class);


    //Constructor needed for CDI. Do not remove
    RuleDaoImpl() {
        this(null);
    }

    @Autowired
    public RuleDaoImpl(final EntityManagerFactory entityManagerFactory) {
        super(BusinessRule.class, entityManagerFactory);
    }

    @Override
    public Collection<BusinessRule> findAll() {
        logger.info("findAll()");
        Collection<BusinessRule> rules = new ArrayList<>(Collections.emptyList());
        return doInJPA(() -> emf, entityManager -> {
            var session = entityManager.unwrap(Session.class);
            var result = session.createQuery("""
                        SELECT DISTINCT rule FROM BusinessRule rule
                        LEFT JOIN FETCH rule.ruleAttributes ras
                        LEFT JOIN FETCH ras.ruleAttributeValues ravs
                        LEFT JOIN FETCH ravs.ruleValue rv
                        ORDER BY rule.active DESC, rule.priority , rule.ruleName , rule.ruleType ASC
                    """, BusinessRule.class).getResultList();
            logger.debug("findAll() returning BusinessRules with size {}", result.size());
            return result;
        }, null);
    }

    @Override
    public Collection<BusinessRule> findByName(String ruleName, boolean isActive) {
        logger.info("findByName() with name: {}, isActive: {} ", ruleName, isActive);
        Collection<BusinessRule> businessRules = new ArrayList<>(Collections.emptyList());
        String sql = """
                SELECT rule FROM BusinessRule rule
                LEFT JOIN FETCH rule.ruleAttributes ras
                LEFT JOIN FETCH ras.ruleAttributeValues ravs
                LEFT JOIN FETCH ravs.ruleValue rv
                WHERE rule.ruleName = :ruleName
                AND rule.active = :active
                """;
        doInJPA(() -> emf, entityManager -> {
            List<?> result = entityManager.createQuery(sql)
                    .setParameter("ruleName", ruleName)
                    .setParameter("active", isActive)
                    .getResultList();
            result.forEach(row -> businessRules.add((BusinessRule) row));
        }, null);
        return businessRules;
    }

    @Override
    public Collection<BusinessRule> findByType(String type, boolean isActive) {
        logger.info("findByType() with type: {}, isActive:{}", type, isActive);
        Collection<BusinessRule> businessRules = new ArrayList<>(Collections.emptyList());
        String sql = """
                SELECT rule FROM BusinessRule rule
                LEFT JOIN FETCH rule.ruleAttributes ras
                LEFT JOIN FETCH ras.ruleAttributeValues ravs
                LEFT JOIN FETCH ravs.ruleValue rv
                WHERE rule.ruleType = :type
                AND rule.active = :active
                """;
        doInJPA(() -> emf, entityManager -> {
            List<?> result = entityManager.createQuery(sql)
                    .setParameter("type", type)
                    .setParameter("active", isActive)
                    .getResultList();
            result.forEach(row -> {
                businessRules.add((BusinessRule) row);
            });
        }, null);
        return businessRules;
    }

    @Override
    public Collection<BusinessRule> findActiveRules(boolean isActive) {
        logger.info("findActiveRules() with isActive: {}", isActive);
        Collection<BusinessRule> businessRules = new ArrayList<>(Collections.emptyList());
        String sql = """
                SELECT rule FROM BusinessRule rule
                LEFT JOIN FETCH rule.ruleAttributes ras
                LEFT JOIN FETCH ras.ruleAttributeValues ravs
                LEFT JOIN FETCH ravs.ruleValue rv
                WHERE rule.active = :active
                """;
        doInJPA(() -> emf, entityManager -> {
            List<?> result = entityManager.createQuery(sql)
                    .setParameter("active", isActive)
                    .getResultList();
            result.forEach(row -> {
                businessRules.add((BusinessRule) row);
            });
        }, null);
        return businessRules;
    }

    @Override
    public Optional<BusinessRule> findByNameAndType(String ruleName, String ruleType, boolean isActive) {
        logger.info("findByNameAndType() with ruleName: {}, ruleType: {}, isActive: {}", ruleName, ruleType, isActive);
        String sql = """
                SELECT rule FROM BusinessRule rule
                LEFT JOIN FETCH rule.ruleAttributes ra
                LEFT JOIN FETCH ra.ruleAttributeValues ravs
                LEFT JOIN FETCH ravs.ruleValue rv
                WHERE rule.ruleName = :ruleName
                AND rule.ruleType = :ruleType
                AND rule.active = :active
                """;
        var result = doInJPA(() -> emf, entityManager -> {
            var session = entityManager.unwrap(Session.class);
            return session.createQuery(sql, BusinessRule.class)
                    .setParameter("ruleName", ruleName)
                    .setParameter("ruleType", ruleType)
                    .setParameter("active", isActive)
                    .getResultList();
        }, null);
        return result.isEmpty()? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Collection<RuleValue> findByNameAndAttribute(String ruleName, String ruleType, RuleAttribute ruleAttribute, boolean isActive) {
        logger.info("findByNameAndAttribute() with ruleName: {}, ruleType: {}, ruleAttribute: {}, isActive: {}", ruleName, ruleType, ruleAttribute, isActive);
        Collection<RuleValue> values = new ArrayList<>(0);
        String sql = """
                SELECT ruleVal FROM RuleValue ruleVal, RuleAttributeValue rav, RuleAttribute attr, BusinessRule rule
                WHERE rule.ruleName = :ruleName
                AND rule.ruleType = :ruleType
                AND attr.attributeName =:attributeName
                AND rav.ruleValue = ruleVal
                AND rav.ruleAttribute = attr
                AND rule.ruleType = attr.ruleType
                AND rule.active = :active
                """;
        doInJPA(() -> emf, entityManager -> {
            Session session = entityManager.unwrap(Session.class);
            List<RuleValue> ruleValues = session.createQuery(sql, RuleValue.class)
                    .setParameter("ruleName", ruleName)
                    .setParameter("ruleType", ruleType)
                    .setParameter("attributeName", ruleAttribute.getAttributeName())
                    .setParameter("active", isActive)
                    .getResultList();
            values.addAll(ruleValues);
        }, null);
        return values;
    }
}
