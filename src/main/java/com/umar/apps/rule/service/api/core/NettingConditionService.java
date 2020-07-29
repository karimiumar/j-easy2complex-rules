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

public class NettingConditionService extends AbstractConditionService implements ConditionService {

    private static final Logger logger = LogManager.getLogger(NettingConditionService.class);

    protected NettingConditionService(){}

    @Inject
    public NettingConditionService(RuleDao ruleDao) {
        super(ruleDao);
    }

    @Override
    public <T> Condition getCondition(T workflowItem, String ruleName, String ruleType) {
        Optional<BusinessRule> optionalBusinessRule = ruleDao.findByNameAndType(ruleName, ruleType);
        if(optionalBusinessRule.isPresent()){
            Object value = null;
            Collection<RuleValue> ruleValues = new ArrayList<>(0);
            BusinessRule businessRule = optionalBusinessRule.get();
            /*Set<RuleAttribute> ruleAttributes = businessRule.getRuleAttributes();
            for (RuleAttribute ruleAttribute: ruleAttributes) {
                String attributeName = ruleAttribute.getAttributeName();
                try {
                    Field field = workflowItem.getClass().getDeclaredField(attributeName);
                    field.setAccessible(true);
                    value = field.get(workflowItem);
                    ruleValues.addAll(ruleDao.findByNameAndAttribute(ruleName, ruleType, ruleAttribute));
                }catch (NoSuchFieldException | IllegalAccessException e) {
                    //eat up
                    logger.info("Exception Thrown: {}", e.getMessage());
                }
            }*/
            return getCondition(value, ruleValues);
        }
        return Condition.FALSE;
    }

    @Override
    protected Condition getCondition(Object value, Collection<RuleValue> ruleValues) {
        List<Condition> conditions = new ArrayList<>(0);
        for(RuleValue ruleValue: ruleValues) {
            logger.info("`{}`.equals(`{}`)", value, ruleValue.getOperand());
            conditions.add(condition -> value.equals(ruleValue.getOperand()));
        }
        logger.info("Conditions.size():{}", conditions.size());
        return conditions.size() == 0? Condition.FALSE: conditions.get(0);
    }
}
