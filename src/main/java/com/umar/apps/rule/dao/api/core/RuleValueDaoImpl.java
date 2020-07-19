package com.umar.apps.rule.dao.api.core;

import com.umar.apps.rule.RuleValue;
import com.umar.apps.rule.dao.api.RuleValueDao;
import com.umar.apps.rule.infra.dao.api.core.GenericJpaDao;
import com.umar.simply.jdbc.dml.operations.SelectOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.NoResultException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.umar.apps.rule.RuleValue.*;

public class RuleValueDaoImpl extends GenericJpaDao<RuleValue, Long> implements RuleValueDao {

    private static final Logger logger = LogManager.getLogger(RuleValueDaoImpl.class);

    public RuleValueDaoImpl(String persistenceUnit) {
        super(RuleValue.class, persistenceUnit);
    }

    @Override
    public Optional<RuleValue> findByOperand(String operand) {
        logger.info("findByOperand() with operand {}", operand);
        AtomicReference<Object> result = new AtomicReference<>();
        String sql = SelectOp.create()
                .SELECT().COLUMN(RULE_VALUE)
                .FROM(RULE_VALUE$ALIAS)
                .WHERE().COLUMN(RULE_VALUE$OPERAND).EQ(":operand")
                .getSQL();
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
    public Collection<RuleValue> findAll() {
        logger.info("findAll()");
        Collection<RuleValue> ruleValues = new ArrayList<>(Collections.emptyList());
        String sql = SelectOp.create()
                .SELECT()
                .COLUMN(RULE_VALUE)
                .FROM(RULE_VALUE$ALIAS)
                .getSQL();
        executeInTransaction(entityManager -> {
            List<?> result = entityManager.createQuery(sql).getResultList();
            result.forEach(row -> {
                ruleValues.add((RuleValue) row);
            });
        });
        return ruleValues;
    }
}
