package com.umar.apps.rule.service.api.core;

import com.umar.apps.rule.dao.api.RuleAttributeDao;
import com.umar.apps.rule.dao.api.RuleDao;
import com.umar.apps.rule.dao.api.RuleValueDao;
import com.umar.apps.rule.domain.BusinessRule;
import com.umar.apps.rule.domain.RuleAttribute;
import com.umar.apps.rule.domain.RuleAttributeValue;
import com.umar.apps.rule.domain.RuleValue;
import com.umar.apps.rule.service.api.BusinessRuleService;
import com.umar.apps.rule.web.exceptions.ElementAlreadyExistException;
import com.umar.apps.util.GenericBuilder;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.umar.apps.infra.dao.api.core.AbstractTxExecutor.doInJPA;

@Service
public class BusinessRuleServiceImpl implements BusinessRuleService {

    private RuleDao ruleDao;
    private RuleAttributeDao ruleAttributeDao;
    private RuleValueDao ruleValueDao;

    private static final Logger logger = LoggerFactory.getLogger(BusinessRuleServiceImpl.class);

    //Constructor needed for CDI. Do not remove
    BusinessRuleServiceImpl() {
    }

    @Autowired
    public BusinessRuleServiceImpl(final RuleDao ruleDao, final RuleAttributeDao ruleAttributeDao, final RuleValueDao ruleValueDao) {
        this.ruleDao = ruleDao;
        this.ruleAttributeDao = ruleAttributeDao;
        this.ruleValueDao = ruleValueDao;
    }

    @Override
    public void createRule(String ruleName, String ruleType, String description, int priority, boolean isActive) {
        logger.info("createRule() ruleName: {}, ruleType: {}, priority: {}, active: {}", ruleName, ruleType, priority, isActive);
        final BusinessRule businessRule = createFromScratch(ruleName, ruleType, description, priority, isActive);
        var persistedRule = findExistingRule(businessRule, isActive);
        persistedRule.ifPresentOrElse(rule -> {
                    logger.debug("Found Rule : {}", rule);
                    throw new ElementAlreadyExistException("Rule already exist:" + rule);
                },
                /*Otherwise persist the new business Rule*/
                () -> doInJPA(()-> ruleDao.getEMF(), entityManager -> { entityManager.persist(businessRule); }, null));
    }

    @Override
    public void createAttribute(BusinessRule businessRule, String attributeName, String ruleType, String displayName) {
        var ruleAttribute = new RuleAttribute();
        ruleAttribute.setAttributeName(attributeName);
        ruleAttribute.setDisplayName(displayName);
        ruleAttribute.setRuleType(ruleType);
        var persistedAttribute = findExistingAttribute(ruleAttribute);
        persistedAttribute.ifPresentOrElse(attribute -> {
            logger.debug("Found attribute {}", attribute);
            doInJPA(() -> ruleAttributeDao.getEMF(), entityManager -> {
                var session = entityManager.unwrap(Session.class);
                var attributeEntity = session.find(RuleAttribute.class, attribute.getId());
                var businessRuleEntity = session.find(BusinessRule.class, businessRule.getId());
                attributeEntity.setBusinessRule(businessRuleEntity);
                businessRuleEntity.addRuleAttribute(attributeEntity);
                entityManager.merge(attributeEntity);
            }, null);
        }, /* The Else Clause */() -> {
            logger.info("No Rule Attribute found in database. Saving RuleAttribute");
            doInJPA(() -> ruleAttributeDao.getEMF(), entityManager -> {
                var session = entityManager.unwrap(Session.class);
                var businessRuleEntity = session.find(BusinessRule.class, businessRule.getId());
                session.save(ruleAttribute);
                businessRuleEntity.addRuleAttribute(ruleAttribute);
                session.merge(businessRuleEntity);
                }, null
            );
            logger.info("Saved RuleAttribute {}:", ruleAttribute);
        });
    }

