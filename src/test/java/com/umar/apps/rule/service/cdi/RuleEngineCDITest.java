package com.umar.apps.rule.service.cdi;

import com.umar.apps.rule.BusinessRule;
import com.umar.apps.rule.RuleAttribute;
import com.umar.apps.rule.RuleValue;
import com.umar.apps.rule.api.*;
import com.umar.apps.rule.api.core.InferenceRuleEngine;
import com.umar.apps.rule.api.core.RuleBuilder;
import com.umar.apps.rule.dao.api.RuleAttributeDao;
import com.umar.apps.rule.dao.api.RuleValueDao;
import com.umar.apps.rule.dao.api.core.RuleAttributeDaoImpl;
import com.umar.apps.rule.dao.api.core.RuleDaoImpl;
import com.umar.apps.rule.dao.api.core.RuleValueDaoImpl;
import com.umar.apps.rule.infra.dao.api.core.DeleteFunction;
import com.umar.apps.rule.infra.dao.api.core.SelectFunction;
import com.umar.apps.rule.service.api.BusinessRuleService;
import com.umar.apps.rule.service.api.core.BusinessRuleServiceImpl;
import org.junit.jupiter.api.*;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RuleEngineCDITest {

    private static SeContainer container;

    @BeforeAll
    public static void before() {
        // simulate another way than @Cdi to bootstrap the container,
        // can be another server (meecrowave, tomee, playx, ...) or just a custom preconfigured setup
        //sqlFunctions = SelectOp.create();
        SeContainerInitializer initializer = SeContainerInitializer.newInstance();
        container = initializer
                .disableDiscovery()
                .addBeanClasses(RuleDaoImpl.class
                        , RuleAttributeDaoImpl.class
                        , RuleValueDaoImpl.class
                        , BusinessRuleServiceImpl.class
                        , SelectFunction.class
                        , CashflowDao.class
                        , DeleteFunction.class
                )
                .initialize();
        setup();
    }

    @AfterAll
    public static void after() {
        if(container.isRunning()){
            CashflowDao cashflowDao = container.select(CashflowDao.class).get();
            cashflowDao.delete();
            container.close();
        }
    }

    @Test @Order(1)
    public void whenGivenDataThenCounterPartySTPRuleIsAdded() {
        RuleAttributeDao ruleAttributeDao = container.select(RuleAttributeDaoImpl.class).get();
        RuleValueDao ruleValueDao = container.select(RuleValueDaoImpl.class).get();
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

    @Test @Order(2)
    public void givenCashFlows_WhenFact_Then_ApplyRules() {
        CashflowDao cashflowDao = container.select(CashflowDao.class).get();
        BusinessRuleService ruleService = container.select(BusinessRuleServiceImpl.class).get();
        Facts facts = new Facts();
        Rules rules = new Rules();
        RulesEngine rulesEngine = new InferenceRuleEngine();
        Collection<Cashflow> cashflows = cashflowDao.findAll();
        for(Cashflow cashflow: cashflows) {
            facts.put("cashflow", cashflow);
            Condition condition = ruleService.getSTPCondition(cashflow, "NON-STP", "Counterparty STP Rule");
            Rule stpRules = getRule(cashflowDao, cashflow, condition);
            rules.register(stpRules);
            rulesEngine.fire(rules, facts);
        }
    }

    private Rule getRule(CashflowDao cashflowDao, Cashflow cashflow, Condition condition) {
        System.out.println("Cashflow to evaluate:" + cashflow.getId());
        return new RuleBuilder().when(condition).then(new Action() {
            @Override
            public void execute(Facts facts) throws Exception {
                System.out.println("Cashflow Id to Load: " + cashflow.getId());
                //cashflowDao.applySTPRule(cashflow, "Cashflow marked as NON-STP.");
            }
        }).build();
        /*return new RuleBuilder()
                        .when(condition).then(action -> {
                            System.out.println("Cashflow to evaluate:" + cashflow.getId());
                            cashflowDao.applySTPRule(cashflow, "Cashflow marked as NON-STP.");
                        }).build();*/
    }

    @Test @Order(3)
    public void givenCashFlows_WhenCptyLehman_Brothers_PLC_ThenCashflowIsNotSTPAllowed() {
        CashflowDao cashflowDao = container.select(CashflowDao.class).get();
        List<Cashflow> cashflows = cashflowDao.findByCounterParty("Lehman Brothers PLC");
        assertEquals(2, cashflows.size());
        cashflows.forEach(cashflow -> assertFalse(cashflow.isStpAllowed()));
        cashflows.forEach(cashflow -> assertEquals("Cashflow marked as NON-STP.", cashflow.getNote()));
        cashflows.forEach(cashflow -> assertEquals(1, cashflow.getVersion()));
    }

    @Test @Order(3)
    public void givenCashFlows_WhenCptyMeryl_Lynch_PLC_ThenCashflowIsSTPAllowed() {
        CashflowDao cashflowDao = container.select(CashflowDao.class).get();
        List<Cashflow> cashflows = cashflowDao.findByCounterParty("Meryl Lynch PLC");
        assertEquals(1, cashflows.size());
        cashflows.forEach(cashflow -> assertTrue(cashflow.isStpAllowed()));
        cashflows.forEach(cashflow -> assertNull(cashflow.getNote()));
        cashflows.forEach(cashflow -> assertEquals(0, cashflow.getVersion()));
    }

    private BusinessRule createRule(Map<String, String> attributeNameTypeMap, List<String> attributeValues) {
        BusinessRuleService ruleService = container.select(BusinessRuleServiceImpl.class).get();
        return ruleService.createRule("Counterparty Netting Rule", "NETTING", 1,attributeNameTypeMap, attributeValues);
    }

    private BusinessRule createRule(String ruleName, String ruleType, int priority, String operand, Map<String, String> attributeNameTypeMap) {
        BusinessRuleService ruleService = container.select(BusinessRuleServiceImpl.class).get();
        return ruleService.createRule(ruleName,ruleType,priority,operand,attributeNameTypeMap);
    }

    private static void setup() {
        CashflowDao cashflowDao = container.select(CashflowDao.class).get();
        Cashflow cf1 = createCashFlow("Lehman Brothers PLC", "USD", 210000.00, LocalDate.now().plusDays(10));
        Cashflow cf2 = createCashFlow("Lehman Brothers PLC", "USD", 10000.00, LocalDate.now().plusDays(10));
        Cashflow cf3 = createCashFlow("Meryl Lynch PLC", "EUR", 220000.00, LocalDate.now().plusDays(10));
        Cashflow cf4 = createCashFlow("Lehman Brothers PLC", "USD", 210000.00, LocalDate.now().plusDays(10));
        Cashflow cf5 = createCashFlow("Lehman Brothers PLC", "EUR", 210000.00, LocalDate.now().plusDays(15));
        Cashflow cf6 = createCashFlow("Meryl Lynch PLC", "EUR", 10000.00, LocalDate.now().plusDays(10));
        Cashflow cf7 = createCashFlow("Lehman Brothers PLC", "USD", 20000.00, LocalDate.now().plusDays(10));
        Cashflow cf8 = createCashFlow("Lehman Brothers PLC", "USD", 20000.00, LocalDate.now().plusDays(15));
        Cashflow cf9 = createCashFlow("Lehman Brothers PLC", "USD", 20000.00, LocalDate.now().plusDays(10));
        Cashflow cf10 = createCashFlow("Lehman Brothers PLC", "USD", 20000.00, LocalDate.now().plusDays(15));

        cashflowDao.save(cf1);
        cashflowDao.save(cf2);
        cashflowDao.save(cf3);
        /*cashflowDao.save(cf4);
        cashflowDao.save(cf5);
        cashflowDao.save(cf6);
        cashflowDao.save(cf7);
        cashflowDao.save(cf8);
        cashflowDao.save(cf9);
        cashflowDao.save(cf10);*/
    }

    private static Cashflow createCashFlow(String counterParty, String currency, double amount, LocalDate settlementDate) {
        return new Cashflow.CashflowBuilder().with(cashflowBuilder -> {
            cashflowBuilder.amount = amount;
            cashflowBuilder.counterParty = counterParty;
            cashflowBuilder.currency = currency;
            cashflowBuilder.stpAllowed = true;
            cashflowBuilder.settlementDate = settlementDate;
            cashflowBuilder.version = 0;
        }).build();
    }
}
