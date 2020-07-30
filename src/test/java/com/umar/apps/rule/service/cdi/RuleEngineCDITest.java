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
import com.umar.apps.rule.service.api.ConditionService;
import com.umar.apps.rule.service.api.core.*;
import org.junit.jupiter.api.*;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RuleEngineCDITest {

    private static SeContainer container;

    @BeforeAll
    public static void before() {
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
                        , CounterPartyConditionService.class
                        , SettlementDateConditionService.class
                        , AmountConditionService.class
                        , CurrencyConditionService.class
                        , NettingConditionService.class
                )
                .initialize();
    }

    @AfterEach
    public void afterEach(){
        CashflowDao cashflowDao = container.select(CashflowDao.class).get();
        cashflowDao.delete();
    }

    @AfterAll
    public static void after() {
        if(container.isRunning()){
            container.close();
        }
    }

    /*@Test @Order(1)
    public void whenGivenDataThenCounterPartySTPRuleIsAdded() {
        RuleAttributeDao ruleAttributeDao = container.select(RuleAttributeDaoImpl.class).get();
        RuleValueDao ruleValueDao = container.select(RuleValueDaoImpl.class).get();
        BusinessRule cptyStpRule2 = createRule("Counterparty STP Rule", "NON-STP",1, Map.of("counterParty", List.of("Lehman Brothers PLC")));
        BusinessRule settlementDateSTPRule = createRule("Settlement Date STP Rule", "NON-STP", 1, Map.of("settlementDate", List.of(LocalDate.now().plusDays(10).toString())));
        assertNotEquals(-1L, cptyStpRule2.getId());
        assertEquals("Counterparty STP Rule", cptyStpRule2.getRuleName());
        assertEquals("NON-STP", cptyStpRule2.getRuleType());
        assertEquals(1, cptyStpRule2.getPriority());
        RuleAttribute ruleAttribute = ruleAttributeDao.findRuleAttribute("counterParty", "NON-STP").orElseThrow();
        assertEquals("counterParty", ruleAttribute.getAttributeName());
        RuleValue ruleValue = ruleValueDao.findByOperand("Lehman Brothers PLC").orElseThrow();
        assertEquals("Lehman Brothers PLC", ruleValue.getOperand());
    }

    @Test @Order(2)
    public void givenCashFlows_WhenEitherFact_Then_ApplyRules() {
        Cashflow cf1 = createCashFlow("Lehman Brothers PLC", "USD", 210000.00, LocalDate.now().plusDays(10));
        Cashflow cf2 = createCashFlow("Lehman Brothers PLC", "USD", 10000.00, LocalDate.now().plusDays(10));
        Cashflow cf3 = createCashFlow("Meryl Lynch PLC", "EUR", 220000.00, LocalDate.now().plusDays(10));
        CashflowDao cashflowDao = container.select(CashflowDao.class).get();
        cashflowDao.save(cf1);
        cashflowDao.save(cf2);
        cashflowDao.save(cf3);
        ConditionService counterPartyCondition = container.select(CounterPartyConditionService.class).get();
        ConditionService currencyCondition = container.select(CurrencyConditionService.class).get();
        RulesEngine rulesEngine = new InferenceRuleEngine();
        Collection<Cashflow> cashflows = cashflowDao.findAll();
        Facts facts = new Facts();
        Rules rules = new Rules();
        int cnt = 1;
        for(Cashflow cashflow: cashflows) {
            facts.put("cashflow-" + cnt, cashflow);
            cnt++;
            Condition counterPartySTPRule = counterPartyCondition.getCondition(cashflow, "Counterparty STP Rule", "NON-STP");
            Condition currencySTPRule = currencyCondition.getCondition(cashflow, "Currency STP Rule", "NON-STP");
            //Hack the comparator logic of DefaultRule/BasicRule in order to override their internal logic as below.
            //Otherwise the first cashflow in the collection will be the only Rule in registered Rules.
            Rule stpRules = new RuleBuilder((o1, o2) -> o1.getId().compareTo(cashflow.getId()))
                    .when(counterPartySTPRule.or(currencySTPRule))
                    .then(action -> cashflowDao.applySTPRule(cashflow, "Cashflow Marked as NON-STP. Either Counterparty or Currency is NON STP."))
                    .build();
            rules.register(stpRules);
        }
        rulesEngine.fire(rules, facts);

        List<Cashflow> lehmanCashflows = cashflowDao.findByCounterPartyCurrencyAndSettlementDate("Lehman Brothers PLC", "USD", LocalDate.now().plusDays(10));
        assertEquals(2, lehmanCashflows.size());
        lehmanCashflows.forEach(cashflow -> assertFalse(cashflow.isStpAllowed()));
        lehmanCashflows.forEach(cashflow -> assertEquals("Cashflow Marked as NON-STP. Either Counterparty or Currency is NON STP.", cashflow.getNote()));
        lehmanCashflows.forEach(cashflow -> assertEquals(1, cashflow.getVersion()));

        List<Cashflow> merylLynchCashflows = cashflowDao.findByCounterParty("Meryl Lynch PLC");
        assertEquals(1, merylLynchCashflows.size());
        merylLynchCashflows.forEach(cashflow -> assertTrue(cashflow.isStpAllowed()));
        merylLynchCashflows.forEach(cashflow -> assertNull(cashflow.getNote()));
        merylLynchCashflows.forEach(cashflow -> assertEquals(0, cashflow.getVersion()));
    }

    @Test @Order(3)
    public void givenCashFlows_WhenCptyLehman_Brothers_PLC_And_SettlementDateNONSTPThenCashflowIsNotSTPAllowed() {
        CashflowDao cashflowDao = container.select(CashflowDao.class).get();
        ConditionService counterPartyCondition = container.select(CounterPartyConditionService.class).get();
        ConditionService settlementDateConditon = container.select(SettlementDateConditionService.class).get();
        createRule("Counterparty STP Rule", "NON-STP",1, Map.of("counterParty", List.of("Lehman Brothers PLC")));
        Cashflow cf4 = createCashFlow("Lehman Brothers PLC", "YUAN", 210000.00, LocalDate.now().plusDays(10));
        cashflowDao.save(cf4);
        RulesEngine rulesEngine = new InferenceRuleEngine();
        Collection<Cashflow> cashflows = cashflowDao.findByCounterPartyAndSettlementDate("Lehman Brothers PLC", LocalDate.now().plusDays(10));
        Facts facts = new Facts();
        Rules rules = new Rules();
        int cnt = 1;
        for(Cashflow cashflow: cashflows) {
            facts.put("cashflow-" + cnt, cashflow);
            cnt++;
            Condition counterPartySTPRule = counterPartyCondition.getCondition(cashflow, "Counterparty STP Rule", "NON-STP");
            Condition settlementDateSTPRule = settlementDateConditon.getCondition(cashflow, "Settlement Date STP Rule", "NON-STP");
            //Hack the comparator logic of DefaultRule/BasicRule in order to override their internal logic as below.
            //Otherwise the first cashflow in the collection will be the only Rule in registered Rules.
            Rule stpRules = new RuleBuilder((o1, o2) -> o1.getId().compareTo(cashflow.getId()))
                    .when(counterPartySTPRule.and(settlementDateSTPRule))
                    .then(action -> cashflowDao.applySTPRule(cashflow, "Cashflow Marked as NON-STP. Both Counterparty and Settlement Date is NON STP."))
                    .build();
            rules.register(stpRules);
        }
        rulesEngine.fire(rules, facts);
        List<Cashflow> lehmanCashflows = cashflowDao.findByCounterPartyCurrencyAndSettlementDate("Lehman Brothers PLC", "YUAN", LocalDate.now().plusDays(10));
        lehmanCashflows.forEach(cashflow -> assertFalse(cashflow.isStpAllowed()));
        lehmanCashflows.forEach(cashflow -> assertEquals("Cashflow Marked as NON-STP. Both Counterparty and Settlement Date is NON STP.", cashflow.getNote()));
        lehmanCashflows.forEach(cashflow -> assertEquals(1, cashflow.getVersion()));
    }

    @Test @Order(3)
    public void givenCashFlowsHavingSameSettlementDate_WhenDistinctCpty_DistinctCurrency_ThenNettCashflows() {
        CashflowDao cashflowDao = container.select(CashflowDao.class).get();
        ConditionService counterPartyCondition = container.select(NettingConditionService.class).get();
        createRule("Counterparty Netting Rule", "NETTING" ,1
                , Map.of("counterParty", List.of("Lehman Brothers PLC")
                        ,"currency",List.of("USD"))
        );
        createRule("Counterparty Netting Rule", "NETTING" ,1
                , Map.of("counterParty", List.of("Meryl Lynch PLC")
                        ,"currency",List.of("USD"))
        );
        Cashflow cf3 = createCashFlow("Meryl Lynch PLC", "USD", 220000.00, LocalDate.now().plusDays(10));
        Cashflow cf6 = createCashFlow("Meryl Lynch PLC", "USD", 10000.00, LocalDate.now().plusDays(10));
        Cashflow cf7 = createCashFlow("Meryl Lynch PLC", "USD", 20000.00, LocalDate.now().plusDays(10));
        Cashflow cf8 = createCashFlow("Lehman Brothers PLC", "EUR", 20000.00, LocalDate.now().plusDays(10));
        Cashflow cf9 = createCashFlow("Lehman Brothers PLC", "EUR", 20000.00, LocalDate.now().plusDays(10));
        Cashflow cf10 = createCashFlow("Lehman Brothers PLC", "YUAN", 20000.00, LocalDate.now().plusDays(10));
        cashflowDao.save(cf3);
        cashflowDao.save(cf6);
        cashflowDao.save(cf7);
        cashflowDao.save(cf8);
        cashflowDao.save(cf9);
        cashflowDao.save(cf10);

        Collection<Cashflow> cashflows = cashflowDao.findBySettlementDate(LocalDate.now().plusDays(10));

        RulesEngine rulesEngine = new InferenceRuleEngine();
        Facts facts = new Facts();
        Rules rules = new Rules();
        Map<String, Set<Cashflow>> cashflowMap = new ConcurrentHashMap<>();
        Set<Cashflow> cashflowSet = new HashSet<>();
        int cnt = 1;
        for(Cashflow cashflow: cashflows) {
            facts.put("cashflow-" + cnt, cashflow);
            cnt++;
            Condition counterPartyNettingRule = counterPartyCondition.getCondition(cashflow, "Counterparty Netting Rule", "NETTING");
            //Hack the comparator logic of DefaultRule/BasicRule in order to override their internal logic as below.
            //Otherwise the first cashflow in the collection will be the only Rule in registered Rules.
            Rule stpRules = new RuleBuilder((o1, o2) -> o1.getId().compareTo(cashflow.getId()))
                    .when(counterPartyNettingRule)
                    .then(action -> {
                        cashflowSet.add(cashflow);
                        cashflowMap.put(cashflow.getCounterParty(), cashflowSet);
                    })
                    .build();
            rules.register(stpRules);
        }
        rulesEngine.fire(rules, facts);

    }

    private void createRule(String ruleName, String ruleType, int priority) {
        BusinessRuleService ruleService = container.select(BusinessRuleServiceImpl.class).get();
        ruleService.createRule(ruleName,ruleType,priority);
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
    }*/
}
