package com.umar.apps.rule.service.api.core;

import com.umar.apps.rule.api.Condition;
import com.umar.apps.rule.dao.api.RuleDao;
import com.umar.apps.rule.dao.api.RuleValueDao;
import com.umar.apps.rule.service.api.ConditionService;

import java.util.Objects;

/**
 * It composes logical OR 
 * 
 * @author Mohammad Umar Ali Karimi (karimiumar@gmail.com)
 */
public class OrComposer implements ConditionService {

    private final RuleDao ruleDao;
    private final RuleValueDao ruleValueDao;

    OrComposer(){
        ruleValueDao = null;
        ruleDao = null;
    }

    public OrComposer(final RuleDao ruleDao, final RuleValueDao ruleValueDao) {
        this.ruleDao = ruleDao;
        this.ruleValueDao = ruleValueDao;
    }

    @Override
    public <T> Condition getCondition(T workflowItem, String ruleName, String ruleType, boolean isActive) {
        Objects.requireNonNull(ruleDao, "RuleDao is null");
        Objects.requireNonNull(ruleValueDao, "RuleValueDao is null");
        Objects.requireNonNull(ruleName, "RuleName is required");
        Objects.requireNonNull(ruleType, "RuleType is required");
        var conditions = AndOrUtil.createConditions(workflowItem, ruleDao, ruleValueDao, ruleName, ruleType, isActive);
        var actualCondition = Condition.FALSE;
        int count = 0;
        int size = conditions.size(); //"5" == 5 , "Age" == "Age", "xyz" == "abc"
        if(size > 0) {
            var iterator = conditions.iterator();
            Condition prevCondition;
            while (count < size) {
                var current = iterator.next(); // value == 5, value == "Age", value = "abc"
                if(count == 0) {
                    actualCondition = current; //actual -> Condition(value.equals(5))
                }
                else if (count > 0) {
                    prevCondition = actualCondition; //prev = Condition("5".equals(5)), prev = Condition("5".equals(5)) || Condition("Age".equals("Age"))
                    actualCondition = prevCondition.or(current); // Condition(Condition("5".equals(5)) || Condition("Age".equals("Age"))) || Condition("abc".equals("abc"))
                }
                count++;
            }
        }
        return actualCondition;
    }
}
