package com.umar.apps.rule.service.api.core;

import com.umar.apps.rule.*;
import com.umar.apps.rule.api.Rule;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.umar.apps.rule.RuleAttribute.ATTRIB$ATTRIB;
import static com.umar.apps.rule.RuleAttribute.ATTRIB$ATTRIB_NAME;
import static com.umar.apps.rule.RuleValue.RULE_VALUE$OPERAND;

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
        BusinessRule persistedRule = findExistingRule(businessRule);
        if(null != persistedRule) {
            if(!persistedRule.equals(businessRule)) {
                logger.info("Rule exist in database. Firing update for the given set.");
                fireUpdate(persistedRule, businessRule);
            }
        } else {
            Optional<BusinessRule> optionalDBBusinessRule = ruleDao.findByNameAndType(ruleName, ruleType);
            if(optionalDBBusinessRule.isPresent()){
                BusinessRule dbRule = optionalDBBusinessRule.get();
                logger.info("Rule {} exist in database for ruleName: {}, ruleType: {}, ", dbRule, ruleName, ruleType);
                fireUpdate(dbRule, businessRule);
                persistedRule = businessRule;
            }else{
                Set<RuleAttribute> ruleAttributes = businessRule.getRuleAttributes();
                doInJPA(entityManager -> {
                    Session session = entityManager.unwrap(Session.class);
                    for(RuleAttribute ruleAttribute: ruleAttributes) {
                        Optional<RuleAttribute> optionalDBRuleAttribute = ruleAttributeDao.findRuleAttribute(ruleAttribute.getAttributeName(), ruleType);
                        if(optionalDBRuleAttribute.isPresent()) {
                            RuleAttribute dbRuleAttribute = optionalDBRuleAttribute.get();
                            assignExistingAttribute(businessRule, ruleAttribute, dbRuleAttribute);
                            fireUpdate(businessRule, entityManager, session, dbRuleAttribute);
                        } else {
                            Set<RuleValue> ruleValues = ruleAttribute.getRuleValues();
                            for (RuleValue ruleValue: ruleValues) {
                                Optional<RuleValue> optionalDBRuleValue = ruleValueDao.findByOperand(ruleValue.getOperand());
                                if(optionalDBRuleValue.isPresent()) {
                                    RuleValue dbRuleValue = optionalDBRuleValue.get();
                                    ruleAttribute.addRuleValue(dbRuleValue);
                                    fireUpdate(ruleAttribute, dbRuleValue, entityManager, session);
                                    session.find(RuleValue.class, dbRuleValue.getId());
                                }
                            }
                        }
                    }
                    entityManager.persist(businessRule);
                    entityManager.flush();
                }, ruleDao);
                return businessRule;
            }
        }
        return persistedRule;
    }

    private void assignExistingAttribute(BusinessRule businessRule, RuleAttribute ruleAttribute, RuleAttribute dbRuleAttribute) {
        Set<RuleValue> dbRuleValues = dbRuleAttribute.getRuleValues();
        Set<RuleValue> inComingRuleValues = ruleAttribute.getRuleValues();
        dbRuleValues.addAll(inComingRuleValues);
        businessRule.getRuleAttributes().clear();
        businessRule.getRuleAttributes().add(dbRuleAttribute);
    }

    private void fireUpdate(BusinessRule persistedRule, BusinessRule businessRule) {
        Set<RuleAttribute> ruleAttributes = businessRule.getRuleAttributes().stream()
                .collect(Collectors.toCollection(()-> new TreeSet<>(Comparator.comparing(RuleAttribute::getAttributeName))));
        Set<RuleAttribute> dbRuleAttributes = persistedRule.getRuleAttributes().stream()
                .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(RuleAttribute::getAttributeName))));
        Set<RuleValue> values = new HashSet<>();
        Set<RuleValue> dbValues = new HashSet<>();
        /*
            We only need the attribute and value that are coming for update/insert.
         */
        extractValues(ruleAttributes, values);
        extractValues(dbRuleAttributes, dbValues);
        retainExistingAndAddNewValues(dbRuleAttributes,ruleAttributes);
        retainExistingAndAddNewValues(dbValues, values);
        assignValuesToRuleAttributes(dbRuleAttributes, dbValues);
        businessRule = persistedRule;
        businessRule.getRuleAttributes().clear();
        businessRule.setRuleAttributes(dbRuleAttributes);
        fireUpdate(businessRule);
    }

    private void assignValuesToRuleAttributes(Set<RuleAttribute> dbRuleAttributes, Set<RuleValue> dbValues) {
        for (RuleAttribute ruleAttribute: dbRuleAttributes) {
            for(RuleValue ruleValue: dbValues) {
                if(ruleAttribute.equals(ruleValue.getRuleAttribute())) {
                    ruleAttribute.addRuleValue(ruleValue);
                }
            }
        }
    }

    private <T> void retainExistingAndAddNewValues(Set<T> dbValues, Set<T> values) {
        Set<T> existingVals = new HashSet<>(0);
        for (T dbValue: dbValues) {
            //RuleValues's equals method compares by operand
            //id can be safely ignored.
            for(T value: values) {
                if(dbValue.equals(value)) {
                    //always retain existing values
                    existingVals.add(dbValue);
                }
            }
        }
        dbValues.clear();
        dbValues.addAll(existingVals);
        dbValues.addAll(values);//add all the new values that are not in database
    }

    private void extractValues(Set<RuleAttribute> businessRuleAttributes, Set<RuleValue> values) {
        for(RuleAttribute ruleAttribute: businessRuleAttributes) {
            Set<RuleValue> ruleValues = ruleAttribute.getRuleValues();
            values.addAll(ruleValues);
        }
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
            session.flush();
        }, ruleDao);
    }

    private void saveOrUpdate(RuleAttribute ruleAttribute, EntityManager entityManager) {
        Session session = entityManager.unwrap(Session.class);
        if(null == ruleAttribute.getId()) {
            Set<RuleValue> ruleValues = ruleAttribute.getRuleValues();
            ruleAttribute.getRuleValues().clear();
            session.save(ruleAttribute);
            for(RuleValue ruleValue: ruleValues) {
                ruleAttribute.addRuleValue(ruleValue);
            }
            for(RuleValue ruleValue: ruleValues) {
                session.saveOrUpdate(ruleValue);
            }
        }else{
            for (RuleValue ruleValue : ruleAttribute.getRuleValues()) {
                if(ruleValue.getId() != null) {
                    session.merge(ruleValue);
                }else{
                    session.saveOrUpdate(ruleValue);
                }
            }
        }
        session.merge(ruleAttribute);
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

    private BusinessRule findExistingRule(BusinessRule businessRule) {
        Set<RuleAttribute> ruleAttributes = businessRule.getRuleAttributes();
        Set<String> operands = new HashSet<>();
        Set<String> attributes = new HashSet<>();
        for (RuleAttribute ruleAttribute: ruleAttributes) {
            //this will be needed by the SQL query in order to create a list of attributes to query.
            attributes.add(ATTRIB$ATTRIB_NAME + "='"+ ruleAttribute.getAttributeName()+"'");
            Set<RuleValue> ruleValues = ruleAttribute.getRuleValues();
            for (RuleValue ruleValue: ruleValues) {
                operands.add(RULE_VALUE$OPERAND + "='" + ruleValue.getOperand() +"'");
            }
        }
        Optional<BusinessRule> optionalBusinessRule = ruleDao.findByNameTypeAttributesAndOperands(businessRule.getRuleName(), businessRule.getRuleType(), attributes, operands);
        return optionalBusinessRule.orElse(null);
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
