package com.umar.apps.rule.dao.api;

import com.umar.apps.rule.domain.BusinessRule;
import com.umar.apps.rule.service.api.BusinessRuleService;

public class CashflowRulesTestProvider {

    public static void createSomeRulesAndAttributes(RuleDao ruleDao, BusinessRuleService ruleService) {
        createRule("Counterparty STP Rule", "NON-STP",1, ruleService, true);
        BusinessRule businessRule = ruleDao.findByNameAndType("Counterparty STP Rule", "NON-STP", true).orElseThrow();
        createAttribute(businessRule, "counterParty", "NON-STP", "Counter Party", ruleService);

        createRule("Currency STP Rule", "NON-STP",3, ruleService, true);
        BusinessRule currencyBusinessRule = ruleDao.findByNameAndType("Currency STP Rule", "NON-STP", true).orElseThrow();
        createAttribute(currencyBusinessRule, "currency", "NON-STP", "Currency", ruleService);

        createRule("Amount STP Rule", "NON-STP", 2, ruleService, true);
        BusinessRule amountBusinessRule = ruleDao.findByNameAndType("Amount STP Rule", "NON-STP", true).orElseThrow();
        createAttribute(amountBusinessRule, "amount", "NON-STP", "Amount", ruleService);

        createRule("Counterparty Netting Rule", "NETTING" ,1, ruleService, true);
        BusinessRule cptyNettingRule = ruleDao.findByNameAndType("Counterparty Netting Rule", "NETTING", true).orElseThrow();
        createAttribute(cptyNettingRule, "counterParty", "NETTING", "Counter Party", ruleService);

        createRule("Currency Netting Rule", "NETTING" ,1, ruleService, true);
        BusinessRule currencyNettingRule = ruleDao.findByNameAndType("Currency Netting Rule", "NETTING", true).orElseThrow();
        createAttribute(currencyNettingRule, "currency", "NETTING", "Currency", ruleService);

        createRule("Settlement Date Netting Rule", "NETTING" ,1, ruleService, true);
        BusinessRule stmtDateNettingRule = ruleDao.findByNameAndType("Settlement Date Netting Rule", "NETTING", true).orElseThrow();
        createAttribute(stmtDateNettingRule, "settlementDate", "NETTING", "Settlement Date", ruleService);
    }

    public static void createRule(String ruleName, String ruleType, int priority, BusinessRuleService ruleService, boolean isActive) {
        ruleService.createRule(ruleName,ruleType,"testing", priority, isActive);
    }

    public static void createAttribute(BusinessRule businessRule, String attributeName, String ruleType, String displayName, BusinessRuleService ruleService) {
        ruleService.createAttribute(businessRule, attributeName, ruleType, displayName);
    }
}
