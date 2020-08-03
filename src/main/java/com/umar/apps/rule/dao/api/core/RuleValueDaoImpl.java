package com.umar.apps.rule.dao.api.core;

import com.umar.apps.rule.RuleAttribute;
import com.umar.apps.rule.RuleValue;
import com.umar.apps.rule.dao.api.RuleValueDao;
import com.umar.apps.rule.infra.dao.api.core.GenericJpaDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
@Named
public class RuleValueDaoImpl extends GenericJpaDao<RuleValue, Long> implements RuleValueDao {

    private static final Logger logger = LogManager.getLogger(RuleValueDaoImpl.class);

    //Constructor needed for CDI. Do not remove
    RuleValueDaoImpl() {
        this(null);
    }

    public RuleValueDaoImpl(String persistenceUnit) {
        super(RuleValue.class, persistenceUnit);
    }

    @Override
    public Optional<RuleValue> findByOperand(String operand) {
        logger.info("findByOperand() with operand {}", operand);
        AtomicReference<Object> result = new AtomicReference<>();
        String sql = """
                SELECT ruleVal FROM RuleValue ruleVal, RuleAttributeValue rav, RuleAttribute ra
                WHERE ruleVal = rav.ruleValue
                AND rav.ruleAttribute = ra
                AND ruleVal.operand = :operand
                """;
        executeInTransaction(entityManager -> {
            try {
                result.set(entityManager.createQuery(sql)
                        .setParameter("operand", operand)
                        .getSingleResult());
            } catch (NoResultException ex) {
                //Simply ignore it. This is expected when no data exist.
            }
        });

        if(null != result.get()) {
            RuleValue ruleValue = (RuleValue) result.get();
            return Optional.of(ruleValue);
        }
        return Optional.empty();
    }

    @Override
    public Optional<RuleValue> findByRuleAttributeAndValue(RuleAttribute ruleAttribute, String operand) {
        logger.info("findByRuleAttributeAndValue() for params ruleAttribute{}, operand{}", ruleAttribute, operand);
        AtomicReference<Object> result = new AtomicReference<>();
        String sql = """
                SELECT ruleVal FROM RuleValue ruleVal, RuleAttributeValue rav, RuleAttribute ra
                WHERE ruleVal = rav.ruleValue
                AND rav.ruleAttribute = ra
                AND ra.attributeName = :attributeName
                AND ra.ruleType = :ruleType
                AND ruleVal.operand = :operand
                """;
        executeInTransaction(entityManager -> {
            try {
                result.set(entityManager.createQuery(sql)
                        .setParameter("attributeName", ruleAttribute.getAttributeName())
                        .setParameter("ruleType", ruleAttribute.getRuleType())
                        .setParameter("operand", operand)
                        .getSingleResult());
            } catch (NoResultException ex) {
                //Simply ignore it. This is expected when no data exist.
            }
        });

        if(null != result.get()) {
            RuleValue ruleValue = (RuleValue) result.get();
            return Optional.of(ruleValue);
        }
        return Optional.empty();
    }

    @Override
    public List<RuleValue> findByRuleAttribute(RuleAttribute ruleAttribute) {
        logger.info("findByRuleAttribute() for params ruleAttribute{}", ruleAttribute);
        List<RuleValue> ruleValues = new ArrayList<>(0);
        String sql = """
                SELECT "ruleVal FROM RuleValue ruleVal, RuleAttributeValue rav, RuleAttribute ra
                WHERE ruleVal = rav.ruleValue
                AND rav.ruleAttribute = ra
                AND ra.attributeName = :attributeName
                AND ra.ruleType = :ruleType
                """;
        executeInTransaction(entityManager -> {
            try {
                Session session = entityManager.unwrap(Session.class);
                ruleValues.addAll(session.createQuery(sql, RuleValue.class)
                        .setParameter("attributeName", ruleAttribute.getAttributeName())
                        .setParameter("ruleType", ruleAttribute.getRuleType()).getResultList());
            } catch (NoResultException ex) {
                //Simply ignore it. This is expected when no data exist.
            }
        });
        return ruleValues;
    }

    @Override
    public Collection<RuleValue> findAll() {
        logger.info("findAll()");
        Collection<RuleValue> ruleValues = new ArrayList<>(Collections.emptyList());
        executeInTransaction(entityManager -> {
            List<?> result = entityManager.createQuery("SELECT rv FROM RuleValue rv").getResultList();
            result.forEach(row -> {
                ruleValues.add((RuleValue) row);
            });
        });
        return ruleValues;
    }
}
