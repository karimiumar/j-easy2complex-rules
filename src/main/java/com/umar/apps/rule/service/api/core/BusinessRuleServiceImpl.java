package com.umar.apps.rule.service.api.core;

import com.umar.apps.rule.dao.api.RuleAttributeDao;
import com.umar.apps.rule.dao.api.RuleDao;
import com.umar.apps.rule.dao.api.RuleValueDao;
import com.umar.apps.rule.domain.BusinessRule;
import com.umar.apps.rule.domain.RuleAttribute;
import com.umar.apps.rule.domain.RuleAttributeValue;
import com.umar.apps.rule.domain.RuleValue;
import com.umar.apps.rule.rest.exceptions.ElementAlreadyExistException;
import com.umar.apps.rule.rest.exceptions.NoSuchElementFoundException;
import com.umar.apps.rule.service.api.BusinessRuleService;
import com.umar.apps.util.GenericBuilder;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
    public void createRule(String ruleName, String ruleType, int priority) {
        logger.info("createRule() ruleName: {}, ruleType: {}, priority: {}", ruleName, ruleType, priority);
        final BusinessRule businessRule = createFromScratch(ruleName, ruleType, priority);
        var persistedRule = findExistingRule(businessRule);
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
            attribute.setBusinessRule(businessRule);
            businessRule.addRuleAttribute(attribute);
            doInJPA(() -> ruleAttributeDao.getEMF(), entityManager -> {entityManager.merge(attribute);}, null);
        }, () -> {
            logger.info("No Rule Attribute found in database. Saving RuleAttribute");
            doInJPA(() -> ruleAttributeDao.getEMF(), entityManager -> {
                var session = entityManager.unwrap(Session.class);
                session.save(ruleAttribute);
                businessRule.addRuleAttribute(ruleAttribute);
                session.merge(businessRule);
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
            doInJPA(()-> ruleValueDao.getEMF() ,entityManager -> {
                Session session = entityManager.unwrap(Session.class);
                RuleAttribute attribute = session.find(RuleAttribute.class, ruleAttribute.getId());
                ruleValue.addRuleAttribute(attribute);
                session.save(ruleValue);
            }, null);
        }
        else if(null != existingAttribute && !existingValue.getRuleAttributeValues().contains(new RuleAttributeValue(existingAttribute, existingValue))){
            doInJPA(()-> ruleValueDao.getEMF(), entityManager -> {
                Session session = entityManager.unwrap(Session.class);
                var value = session.find(RuleValue.class, existingValue.getId());
                var attrib = session.find(RuleAttribute.class, existingAttribute.getId());
                value.addRuleAttribute(attrib);
                session.saveOrUpdate(value);
            }, null);
        }
        else {
            doInJPA(()-> ruleValueDao.getEMF(), entityManager -> {
                Session session = entityManager.unwrap(Session.class);
                var attribute = session.find(RuleAttribute.class, ruleAttribute.getId());
                var value = session.find(RuleValue.class, existingValue.getId());
                value.addRuleAttribute(attribute);
                session.saveOrUpdate(value);
            }, null);
        }

    }

    @Override
    public List<BusinessRule> findAll() {
        return List.copyOf(ruleDao.findAll());
    }

    @Override
    public BusinessRule findRuleById(long id) {
        return ruleDao.findById(id).orElseThrow(() -> new NoSuchElementFoundException("No BusinessRule exist for id:" + id));
    }


    private BusinessRule createFromScratch(String ruleName, String ruleType, int priority) {
        return GenericBuilder.of(BusinessRule::new)
                .with(BusinessRule::setRuleName, ruleName)
                .with(BusinessRule::setRuleType, ruleType)
                .with(BusinessRule::setActive, true)
                .with(BusinessRule::setPriority, priority)
                .build();
    }

    private Optional<BusinessRule> findExistingRule(BusinessRule businessRule) {
        return ruleDao.findByNameAndType(businessRule.getRuleName(), businessRule.getRuleType());
    }

    private Optional<RuleAttribute> findExistingAttribute(RuleAttribute ruleAttribute) {
        return ruleAttributeDao.findRuleAttribute(ruleAttribute.getAttributeName(), ruleAttribute.getRuleType());
    }

    private Optional<RuleValue>  findExistingValue(String operand) {
        return ruleValueDao.findByOperand(operand);
    }

}
