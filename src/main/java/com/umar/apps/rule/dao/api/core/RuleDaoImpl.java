package com.umar.apps.rule.dao.api.core;

import com.umar.apps.infra.dao.api.core.GenericJpaDao;
import com.umar.apps.rule.dao.api.RuleDao;
import com.umar.apps.rule.domain.BusinessRule;
import com.umar.apps.rule.domain.RuleAttribute;
import com.umar.apps.rule.domain.RuleValue;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.NoResultException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.umar.apps.infra.dao.api.core.AbstractTxExecutor.doInJPA;

/**
 * A default implementation of {@link RuleDao} interface.
 * 
 * @author Mohammad Umar Ali Karimi (karimiumar@gmail.com)
 */
public class RuleDaoImpl extends GenericJpaDao<BusinessRule, Long> implements RuleDao {

    private static final Logger logger = LoggerFactory.getLogger(RuleDaoImpl.class);


    //Constructor needed for CDI. Do not remove
    RuleDaoImpl() {
        this(null);
    }

    public RuleDaoImpl(final String persistenceUnit) {
        super(BusinessRule.class, persistenceUnit);
    }

    @Override
    public Collection<BusinessRule> findAll() {
        logger.info("findAll()");
        Collection<BusinessRule> rules = new ArrayList<>(Collections.emptyList());
        doInJPA(() -> emf, entityManager -> {
            List<?> result = entityManager.createQuery("SELECT br FROM BusinessRule br")
                    .getResultList();
            result.forEach(row -> {
                rules.add((BusinessRule) row);
            });
        }, null);
        return rules;
    }

    @Override
    public Collection<BusinessRule> findByName(String ruleName) {
        logger.info("findByName() with name: {}", ruleName);
        Collection<BusinessRule> businessRules = new ArrayList<>(Collections.emptyList());
        String sql = """
                SELECT rule FROM BusinessRule rule
                WHERE rule.ruleName = :ruleName
                AND rule.active = :active
                """;
        doInJPA(() -> emf, entityManager -> {
            List<?> result = entityManager.createQuery(sql)
                    .setParameter("ruleName", ruleName)
                    .setParameter("active", true)
                    .getResultList();
            result.forEach(row -> businessRules.add((BusinessRule) row));
        }, null);
        return businessRules;
    }

    @Override
    public Collection<BusinessRule> findByType(String type) {
        logger.info("findByType() with type: {}", type);
        Collection<BusinessRule> businessRules = new ArrayList<>(Collections.emptyList());
        String sql = """
                SELECT rule FROM BusinessRule rule
                WHERE rule.ruleType = :type
                AND rule.active = :active
                """;
        doInJPA(() -> emf, entityManager -> {
            List<?> result = entityManager.createQuery(sql)
                    .setParameter("type", type)
                    .setParameter("active", true)
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
    public Optional<BusinessRule> findByNameAndType(String ruleName, String ruleType) {
        logger.info("findByNameAndType() with ruleName: {}, ruleType: {}", ruleName, ruleType);
        AtomicReference<Object> result = new AtomicReference<>();
        String sql = """
                SELECT rule FROM BusinessRule rule
                WHERE rule.ruleName = :ruleName
                AND rule.ruleType = :ruleType
                AND rule.active = :active
                """;
        doInJPA(() -> emf, entityManager -> {
            try {
                result.set(entityManager.createQuery(sql)
                        .setParameter("ruleName", ruleName)
                        .setParameter("ruleType", ruleType)
                        .setParameter("active", true)
                        .getSingleResult());
            }catch (NoResultException e) {
                //Simply ignore it. This is expected when no data exist.
            }
        }, null);
        if(null != result.get()) {
            BusinessRule rule = (BusinessRule) result.get();
            return Optional.of(rule);
        }
        return Optional.empty();
    }

    @Override
    public Collection<RuleValue> findByNameAndAttribute(String ruleName, String ruleType, RuleAttribute ruleAttribute) {
        logger.info("findByNameAndAttribute() with ruleName: {}, ruleType: {}, ruleAttribute: {}", ruleName, ruleType, ruleAttribute);
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
                    .setParameter("active", true)
                    .getResultList();
            values.addAll(ruleValues);
        }, null);
        return values;
    }
}
