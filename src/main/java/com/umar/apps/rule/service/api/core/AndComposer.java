package com.umar.apps.rule.service.api.core;

import com.umar.apps.rule.api.Condition;
import com.umar.apps.rule.dao.api.RuleDao;
import com.umar.apps.rule.dao.api.RuleValueDao;
import com.umar.apps.rule.service.api.ConditionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Objects;

/**
 * Composes Logical AND objects
 * 
 * @author Mohammad Umar Ali Karimi (karimiumar@gmail.com)
 */
@Component
public class AndComposer implements ConditionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AndComposer.class);

    private final RuleValueDao ruleValueDao;
    private final RuleDao ruleDao;

    @Autowired
    public AndComposer(RuleDao ruleDao, RuleValueDao ruleValueDao) {
        this.ruleDao = ruleDao;
        this.ruleValueDao = ruleValueDao;
    }

    @Override
    public <T> Condition getCondition(T workflowItem, String ruleName, String ruleType, boolean isActive) {
        Objects.requireNonNull(workflowItem, "WorkflowItem is required");
        Objects.requireNonNull(ruleDao, "RuleDao is required");
        Objects.requireNonNull(ruleValueDao, "RuleValueDao is required");
        Objects.requireNonNull(ruleName, "RuleName is required");
        Objects.requireNonNull(ruleType, "RuleType is required");
        var conditions = AndOrUtil.createConditions(workflowItem, ruleDao, ruleValueDao, ruleName, ruleType, isActive);
        LOGGER.debug("Creating AndCondition");
        var actualCondition = Condition.FALSE;
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
        LOGGER.debug("Actual AndCondition {}", actualCondition);
        return actualCondition;
    }
}
