package com.umar.apps.rule.service;

import com.umar.apps.rule.BusinessRule;
import com.umar.apps.rule.RuleAttribute;
import com.umar.apps.rule.RuleValue;
import com.umar.apps.rule.dao.api.RuleAttributeDao;
import com.umar.apps.rule.dao.api.RuleDao;
import com.umar.apps.rule.dao.api.RuleValueDao;
import com.umar.apps.rule.dao.api.core.RuleAttributeDaoImpl;
import com.umar.apps.rule.dao.api.core.RuleDaoImpl;
import com.umar.apps.rule.dao.api.core.RuleValueDaoImpl;
import com.umar.apps.rule.infra.dao.api.core.SelectFunction;
import com.umar.apps.rule.service.api.BusinessRuleService;
import com.umar.apps.rule.service.api.core.BusinessRuleServiceImpl;
import com.umar.simply.jdbc.dml.operations.SelectOp;
import com.umar.simply.jdbc.dml.operations.api.SqlFunctions;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BusinessRuleServiceTest {
    final static SelectFunction sqlFunctions = new SelectFunction();
    final static RuleDao ruleDao = new RuleDaoImpl("test_rulesPU", sqlFunctions);
    final static RuleAttributeDao ruleAttributeDao = new RuleAttributeDaoImpl("test_rulesPU", sqlFunctions);
    final static RuleValueDao ruleValueDao = new RuleValueDaoImpl("test_rulesPU", sqlFunctions);
    final static BusinessRuleService ruleService = new BusinessRuleServiceImpl(ruleDao, ruleAttributeDao,ruleValueDao);

    @AfterAll
    public static void after() {
        ruleDao.closeEntityManagerFactory();
    }

    @BeforeAll
    public static void before() {

    }

    @Test
    @Order(1)
    public void whenGivenDataThenCounterPartySTPRuleIsCreated() {
        BusinessRule cptyStpRule = createRule("Counterparty STP Rule", "NON-STP",1, "Historic Defaulter Party X", Map.of("counterParty", "java.lang.String"));
        assertNotEquals(-1L, cptyStpRule.getId());
        assertEquals("Counterparty STP Rule", cptyStpRule.getRuleName());
        assertEquals("NON-STP", cptyStpRule.getRuleType());
        assertEquals(1, cptyStpRule.getPriority());
        RuleAttribute ruleAttribute = ruleAttributeDao.findRuleAttribute("counterParty", "java.lang.String", "NON-STP").orElseThrow();
        assertEquals("counterParty", ruleAttribute.getAttributeName());
        assertEquals("java.lang.String", ruleAttribute.getAttributeType());
        RuleValue ruleValue = ruleValueDao.findByOperand("Historic Defaulter Party X").orElseThrow();
        assertEquals("Historic Defaulter Party X", ruleValue.getOperand());
        //This should be done at the time of Rule evaluation
        /*
        cptyStpRule.setRuleAction(workflowItem -> {
            trade.setStpAllowed(false);
            trade.setComment("Counterparty marked as Non-STP");
        });
        */
    }
    @Test @Order(2)
    public void whenGivenDataThenCounterPartySTPRuleIsAmendedAndNewOperandIsAdded() {
        BusinessRule cptyStpRule2 = createRule("Counterparty STP Rule", "NON-STP",1, "Lehman Brothers PLC", Map.of("counterParty", "java.lang.String"));
        assertNotEquals(-1L, cptyStpRule2.getId());
        assertEquals("Counterparty STP Rule", cptyStpRule2.getRuleName());
        assertEquals("NON-STP", cptyStpRule2.getRuleType());
        assertEquals(1, cptyStpRule2.getPriority());
        RuleAttribute ruleAttribute = ruleAttributeDao.findRuleAttribute("counterParty", "java.lang.String", "NON-STP").orElseThrow();
        assertEquals("counterParty", ruleAttribute.getAttributeName());
        assertEquals("java.lang.String", ruleAttribute.getAttributeType());
        RuleValue ruleValue = ruleValueDao.findByOperand("Lehman Brothers PLC").orElseThrow();
        assertEquals("Lehman Brothers PLC", ruleValue.getOperand());
    }

    @Test @Order(3)
    public void whenGivenDataThenCurrencySTPRuleIsCreated() {
        BusinessRule currencyStpRule = createRule("Currency STP Rule", "NON-STP",3, "KOD", Map.of("currency", "java.lang.String"));
        assertNotEquals(-1L, currencyStpRule.getId());
        assertEquals("Currency STP Rule", currencyStpRule.getRuleName());
        assertEquals("NON-STP", currencyStpRule.getRuleType());
        assertEquals(3, currencyStpRule.getPriority());
        RuleAttribute ruleAttribute = ruleAttributeDao.findRuleAttribute("currency", "java.lang.String", "NON-STP").orElseThrow();
        assertEquals("currency", ruleAttribute.getAttributeName());
        assertEquals("java.lang.String", ruleAttribute.getAttributeType());
        RuleValue ruleValue = ruleValueDao.findByOperand("KOD").orElseThrow();
        assertEquals("KOD", ruleValue.getOperand());
    }

    @Test @Order(4)
    public void whenGivenDataThenCurrencySTPRuleIsAmendedNewOperandsAreAdded() {
        BusinessRule currencyStpRule2 = createRule("Currency STP Rule", "NON-STP",1, "YUAN", Map.of("currency", "java.lang.String"));
        BusinessRule currencyStpRule3 = createRule("Currency STP Rule", "NON-STP",2, "YEN", Map.of("currency", "java.lang.String"));
        RuleAttribute ruleAttribute = ruleAttributeDao.findRuleAttribute("currency", "java.lang.String", "NON-STP").orElseThrow();
        assertEquals(3, currencyStpRule2.getPriority());//priority is 3 as part of previous insert of currency STP and hence same is used.
        assertEquals(3, currencyStpRule3.getPriority());
        assertEquals("currency", ruleAttribute.getAttributeName());
        assertEquals("java.lang.String", ruleAttribute.getAttributeType());
        RuleValue yuan = ruleValueDao.findByOperand("YUAN").orElseThrow();
        assertEquals("YUAN", yuan.getOperand());
        RuleValue yen = ruleValueDao.findByOperand("YEN").orElseThrow();
        assertEquals("YEN", yen.getOperand());
        assertEquals(currencyStpRule3, currencyStpRule2);
    }

    @Test @Order(5)
    public void whenGivenDataThenAmountSTPRuleIsCreated() {
        BusinessRule amountStpRule = createRule("Amount STP Rule", "NON-STP", 2,String.valueOf(2300000.00), Map.of("amount", "java.lang.Double"));
        RuleAttribute ruleAttribute = ruleAttributeDao.findRuleAttribute("amount", "java.lang.Double", "NON-STP").orElseThrow();
        assertEquals(2, amountStpRule.getPriority());
        assertEquals("amount", ruleAttribute.getAttributeName());
        assertEquals("java.lang.Double", ruleAttribute.getAttributeType());
        RuleValue amount = ruleValueDao.findByOperand(String.valueOf(2300000.0)).orElseThrow();
        assertEquals(String.valueOf(2300000.0), amount.getOperand());
    }

    @Test @Order(6)
    public void whenGivenDataThenNettingRuleIsCreated() {
        BusinessRule nettingRule = createRule("Counterparty Netting Rule", "NETTING" ,1
                , Map.of("counterParty", "java.lang.String","currency","java.lang.String", "settlementDate", "java.time.LocalDate")
                , List.of("Historic Defaulter Party X", "KOD", LocalDate.now().plusDays(10).toString()));
        assertNotEquals(-1L, nettingRule.getId());
        assertEquals("Counterparty Netting Rule", nettingRule.getRuleName());
        assertEquals("NETTING", nettingRule.getRuleType());
        assertEquals(1, nettingRule.getPriority());
        RuleAttribute ruleAttributeCpty = ruleAttributeDao.findRuleAttribute("counterParty", "java.lang.String", "NETTING").orElseThrow();
        assertEquals("java.lang.String",ruleAttributeCpty.getAttributeType());
        assertEquals("counterParty", ruleAttributeCpty.getAttributeName());
        RuleAttribute ruleAttributeCurrency = ruleAttributeDao.findRuleAttribute("currency", "java.lang.String", "NETTING").orElseThrow();
        assertEquals("java.lang.String",ruleAttributeCurrency.getAttributeType());
        assertEquals("currency", ruleAttributeCurrency.getAttributeName());
        RuleAttribute ruleAttributeSettlementDate = ruleAttributeDao.findRuleAttribute("settlementDate", "java.time.LocalDate", "NETTING").orElseThrow();
        assertEquals("java.time.LocalDate",ruleAttributeSettlementDate.getAttributeType());
        assertEquals("settlementDate", ruleAttributeSettlementDate.getAttributeName());
        assertThrows(Exception.class, ()-> ruleAttributeDao.findRuleAttribute("amount", "java.lang.Double", "NETTING").orElseThrow());
        RuleValue currency = ruleValueDao.findByOperand("KOD").orElseThrow();
        assertEquals("KOD", currency.getOperand());
        RuleValue settlementDate = ruleValueDao.findByOperand(LocalDate.now().plusDays(10).toString()).orElseThrow();
        assertEquals(LocalDate.now().plusDays(10), LocalDate.parse(settlementDate.getOperand()));
    }

    @Test @Order(7)
    public void whenGivenDataThenNettingRuleIsAmended() {
        BusinessRule nettingRule2 = createRule("Counterparty Netting Rule", "NETTING" ,1
                , Map.of("counterParty", "java.lang.String","currency","java.lang.String", "settlementDate", "java.time.LocalDate")
                , List.of("Lehman Brothers PLC", "USD", LocalDate.now().plusDays(10).toString()));
        assertNotEquals(-1L, nettingRule2.getId());
        assertEquals("Counterparty Netting Rule", nettingRule2.getRuleName());
        assertEquals("NETTING", nettingRule2.getRuleType());
        assertEquals(1, nettingRule2.getPriority());
        RuleAttribute ruleAttributeCpty = ruleAttributeDao.findRuleAttribute("counterParty", "java.lang.String", "NETTING").orElseThrow();
        assertEquals("java.lang.String",ruleAttributeCpty.getAttributeType());
        assertEquals("counterParty", ruleAttributeCpty.getAttributeName());
        RuleAttribute ruleAttributeCurrency = ruleAttributeDao.findRuleAttribute("currency", "java.lang.String", "NETTING").orElseThrow();
        assertEquals("java.lang.String",ruleAttributeCurrency.getAttributeType());
        assertEquals("currency", ruleAttributeCurrency.getAttributeName());
        RuleAttribute ruleAttributeSettlementDate = ruleAttributeDao.findRuleAttribute("settlementDate", "java.time.LocalDate", "NETTING").orElseThrow();
        assertEquals("java.time.LocalDate",ruleAttributeSettlementDate.getAttributeType());
        assertEquals("settlementDate", ruleAttributeSettlementDate.getAttributeName());
        assertThrows(Exception.class, ()-> ruleAttributeDao.findRuleAttribute("amount", "java.lang.Double", "NETTING").orElseThrow());
        RuleValue currency = ruleValueDao.findByOperand("USD").orElseThrow();
        assertEquals("USD", currency.getOperand());
        RuleValue settlementDate = ruleValueDao.findByOperand(LocalDate.now().plusDays(10).toString()).orElseThrow();
        assertEquals(LocalDate.now().plusDays(10), LocalDate.parse(settlementDate.getOperand()));
        RuleValue cpty = ruleValueDao.findByOperand("Lehman Brothers PLC").orElseThrow();
        assertEquals("Lehman Brothers PLC", cpty.getOperand());
        assertThrows(Exception.class, ()-> ruleValueDao.findByOperand("Throws Exception").orElseThrow());
    }

    @Test @Order(8)
    public void whenGivenDataThenNettingRuleIsAdded() {
        BusinessRule nettingRule3 = createRule("Counterparty Netting Rule", "NETTING" ,1
                , Map.of("counterParty", "java.lang.String","currency","java.lang.String", "settlementDate", "java.time.LocalDate")
                , List.of("Lehman Brothers PLC", "EUR", LocalDate.now().plusDays(15).toString()));

        assertNotEquals(-1L, nettingRule3.getId());
        assertEquals("Counterparty Netting Rule", nettingRule3.getRuleName());
        assertEquals("NETTING", nettingRule3.getRuleType());
        assertEquals(1, nettingRule3.getPriority());
        RuleAttribute ruleAttributeCpty = ruleAttributeDao.findRuleAttribute("counterParty", "java.lang.String", "NETTING").orElseThrow();
        assertEquals("java.lang.String",ruleAttributeCpty.getAttributeType());
        assertEquals("counterParty", ruleAttributeCpty.getAttributeName());
        RuleAttribute ruleAttributeCurrency = ruleAttributeDao.findRuleAttribute("currency", "java.lang.String", "NETTING").orElseThrow();
        assertEquals("java.lang.String",ruleAttributeCurrency.getAttributeType());
        assertEquals("currency", ruleAttributeCurrency.getAttributeName());
        RuleAttribute ruleAttributeSettlementDate = ruleAttributeDao.findRuleAttribute("settlementDate", "java.time.LocalDate", "NETTING").orElseThrow();
        assertEquals("java.time.LocalDate",ruleAttributeSettlementDate.getAttributeType());
        assertEquals("settlementDate", ruleAttributeSettlementDate.getAttributeName());
        assertThrows(Exception.class, ()-> ruleAttributeDao.findRuleAttribute("amount", "java.lang.Double", "NETTING").orElseThrow());
        RuleValue currency = ruleValueDao.findByOperand("EUR").orElseThrow();
        assertEquals("EUR", currency.getOperand());
        RuleValue settlementDate = ruleValueDao.findByOperand(LocalDate.now().plusDays(15).toString()).orElseThrow();
        assertEquals(LocalDate.now().plusDays(15), LocalDate.parse(settlementDate.getOperand()));
        RuleValue cpty = ruleValueDao.findByOperand("Lehman Brothers PLC").orElseThrow();
        assertEquals("Lehman Brothers PLC", cpty.getOperand());
        assertThrows(Exception.class, ()-> ruleValueDao.findByOperand("Throws Exception").orElseThrow());
    }


    private BusinessRule createRule(String ruleName, String ruleType, int priority, Map<String, String> attributeNameTypeMap, List<String> attributeValues) {
        return ruleService.createRule(ruleName,ruleType,priority,attributeNameTypeMap, attributeValues);
    }

    private BusinessRule createRule(String ruleName, String ruleType, int priority, String operand, Map<String, String> attributeNameTypeMap) {
        return ruleService.createRule(ruleName,ruleType,priority,operand,attributeNameTypeMap);
    }
}
