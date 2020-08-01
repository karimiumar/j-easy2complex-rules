package com.umar.apps.rule.dao.api.core;

import com.umar.apps.rule.RuleAttribute;
import com.umar.apps.rule.RuleValue;
import com.umar.apps.rule.dao.api.RuleValueDao;
import com.umar.apps.rule.infra.dao.api.core.GenericJpaDao;
import com.umar.apps.rule.infra.dao.api.core.SelectFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.umar.apps.rule.RuleAttribute.ATTRIB$ALIAS;
import static com.umar.apps.rule.RuleAttribute.ATTRIB$ATTRIB;
import static com.umar.apps.rule.RuleValue.*;

@ApplicationScoped
@Named
public class RuleValueDaoImpl extends GenericJpaDao<RuleValue, Long> implements RuleValueDao {

    private static final Logger logger = LogManager.getLogger(RuleValueDaoImpl.class);

    @Inject private final SelectFunction selectFunction;

    //Constructor needed for CDI. Do not remove
    protected RuleValueDaoImpl() {
        this(null, null);
    }

    public RuleValueDaoImpl(String persistenceUnit, final SelectFunction selectFunction) {
        super(RuleValue.class, persistenceUnit);
        this.selectFunction = selectFunction;
    }

    @Override
    public Optional<RuleValue> findByOperand(String operand) {
        logger.info("findByOperand() with operand {}", operand);
        AtomicReference<Object> result = new AtomicReference<>();
        String sql = selectFunction.select()
                .SELECT().COLUMN("ruleVal")
                .FROM("RuleValue ruleVal, RuleAttributeValue rav, RuleAttribute ra")
                .WHERE().COLUMN("ruleVal = rav.ruleValue")
                .AND().COLUMN("rav.ruleAttribute = ra")
                .AND().COLUMN("ruleVal.operand").EQ(":operand")
                .getSQL();
        logger.info("SQL:{}", sql);
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
        String sql = selectFunction.select()
                .SELECT().COLUMN("ruleVal")
                .FROM("RuleValue ruleVal, RuleAttributeValue rav, RuleAttribute ra")
                .WHERE().COLUMN("ruleVal = rav.ruleValue")
                .AND().COLUMN("rav.ruleAttribute = ra")
                .AND().COLUMN("ra.attributeName").EQ(":attributeName")
                .AND().COLUMN("ra.ruleType").EQ(":ruleType")
                .AND().COLUMN("ruleVal.operand").EQ(":operand")
                .getSQL();
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
    public Collection<RuleValue> findAll() {
        logger.info("findAll()");
        Collection<RuleValue> ruleValues = new ArrayList<>(Collections.emptyList());
        String sql = selectFunction.select()
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
