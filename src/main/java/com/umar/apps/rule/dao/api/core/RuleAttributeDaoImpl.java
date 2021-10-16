package com.umar.apps.rule.dao.api.core;

import com.umar.apps.infra.dao.api.core.GenericJpaDao;
import com.umar.apps.rule.dao.api.RuleAttributeDao;
import com.umar.apps.rule.domain.RuleAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.umar.apps.infra.dao.api.core.AbstractTxExecutor.doInJPA;

/**
 * A default implementation of {@link RuleAttributeDao} interface
 * 
 * @author Mohammad Umar Ali Karimi (karimiumar@gmail.com)
 */
@Repository
public class RuleAttributeDaoImpl extends GenericJpaDao<RuleAttribute, Long> implements RuleAttributeDao {

    private static final Logger logger = LoggerFactory.getLogger(RuleAttributeDaoImpl.class);

    //Constructor needed for CDI. Do not remove
    RuleAttributeDaoImpl() {
        this(null);
    }

    @Autowired
    public RuleAttributeDaoImpl(EntityManagerFactory entityManagerFactory) {
        super(RuleAttribute.class, entityManagerFactory);
    }

    @Override
    public Collection<RuleAttribute> findAll() {
        logger.info("findAll()");
        Collection<RuleAttribute> ruleValues = new ArrayList<>(Collections.emptyList());
        doInJPA(() -> emf, entityManager -> {
            List<?> result = entityManager.createQuery("SELECT ra FROM RuleAttribute ra").getResultList();
            result.forEach(row -> {
                ruleValues.add((RuleAttribute) row);
            });
        }, null);
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
        doInJPA(() -> emf, entityManager -> {
            try {
                result.set(entityManager.createQuery(sql)
                        .setParameter("attributeName", attributeName)
                        .setParameter("ruleType", ruleType)
                        .getSingleResult()
                );
            }catch (NoResultException ex) {
                //Simply ignore it. This is expected when no data exist.
            }
        }, null);
        if(null != result.get()) {
            RuleAttribute ruleAttribute = (RuleAttribute) result.get();
            return Optional.of(ruleAttribute);
        }
        return Optional.empty();
    }
}
