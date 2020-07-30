package com.umar.apps.rule.service.cdi;

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

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BusinessRuleServiceCDITest {
    private static SeContainer container;

    @BeforeAll
    public static void before() {
        // simulate another way than @Cdi to bootstrap the container,
        // can be another server (meecrowave, tomee, playx, ...) or just a custom preconfigured setup
        //sqlFunctions = SelectOp.create();
        SeContainerInitializer initializer = SeContainerInitializer.newInstance();
        container = initializer
                .disableDiscovery()
                .addBeanClasses(RuleDaoImpl.class, RuleAttributeDaoImpl.class, RuleValueDaoImpl.class, BusinessRuleServiceImpl.class, SelectFunction.class)
                .initialize();
    }

    @AfterAll
    public static void after() {
        if(container.isRunning()){
            container.close();
        }
    }

    /*@Test
    @Order(1)
    public void whenGivenDataThenCounterPartySTPRuleIsCreated() {
        RuleDao ruleDao = container.select(RuleDaoImpl.class).get();
        RuleAttributeDao ruleAttributeDao = container.select(RuleAttributeDaoImpl.class).get();
        RuleValueDao ruleValueDao = container.select(RuleValueDaoImpl.class).get();

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
        RuleAttributeDao ruleAttributeDao = container.select(RuleAttributeDaoImpl.class).get();
        RuleValueDao ruleValueDao = container.select(RuleValueDaoImpl.class).get();

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
        RuleAttributeDao ruleAttributeDao = container.select(RuleAttributeDaoImpl.class).get();
        RuleValueDao ruleValueDao = container.select(RuleValueDaoImpl.class).get();
        BusinessRule currencyStpRule = createRule("Currency STP Rule", "NON-STP",3,Map.of("currency", List.of("KOD")));
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
        RuleAttributeDao ruleAttributeDao = container.select(RuleAttributeDaoImpl.class).get();
        RuleValueDao ruleValueDao = container.select(RuleValueDaoImpl.class).get();
        BusinessRule currencyStpRule2 = createRule("Currency STP Rule", "NON-STP",1, Map.of("currency", List.of("YUAN", "YEN")));
        RuleAttribute ruleAttribute = ruleAttributeDao.findRuleAttribute("currency", "NON-STP").orElseThrow();
        assertEquals("currency", ruleAttribute.getAttributeName());
        RuleValue yuan = ruleValueDao.findByOperand("YUAN").orElseThrow();
        assertEquals("YUAN", yuan.getOperand());
        RuleValue yen = ruleValueDao.findByOperand("YEN").orElseThrow();
        assertEquals("YEN", yen.getOperand());
    }

    @Test @Order(5)
    public void whenGivenDataThenAmountSTPRuleIsCreated() {
        RuleAttributeDao ruleAttributeDao = container.select(RuleAttributeDaoImpl.class).get();
        RuleValueDao ruleValueDao = container.select(RuleValueDaoImpl.class).get();
        BusinessRule amountStpRule = createRule("Amount STP Rule", "NON-STP", 2,Map.of("amount", List.of("2300000.00")));
        RuleAttribute ruleAttribute = ruleAttributeDao.findRuleAttribute("amount", "NON-STP").orElseThrow();
        assertEquals(2, amountStpRule.getPriority());
        assertEquals("amount", ruleAttribute.getAttributeName());
        RuleValue amount = ruleValueDao.findByOperand("2300000.00").orElseThrow();
        assertEquals("2300000.00", amount.getOperand());
    }

    private void createRule(String ruleName, String ruleType, int priority, Map<String, List<String>> attributeNameValuesMap) {
        BusinessRuleService ruleService = container.select(BusinessRuleServiceImpl.class).get();
        ruleService.createRule(ruleName,ruleType,priority);
    }*/
}
