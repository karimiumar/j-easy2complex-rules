package com.umar.apps.rule.dao.api.core;

import com.umar.apps.rule.RuleAttribute;
import com.umar.apps.rule.dao.api.RuleAttributeDao;
import com.umar.apps.rule.infra.dao.api.core.GenericJpaDao;
import com.umar.simply.jdbc.dml.operations.SelectOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.NoResultException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.umar.apps.rule.RuleAttribute.*;

public class RuleAttributeDaoImpl extends GenericJpaDao<RuleAttribute, Long> implements RuleAttributeDao {

    private static final Logger logger = LogManager.getLogger(RuleAttributeDaoImpl.class);

    public RuleAttributeDaoImpl(String persistenceUnit) {
        super(RuleAttribute.class, persistenceUnit);
    }

    @Override
    public Collection<RuleAttribute> findAll() {
        logger.info("findAll()");
        Collection<RuleAttribute> ruleValues = new ArrayList<>(Collections.emptyList());
        String sql = SelectOp.create()
                .SELECT()
                .COLUMN(ATTRIB$ATTRIB)
                .FROM(ATTRIB$ALIAS)
                .getSQL();
        executeInTransaction(entityManager -> {
            List<?> result = entityManager.createQuery(sql).getResultList();
            result.forEach(row -> {
                ruleValues.add((RuleAttribute) row);
            });
        });
        return ruleValues;
    }

    @Override
    public Optional<RuleAttribute> findRuleAttribute(String attributeName, String attributeType, String ruleType) {
        logger.info("findRuleAttribute() with attributeName: {}, attributeType: {}, ruleType: {}", attributeName, attributeType, ruleType);
        AtomicReference<Object> result = new AtomicReference<>();
        String sql = SelectOp.create()
                .SELECT().COLUMN(ATTRIB$ATTRIB)
                .FROM(ATTRIB$ALIAS)
                .WHERE()
                .COLUMN(ATTRIB$ATTRIB_NAME).EQ(":attributeName")
                .AND()
                .COLUMN(ATTRIB$ATTRIB_TYPE).EQ(":attributeType")
                .AND()
                .COLUMN(ATTRIB$RULE_TYPE).EQ(":ruleType")
                .getSQL();
        executeInTransaction(entityManager -> {
            try {
                result.set(entityManager.createQuery(sql)
                        .setParameter("attributeName", attributeName)
                        .setParameter("attributeType", attributeType)
                        .setParameter("ruleType", ruleType)
                        .getSingleResult()
                );
            }catch (NoResultException ex) {
                //Simply ignore it. This is expected when no data exist.
            }
        });
        if(null != result.get()) {
            RuleAttribute ruleAttribute = (RuleAttribute) result.get();
            return Optional.of(ruleAttribute);
        }
        return Optional.empty();
    }
}
