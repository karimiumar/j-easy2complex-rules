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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.*;
import java.util.function.Consumer;

@ApplicationScoped
@Named
public class BusinessRuleServiceImpl implements BusinessRuleService {

    private RuleDao ruleDao;
    private RuleAttributeDao ruleAttributeDao;
    private RuleValueDao ruleValueDao;

    private static final Logger logger = LogManager.getLogger(BusinessRuleServiceImpl.class);

    //Constructor needed for CDI. Do not remove
    protected BusinessRuleServiceImpl() {
    }

    @Inject
    public BusinessRuleServiceImpl(final RuleDao ruleDao, final RuleAttributeDao ruleAttributeDao, final RuleValueDao ruleValueDao) {
        this.ruleDao = ruleDao;
        this.ruleAttributeDao = ruleAttributeDao;
        this.ruleValueDao = ruleValueDao;
    }

    @Override
    public BusinessRule createRule(String ruleName, String ruleType, int priority, Map<String, List<String>> attributeNameValuesMap) {
        logger.info("createRule() ruleName: {}, ruleType: {}, priority: {}", ruleName, ruleType, priority);
        final BusinessRule businessRule = createFromScratch(ruleName, ruleType, priority, attributeNameValuesMap);
        BusinessRule persistedRule = findByNameAndType(ruleName, ruleType);
        if(null != persistedRule) {
            if(!persistedRule.equals(businessRule)) {
                logger.info("Rule exist in database. Firing update for the given set.");
                fireUpdate(persistedRule, businessRule);
            }
        } else {
            //TODO:Needs checking for existing attributes and values
            Set<RuleAttribute> ruleAttributes = businessRule.getRuleAttributes();
            doInJPA(entityManager -> {
                Session session = entityManager.unwrap(Session.class);
                for(RuleAttribute ruleAttribute: ruleAttributes) {
                    Optional<RuleAttribute> optionalDBRuleAttribute = ruleAttributeDao.findRuleAttribute(ruleAttribute.getAttributeName(), ruleType);
                    if(optionalDBRuleAttribute.isPresent()) {
                        RuleAttribute dbRuleAttribute = optionalDBRuleAttribute.get();
                        assignExistingAttribute(businessRule, ruleAttribute, dbRuleAttribute);
                        fireUpdate(businessRule, entityManager, session, ruleAttribute);
                    } else {
                        Set<RuleValue> ruleValues = ruleAttribute.getRuleValues();
                        for (RuleValue ruleValue: ruleValues) {
                            Optional<RuleValue> optionalDBRuleValue = ruleValueDao.findByOperand(ruleValue.getOperand());
                            if(optionalDBRuleValue.isPresent()) {
                                RuleValue dbRuleValue = optionalDBRuleValue.get();
                                ruleAttribute.removeRuleValue(dbRuleValue);
                                ruleAttribute.addRuleValue(dbRuleValue);
                                //dbRuleValue.setRuleAttribute(ruleAttribute);
                                fireUpdate(ruleAttribute, dbRuleValue, entityManager, session);
                                session.find(RuleValue.class, dbRuleValue.getId());
                            }
                        }
                    }
                }
                entityManager.persist(businessRule);
            }, ruleDao);
            return businessRule;
        }
        return persistedRule;
    }

    private void assignExistingAttribute(BusinessRule businessRule, RuleAttribute ruleAttribute, RuleAttribute dbRuleAttribute) {
        Set<RuleValue> dbRuleValues = dbRuleAttribute.getRuleValues();
        Set<RuleValue> inComingRuleValues = ruleAttribute.getRuleValues();
        if (!compare(dbRuleValues, inComingRuleValues)) {
            dbRuleValues.addAll(inComingRuleValues);
        }
        businessRule.getRuleAttributes().clear();
        businessRule.getRuleAttributes().add(dbRuleAttribute);
    }

    private void fireUpdate(BusinessRule persistedRule, BusinessRule businessRule) {
        Set<RuleAttribute> ruleAttributes = businessRule.getRuleAttributes();
        Set<RuleAttribute> dbRuleAttributes = persistedRule.getRuleAttributes();
        for (RuleAttribute ruleAttribute : ruleAttributes) {
            if (attributeExists(dbRuleAttributes, ruleAttribute)) {
                Set<RuleValue> ruleValues = ruleAttribute.getRuleValues();
                ruleValues.forEach(ruleValue -> addNewValue(dbRuleAttributes, ruleValue));
            } else {
                //New Attributes added. Save with the persisted rule
                dbRuleAttributes.addAll(ruleAttributes);
            }
        }
        fireUpdate(persistedRule);
    }

    private boolean attributeExists(Set<RuleAttribute> dbRuleAttributes, RuleAttribute ruleAttribute) {
        boolean exists = dbRuleAttributes.stream().map(dbRuleAttribute -> dbRuleAttribute.equalByNameAndType(ruleAttribute)).findFirst().get();
        logger.info("Attribute Exists: {}", exists);
        return exists;
    }

    private static boolean compare(Set<?> set1, Set<?> set2) {
        if(null == set1 || null == set2) return false;
        if(set1.size() != set2.size()) return false;
        return set1.containsAll(set2);
    }

