package com.umar.apps.rule.service.api.core;

import com.umar.apps.rule.*;
import com.umar.apps.rule.dao.api.RuleAttributeDao;
import com.umar.apps.rule.dao.api.RuleDao;
import com.umar.apps.rule.dao.api.RuleValueDao;
import com.umar.apps.rule.infra.dao.api.GenericDao;
import com.umar.apps.rule.service.api.BusinessRuleService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.rule.Rule;
import org.hibernate.Session;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
    public void createRule(String ruleName, String ruleType, int priority) {
        logger.info("createRule() ruleName: {}, ruleType: {}, priority: {}", ruleName, ruleType, priority);
        final BusinessRule businessRule = createFromScratch(ruleName, ruleType, priority);
        BusinessRule persistedRule = findExistingRule(businessRule);
        if(null == persistedRule) {
            doInJPA(entityManager -> ruleDao.save(businessRule), ruleDao);
        }
    }

    @Override
    public void createAttribute(BusinessRule businessRule, String attributeName, String ruleType, String displayName) {
        RuleAttribute ruleAttribute = new RuleAttribute();
        ruleAttribute.setAttributeName(attributeName);
        ruleAttribute.setDisplayName(displayName);
        ruleAttribute.setRuleType(ruleType);
        RuleAttribute persistedAttribute = findExistingAttribute(ruleAttribute);
        if(null == persistedAttribute) {
            logger.info("No Rule Attribute found in database. Saving RuleAttribute");
            doInJPA(entityManager -> ruleAttributeDao.save(ruleAttribute), ruleAttributeDao);
            logger.info("No Rule Attribute found in database. Saved RuleAttribute {}:", ruleAttribute);
        }else {
            persistedAttribute.setBusinessRule(businessRule);
            RuleAttribute finalPersistedAttribute = persistedAttribute;
            doInJPA(entityManager -> ruleAttributeDao.merge(finalPersistedAttribute), ruleAttributeDao);
        }
        persistedAttribute = findExistingAttribute(ruleAttribute);
        RuleAttribute finalPersistedAttribute = persistedAttribute;
        doInJPA(entityManager -> {
            Session session = entityManager.unwrap(Session.class);
            entityManager.find(RuleAttribute.class, finalPersistedAttribute.getId());
            businessRule.addRuleAttribute(finalPersistedAttribute);
            session.merge(businessRule);
        }, ruleAttributeDao);
    }

    @Override
    public void createValue(RuleAttribute ruleAttribute, String operand) {
        //TODO: write logic to check if incoming values same as database values then ignore updates.
        RuleValue ruleValue = new RuleValue();
        ruleValue.setOperand(operand);
        RuleValue persistedValue = findExistingValue(operand);
        if(null == persistedValue) {
            doInJPA(entityManager -> ruleValueDao.save(ruleValue), ruleValueDao);
        }
        RuleAttribute persistedAttribute = findExistingAttribute(ruleAttribute);
        if(null != persistedAttribute) {
            //TODO:FIX ME
            persistedValue = findExistingValue(operand);
            persistedValue.addRuleAttribute(persistedAttribute);
            RuleValue finalPersistedValue = persistedValue;
            doInJPA(entityManager -> {
                entityManager.find(RuleAttribute.class, persistedAttribute.getId());
                ruleValueDao.merge(finalPersistedValue);
            }, ruleValueDao);
        }else{
            persistedValue = findExistingValue(operand);
            persistedValue.addRuleAttribute(ruleAttribute);
            RuleValue finalPersistedValue = persistedValue;
            doInJPA(entityManager -> ruleValueDao.merge(finalPersistedValue), ruleValueDao);
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

    private BusinessRule createFromScratch(String ruleName, String ruleType, int priority) {
        return new BusinessRule
                .BusinessRuleBuilder(ruleName, ruleType)
                .with(businessRuleBuilder -> {
                    businessRuleBuilder.active = true;
                    businessRuleBuilder.priority = priority;
                })
                .build();
    }

    private BusinessRule findExistingRule(BusinessRule businessRule) {
        Optional<BusinessRule> optionalBusinessRule = ruleDao.findByNameAndType(businessRule.getRuleName(), businessRule.getRuleType());
        return optionalBusinessRule.orElse(null);
    }

    private RuleAttribute findExistingAttribute(RuleAttribute ruleAttribute) {
        Optional<RuleAttribute> optionalRuleAttribute = ruleAttributeDao.findRuleAttribute(ruleAttribute.getAttributeName(), ruleAttribute.getRuleType());
        return optionalRuleAttribute.orElse(null);
    }

    private RuleValue findExistingValue(String operand) {
        Optional<RuleValue> optionalRuleValue = ruleValueDao.findByOperand(operand);
        return optionalRuleValue.orElse(null);
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
