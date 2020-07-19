package com.umar.apps.rule.dao;

import com.umar.apps.rule.BusinessRule;
import com.umar.apps.rule.dao.api.RuleAttributeDao;
import com.umar.apps.rule.dao.api.RuleDao;
import com.umar.apps.rule.dao.api.RuleValueDao;
import com.umar.apps.rule.dao.api.core.RuleAttributeDaoImpl;
import com.umar.apps.rule.dao.api.core.RuleDaoImpl;
import com.umar.apps.rule.dao.api.core.RuleValueDaoImpl;
import com.umar.apps.rule.service.api.BusinessRuleService;
import com.umar.apps.rule.service.api.core.BusinessRuleServiceImpl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class BusinessRuleDaoTest {
    final static RuleDao ruleDao = new RuleDaoImpl("test_rulesPU");
    final static RuleAttributeDao ruleAttributeDao = new RuleAttributeDaoImpl("test_rulesPU");
    final static RuleValueDao ruleValueDao = new RuleValueDaoImpl("test_rulesPU");
    final static BusinessRuleService ruleService = new BusinessRuleServiceImpl(ruleDao, ruleAttributeDao,ruleValueDao);
    @AfterAll
    public static void after() {
        ruleDao.closeEntityManagerFactory();
    }

    @BeforeAll
    public static void before() {

    }

    @Test
    public void saveRules() {
        BusinessRule cptyStpRule = createRule("Counterparty STP Rule", "NON-STP",1, "Historic Defaulter Party X", Map.of("counterParty", "java.lang.String"));
        BusinessRule cptyStpRule2 = createRule("Counterparty STP Rule", "NON-STP",1, "Lehman Brothers PLC", Map.of("counterParty", "java.lang.String"));
        BusinessRule amountStpRule = createRule("Amount STP Rule", "NON-STP", 2,String.valueOf(2300000.00), Map.of("amount", "java.lang.Double"));
        BusinessRule currencyStpRule = createRule("Currency STP Rule", "NON-STP",3, "KOD", Map.of("currency", "java.lang.String"));
        BusinessRule currencyStpRule2 = createRule("Currency STP Rule", "NON-STP",1, "YUAN", Map.of("currency", "java.lang.String"));
        BusinessRule currencyStpRule3 = createRule("Currency STP Rule", "NON-STP",2, "YEN", Map.of("currency", "java.lang.String"));
        BusinessRule nettingRule = createRule("Counterparty Netting Rule", "NETTING" ,1
                , Map.of("counterParty", "java.lang.String","currency","java.lang.String", "settlementDate", "java.time.LocalDate")
                , List.of("Historic Defaulter Party X", "KOD", LocalDate.now().plusDays(10).toString()));

        BusinessRule nettingRule2 = createRule("Counterparty Netting Rule", "NETTING" ,1
                , Map.of("counterParty", "java.lang.String","currency","java.lang.String", "settlementDate", "java.time.LocalDate")
                , List.of("Lehman Brothers PLC", "USD", LocalDate.now().plusDays(10).toString()));

        BusinessRule nettingRule3 = createRule("Counterparty Netting Rule", "NETTING" ,1
                , Map.of("counterParty", "java.lang.String","currency","java.lang.String", "settlementDate", "java.time.LocalDate")
                , List.of("Lehman Brothers PLC", "EUR", LocalDate.now().plusDays(15).toString()));

        //This should be done at the time of Rule evaluation
        /*
        cptyStpRule.setRuleAction(workflowItem -> {
            trade.setStpAllowed(false);
            trade.setComment("Counterparty marked as Non-STP");
        });
        */
    }

    private BusinessRule createRule(String ruleName, String ruleType, int priority, Map<String, String> attributeNameTypeMap, List<String> attributeValues) {
        return ruleService.createRule(ruleName,ruleType,priority,attributeNameTypeMap, attributeValues);
    }

    private BusinessRule createRule(String ruleName, String ruleType, int priority, String operand, Map<String, String> attributeNameTypeMap) {
        return ruleService.createRule(ruleName,ruleType,priority,operand,attributeNameTypeMap);
    }
}
