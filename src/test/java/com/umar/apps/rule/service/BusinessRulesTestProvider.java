package com.umar.apps.rule.service;

import com.umar.apps.rule.BusinessRule;
import com.umar.apps.rule.dao.api.RuleDao;
import com.umar.apps.rule.service.api.BusinessRuleService;

public class BusinessRulesTestProvider {

    public static void createSomeRulesAndAttributes(RuleDao ruleDao, BusinessRuleService ruleService) {
        createRule("Counterparty STP Rule", "NON-STP",1, ruleService);
        BusinessRule businessRule = ruleDao.findByNameAndType("Counterparty STP Rule", "NON-STP").orElseThrow();
        createAttribute(businessRule, "counterParty", "NON-STP", "Counter Party", ruleService);

        createRule("Currency STP Rule", "NON-STP",3, ruleService);
        BusinessRule currencyBusinessRule = ruleDao.findByNameAndType("Currency STP Rule", "NON-STP").orElseThrow();
        createAttribute(currencyBusinessRule, "currency", "NON-STP", "Currency", ruleService);

        createRule("Amount STP Rule", "NON-STP", 2, ruleService);
        BusinessRule amountBusinessRule = ruleDao.findByNameAndType("Amount STP Rule", "NON-STP").orElseThrow();
        createAttribute(amountBusinessRule, "amount", "NON-STP", "Amount", ruleService);

        createRule("Counterparty Netting Rule", "NETTING" ,1, ruleService);
        BusinessRule cptyNettingRule = ruleDao.findByNameAndType("Counterparty Netting Rule", "NETTING").orElseThrow();
        createAttribute(cptyNettingRule, "counterParty", "NETTING", "Counter Party", ruleService);

        createRule("Currency Netting Rule", "NETTING" ,1, ruleService);
        BusinessRule currencyNettingRule = ruleDao.findByNameAndType("Currency Netting Rule", "NETTING").orElseThrow();
        createAttribute(currencyNettingRule, "currency", "NETTING", "Currency", ruleService);

        createRule("Settlement Date Netting Rule", "NETTING" ,1, ruleService);
        BusinessRule stmtDateNettingRule = ruleDao.findByNameAndType("Settlement Date Netting Rule", "NETTING").orElseThrow();
        createAttribute(stmtDateNettingRule, "settlementDate", "NETTING", "Settlement Date", ruleService);
    }

    public static void createRule(String ruleName, String ruleType, int priority, BusinessRuleService ruleService) {
        ruleService.createRule(ruleName,ruleType,priority);
    }

    public static void createAttribute(BusinessRule businessRule, String attributeName, String ruleType, String displayName, BusinessRuleService ruleService) {
        ruleService.createAttribute(businessRule, attributeName, ruleType, displayName);
    }
}
