package com.umar.apps.rule.dao.api.core;

import com.umar.apps.infra.dao.api.core.GenericJpaDao;
import com.umar.apps.rule.dao.api.RuleAttributeDao;
import com.umar.apps.rule.domain.RuleAttribute;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManagerFactory;
import java.util.*;

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
        String sql = """
                SELECT ra FROM RuleAttribute ra
                LEFT JOIN FETCH ra.businessRule br
                LEFT JOIN FETCH ra.ruleAttributeValues ravs
                LEFT JOIN FETCH ravs.ruleValue rv
                WHERE ra.attributeName = :attributeName
                AND ra.ruleType = :ruleType
                """;
        var result = doInJPA(() -> emf, entityManager -> {
            var session = entityManager.unwrap(Session.class);
            return session.createQuery(sql, RuleAttribute.class)
                    .setParameter("attributeName", attributeName)
                    .setParameter("ruleType", ruleType)
                    .getResultList();
        }, null);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
}
