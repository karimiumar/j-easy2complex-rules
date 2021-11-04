package com.umar.apps.rule.dao.api.core;

import com.umar.apps.infra.dao.api.core.GenericJpaDao;
import com.umar.apps.rule.dao.api.RuleDao;
import com.umar.apps.rule.domain.BusinessRule;
import com.umar.apps.rule.domain.RuleAttribute;
import com.umar.apps.rule.domain.RuleValue;
import com.umar.apps.util.GenericBuilder;
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

    @Autowired
    public RuleDaoImpl(final EntityManagerFactory entityManagerFactory) {
        super(BusinessRule.class, entityManagerFactory);
    }

    @Override
    public Collection<BusinessRule> findAll() {
        logger.info("findAll()");
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
        String sql = """
                SELECT rule FROM BusinessRule rule
                LEFT JOIN FETCH rule.ruleAttributes ras
                LEFT JOIN FETCH ras.ruleAttributeValues ravs
                LEFT JOIN FETCH ravs.ruleValue rv
                WHERE rule.ruleName = :ruleName
                AND rule.active = :active
                """;
        return doInJPA(() -> emf, entityManager -> {
            return entityManager.createQuery(sql, BusinessRule.class)
                    .setParameter("ruleName", ruleName)
                    .setParameter("active", isActive)
                    .getResultList();
        }, null);
    }

    @Override
    public Collection<BusinessRule> findByType(String type, boolean isActive) {
        logger.info("findByType() with type: {}, isActive:{}", type, isActive);
        String sql = """
                SELECT rule FROM BusinessRule rule
                LEFT JOIN FETCH rule.ruleAttributes ras
                LEFT JOIN FETCH ras.ruleAttributeValues ravs
                LEFT JOIN FETCH ravs.ruleValue rv
                WHERE rule.ruleType = :type
                AND rule.active = :active
                """;
        return doInJPA(() -> emf, entityManager -> {
            return entityManager.createQuery(sql, BusinessRule.class)
                    .setParameter("type", type)
                    .setParameter("active", isActive)
                    .getResultList();
        }, null);
    }

    @Override
    public Collection<BusinessRule> findActiveRules(boolean isActive) {
        logger.info("findActiveRules() with isActive: {}", isActive);
        String sql = """
                SELECT rule FROM BusinessRule rule
                LEFT JOIN FETCH rule.ruleAttributes ras
                LEFT JOIN FETCH ras.ruleAttributeValues ravs
                LEFT JOIN FETCH ravs.ruleValue rv
                WHERE rule.active = :active
                """;
        return doInJPA(() -> emf, entityManager -> {
            return entityManager.createQuery(sql, BusinessRule.class)
                    .setParameter("active", isActive)
                    .getResultList();
        }, null);
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
    public String findRuleNameById(long ruleId) {
        doInJPA(this::getEMF, entityManager -> {
            var session = entityManager.unwrap(Session.class);
            return session
                    .createQuery("SELECT ruleName FROM BusinessRule br WHERE br.id = :id", String.class)
                    .setParameter("id", ruleId)
                    .uniqueResult();
        }, null);
        return null;
    }

    @Override
    public void update(BusinessRule businessRule) {
        doInJPA(this::getEMF, entityManager -> {
            var session = entityManager.unwrap(Session.class);
            var entity = session.find(BusinessRule.class, businessRule.getId());
            logger.debug("Found BusinessRule {} to update. ", entity);
            update(entity, businessRule);
            session.saveOrUpdate(entity);
            logger.debug("Updated BusinessRule {} successfully. ", entity);
        }, null);
    }

    @Override
    public void deleteById(long id) {
        doInJPA(this::getEMF, entityManager -> {
            var session = entityManager.unwrap(Session.class);
            var entity = session.find(BusinessRule.class, id);
            logger.debug("Found BusinessRule {} to delete. ", entity);
            session.delete(entity);
            logger.debug("Deleted BusinessRule {} successfully. ", entity);
        }, null);
    }

    @Override
    public Optional<BusinessRule> findByIdWithSubgraphs(long id) {
        return doInJPA(this::getEMF, entityManager -> {
            var sql = """
                    SELECT rule FROM BusinessRule rule
                    LEFT JOIN FETCH rule.ruleAttributes ras
                    LEFT JOIN FETCH ras.ruleAttributeValues ravs
                    LEFT JOIN FETCH ravs.ruleValue rv
                    WHERE rule.id = :id
                    """;
            var session = entityManager.unwrap(Session.class);
            var result = session.createQuery(sql, BusinessRule.class)
                    .setParameter("id", id)
                    .getResultList();
            return  result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
        }, null);
    }

    private void update(BusinessRule entity, BusinessRule transientObj) {
        GenericBuilder.of(() -> entity)
                .with(BusinessRule::setRuleName, transientObj.getRuleName())
                .with(BusinessRule::setRuleType, transientObj.getRuleType())
                .with(BusinessRule::setDescription, transientObj.getDescription())
                .with(BusinessRule::setPriority, transientObj.getPriority())
                .with(BusinessRule::setActive, transientObj.isActive())
                .build();
    }
}
