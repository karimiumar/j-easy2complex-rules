package com.umar.apps.rule.dao.api.core;

import com.umar.apps.rule.RuleAttribute;
import com.umar.apps.rule.dao.api.RuleAttributeDao;
import com.umar.apps.rule.infra.dao.api.core.GenericJpaDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
@Named
public class RuleAttributeDaoImpl extends GenericJpaDao<RuleAttribute, Long> implements RuleAttributeDao {

    private static final Logger logger = LogManager.getLogger(RuleAttributeDaoImpl.class);

    //Constructor needed for CDI. Do not remove
    RuleAttributeDaoImpl() {
        this(null);
    }

    public RuleAttributeDaoImpl(String persistenceUnit) {
        super(RuleAttribute.class, persistenceUnit);
    }

    @Override
    public Collection<RuleAttribute> findAll() {
        logger.info("findAll()");
        Collection<RuleAttribute> ruleValues = new ArrayList<>(Collections.emptyList());
        executeInTransaction(entityManager -> {
            List<?> result = entityManager.createQuery("SELECT ra FROM RuleAttribute ra").getResultList();
            result.forEach(row -> {
                ruleValues.add((RuleAttribute) row);
            });
        });
        return ruleValues;
    }

    @Override
    public Optional<RuleAttribute> findRuleAttribute(String attributeName, String ruleType) {
        logger.info("findRuleAttribute() with attributeName: {}, ruleType: {}", attributeName, ruleType);
        AtomicReference<Object> result = new AtomicReference<>();
        String sql = """
                SELECT ra FROM RuleAttribute ra
                WHERE ra.attributeName = : attributeName
                AND ra.ruleType = :ruleType
                """;
        executeInTransaction(entityManager -> {
            try {
                result.set(entityManager.createQuery(sql)
                        .setParameter("attributeName", attributeName)
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
