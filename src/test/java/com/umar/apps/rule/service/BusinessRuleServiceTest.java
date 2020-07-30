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
        /*
        DELETE FROM ATTRIBUTE_VALUES ;
        DELETE FROM RULE_ATTRIBUTE ;
        DELETE FROM "VALUES";
        DELETE FROM ATTRIBUTES ;
        DELETE FROM RULES ;
         */
        try {
            createSomeRules();
            createSomeAttributes();
        }catch (Exception e) {
            //eatup.
        }
    }

    private static void createSomeAttributes() {
        BusinessRule cptySTPRule = ruleDao.findByNameAndType("Counterparty STP Rule","NON-STP").orElseThrow();
        RuleAttribute counterPartyAttrib = new RuleAttribute();
        counterPartyAttrib.setRuleType("NON-STP");
        counterPartyAttrib.setAttributeName("counterParty");
        counterPartyAttrib.setDisplayName("Counter Party");
        cptySTPRule.addRuleAttribute(counterPartyAttrib);
        ruleDao.doInJPA(entityManager -> {
            entityManager.find(BusinessRule.class, cptySTPRule.getId());
            ruleAttributeDao.save(counterPartyAttrib);
            ruleDao.merge(cptySTPRule);
        }, ruleDao);

        BusinessRule currencySTPRule = ruleDao.findByNameAndType("Currency STP Rule", "NON-STP").orElseThrow();
        RuleAttribute currencyAttrib = new RuleAttribute();
        currencyAttrib.setRuleType("NON-STP");
        currencyAttrib.setAttributeName("currency");
        currencyAttrib.setDisplayName("Currency");
        currencySTPRule.addRuleAttribute(currencyAttrib);
        ruleDao.doInJPA(entityManager -> {
            entityManager.find(BusinessRule.class, currencySTPRule.getId());
            ruleAttributeDao.save(currencyAttrib);
            ruleDao.merge(currencySTPRule);
        }, ruleDao);

        BusinessRule settlementDateSTPRule = ruleDao.findByNameAndType("Settlement Date STP Rule", "NON-STP").orElseThrow();
        RuleAttribute settlementDateAttrib = new RuleAttribute();
        settlementDateAttrib.setRuleType("NON-STP");
        settlementDateAttrib.setAttributeName("settlementDate");
        settlementDateAttrib.setDisplayName("Settlement Date");
        settlementDateSTPRule.addRuleAttribute(settlementDateAttrib);
        ruleDao.doInJPA(entityManager -> {
            entityManager.find(BusinessRule.class, settlementDateSTPRule.getId());
            ruleAttributeDao.save(settlementDateAttrib);
            ruleDao.merge(settlementDateSTPRule);
        }, ruleDao);
    }

    private static void createSomeRules() {
        BusinessRule cptySTPRule = new BusinessRule.BusinessRuleBuilder("Counterparty STP Rule", "NON-STP").with(
                businessRuleBuilder -> {
                    businessRuleBuilder.active = true;
                    businessRuleBuilder.priority = 1;
                }
        ).build();

        BusinessRule currencySTPRule = new BusinessRule.BusinessRuleBuilder("Currency STP Rule", "NON-STP").with(
                businessRuleBuilder -> {
                    businessRuleBuilder.active = true;
                    businessRuleBuilder.priority = 1;
                }
        ).build();

        BusinessRule settlementDateSTPRule = new BusinessRule.BusinessRuleBuilder("Settlement Date STP Rule", "NON-STP").with(
                businessRuleBuilder -> {
                    businessRuleBuilder.active = true;
                    businessRuleBuilder.priority = 1;
                }
        ).build();

        ruleDao.save(cptySTPRule);
        ruleDao.save(currencySTPRule);
        ruleDao.save(settlementDateSTPRule);
    }

    @Test
    @Order(1)
    public void whenGivenDataThenCounterPartySTPRuleIsCreated() {
        createRule("Counterparty STP Rule", "NON-STP",1);
        BusinessRule businessRule = ruleDao.findByNameAndType("Counterparty STP Rule", "NON-STP").orElseThrow();
        createAttribute(businessRule, "counterParty", "NON-STP", "Counter Party");
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
        createRule("Currency STP Rule", "NON-STP",3);
        BusinessRule businessRule = ruleDao.findByNameAndType("Currency STP Rule", "NON-STP").orElseThrow();
        createAttribute(businessRule, "currency", "NON-STP", "Currency");
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
        createRule("Amount STP Rule", "NON-STP", 2);
        BusinessRule businessRule = ruleDao.findByNameAndType("Amount STP Rule", "NON-STP").orElseThrow();
        createAttribute(businessRule, "amount", "NON-STP", "Amount");
        RuleAttribute ruleAttribute = ruleAttributeDao.findRuleAttribute("amount", "NON-STP").orElseThrow();
        createValue(ruleAttribute, "2300000.00");
        assertEquals("amount", ruleAttribute.getAttributeName());
        RuleValue amount = ruleValueDao.findByOperand("2300000.00").orElseThrow();
        assertEquals("2300000.00", amount.getOperand());
    }

    /*@Test @Order(7)
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
        RuleValue cpty = ruleValueDao.findByOperand("Lehman Brothers PLC").orElseThrow();
        assertEquals("Lehman Brothers PLC", cpty.getOperand());
        RuleValue settlementDate = ruleValueDao.findByOperand(LocalDate.now().plusDays(15).toString()).orElseThrow();
        assertEquals(LocalDate.now().plusDays(15), LocalDate.parse(settlementDate.getOperand()));
        RuleValue currency = ruleValueDao.findByOperand("EUR").orElseThrow();
        assertEquals("EUR", currency.getOperand());
        assertThrows(Exception.class, ()-> ruleValueDao.findByOperand("Throws Exception").orElseThrow());
    }*/


    private void createRule(String ruleName, String ruleType, int priority) {
        ruleService.createRule(ruleName,ruleType,priority);
    }

    private void createAttribute(BusinessRule businessRule, String attributeName, String ruleType, String displayName) {
        ruleService.createAttribute(businessRule, attributeName, ruleType, displayName);
    }

    private void createValue(RuleAttribute ruleAttribute, String operand) {
        ruleService.createValue(ruleAttribute, operand);
    }
}