    @Override
    public void createValue(RuleAttribute ruleAttribute, String operand) {
        var ruleValue = new RuleValue();
        ruleValue.setOperand(operand);
        var optionalValue = findExistingValue(operand);
        var optionalAttribute = findExistingAttribute(ruleAttribute);
        var existingValue = optionalValue.orElse(null);
        var existingAttribute = optionalAttribute.orElse(null);
        if(null != existingValue && null != existingAttribute
                && existingValue.getRuleAttributeValues().contains(new RuleAttributeValue(existingAttribute, existingValue))) {
            logger.debug("RuleValue{} already exist for the given operand {}", existingValue, operand);
            return;
        }
        if(null == existingValue) {
            logger.debug("No RuleValue exist {} for operand", operand);
            doInJPA(()-> ruleValueDao.getEMF() ,entityManager -> {
                Session session = entityManager.unwrap(Session.class);
                var attribute = session.find(RuleAttribute.class, ruleAttribute.getId());
                logger.debug("Found RuleAttribute {} for operand {}. ", attribute, operand);
                ruleValue.addRuleAttribute(attribute);
                session.save(ruleValue);
                logger.debug("Saved Operand {} successfully", ruleValue);
            }, null);
        }
        else if(null != existingAttribute && !existingValue.getRuleAttributeValues().contains(new RuleAttributeValue(existingAttribute, existingValue))){
            logger.debug("Existing RuleAttributes {} does not contain RuleValue {} ", existingValue.getRuleAttributeValues(), existingValue);
            doInJPA(()-> ruleValueDao.getEMF(), entityManager -> {
                Session session = entityManager.unwrap(Session.class);
                var value = session.find(RuleValue.class, existingValue.getId());
                var attrib = session.find(RuleAttribute.class, existingAttribute.getId());
                value.addRuleAttribute(attrib);
                session.saveOrUpdate(value);
                logger.debug("Updated existing RuleAttributes with RuleValue {} ", value);
            }, null);
        }
        else {
            doInJPA(()-> ruleValueDao.getEMF(), entityManager -> {
                Session session = entityManager.unwrap(Session.class);
                var attribute = session.find(RuleAttribute.class, ruleAttribute.getId());
                var value = session.find(RuleValue.class, existingValue.getId());
                value.addRuleAttribute(attribute);
                session.saveOrUpdate(value);
                logger.debug("Updated existing RuleAttribute {} with RuleValue {} ", attribute,  value);
            }, null);
        }

    }

    @Override
    public List<BusinessRule> findAll() {
        return new ArrayList<>(ruleDao.findAll());
    }

    @Override
    public Optional<BusinessRule> findRuleById(long id) {
        return ruleDao.findById(id);
    }

    @Override
    public String findRuleNameById(long ruleId) {
        doInJPA(() -> ruleDao.getEMF(), entityManager -> {
            var session = entityManager.unwrap(Session.class);
            return session
                    .createQuery("SELECT ruleName FROM BusinessRule br WHERE br.id = :id", String.class)
                    .setParameter("id", ruleId)
                    .uniqueResult();
        }, null);
        return null;
    }

    @Override
    public void update(BusinessRule businessRule) {
        Objects.requireNonNull(businessRule, "Incoming BusinessRule cannot be null");
        doInJPA(() -> ruleDao.getEMF(), entityManager -> {
            var session = entityManager.unwrap(Session.class);
            var entity = session.find(BusinessRule.class, businessRule.getId());
            logger.debug("Found BusinessRule {} to update. ", entity);
            update(entity, businessRule);
            session.saveOrUpdate(entity);
            logger.debug("Updated BusinessRule {} successfully. ", entity);
        }, null);
    }

    @Override
    public void update(RuleAttribute ruleAttribute) {
        Objects.requireNonNull(ruleAttribute, "Incoming RuleAttribute cannot be null");
        doInJPA(() -> ruleAttributeDao.getEMF(), entityManager -> {
            var session = entityManager.unwrap(Session.class);
            var entity = session.find(RuleAttribute.class, ruleAttribute.getId());
            logger.debug("Found RuleAttribute {} to update. ", entity);
            update(entity, ruleAttribute);
            session.saveOrUpdate(entity);
            logger.debug("Updated RuleAttribute {} successfully. ", entity);
        }, null);
    }

    @Override
    public void update(RuleValue ruleValue) {
        Objects.requireNonNull(ruleValue, "Incoming RuleValue cannot be null");
        doInJPA(() -> ruleValueDao.getEMF(), entityManager -> {
            var session = entityManager.unwrap(Session.class);
            var entity = session.find(RuleValue.class, ruleValue.getId());
            logger.debug("Found RuleValue {} to update. ", entity);
            update(entity, ruleValue);
            session.saveOrUpdate(entity);
            logger.debug("Updated RuleValue {} successfully. ", entity);
        }, null);
    }

    @Override
    public void deleteRuleById(long id) {
        doInJPA(() -> ruleDao.getEMF(), entityManager -> {
            var session = entityManager.unwrap(Session.class);
            var entity = session.find(BusinessRule.class, id);
            logger.debug("Found BusinessRule {} to delete. ", entity);
            session.delete(entity);
            logger.debug("Deleted BusinessRule {} successfully. ", entity);
        }, null);
    }

