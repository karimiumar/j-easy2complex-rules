package com.umar.apps.rule.service;

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
import org.junit.jupiter.api.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BusinessRuleServiceTest {
    final static SelectFunction sqlFunctions = new SelectFunction();
    final static RuleDao ruleDao = new RuleDaoImpl("test_rulesPU", sqlFunctions);
    final static RuleAttributeDao ruleAttributeDao = new RuleAttributeDaoImpl("test_rulesPU", sqlFunctions);
    final static RuleValueDao ruleValueDao = new RuleValueDaoImpl("test_rulesPU", sqlFunctions);
    final static BusinessRuleService ruleService = new BusinessRuleServiceImpl(ruleDao, ruleAttributeDao, ruleValueDao);

    @AfterAll
    public static void after() {
        ruleDao.closeEntityManagerFactory();
    }

    @BeforeAll
    public static void before() {
        createSomeRules();
    }

    private static void createSomeRules() {
        BusinessRulesTestProvider.createSomeRulesAndAttributes(ruleDao, ruleService);
    }

    @Test
    @Order(1)
    public void whenGivenDataThenCounterPartySTPRuleIsCreated() {
        RuleAttribute ruleAttribute = ruleAttributeDao.findRuleAttribute("counterParty", "NON-STP").orElseThrow();
        createValue(ruleAttribute, "Historic Defaulter Party X");
        assertEquals("counterParty", ruleAttribute.getAttributeName());
        RuleValue ruleValue = ruleValueDao.findByOperand("Historic Defaulter Party X").orElseThrow();
        assertEquals("Historic Defaulter Party X", ruleValue.getOperand());
    }

    @Test @Order(2)
    public void whenGivenDataThenCounterPartySTPRuleIsAmendedAndNewOperandIsAdded() {
        RuleAttribute ruleAttribute = ruleAttributeDao.findRuleAttribute("counterParty", "NON-STP").orElseThrow();
        createValue(ruleAttribute, "Lehman Brothers PLC");
        assertEquals("counterParty", ruleAttribute.getAttributeName());
        RuleValue ruleValue = ruleValueDao.findByOperand("Lehman Brothers PLC").orElseThrow();
        assertEquals("Lehman Brothers PLC", ruleValue.getOperand());
    }

    @Test @Order(3)
    public void whenGivenDataThenCurrencySTPRuleIsCreated() {
        RuleAttribute ruleAttribute = ruleAttributeDao.findRuleAttribute("currency", "NON-STP").orElseThrow();
        createValue(ruleAttribute, "KOD");
        assertEquals("currency", ruleAttribute.getAttributeName());
        RuleValue ruleValue = ruleValueDao.findByOperand("KOD").orElseThrow();
        assertEquals("KOD", ruleValue.getOperand());
    }

    @Test @Order(4)
    public void whenGivenDataThenCurrencySTPRuleIsAmendedNewOperandsAreAdded() {
        RuleAttribute ruleAttribute = ruleAttributeDao.findRuleAttribute("currency", "NON-STP").orElseThrow();
        createValue(ruleAttribute, "YUAN");
        createValue(ruleAttribute, "YEN");
        assertEquals("currency", ruleAttribute.getAttributeName());
        RuleValue yuan = ruleValueDao.findByOperand("YUAN").orElseThrow();
        assertEquals("YUAN", yuan.getOperand());
        RuleValue yen = ruleValueDao.findByOperand("YEN").orElseThrow();
        assertEquals("YEN", yen.getOperand());
    }

    @Test @Order(5)
    public void whenGivenDataThenAmountSTPRuleIsCreated() {
        RuleAttribute ruleAttribute = ruleAttributeDao.findRuleAttribute("amount", "NON-STP").orElseThrow();
        createValue(ruleAttribute, "2300000.00");
        assertEquals("amount", ruleAttribute.getAttributeName());
        RuleValue amount = ruleValueDao.findByOperand("2300000.00").orElseThrow();
        assertEquals("2300000.00", amount.getOperand());
    }

    @Test @Order(7)
    public void whenGivenDataThenNettingRuleIsCreated() {

        RuleAttribute cptyAttribute = ruleAttributeDao.findRuleAttribute("counterParty", "NETTING").orElseThrow();
        RuleAttribute currencyAttribute = ruleAttributeDao.findRuleAttribute("currency", "NETTING").orElseThrow();
        RuleAttribute settlementDtAttribute = ruleAttributeDao.findRuleAttribute("settlementDate", "NETTING").orElseThrow();
        createValue(cptyAttribute, "Lehman Brothers PLC");
        createValue(currencyAttribute, "USD");
        createValue(settlementDtAttribute, LocalDate.now().plusDays(10).toString());
        assertThrows(Exception.class, ()-> ruleAttributeDao.findRuleAttribute("amount",  "NETTING").orElseThrow());
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
        RuleAttribute currencyAttribute = ruleAttributeDao.findRuleAttribute("currency", "NETTING").orElseThrow();
        RuleAttribute settlementDtAttribute = ruleAttributeDao.findRuleAttribute("settlementDate", "NETTING").orElseThrow();
        createValue(currencyAttribute, "EUR");
        createValue(settlementDtAttribute, LocalDate.now().plusDays(15).toString());
        RuleValue cpty = ruleValueDao.findByOperand("Lehman Brothers PLC").orElseThrow();
        assertEquals("Lehman Brothers PLC", cpty.getOperand());
        RuleValue settlementDate = ruleValueDao.findByOperand(LocalDate.now().plusDays(15).toString()).orElseThrow();
        assertEquals(LocalDate.now().plusDays(15), LocalDate.parse(settlementDate.getOperand()));
        RuleValue currency = ruleValueDao.findByOperand("EUR").orElseThrow();
        assertEquals("EUR", currency.getOperand());
        assertThrows(Exception.class, ()-> ruleValueDao.findByOperand("Throws Exception").orElseThrow());
    }

    private void createValue(RuleAttribute ruleAttribute, String operand) {
        ruleService.createValue(ruleAttribute, operand);
    }
}
