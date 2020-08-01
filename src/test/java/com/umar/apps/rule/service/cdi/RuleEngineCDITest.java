package com.umar.apps.rule.service.cdi;

import com.umar.apps.rule.BusinessRule;
import com.umar.apps.rule.RuleAttribute;
import com.umar.apps.rule.RuleValue;
import com.umar.apps.rule.api.*;
import com.umar.apps.rule.api.core.InferenceRuleEngine;
import com.umar.apps.rule.api.core.RuleBuilder;
import com.umar.apps.rule.dao.api.RuleAttributeDao;
import com.umar.apps.rule.dao.api.RuleDao;
import com.umar.apps.rule.dao.api.RuleValueDao;
import com.umar.apps.rule.dao.api.core.RuleAttributeDaoImpl;
import com.umar.apps.rule.dao.api.core.RuleDaoImpl;
import com.umar.apps.rule.dao.api.core.RuleValueDaoImpl;
import com.umar.apps.rule.infra.dao.api.core.DeleteFunction;
import com.umar.apps.rule.infra.dao.api.core.SelectFunction;
import com.umar.apps.rule.service.BusinessRulesTestProvider;
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
                )
                .initialize();
        RuleDao ruleDao = container.select(RuleDaoImpl.class).get();
        BusinessRuleService ruleService = container.select(BusinessRuleServiceImpl.class).get();
        BusinessRulesTestProvider.createSomeRulesAndAttributes(ruleDao, ruleService);
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

    @Test @Order(1)
    public void whenGivenDataThenCounterPartySTPRule() {
        RuleAttributeDao ruleAttributeDao = container.select(RuleAttributeDaoImpl.class).get();
        RuleValueDao ruleValueDao = container.select(RuleValueDaoImpl.class).get();
        BusinessRuleService ruleService = container.select(BusinessRuleServiceImpl.class).get();
        RuleAttribute ruleAttribute = ruleAttributeDao.findRuleAttribute("counterParty", "NON-STP").orElseThrow();
        createValue(ruleAttribute, "Lehman Brothers PLC", ruleService);
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
        BusinessRuleService ruleService = container.select(BusinessRuleServiceImpl.class).get();
        RuleDao ruleDao = container.select(RuleDaoImpl.class).get();
        RuleAttributeDao ruleAttributeDao = container.select(RuleAttributeDaoImpl.class).get();

        BusinessRulesTestProvider.createRule("Settlement Date STP Rule", "NON-STP", 1, ruleService);
        BusinessRule stmtDtSTPRule = ruleDao.findByNameAndType("Settlement Date STP Rule", "NON-STP").orElseThrow();
        BusinessRulesTestProvider.createAttribute(stmtDtSTPRule, "settlementDate", "NON-STP", "Settlement Date",ruleService);
        RuleAttribute stmtDtAttrib = ruleAttributeDao.findRuleAttribute("settlementDate","NON-STP").orElseThrow();
        createValue(stmtDtAttrib, LocalDate.now().plusDays(10).toString(), ruleService);

        CashflowDao cashflowDao = container.select(CashflowDao.class).get();
        ConditionService counterPartyCondition = container.select(CounterPartyConditionService.class).get();
        ConditionService settlementDateConditon = container.select(SettlementDateConditionService.class).get();
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
        BusinessRuleService ruleService = container.select(BusinessRuleServiceImpl.class).get();
        RuleAttributeDao ruleAttributeDao = container.select(RuleAttributeDaoImpl.class).get();
        CashflowDao cashflowDao = container.select(CashflowDao.class).get();

        RuleAttribute stmtDateAttrib = ruleAttributeDao.findRuleAttribute("settlementDate","NETTING").orElseThrow();
        RuleAttribute cptyAttrib = ruleAttributeDao.findRuleAttribute("counterParty", "NETTING").orElseThrow();
        RuleAttribute currencyAttrib = ruleAttributeDao.findRuleAttribute("currency", "NETTING").orElseThrow();
        createValue(cptyAttrib, "Meryl Lynch PLC", ruleService);
        createValue(cptyAttrib, "Lehman Brothers PLC", ruleService);
        createValue(stmtDateAttrib, LocalDate.now().plusDays(10).toString(), ruleService);
        createValue(currencyAttrib, "USD", ruleService);
        createValue(currencyAttrib, "EUR", ruleService);
        createValue(currencyAttrib, "YUAN", ruleService);

        Cashflow cf3 = createCashFlow("Meryl Lynch PLC", "USD", 220000.00, LocalDate.now().plusDays(10));
        Cashflow cf6 = createCashFlow("Meryl Lynch PLC", "USD", 10000.00, LocalDate.now().plusDays(10));
        Cashflow cf7 = createCashFlow("Meryl Lynch PLC", "USD", 20000.00, LocalDate.now().plusDays(10));
        Cashflow cf8 = createCashFlow("Lehman Brothers PLC", "EUR", 90000.00, LocalDate.now().plusDays(10));
        Cashflow cf9 = createCashFlow("Lehman Brothers PLC", "EUR", 30500.00, LocalDate.now().plusDays(10));
        Cashflow cf10 = createCashFlow("Lehman Brothers PLC", "YUAN", 20900.00, LocalDate.now().plusDays(10));
        cashflowDao.save(cf3);
        cashflowDao.save(cf6);
        cashflowDao.save(cf7);
        cashflowDao.save(cf8);
        cashflowDao.save(cf9);
        cashflowDao.save(cf10);

        List<Cashflow> cashflows = new LinkedList<>(cashflowDao.findBySettlementDate(LocalDate.now().plusDays(10)));
        Map<String, Set<Cashflow>> cashflowMap = netTogether(cashflows);
        assertEquals(3, cashflowMap.size());
        assertEquals(1, cashflowMap.get("Lehman Brothers PLC-YUAN").size());
        assertEquals(2, cashflowMap.get("Lehman Brothers PLC-EUR").size());
        assertEquals(3, cashflowMap.get("Meryl Lynch PLC-USD").size());
    }

    Map<String, Set<Cashflow>> netTogether(List<Cashflow> cashflows) {
        ConditionService counterPartyConditionService = container.select(CounterPartyConditionService.class).get();
        ConditionService currencyConditionService = container.select(CurrencyConditionService.class).get();
        ConditionService stmtDtConditionService = container.select(SettlementDateConditionService.class).get();
        Map<String, Set<Cashflow>> cashflowMap = new ConcurrentHashMap<>();
        RulesEngine rulesEngine = new InferenceRuleEngine();
        Facts facts = new Facts();
        Rules rules = new Rules();
        int cnt = 1;
        for(Cashflow cashflow: cashflows) {
            facts.put("cashflow-" + cnt, cashflow);
            cnt++;
            Condition cptyNettingCondition = counterPartyConditionService.getCondition(cashflow, "Counterparty Netting Rule", "NETTING");
            Condition currencyCondition = currencyConditionService.getCondition(cashflow, "Currency Netting Rule", "NETTING");
            Condition stmtDateCondition = stmtDtConditionService.getCondition(cashflow, "Settlement Date Netting Rule", "NETTING");
            Set<Cashflow> cashflowSet = new HashSet<>();
            //Hack the comparator logic of DefaultRule/BasicRule in order to override their internal logic as below.
            //Otherwise the first cashflow in the collection will be the only Rule in registered Rules.
            Rule stpRules = new RuleBuilder((o1, o2) -> o1.getId().compareTo(cashflow.getId()))
                    .when(cptyNettingCondition.and(currencyCondition).and(stmtDateCondition))
                    .then(action -> {
                        String key = cashflow.getCounterParty() +"-"+ cashflow.getCurrency();
                        if(cashflowMap.containsKey(key)){
                            cashflowMap.get(key).add(cashflow);
                        }else{
                            cashflowSet.add(cashflow);
                            cashflowMap.put(cashflow.getCounterParty() +"-"+ cashflow.getCurrency(),cashflowSet);
                        }
                    })
                    .build();
            rules.register(stpRules);
        }
        rulesEngine.fire(rules, facts);
        return cashflowMap;
    }

    private void createValue(RuleAttribute ruleAttribute, String operand, BusinessRuleService ruleService) {
        ruleService.createValue(ruleAttribute, operand);
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