    private void addNewValue(Set<RuleAttribute> dbRuleAttributes, RuleValue ruleValue) {
        dbRuleAttributes.forEach(dbRuleAttribute -> {
            Set<RuleValue> dbRuleValues = dbRuleAttribute.getRuleValues();
            Optional<RuleValue> optionalDBRuleValue = ruleValueDao.findByOperand(ruleValue.getOperand());
            if(optionalDBRuleValue.isEmpty()) {
                dbRuleValues.add(ruleValue);
                ruleValue.setRuleAttribute(dbRuleAttribute);
            }
        });
    }

    private void fireUpdate(BusinessRule businessRule, EntityManager entityManager, Session session, RuleAttribute ruleAttribute) {
        logger.info("RuleAttribute: {} is present in db", ruleAttribute);
        if (!entityManager.contains(ruleAttribute)) {
            logger.info("""
                    EntityManager doesn't contain RuleAttribute: {} and is detached. 
                    Reattaching it by finding from the current context.""", ruleAttribute);
            RuleAttribute attribute = entityManager.find(RuleAttribute.class, ruleAttribute.getId());
            businessRule.addRuleAttribute(attribute);
            session.saveOrUpdate(attribute);
        }
    }

    private void fireUpdate(RuleAttribute ruleAttribute, RuleValue dbRuleValue, EntityManager entityManager, Session session) {
        logger.info("RuleValue: {} is present in db", dbRuleValue);
        ruleAttribute.getRuleValues().remove(dbRuleValue);
        ruleAttribute.getRuleValues().add(dbRuleValue);
        session.saveOrUpdate(dbRuleValue);
        session.saveOrUpdate(ruleAttribute);
        if (!entityManager.contains(dbRuleValue)) {
            logger.info("""
                    EntityManager doesn't contain RuleValue: {} and is detached. 
                    Reattaching it by finding from the current context.""", dbRuleValue);
            entityManager.find(RuleValue.class, dbRuleValue.getId());
        }
    }

    private void fireUpdate(BusinessRule businessRule) {
        doInJPA(entityManager -> {
            businessRule.getRuleAttributes().forEach(ruleAttribute -> saveOrUpdate(ruleAttribute, entityManager));
            Session session = entityManager.unwrap(Session.class);
            session.find(BusinessRule.class, businessRule.getId());
            session.merge(businessRule);
        }, ruleDao);
    }

    private void saveOrUpdate(RuleAttribute ruleAttribute, EntityManager entityManager) {
        Session session = entityManager.unwrap(Session.class);
        if(null == ruleAttribute.getId()) {
            session.save(ruleAttribute);
        }else{
            ruleAttribute.getRuleValues().forEach(ruleValue -> {
                if(null == ruleValue.getId()) {
                    session.saveOrUpdate(ruleValue);
                }else {
                    if (!entityManager.contains(ruleValue)) {
                        logger.info("""
                                EntityManager doesn't contain RuleValue: {} and is detached. 
                                Reattaching it by finding from the current context.""", ruleValue);
                        entityManager.find(RuleValue.class, ruleValue.getId());
                        session.merge(ruleValue);
                    }
                }
            });
            if (!entityManager.contains(ruleAttribute)) {
                logger.info("""
                            EntityManager doesn't contain RuleAttribute: {} and is detached. 
                            Reattaching it by finding from the current context.""", ruleAttribute);
                entityManager.find(RuleAttribute.class, ruleAttribute.getId());
                session.merge(ruleAttribute);
            }
        }
    }


    private BusinessRule findByNameAndType(String ruleName, String ruleType) {
        Optional<BusinessRule> optionalBusinessRule = ruleDao.findByNameAndType(ruleName, ruleType);
        return optionalBusinessRule.orElse(null);
    }

    private BusinessRule createFromScratch(String ruleName, String ruleType, int priority, Map<String, List<String>> attributeNameValuesMap) {
        BusinessRule businessRule = new BusinessRule
                .BusinessRuleBuilder(ruleName, ruleType)
                .with(businessRuleBuilder -> {
                    businessRuleBuilder.active = true;
                    businessRuleBuilder.priority = priority;
                })
                .build();
        attributeNameValuesMap.forEach((attributeName, values)-> {
            RuleAttribute ruleAttribute = new RuleAttribute();
            ruleAttribute.setAttributeName(attributeName);
            ruleAttribute.setRuleType(ruleType);
            ruleAttribute.setBusinessRule(businessRule);
            ruleAttribute.getBusinessRule().addRuleAttribute(ruleAttribute);
            values.forEach(value->{
                RuleValue ruleValue = new RuleValue();
                ruleValue.setRuleAttribute(ruleAttribute);
                ruleValue.setOperand(value);
                ruleValue.getRuleAttribute().addRuleValue(ruleValue);
            });
        });
        return businessRule;
    }

    private void doInJPA(Consumer<EntityManager> consumer, GenericDao<?, Long> dao) {
        EntityManager entityManager = dao.getEMF().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        if (!transaction.isActive()) {
            transaction.begin();
        }
        consumer.accept(entityManager);
        transaction.commit();
        entityManager.close();
    }
}
