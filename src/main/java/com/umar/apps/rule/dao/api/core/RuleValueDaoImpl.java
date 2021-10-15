package com.umar.apps.rule.dao.api.core;

import com.umar.apps.infra.dao.api.core.GenericJpaDao;
import com.umar.apps.rule.dao.api.RuleValueDao;
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
 * A default implementation of {@link RuleValueDao} interface.
 * 
 * @author Mohammad Umar Ali Karimi (karimiumar@gmail.com)
 */
public class RuleValueDaoImpl extends GenericJpaDao<RuleValue, Long> implements RuleValueDao {

    private static final Logger logger = LoggerFactory.getLogger(RuleValueDaoImpl.class);

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
        doInJPA(() -> emf, entityManager -> {
            try {
                result.set(entityManager.createQuery(sql)
                        .setParameter("operand", operand)
                        .getSingleResult());
            } catch (NoResultException ex) {
                //Simply ignore it. This is expected when no data exist.
            }
        }, null);

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
        doInJPA(() -> emf, entityManager -> {
            try {
                result.set(entityManager.createQuery(sql)
                        .setParameter("attributeName", ruleAttribute.getAttributeName())
                        .setParameter("ruleType", ruleAttribute.getRuleType())
                        .setParameter("operand", operand)
                        .getSingleResult());
            } catch (NoResultException ex) {
                //Simply ignore it. This is expected when no data exist.
            }
        }, null);

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
    public Collection<RuleValue> findAll() {
        logger.info("findAll()");
        Collection<RuleValue> ruleValues = new ArrayList<>(Collections.emptyList());
        doInJPA(() -> emf, entityManager -> {
            List<?> result = entityManager.createQuery("SELECT rv FROM RuleValue rv").getResultList();
            result.forEach(row -> {
                ruleValues.add((RuleValue) row);
            });
        }, null);
        return ruleValues;
    }
}
