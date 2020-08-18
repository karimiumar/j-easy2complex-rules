package com.umar.apps.rule.service.api.core;

import com.umar.apps.rule.api.Condition;
import com.umar.apps.rule.dao.api.RuleDao;
import com.umar.apps.rule.dao.api.RuleValueDao;
import com.umar.apps.rule.service.api.ConditionService;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.Set;

public class AndComposer implements ConditionService {

    private final RuleValueDao ruleValueDao;
    private final RuleDao ruleDao;

    AndComposer(){
        ruleValueDao = null;
        ruleDao = null;
    }

    @Inject
    public AndComposer(RuleDao ruleDao, RuleValueDao ruleValueDao) {
        this.ruleDao = ruleDao;
        this.ruleValueDao = ruleValueDao;
    }

    @Override
    public <T> Condition getCondition(T workflowItem, String ruleName, String ruleType) {
        assert ruleDao != null;
        Set<Condition> conditions = AndOrUtil.createConditions(workflowItem, ruleDao, ruleValueDao, ruleName, ruleType);
        Condition actualCondition = Condition.FALSE;
        int count = 0;
        int size = conditions.size(); //"5" == 5 , "Age" == "Age", "xyz" == "abc"
        if(size > 0) {
            Iterator<Condition> iterator = conditions.iterator();
            Condition prevCondition;
            while (count < size) {
                Condition current = iterator.next(); // value == 5, value == "Age", value = "abc"
                if(count == 0) {
                    actualCondition = current; //actual -> Condition(value.equals(5))
                }
                else if (count > 0) {
                    prevCondition = actualCondition; //prev = Condition("5".equals(5)), prev = Condition("5".equals(5)) && Condition("Age".equals("Age"))
                    actualCondition = prevCondition.and(current); // Condition(Condition("5".equals(5)) && Condition("Age".equals("Age"))) && Condition("abc".equals("abc"))
                }
                count++;
            }
        }
        return actualCondition;
    }
}
