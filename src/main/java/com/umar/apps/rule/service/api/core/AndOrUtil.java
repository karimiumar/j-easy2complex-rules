package com.umar.apps.rule.service.api.core;

import com.umar.apps.rule.api.Condition;
import com.umar.apps.rule.dao.api.RuleDao;
import com.umar.apps.rule.dao.api.RuleValueDao;
import com.umar.apps.rule.domain.BusinessRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.umar.apps.rule.api.Condition.holds;


/**
 * A utility class for creating {@link Condition}
 * 
 * @author Mohammad Umar Ali Karimi (karimiumar@gmail.com)
 */
public class AndOrUtil {

    private static final Logger logger = LoggerFactory.getLogger(AndOrUtil.class);

    /**
     * Creates a {@link Set} of {@link Condition} for the given parameters.
     * 
     * @param <T> The Type of workflowItem
     * @param workflowItem The workflowItem
     * @param ruleDao The {@link RuleDao}
     * @param ruleValueDao The {@link RuleValueDao}
     * @param ruleName The name of the rule to lookup
     * @param ruleType The type of the rule to lookup
     * 
     * @return Returns a {@link Set} of {@link Condition}
     */
    public static <T> Set<Condition> createConditions(T workflowItem, RuleDao ruleDao, RuleValueDao ruleValueDao, String ruleName, String ruleType) {
        Objects.requireNonNull(workflowItem, "WorkflowItem is required");
        Objects.requireNonNull(ruleDao, "RuleDao is required");
        Objects.requireNonNull(ruleValueDao, "RuleValueDao is required");
        Objects.requireNonNull(ruleName, "RuleName is required");
        Objects.requireNonNull(ruleType, "RuleType is required");
        Optional<BusinessRule> optionalBusinessRule = ruleDao.findByNameAndType(ruleName, ruleType);
        Set<Condition> conditions = new HashSet<>();
        optionalBusinessRule.ifPresent(rule -> {
            var ruleAttributes = rule.getRuleAttributes();
            ruleAttributes.forEach(ruleAttribute -> {
                String attributeName = ruleAttribute.getAttributeName();
                try {
                    var field = workflowItem.getClass().getDeclaredField(attributeName);
                    field.setAccessible(true);
                    var value = field.get(workflowItem);
                    var ruleValue = ruleValueDao.findByRuleAttributeAndValue(ruleAttribute, value.toString());
                    var condition = holds(fact -> ruleValue.filter(rv -> rv.getOperand().equals(value.toString())).isPresent(), "The operand is not equal to " + value.toString());
                    conditions.add(condition);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    //eat up
                    logger.info("Exception Thrown: {}", e.getMessage());
                }
            });
        });
        return conditions;
    }
}
