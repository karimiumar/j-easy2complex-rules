package com.umar.apps.rule.service.api.core;

import com.umar.apps.rule.BusinessRule;
import com.umar.apps.rule.RuleAttribute;
import com.umar.apps.rule.RuleValue;
import com.umar.apps.rule.api.Condition;
import com.umar.apps.rule.dao.api.RuleDao;
import com.umar.apps.rule.dao.api.RuleValueDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class AndOrUtil {
    private static final Logger logger = LogManager.getLogger(AndOrUtil.class);

    public static <T> Set<Condition> createConditions(T workflowItem, RuleDao ruleDao, RuleValueDao ruleValueDao, String ruleName, String ruleType) {
        Set<Condition> conditions = new HashSet<>();
        assert ruleDao != null;
        Optional<BusinessRule> optionalBusinessRule = ruleDao.findByNameAndType(ruleName, ruleType);
        if(optionalBusinessRule.isPresent()){
            BusinessRule businessRule = optionalBusinessRule.get();
            Set<RuleAttribute> ruleAttributes = businessRule.getRuleAttributes();
            for (RuleAttribute ruleAttribute: ruleAttributes) {
                String attributeName = ruleAttribute.getAttributeName();
                try {
                    Field field = workflowItem.getClass().getDeclaredField(attributeName);
                    field.setAccessible(true);
                    Object value = field.get(workflowItem);
                    assert ruleValueDao != null;
                    Optional<RuleValue> ruleValue = ruleValueDao.findByRuleAttributeAndValue(ruleAttribute, value.toString());
                    conditions.add(condition -> ruleValue.filter(rv -> rv.getOperand().equals(value.toString())).isPresent());
                }catch (NoSuchFieldException | IllegalAccessException e) {
                    //eat up
                    logger.info("Exception Thrown: {}", e.getMessage());
                }
            }
        }
        return conditions;
    }
}
