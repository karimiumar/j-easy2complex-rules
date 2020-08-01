package com.umar.apps.rule.service.api.core;

import com.umar.apps.rule.BusinessRule;
import com.umar.apps.rule.RuleAttribute;
import com.umar.apps.rule.RuleValue;
import com.umar.apps.rule.api.Condition;
import com.umar.apps.rule.dao.api.RuleDao;
import com.umar.apps.rule.dao.api.RuleValueDao;
import com.umar.apps.rule.service.api.ConditionService;
import com.umar.apps.rule.service.api.NettingService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.*;

public class NettingConditionService implements NettingService {

    private static final Logger logger = LogManager.getLogger(NettingConditionService.class);
    private final RuleValueDao ruleValueDao;
    private final RuleDao ruleDao;

    protected NettingConditionService(){
        ruleValueDao = null;
        ruleDao = null;
    }

    @Inject
    public NettingConditionService(RuleDao ruleDao, RuleValueDao ruleValueDao) {
        this.ruleDao = ruleDao;
        this.ruleValueDao = ruleValueDao;
    }

    protected Condition getCondition(Object value, Collection<RuleValue> ruleValues) {
        List<Condition> conditions = new ArrayList<>(0);
        for(RuleValue ruleValue: ruleValues) {
            logger.info("`{}`.equals(`{}`)", value, ruleValue.getOperand());
            conditions.add(condition -> value.toString().equals(ruleValue.getOperand()));
        }
        logger.info("Conditions.size():{}", conditions.size());
        return conditions.size() == 0? Condition.FALSE: conditions.get(0);
    }

    @Override
    public <T> Set<Condition> getNettingConditions(T workflowItem, String ruleName, String ruleType) {
        Set<Condition> conditions = new HashSet<>();
        assert ruleDao != null;
        Optional<BusinessRule> optionalBusinessRule = ruleDao.findByNameAndType(ruleName, ruleType);
        if(optionalBusinessRule.isPresent()){
            Object value = null;
            BusinessRule businessRule = optionalBusinessRule.get();
            Set<RuleAttribute> ruleAttributes = businessRule.getRuleAttributes();
            for (RuleAttribute ruleAttribute: ruleAttributes) {
                String attributeName = ruleAttribute.getAttributeName();
                try {
                    Collection<RuleValue> ruleValues = new ArrayList<>(0);
                    Field field = workflowItem.getClass().getDeclaredField(attributeName);
                    field.setAccessible(true);
                    value = field.get(workflowItem);
                    assert ruleValueDao != null;
                    ruleValueDao.findByRuleAttributeAndValue(ruleAttribute, value.toString()).ifPresent(ruleValues::add);
                    conditions.add(getCondition(value, ruleValues));
                }catch (NoSuchFieldException | IllegalAccessException e) {
                    //eat up
                    logger.info("Exception Thrown: {}", e.getMessage());
                }
            }
        }
        return conditions;
    }
}
