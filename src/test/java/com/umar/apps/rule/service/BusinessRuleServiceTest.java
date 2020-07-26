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
    final static BusinessRuleService ruleService = new BusinessRuleServiceImpl(ruleDao, ruleAttributeDao, ruleValueDao);

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
        BusinessRule cptyStpRule = createRule("Counterparty STP Rule", "NON-STP",1, Map.of("counterParty", List.of("Historic Defaulter Party X")));
        assertNotEquals(-1L, cptyStpRule.getId());
        assertEquals("Counterparty STP Rule", cptyStpRule.getRuleName());
        assertEquals("NON-STP", cptyStpRule.getRuleType());
        assertEquals(1, cptyStpRule.getPriority());
        RuleAttribute ruleAttribute = ruleAttributeDao.findRuleAttribute("counterParty", "NON-STP").orElseThrow();
        assertEquals("counterParty", ruleAttribute.getAttributeName());
        RuleValue ruleValue = ruleValueDao.findByOperand("Historic Defaulter Party X").orElseThrow();
        assertEquals("Historic Defaulter Party X", ruleValue.getOperand());

    }
    @Test @Order(2)
    public void whenGivenDataThenCounterPartySTPRuleIsAmendedAndNewOperandIsAdded() {
        BusinessRule cptyStpRule2 = createRule("Counterparty STP Rule", "NON-STP",1, Map.of("counterParty", List.of("Lehman Brothers PLC")));
        assertNotEquals(-1L, cptyStpRule2.getId());
        assertEquals("Counterparty STP Rule", cptyStpRule2.getRuleName());
        assertEquals("NON-STP", cptyStpRule2.getRuleType());
        assertEquals(1, cptyStpRule2.getPriority());
        RuleAttribute ruleAttribute = ruleAttributeDao.findRuleAttribute("counterParty", "NON-STP").orElseThrow();
        assertEquals("counterParty", ruleAttribute.getAttributeName());
        RuleValue ruleValue = ruleValueDao.findByOperand("Lehman Brothers PLC").orElseThrow();
        assertEquals("Lehman Brothers PLC", ruleValue.getOperand());
    }

    @Test @Order(3)
    public void whenGivenDataThenCurrencySTPRuleIsCreated() {
        BusinessRule currencyStpRule = createRule("Currency STP Rule", "NON-STP",3, Map.of("currency", List.of("KOD")));
        assertNotEquals(-1L, currencyStpRule.getId());
        assertEquals("Currency STP Rule", currencyStpRule.getRuleName());
        assertEquals("NON-STP", currencyStpRule.getRuleType());
        assertEquals(3, currencyStpRule.getPriority());
        RuleAttribute ruleAttribute = ruleAttributeDao.findRuleAttribute("currency", "NON-STP").orElseThrow();
        assertEquals("currency", ruleAttribute.getAttributeName());
        RuleValue ruleValue = ruleValueDao.findByOperand("KOD").orElseThrow();
        assertEquals("KOD", ruleValue.getOperand());
    }

    @Test @Order(4)
    public void whenGivenDataThenCurrencySTPRuleIsAmendedNewOperandsAreAdded() {
        BusinessRule currencyStpRule2 = createRule("Currency STP Rule", "NON-STP",1, Map.of("currency", List.of("YUAN")));
        BusinessRule currencyStpRule3 = createRule("Currency STP Rule", "NON-STP",2, Map.of("currency", List.of("YEN")));
        RuleAttribute ruleAttribute = ruleAttributeDao.findRuleAttribute("currency", "NON-STP").orElseThrow();
        assertEquals(3, currencyStpRule2.getPriority());//priority is 3 as part of previous insert of currency STP and hence same is used.
        assertEquals(3, currencyStpRule3.getPriority());
        assertEquals("currency", ruleAttribute.getAttributeName());
        RuleValue yuan = ruleValueDao.findByOperand("YUAN").orElseThrow();
        assertEquals("YUAN", yuan.getOperand());
        RuleValue yen = ruleValueDao.findByOperand("YEN").orElseThrow();
        assertEquals("YEN", yen.getOperand());
    }

    @Test @Order(5)
    public void whenGivenDataThenAmountSTPRuleIsCreated() {
        BusinessRule amountStpRule = createRule("Amount STP Rule", "NON-STP", 2, Map.of("amount", List.of("2300000.00")));
        RuleAttribute ruleAttribute = ruleAttributeDao.findRuleAttribute("amount", "NON-STP").orElseThrow();
        assertEquals(2, amountStpRule.getPriority());
        assertEquals("amount", ruleAttribute.getAttributeName());
        RuleValue amount = ruleValueDao.findByOperand("2300000.00").orElseThrow();
        assertEquals("2300000.00", amount.getOperand());
    }

    @Test @Order(7)
    public void whenGivenDataThenNettingRuleIsCreated() {
        BusinessRule nettingRule2 = createRule("Counterparty Netting Rule", "NETTING" ,1
                , Map.of("counterParty", List.of("Lehman Brothers PLC")
                        ,"currency",List.of("USD")
                        , "settlementDate", List.of(LocalDate.now().plusDays(10).toString()))
                );
        assertNotEquals(-1L, nettingRule2.getId());
        assertEquals("Counterparty Netting Rule", nettingRule2.getRuleName());
        assertEquals("NETTING", nettingRule2.getRuleType());
        assertEquals(1, nettingRule2.getPriority());
        RuleAttribute ruleAttributeCpty = ruleAttributeDao.findRuleAttribute("counterParty",  "NETTING").orElseThrow();
        assertEquals("counterParty", ruleAttributeCpty.getAttributeName());
        RuleAttribute ruleAttributeCurrency = ruleAttributeDao.findRuleAttribute("currency",  "NETTING").orElseThrow();
        assertEquals("currency", ruleAttributeCurrency.getAttributeName());
        RuleAttribute ruleAttributeSettlementDate = ruleAttributeDao.findRuleAttribute("settlementDate",  "NETTING").orElseThrow();
        assertEquals("settlementDate", ruleAttributeSettlementDate.getAttributeName());
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
        BusinessRule nettingRule3 = createRule("Counterparty Netting Rule", "NETTING" ,1
                , Map.of("counterParty", List.of("Lehman Brothers PLC"),
                        "currency",List.of("EUR"),
                        "settlementDate", List.of(LocalDate.now().plusDays(15).toString())));

        assertNotEquals(-1L, nettingRule3.getId());
        assertEquals("Counterparty Netting Rule", nettingRule3.getRuleName());
        assertEquals("NETTING", nettingRule3.getRuleType());
        assertEquals(1, nettingRule3.getPriority());
        RuleAttribute ruleAttributeCpty = ruleAttributeDao.findRuleAttribute("counterParty",  "NETTING").orElseThrow();
        assertEquals("counterParty", ruleAttributeCpty.getAttributeName());
        RuleAttribute ruleAttributeCurrency = ruleAttributeDao.findRuleAttribute("currency", "NETTING").orElseThrow();
        assertEquals("currency", ruleAttributeCurrency.getAttributeName());
        RuleAttribute ruleAttributeSettlementDate = ruleAttributeDao.findRuleAttribute("settlementDate", "NETTING").orElseThrow();
        assertEquals("settlementDate", ruleAttributeSettlementDate.getAttributeName());
        assertThrows(Exception.class, ()-> ruleAttributeDao.findRuleAttribute("amount", "NETTING").orElseThrow());
        RuleValue currency = ruleValueDao.findByOperand("EUR").orElseThrow();
        assertEquals("EUR", currency.getOperand());
        RuleValue settlementDate = ruleValueDao.findByOperand(LocalDate.now().plusDays(15).toString()).orElseThrow();
        assertEquals(LocalDate.now().plusDays(15), LocalDate.parse(settlementDate.getOperand()));
        RuleValue cpty = ruleValueDao.findByOperand("Lehman Brothers PLC").orElseThrow();
        assertEquals("Lehman Brothers PLC", cpty.getOperand());
        assertThrows(Exception.class, ()-> ruleValueDao.findByOperand("Throws Exception").orElseThrow());
    }


    private BusinessRule createRule(String ruleName, String ruleType, int priority, Map<String, List<String>> attributeNameValsMap) {
        return ruleService.createRule(ruleName,ruleType,priority,attributeNameValsMap);
    }
}