    @Override
    public List<RuleAttribute> findAttributesOfRule(long ruleId) {
        return doInJPA(() -> ruleAttributeDao.getEMF(), entityManager -> {
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
        var result = doInJPA(() -> ruleAttributeDao.getEMF(), entityManager -> {
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
        doInJPA(() -> ruleAttributeDao.getEMF(), entityManager -> {
            var session = entityManager.unwrap(Session.class);
            var entity = session.find(RuleAttribute.class, id);
            logger.debug("Found RuleAttribute {} to delete. ", entity);
            session.delete(entity);
            logger.debug("Deleted RuleAttribute {} successfully. ", entity);
        }, null);
    }

    @Override
    public void deleteRuleValueById(long id) {
        doInJPA(() -> ruleValueDao.getEMF(), entityManager -> {
            var session = entityManager.unwrap(Session.class);
            var entity = session.find(RuleValue.class, id);
            logger.debug("Found RuleValue {} to delete. ", entity);
            session.delete(entity);
            logger.debug("Deleted RuleValue {} successfully. ", entity);
        }, null);
    }

    @Override
    public Optional<RuleAttribute> findRuleAttribute(String attributeName, String ruleType) {
        return ruleAttributeDao.findRuleAttribute(attributeName, ruleType);
    }

    @Override
    public Optional<RuleValue> findByOperand(String operand) {
        return ruleValueDao.findByOperand(operand);
    }

    @Override
    public Optional<BusinessRule> findByNameAndType(String ruleName, String ruleType, boolean isActive) {
        return ruleDao.findByNameAndType(ruleName, ruleType, isActive);
    }

    @Override
    public List<RuleValue> findValuesOf(long attributeId) {
        var sql = """
                SELECT rv FROM RuleValue rv
                LEFT JOIN FETCH rv.ruleAttributeValues ravs
                LEFT JOIN FETCH ravs.ruleAttribute
                LEFT JOIN FETCH ravs.ruleValue
                WHERE ravs.ruleAttribute.id = :attributeId
                """;
        var result = doInJPA(() -> ruleValueDao.getEMF(), entityManager ->  {
            var session = entityManager.unwrap(Session.class);
            return session.createQuery(sql, RuleValue.class)
                    .setParameter("attributeId", attributeId)
                    .getResultList();
        }, null);
        return result;
    }

    @Override
    public Optional<RuleValue> findRuleValueById(long id) {
        return ruleValueDao.findById(id);
    }


    private BusinessRule createFromScratch(String ruleName, String ruleType, String description, int priority, boolean isActive) {
        return GenericBuilder.of(BusinessRule::new)
                .with(BusinessRule::setRuleName, ruleName)
                .with(BusinessRule::setRuleType, ruleType)
                .with(BusinessRule::setDescription, description)
                .with(BusinessRule::setActive, isActive)
                .with(BusinessRule::setPriority, priority)
                .build();
    }

    private void update(BusinessRule entity, BusinessRule transientObj) {
        GenericBuilder.of(() -> entity)
                .with(BusinessRule::setRuleName, transientObj.getRuleName())
                .with(BusinessRule::setRuleType, transientObj.getRuleType())
                .with(BusinessRule::setDescription, transientObj.getDescription())
                .with(BusinessRule::setPriority, transientObj.getPriority())
                .with(BusinessRule::setActive, transientObj.isActive())
                .build();
    }

    private void update(RuleAttribute entity, RuleAttribute transientObj) {
        GenericBuilder.of(() -> entity)
                .with(RuleAttribute::setAttributeName, transientObj.getAttributeName())
                .with(RuleAttribute::setRuleType, transientObj.getRuleType())
                .with(RuleAttribute::setDisplayName, transientObj.getDisplayName())
                .build();
    }

    private void update(RuleValue entity, RuleValue transientObj) {
        GenericBuilder.of(() -> entity)
                .with(RuleValue::setOperand, transientObj.getOperand())
                .build();
    }

    private Optional<BusinessRule> findExistingRule(BusinessRule businessRule, boolean isActive) {
        return ruleDao.findByNameAndType(businessRule.getRuleName(), businessRule.getRuleType(), isActive);
    }

    private Optional<RuleAttribute> findExistingAttribute(RuleAttribute ruleAttribute) {
        return ruleAttributeDao.findRuleAttribute(ruleAttribute.getAttributeName(), ruleAttribute.getRuleType());
    }

    private Optional<RuleValue>  findExistingValue(String operand) {
        return ruleValueDao.findByOperand(operand);
    }

}
