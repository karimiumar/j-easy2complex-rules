package com.umar.apps.rule.service.api.core;

import com.umar.apps.rule.dao.api.RuleAttributeDao;
import com.umar.apps.rule.dao.api.RuleDao;
import com.umar.apps.rule.dao.api.RuleValueDao;
import com.umar.apps.rule.domain.BusinessRule;
import com.umar.apps.rule.domain.RuleAttribute;
import com.umar.apps.rule.domain.RuleValue;
import com.umar.apps.rule.service.api.BusinessRuleService;
import com.umar.apps.rule.web.exceptions.ElementAlreadyExistException;
import com.umar.apps.rule.web.exceptions.NoSuchElementFoundException;
import com.umar.apps.util.GenericBuilder;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.umar.apps.infra.dao.api.core.AbstractTxExecutor.doInJPA;

@Service
public class BusinessRuleServiceImpl implements BusinessRuleService {

    private final RuleDao ruleDao;
    private final RuleAttributeDao ruleAttributeDao;
    private final RuleValueDao ruleValueDao;

    private static final Logger logger = LoggerFactory.getLogger(BusinessRuleServiceImpl.class);

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
                () -> doInJPA(ruleDao::getEMF, entityManager -> { entityManager.persist(businessRule); }, null));
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
            doInJPA(ruleAttributeDao::getEMF, entityManager -> {
                var session = entityManager.unwrap(Session.class);
                var attributeEntity = session.find(RuleAttribute.class, attribute.getId());
                var businessRuleEntity = session.find(BusinessRule.class, businessRule.getId());
                attributeEntity.setBusinessRule(businessRuleEntity);
                businessRuleEntity.addRuleAttribute(attributeEntity);
                session.merge(attributeEntity);
            }, null);
        }, /* The Else Clause */() -> {
            logger.debug("No Rule Attribute found in database. Saving RuleAttribute");
            doInJPA(ruleAttributeDao::getEMF, entityManager -> {
                var session = entityManager.unwrap(Session.class);
                var businessRuleEntity = session.find(BusinessRule.class, businessRule.getId());
                businessRuleEntity.addRuleAttribute(ruleAttribute);
                session.save(ruleAttribute);
                session.saveOrUpdate(businessRuleEntity);
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
        if(null != existingValue && existingValue.getRuleAttribute().equals(existingAttribute)) {
            logger.debug("RuleValue{} already exist for the given operand {}", existingValue, operand);
            return;
        }
        if(null == existingValue) {
            logger.debug("No RuleValue exist {} for operand", operand);
            doInJPA(ruleValueDao::getEMF, entityManager -> {
                Session session = entityManager.unwrap(Session.class);
                var attribute = session.find(RuleAttribute.class, ruleAttribute.getId());
                logger.debug("Found RuleAttribute {} for operand {}. ", attribute, operand);
                ruleValue.setRuleAttribute(attribute);
                session.save(ruleValue);
                logger.debug("Saved Operand {} successfully", ruleValue);
            }, null);
        }
        else if(null != existingAttribute && !existingValue.getRuleAttribute().equals(existingAttribute)){
            logger.debug("Existing RuleAttributes {} does not contain RuleValue {} ", existingValue.getRuleAttribute(), existingValue);
            doInJPA(ruleValueDao::getEMF, entityManager -> {
                Session session = entityManager.unwrap(Session.class);
                var value = session.find(RuleValue.class, existingValue.getId());
                var attrib = session.find(RuleAttribute.class, existingAttribute.getId());
                value.setRuleAttribute(attrib);
                session.saveOrUpdate(value);
                logger.debug("Updated existing RuleAttributes with RuleValue {} ", value);
            }, null);
        }
        else {
            doInJPA(ruleValueDao::getEMF, entityManager -> {
                Session session = entityManager.unwrap(Session.class);
                var attribute = session.find(RuleAttribute.class, ruleAttribute.getId());
                var value = session.find(RuleValue.class, existingValue.getId());
                value.setRuleAttribute(attribute);
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
        return ruleDao.findByIdWithSubgraphs(id);
    }

    @Override
    public String findRuleNameById(long ruleId) {
        return ruleDao.findRuleNameById(ruleId);
    }

    @Override
    public void update(BusinessRule businessRule) {
        Objects.requireNonNull(businessRule, "Incoming BusinessRule cannot be null");
        ruleDao.update(businessRule);
    }

    @Override
    public void update(RuleAttribute ruleAttribute) {
        Objects.requireNonNull(ruleAttribute, "Incoming RuleAttribute cannot be null");
        ruleAttributeDao.update(ruleAttribute);
    }

    @Override
    public void update(RuleValue ruleValue) {
        Objects.requireNonNull(ruleValue, "Incoming RuleValue cannot be null");
        ruleValueDao.update(ruleValue);
    }

    @Override
    public void deleteRuleById(long id) {
        ruleDao.deleteById(id);
    }

    @Override
    public List<RuleAttribute> findAttributesOfRule(long ruleId) {
        return ruleAttributeDao.findAttributesOfRule(ruleId);
    }

    @Override
    public Optional<RuleAttribute> findAttributeById(long id) {
        return ruleAttributeDao.findAttributeById(id);
    }

    @Override
    public void deleteRuleAttributeById(long id) {
        ruleAttributeDao.deleteRuleAttributeById(id);
    }

    @Override
    public void deleteRuleValueById(long id) {
        ruleValueDao.deleteById(id);
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
        return ruleValueDao.findValuesOf(attributeId);
    }

    @Override
    public Optional<RuleValue> findRuleValueById(long id) {
        return ruleValueDao.findById(id);
    }

    @Override
    public Optional<BusinessRule> findRuleByIdWithSubgraphs(long id) {
        return ruleDao.findByIdWithSubgraphs(id);
    }

    @Override
    public BusinessRule save(BusinessRule rule) {
        var ruleName = rule.getRuleName();
        var ruleType = rule.getRuleType();
        var description = rule.getDescription();
        var priority = rule.getPriority();
        var active = rule.isActive();
        //First Save The Rule
        createRule(ruleName, ruleType, description, priority, active);
        //Find  the saved rule from database.
        var optRule = findByNameAndType(ruleName, ruleType, active);
        optRule.ifPresentOrElse( businessRule -> {
            var attribs = rule.getRuleAttributes();
            attribs.forEach(attr -> {
                var attribName = attr.getAttributeName();
                var displayText = attr.getDisplayName();
                //First Save The attribute. Use the persisted BusinessRule
                createAttribute(businessRule, attribName, ruleType, displayText);
                //Find the saved attribute from database
                var optAttr = findRuleAttribute(attribName, ruleType);
                optAttr.ifPresentOrElse(ruleAttr -> {
                    var ravs = attr.getRuleValues();
                    ravs.forEach(rv -> {
                        var operand = rv.getOperand();
                        //Use the persisted RuleAttribute
                        createValue(ruleAttr, operand);
                    });
                },() -> {});
            });
        },() -> {});
        return optRule.orElseThrow(() -> new NoSuchElementFoundException(String.format("Unable to find an attribute  with the given name %s  and type %s", ruleName, ruleType)));
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
