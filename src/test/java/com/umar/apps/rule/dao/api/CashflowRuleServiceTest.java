package com.umar.apps.rule.dao.api;

import com.umar.apps.rule.dao.api.core.RuleAttributeDaoImpl;
import com.umar.apps.rule.dao.api.core.RuleDaoImpl;
import com.umar.apps.rule.dao.api.core.RuleValueDaoImpl;
import com.umar.apps.rule.domain.RuleAttribute;
import com.umar.apps.rule.domain.RuleValue;
import com.umar.apps.rule.service.api.BusinessRuleService;
import com.umar.apps.rule.service.api.ConditionService;
import com.umar.apps.rule.service.api.core.AndComposer;
import com.umar.apps.rule.service.api.core.BusinessRuleServiceImpl;
import com.umar.apps.rule.service.api.core.DefaultCondition;
import com.umar.apps.rule.service.api.core.OrComposer;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CashflowRuleServiceTest {
    private final static EntityManagerFactory emf = Persistence.createEntityManagerFactory("testPU");
    private final RuleDao ruleDao = new RuleDaoImpl(emf);
    private final RuleAttributeDao ruleAttributeDao = new RuleAttributeDaoImpl(emf);
    private final RuleValueDao ruleValueDao = new RuleValueDaoImpl(emf);
    private final BusinessRuleService ruleService = new BusinessRuleServiceImpl(ruleDao, ruleAttributeDao, ruleValueDao);

    @BeforeEach
    public void before() {
        try {
            createSomeRules();
        }catch (Exception ex) {
            //eat ElementAlreadyExistException
        }
    }

    void createSomeRules() {
        CashflowRulesTestProvider.createSomeRulesAndAttributes(ruleDao, ruleService);
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
