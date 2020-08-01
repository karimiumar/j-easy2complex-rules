package com.umar.apps.rule.service.api.core;

import com.umar.apps.rule.RuleValue;
import com.umar.apps.rule.api.Condition;
import com.umar.apps.rule.dao.api.RuleDao;
import com.umar.apps.rule.service.api.ConditionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CurrencyConditionService extends AbstractConditionService implements ConditionService {

    private static final Logger logger = LogManager.getLogger(CurrencyConditionService.class);

    protected CurrencyConditionService(){}

    @Inject
    public CurrencyConditionService(final RuleDao ruleDao) {
        super(ruleDao);
    }

    @Override
    protected Condition getCondition(Object value, Collection<RuleValue> ruleValues) {
        List<Condition> conditions = new ArrayList<>(0);
        for(RuleValue ruleValue: ruleValues) {
            if(value.equals(ruleValue.getOperand())) {
                logger.info("`{}`.equals(`{}`)", value, ruleValue.getOperand());
                conditions.add(condition -> value.equals(ruleValue.getOperand()));
            }
        }
        logger.info("Conditions.size():{}", conditions.size());
        return conditions.size() == 0? Condition.FALSE: conditions.get(0);
    }

}
