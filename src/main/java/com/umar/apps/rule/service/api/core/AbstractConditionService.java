package com.umar.apps.rule.service.api.core;

import com.umar.apps.rule.BusinessRule;
import com.umar.apps.rule.RuleAttribute;
import com.umar.apps.rule.RuleValue;
import com.umar.apps.rule.api.Condition;
import com.umar.apps.rule.dao.api.RuleDao;
import com.umar.apps.rule.service.api.ConditionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.*;

public abstract class AbstractConditionService implements ConditionService {

    private static final Logger logger = LogManager.getLogger(AbstractConditionService.class);

    protected RuleDao ruleDao;

    protected AbstractConditionService(){}

    @Inject
    public AbstractConditionService(final RuleDao ruleDao) {
        this.ruleDao = ruleDao;
    }

    public <T> Condition getCondition(T workflowItem, String ruleName, String ruleType) {
        //TODO: Replace with find attributes of WorkflowItem
        Optional<BusinessRule> optionalBusinessRule = ruleDao.findByNameAndType(ruleName, ruleType);
        if(optionalBusinessRule.isPresent()) {
            BusinessRule businessRule = optionalBusinessRule.get();
            Set<RuleAttribute> ruleAttributes = businessRule.getRuleAttributes();
            //There will be only one STP attribute for a given rule name
            RuleAttribute ruleAttribute = ruleAttributes.iterator().next();
            String attributeName = ruleAttribute.getAttributeName();
            try {
                Field field = workflowItem.getClass().getDeclaredField(attributeName);
                field.setAccessible(true);
                Object value = field.get(workflowItem);
                Collection<RuleValue> ruleValues = ruleDao.findByNameAndAttribute(ruleName, ruleType, ruleAttribute);
                return getCondition(value, ruleValues);
            }catch (NoSuchFieldException | IllegalAccessException e) {
                //eat up
                logger.info("Exception Thrown: {}", e.getMessage());
            }
        }
        return Condition.FALSE;
    }

    protected abstract Condition getCondition(Object value, Collection<RuleValue> ruleValues);
}
