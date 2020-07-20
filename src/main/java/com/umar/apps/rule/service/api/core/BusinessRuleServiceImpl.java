package com.umar.apps.rule.service.api.core;

import com.umar.apps.rule.BusinessRule;
import com.umar.apps.rule.RuleAttribute;
import com.umar.apps.rule.RuleValue;
import com.umar.apps.rule.dao.api.RuleAttributeDao;
import com.umar.apps.rule.dao.api.RuleDao;
import com.umar.apps.rule.dao.api.RuleValueDao;
import com.umar.apps.rule.infra.dao.api.GenericDao;
import com.umar.apps.rule.service.api.BusinessRuleService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class BusinessRuleServiceImpl implements BusinessRuleService {

    private final RuleDao ruleDao;
    private final RuleAttributeDao ruleAttributeDao;
    private final RuleValueDao ruleValueDao;

    private static final Logger logger = LogManager.getLogger(BusinessRuleServiceImpl.class);

    public BusinessRuleServiceImpl(final RuleDao ruleDao, final RuleAttributeDao ruleAttributeDao, final RuleValueDao ruleValueDao) {
        this.ruleDao = ruleDao;
        this.ruleAttributeDao = ruleAttributeDao;
        this.ruleValueDao =ruleValueDao;
    }

    @Override
    public BusinessRule createRule(String ruleName, String ruleType, int priority, Map<String, String> attributeNameTypeMap, List<String> operands) {
        logger.info("createRule() ruleName: {}, ruleType: {}, priority: {}", ruleName, ruleType, priority);
        BusinessRule ruleWithOperands = findByNameTypeAndOperands(ruleName, ruleType, operands);
        BusinessRule persistedRule;
        if (null != ruleWithOperands) {
            logger.info("Rule already exist in database for the given set.");
            logger.info("Found a Rule: {} for the given ruleName: {}, ruleType: {}", ruleWithOperands, ruleName, ruleType);
            operands.forEach(operand -> fireUpdate(ruleWithOperands, operand, attributeNameTypeMap));
            return ruleWithOperands;

        } else {
            logger.info("findByNameAndType() ruleName: {}, ruleType: {}", ruleName, ruleType);
            persistedRule = findByNameAndType(ruleName, ruleType);
        }
        if (null != persistedRule) {
            logger.info("Found a Rule: {} for the given ruleName: {}, ruleType: {}", persistedRule, ruleName, ruleType);
            operands.forEach(operand -> fireUpdate(persistedRule, operand, attributeNameTypeMap));
        } else {
            final BusinessRule businessRule = createFromScratch(ruleName, ruleType, priority);
            doInJPA(entityManager -> {
                Session session = entityManager.unwrap(Session.class);
                attributeNameTypeMap.forEach((attributeName, attributeType) -> {
                    RuleAttribute ruleAttribute = new RuleAttribute();
                    ruleAttribute.setAttributeName(attributeName);
                    ruleAttribute.setAttributeType(attributeType);
                    ruleAttribute.setRuleType(ruleType);
                    Optional<RuleAttribute> optionalRuleAttribute = ruleAttributeDao.findRuleAttribute(attributeName, attributeType, businessRule.getRuleType());
                    if(optionalRuleAttribute.isPresent()) {
                        RuleAttribute ra = optionalRuleAttribute.get();
                        fireUpdate(businessRule, entityManager, session, ra);
                    }else {
                        businessRule.addRuleAttribute(ruleAttribute);
                        session.saveOrUpdate(ruleAttribute);
                    }
                });
                operands.forEach(operand -> {
                    RuleValue ruleValue = new RuleValue();
                    ruleValue.setOperand(operand);
                    Optional<RuleValue> optionalRuleValue = ruleValueDao.findByOperand(operand);
                    if(optionalRuleValue.isPresent()){
                        RuleValue rv = optionalRuleValue.get();
                        fireUpdate(businessRule, entityManager, session, rv);
                    }else {
                        businessRule.addRuleValue(ruleValue);
                        session.saveOrUpdate(ruleValue);
                    }
                });
                session.saveOrUpdate(businessRule);
            }, ruleDao);
            return businessRule;
        }
        return persistedRule;
    }

    @Override
    public BusinessRule createRule(String ruleName, String ruleType, int priority, String operand, Map<String, String> attributeNameTypeMap) {
        logger.info("createRule() ruleName: {}, ruleType: {}, priority: {}, operand: {}", ruleName, ruleType, priority, operand);
        //First search a Business Rule by Name, Type and Operand.//TODO add rule active status field to the query list
        BusinessRule persistedRule = findByNameTypeAndOperand(ruleName, ruleType, operand);
        if(null == persistedRule) {
            //Search rule by Name and Type.//TODO add rule active status field to the query list
            persistedRule = findByNameAndType(ruleName, ruleType);
        }
        if(null != persistedRule) {
            fireUpdate(persistedRule, operand, attributeNameTypeMap);
            return persistedRule;
        }
        //Else create a Rule from scratch
        final BusinessRule businessRule = createFromScratch(ruleName, ruleType, priority);
        final AtomicReference<BusinessRule> reference = new AtomicReference<>(businessRule);
        doInJPA(entityManager -> {
            logger.info("Creating a new BusinessRule: {}", businessRule);
            //Need Hibernate's Session object in order to perform save, update or saveUpdate. JPA merge/persist is not enough
            Session session = entityManager.unwrap(Session.class);
            attributeNameTypeMap.forEach((attributeName, attributeType) -> {
                RuleAttribute ruleAttribute = new RuleAttribute();
                ruleAttribute.setAttributeName(attributeName);
                ruleAttribute.setAttributeType(attributeType);
                ruleAttribute.setRuleType(ruleType);
                Optional<RuleAttribute> optionalRuleAttribute = ruleAttributeDao.findRuleAttribute(attributeName, attributeType, businessRule.getRuleType());
                if(optionalRuleAttribute.isPresent()) {
                    RuleAttribute ra = optionalRuleAttribute.get();
                    fireUpdate(businessRule, entityManager, session, ra);
                }else {
                    businessRule.addRuleAttribute(ruleAttribute);
                    session.saveOrUpdate(ruleAttribute);
                }
            });
            RuleValue ruleValue = new RuleValue();
            ruleValue.setOperand(operand);
            Optional<RuleValue> optionalRuleValue = ruleValueDao.findByOperand(operand);
            if(optionalRuleValue.isPresent()){
                RuleValue rv = optionalRuleValue.get();
                fireUpdate(businessRule, entityManager, session, rv);
            }else {
                businessRule.addRuleValue(ruleValue);
                session.saveOrUpdate(ruleValue);
            }
            session.saveOrUpdate(businessRule);
            reference.set(businessRule);
        }, ruleDao);
        return reference.get();
    }

    private void fireUpdate(BusinessRule businessRule, String operand, Map<String, String> attributeNameTypeMap) {
        doInJPA(entityManager -> {
            //Need Hibernate's Session object in order to perform save, update or saveUpdate. JPA merge/persist is not enough
            Session session = entityManager.unwrap(Session.class);
            attributeNameTypeMap.forEach((attributeName, attributeType) -> {
                RuleAttribute ruleAttribute = new RuleAttribute();
                ruleAttribute.setAttributeName(attributeName);
                ruleAttribute.setAttributeType(attributeType);
                ruleAttribute.setRuleType(businessRule.getRuleType());
                Optional<RuleAttribute> optionalRuleAttribute = ruleAttributeDao.findRuleAttribute(attributeName, attributeType, businessRule.getRuleType());
                if(optionalRuleAttribute.isPresent()){
                    RuleAttribute ra = optionalRuleAttribute.get();
                    logger.info("RuleAttribute: {} is present in db. Firing Update", ra);
                    fireUpdate(businessRule, entityManager, session, ra);
                } else {
                    businessRule.addRuleAttribute(ruleAttribute);
                }
            });
            RuleValue ruleValue = new RuleValue();
            ruleValue.setOperand(operand);
            Optional<RuleValue> optionalRuleValue = ruleValueDao.findByOperand(operand);
            if(optionalRuleValue.isPresent()) {
                RuleValue value = optionalRuleValue.get();
                logger.info("RuleValue: {} is present in db. Firing Update", value);
                fireUpdate(businessRule, entityManager, session, value);
            } else {
                businessRule.addRuleValue(ruleValue);
            }
            if (!entityManager.contains(businessRule)) {
                logger.info("""
                                    EntityManager doesn't contain BusinessRule: {} and is detached.
                                    "Reattaching it by finding from the current context.""", businessRule);
                entityManager.find(BusinessRule.class, businessRule.getId());
                session.merge(businessRule);
            }
        },ruleDao);
    }

    private void fireUpdate(BusinessRule businessRule, EntityManager entityManager, Session session, RuleValue value) {
        logger.info("RuleValue: {} is present in db", value);
        if (!entityManager.contains(value)) {
            logger.info("""
                            EntityManager doesn't contain RuleValue: {} and is detached.
                            "Reattaching it by finding from the current context.""", value);
            RuleValue rv = entityManager.find(RuleValue.class, value.getId());
            businessRule.addRuleValue(rv);
            session.saveOrUpdate(rv);
        }
    }

    private void fireUpdate(BusinessRule businessRule, EntityManager entityManager, Session session, RuleAttribute ruleAttribute) {
        logger.info("RuleAttribute: {} is present in db", ruleAttribute);
        if(!entityManager.contains(ruleAttribute)){
            logger.info("""
                EntityManager doesn't contain RuleAttribute: {} and is detached. 
                Reattaching it by finding from the current context.""", ruleAttribute);
            RuleAttribute attribute = entityManager.find(RuleAttribute.class, ruleAttribute.getId());
            businessRule.addRuleAttribute(attribute);
            session.saveOrUpdate(attribute);
        }
    }

    private BusinessRule findByNameAndType(String ruleName, String ruleType) {
        Optional<BusinessRule> optionalBusinessRule = ruleDao.findByNameAndType(ruleName, ruleType);
        return optionalBusinessRule.orElse(null);
    }

    private BusinessRule findByNameTypeAndOperand(String ruleName, String ruleType, String operand) {
        Optional<BusinessRule> optionalBusinessRule = ruleDao.findByNameTypeAndOperand(ruleName, ruleType, operand);
        return optionalBusinessRule.orElse(null);
    }

    private BusinessRule findByNameTypeAndOperands(String ruleName, String ruleType, List<String> operands) {
        Optional<BusinessRule> optionalBusinessRule = ruleDao.findByNameTypeAndOperands(ruleName, ruleType, operands);
        return optionalBusinessRule.orElse(null);
    }

    private BusinessRule createFromScratch(String ruleName, String ruleType, int priority) {
        return new BusinessRule
                .BusinessRuleBuilder(ruleName, ruleType)
                .with(businessRuleBuilder -> {
                    businessRuleBuilder.active = true;
                    businessRuleBuilder.priority = priority;
                })
                .build();
    }

    private void doInJPA(Consumer<EntityManager> consumer, GenericDao<?, Long> dao){
        EntityManager entityManager = dao.getEMF().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        if(!transaction.isActive()) {
            transaction.begin();
        }
        consumer.accept(entityManager);
        transaction.commit();
        entityManager.close();
    }
}
