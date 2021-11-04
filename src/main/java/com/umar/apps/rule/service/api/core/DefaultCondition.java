package com.umar.apps.rule.service.api.core;

import com.umar.apps.rule.api.Condition;
import com.umar.apps.rule.dao.api.RuleDao;
import com.umar.apps.rule.dao.api.RuleValueDao;
import com.umar.apps.rule.domain.BusinessRule;
import com.umar.apps.rule.domain.RuleValue;
import com.umar.apps.rule.service.api.ConditionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

import static com.umar.apps.rule.api.Condition.holds;

/**
 * A default implementation of {@link ConditionService}
 * 
 * @author Mohammad Umar Ali Karimi (karimiumar@gmail.com)
 */
@Component
public class DefaultCondition implements ConditionService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultCondition.class);

    protected RuleDao ruleDao;

    @Autowired
    protected RuleValueDao ruleValueDao;

    @Autowired
    public DefaultCondition(final RuleDao ruleDao) {
        this.ruleDao = ruleDao;
    }

    @Override
    public <T> Condition getCondition(T workflowItem, String ruleName, String ruleType, boolean isActive) {
        Optional<BusinessRule> optionalBusinessRule = ruleDao.findByNameAndType(ruleName, ruleType, isActive);
        if(optionalBusinessRule.isPresent()) {
            var businessRule = optionalBusinessRule.get();
            var ruleAttributes = businessRule.getRuleAttributes();
            var ruleAttribute = ruleAttributes.iterator().next();
            var attributeName = ruleAttribute.getAttributeName();
            try {
                var field = workflowItem.getClass().getDeclaredField(attributeName);
                field.setAccessible(true);
                var value = field.get(workflowItem);
                var ruleValues = ruleValueDao.findByNameAndAttribute(ruleName, ruleType, ruleAttribute, isActive);
                logger.debug("Found RuleValues to evaluate {}", ruleValues);
                return getCondition(value, ruleValues);
            }catch (NoSuchFieldException | IllegalAccessException e) {
                //eat up
                logger.info("Exception Thrown: {}", e.getMessage());
            }
        }
        return Condition.FALSE;
    }

    private Condition getCondition(Object value, Collection<RuleValue> ruleValues){
        return holds(fact -> ruleValues.stream().anyMatch(rv -> rv.getOperand().equals(value.toString())), "Operand is not equal to: " + value.toString());
    }
}