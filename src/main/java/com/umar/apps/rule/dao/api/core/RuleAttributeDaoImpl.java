package com.umar.apps.rule.dao.api.core;

import com.umar.apps.infra.dao.api.core.GenericJpaDao;
import com.umar.apps.rule.dao.api.RuleAttributeDao;
import com.umar.apps.rule.domain.RuleAttribute;
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

    @Override
    public List<RuleAttribute> findAttributesOfRule(long ruleId) {
        return doInJPA(this::getEMF, entityManager -> {
            var sql = """
                    SELECT ra from RuleAttribute ra WHERE ra.businessRule.id = :ruleId
                    """;
            var session = entityManager.unwrap(Session.class);
            var result = session
                    .createQuery(sql, RuleAttribute.class)
                    .setParameter("ruleId", ruleId)
                    .getResultList();
            logger.debug("Found RuleAttributes {} for ruleId {} ", result, ruleId);
            return result;
        }, null);
    }

    @Override
    public Optional<RuleAttribute> findAttributeById(long id) {
        var result = doInJPA(this::getEMF, entityManager -> {
            var sql = """
                    SELECT ra FROM RuleAttribute  ra
                    LEFT JOIN FETCH ra.businessRule
                    LEFT JOIN FETCH ra.ruleAttributeValues ravs
                    LEFT JOIN FETCH ravs.ruleValue rv
                    WHERE ra.id = :id
                    """;
            return entityManager.createQuery(sql, RuleAttribute.class)
                    .setParameter("id", id)
                    .getSingleResult();
        }, null);
        return Optional.ofNullable(result);
    }

    @Override
    public void deleteRuleAttributeById(long id) {
        doInJPA(this::getEMF, entityManager -> {
            var session = entityManager.unwrap(Session.class);
            var entity = session.find(RuleAttribute.class, id);
            logger.debug("Found RuleAttribute {} to delete. ", entity);
            session.delete(entity);
            logger.debug("Deleted RuleAttribute {} successfully. ", entity);
        }, null);
    }

    @Override
    public void update(RuleAttribute ruleAttribute) {
        doInJPA(this::getEMF, entityManager -> {
            var session = entityManager.unwrap(Session.class);
            var entity = session.find(RuleAttribute.class, ruleAttribute.getId());
            logger.debug("Found RuleAttribute {} to update. ", entity);
            update(entity, ruleAttribute);
            session.saveOrUpdate(entity);
            logger.debug("Updated RuleAttribute {} successfully. ", entity);
        }, null);
    }

    private void update(RuleAttribute entity, RuleAttribute transientObj) {
        GenericBuilder.of(() -> entity)
                .with(RuleAttribute::setAttributeName, transientObj.getAttributeName())
                .with(RuleAttribute::setRuleType, transientObj.getRuleType())
                .with(RuleAttribute::setDisplayName, transientObj.getDisplayName())
                .build();
    }
}
