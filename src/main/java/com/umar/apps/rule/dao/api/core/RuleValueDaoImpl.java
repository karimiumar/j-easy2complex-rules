package com.umar.apps.rule.dao.api.core;

import com.umar.apps.infra.dao.api.core.GenericJpaDao;
import com.umar.apps.rule.dao.api.RuleValueDao;
import com.umar.apps.rule.domain.RuleAttribute;
import com.umar.apps.rule.domain.RuleValue;
import com.umar.apps.util.GenericBuilder;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import java.util.*;

import static com.umar.apps.infra.dao.api.core.AbstractTxExecutor.doInJPA;

/**
 * A default implementation of {@link RuleValueDao} interface.
 * 
 * @author Mohammad Umar Ali Karimi (karimiumar@gmail.com)
 */
@Repository
public class RuleValueDaoImpl extends GenericJpaDao<RuleValue, Long> implements RuleValueDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuleValueDaoImpl.class);

    @Autowired
    public RuleValueDaoImpl(EntityManagerFactory entityManagerFactory) {
        super(RuleValue.class, entityManagerFactory);
    }

    @Override
    public Optional<RuleValue> findByOperand(String operand) {
        LOGGER.info("findByOperand() with operand {}", operand);
        String sql = """
                SELECT ruleVal FROM RuleValue ruleVal
                LEFT JOIN FETCH ruleVal.ruleAttribute ra
                WHERE ruleVal.operand = :operand
                """;
        var result = doInJPA(() -> emf, entityManager -> {
            var session = entityManager.unwrap(Session.class);
            return session.createQuery(sql, RuleValue.class)
                    .setParameter("operand", operand)
                    .getResultList();
        }, null);
        if(result.isEmpty()) {
            return Optional.empty();
        } else {
            var ruleValue = result.get(0);
            return Optional.of(ruleValue);
        }
    }

    @Override
    public Optional<RuleValue> findByRuleAttributeAndValue(RuleAttribute ruleAttribute, String operand) {
        LOGGER.info("findByRuleAttributeAndValue() for params ruleAttribute{}, operand{}", ruleAttribute, operand);
        String sql = """
                SELECT ruleVal FROM RuleValue ruleVal
                LEFT JOIN FETCH ruleVal.ruleAttribute ra
                WHERE ra.attributeName = :attributeName
                AND ra.ruleType = :ruleType
                AND ruleVal.operand = :operand
                """;
        var result = doInJPA(() -> emf, entityManager -> {
            var session = entityManager.unwrap(Session.class);
            return session.createQuery(sql, RuleValue.class)
                    .setParameter("attributeName", ruleAttribute.getAttributeName())
                    .setParameter("ruleType", ruleAttribute.getRuleType())
                    .setParameter("operand", operand)
                    .getResultList();
        }, null);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public List<RuleValue> findByRuleAttribute(RuleAttribute ruleAttribute) {
        LOGGER.info("findByRuleAttribute() for params ruleAttribute{}", ruleAttribute);
        List<RuleValue> ruleValues = new ArrayList<>(0);
        String sql = """
                SELECT "ruleVal FROM RuleValue ruleVal
                LEFT JOIN FETCH ruleVal.ruleAttribute ra
                WHERE ra.attributeName = :attributeName
                AND ra.ruleType = :ruleType
                """;
        doInJPA(() -> emf, entityManager -> {
            try {
                Session session = entityManager.unwrap(Session.class);
                ruleValues.addAll(session.createQuery(sql, RuleValue.class)
                        .setParameter("attributeName", ruleAttribute.getAttributeName())
                        .setParameter("ruleType", ruleAttribute.getRuleType()).getResultList());
            } catch (NoResultException ex) {
                //Simply ignore it. This is expected when no data exist.
            }
        }, null);
        return ruleValues;
    }

    @Override
    public List<RuleValue> findValuesOf(long attributeId) {
        var sql = """
                SELECT rv FROM RuleValue rv
                LEFT JOIN FETCH rv.ruleAttribute ra
                WHERE ra.id = :attributeId
                """;
        return doInJPA(this::getEMF, entityManager ->  {
            var session = entityManager.unwrap(Session.class);
            return session.createQuery(sql, RuleValue.class)
                    .setParameter("attributeId", attributeId)
                    .getResultList();
        }, null);
    }

    @Override
    public void update(RuleValue ruleValue) {
        doInJPA(this::getEMF, entityManager -> {
            var session = entityManager.unwrap(Session.class);
            var entity = session.find(RuleValue.class, ruleValue.getId());
            LOGGER.debug("Found RuleValue {} to update. ", entity);
            update(entity, ruleValue);
            session.saveOrUpdate(entity);
            LOGGER.debug("Updated RuleValue {} successfully. ", entity);
        }, null);
    }

    @Override
    public void deleteById(long id) {
        doInJPA(this::getEMF, entityManager -> {
            var session = entityManager.unwrap(Session.class);
            var entity = session.find(RuleValue.class, id);
            LOGGER.debug("Found RuleValue {} to delete. ", entity);
            session.delete(entity);
            LOGGER.debug("Deleted RuleValue {} successfully. ", entity);
        }, null);
    }

    @Override
    public Collection<RuleValue> findAll() {
        LOGGER.info("findAll()");
        Collection<RuleValue> ruleValues = new ArrayList<>(Collections.emptyList());
        doInJPA(() -> emf, entityManager -> {
            List<?> result = entityManager.createQuery("SELECT rv FROM RuleValue rv").getResultList();
            result.forEach(row -> {
                ruleValues.add((RuleValue) row);
            });
        }, null);
        return ruleValues;
    }

    @Override
    public Collection<RuleValue> findByNameAndAttribute(String ruleName, String ruleType, RuleAttribute ruleAttribute, boolean isActive) {
        LOGGER.info("findByNameAndAttribute() with ruleName: {}, ruleType: {}, ruleAttribute: {}, isActive: {}", ruleName, ruleType, ruleAttribute, isActive);
        String sql = """
                SELECT rv FROM RuleValue rv
                LEFT JOIN FETCH rv.ruleAttribute ra
                LEFT JOIN FETCH ra.businessRule rule
                WHERE rule.ruleName = :ruleName
                AND rule.ruleType = :ruleType
                AND ra.attributeName =:attributeName
                AND rule.ruleType = ra.ruleType
                AND rule.active = :active
                """;
        return doInJPA(() -> emf, entityManager -> {
            Session session = entityManager.unwrap(Session.class);
            return session.createQuery(sql, RuleValue.class)
                    .setParameter("ruleName", ruleName)
                    .setParameter("ruleType", ruleType)
                    .setParameter("attributeName", ruleAttribute.getAttributeName())
                    .setParameter("active", isActive)
                    .getResultList();
        }, null);
    }

    private void update(RuleValue entity, RuleValue transientObj) {
        GenericBuilder.of(() -> entity)
                .with(RuleValue::setOperand, transientObj.getOperand())
                .build();
    }
}
