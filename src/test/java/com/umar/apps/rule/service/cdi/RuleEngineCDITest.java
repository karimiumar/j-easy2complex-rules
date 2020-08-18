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
                        , CashflowDao.class
                        , DefaultCondition.class
                        , AndComposer.class
                        , OrComposer.class
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
        ConditionService counterPartyCondition = container.select(DefaultCondition.class).get();
        ConditionService currencyCondition = container.select(DefaultCondition.class).get();
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
            //Hack the comparator logic of DefaultRule/BasicRule in order to override its internal logic as below.
            //This is needed to register our Rule with Rules which uses a Set<Rule> to register new Rules
            //with the comparator logic written in BasicRule.
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
        ConditionService conditionService = container.select(DefaultCondition.class).get();
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
            Condition counterPartySTPRule = conditionService.getCondition(cashflow, "Counterparty STP Rule", "NON-STP");
            Condition settlementDateSTPRule = conditionService.getCondition(cashflow, "Settlement Date STP Rule", "NON-STP");
            //Hack the comparator logic of DefaultRule/BasicRule in order to override its internal logic as below.
            //This is needed to register our Rule with Rules which uses a Set<Rule> to register new Rules
            //with the comparator logic written in BasicRule.
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
        var conditionService = container.select(DefaultCondition.class).get();
        Map<String, Set<Cashflow>> cashflowMap = new ConcurrentHashMap<>();
        var rulesEngine = new InferenceRuleEngine();
        var facts = new Facts();
        var rules = new Rules();
        var cnt = 1;
        for(Cashflow cashflow: cashflows) {
            facts.put("cashflow-" + cnt, cashflow);
            cnt++;
            Condition cptyNettingCondition = conditionService.getCondition(cashflow, "Counterparty Netting Rule", "NETTING");
            Condition currencyCondition = conditionService.getCondition(cashflow, "Currency Netting Rule", "NETTING");
            Condition stmtDateCondition = conditionService.getCondition(cashflow, "Settlement Date Netting Rule", "NETTING");
            Set<Cashflow> cashflowSet = new HashSet<>();
            //Hack the comparator logic of DefaultRule/BasicRule in order to override its internal logic as below.
            //This is needed to register our Rule with Rules which uses a Set<Rule> to register new Rules
            //with the comparator logic written in BasicRule.
            //Otherwise the first cashflow in the collection will be the only Rule in registered Rules.
            var andRules = new RuleBuilder((o1, o2) -> o1.getId().compareTo(cashflow.getId()))
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
            rules.register(andRules);
        }
        rulesEngine.fire(rules, facts);
        return cashflowMap;
    }

    @Test @Order(5)
    public void givenCashFlowsHavingSameSettlementDate_WhenDistinctCpty_DistinctCurrency_ThenGroupCashflows() {
        var ruleService = container.select(BusinessRuleServiceImpl.class).get();
        var ruleAttributeDao = container.select(RuleAttributeDaoImpl.class).get();
        var cashflowDao = container.select(CashflowDao.class).get();
        var ruleDao = container.select(RuleDaoImpl.class).get();

        BusinessRulesTestProvider.createRule("Cashflows Anding Rule","ANDER", 1, ruleService);
        var businessRule = ruleDao.findByNameAndType("Cashflows Anding Rule", "ANDER").orElseThrow();
        BusinessRulesTestProvider.createAttribute(businessRule, "counterParty", "ANDER", "Counter Party", ruleService);
        BusinessRulesTestProvider.createAttribute(businessRule, "settlementDate", "ANDER", "Settlement Date", ruleService);
        BusinessRulesTestProvider.createAttribute(businessRule, "currency", "ANDER", "Currency", ruleService);
        var stmtDateAttrib = ruleAttributeDao.findRuleAttribute("settlementDate","ANDER").orElseThrow();
        var cptyAttrib = ruleAttributeDao.findRuleAttribute("counterParty", "ANDER").orElseThrow();
        var currencyAttrib = ruleAttributeDao.findRuleAttribute("currency", "ANDER").orElseThrow();
        createValue(cptyAttrib, "Meryl Lynch PLC", ruleService);
        createValue(cptyAttrib, "Lehman Brothers PLC", ruleService);
        createValue(cptyAttrib, "HSBC", ruleService);
        createValue(stmtDateAttrib, LocalDate.now().plusDays(15).toString(), ruleService);
        createValue(stmtDateAttrib, LocalDate.now().plusDays(10).toString(), ruleService);
        //We are deliberately not creating a rule for LocalDate.now().plusDays(5)
        //This will ensure that cashflows having settlement date as LocalDate.now().plusDays(5)
        // are filtered out from grouping criteria. The AndService will provide Group AND-ING Condition
        // for cashflows on the basis of CounterParty AND Currency AND SettlementDate criteria.
        // The Action will only group cashflows if the values corresponding to 'Cashflows Anding Rule'
        // in database are same as the incoming cashflow
        // The "HSBC" and "Lehman Brothers PLC" with "YUAN" will be filtered out from AND-ING Criteria
        // as their Settlement Date is LocalDate.now().plusDays(5) and no AND-ING rule exist for it.
        createValue(currencyAttrib, "USD", ruleService);
        createValue(currencyAttrib, "EUR", ruleService);
        createValue(currencyAttrib, "YUAN", ruleService);

        var cf3 = createCashFlow("Meryl Lynch PLC", "USD", 220000.00, LocalDate.now().plusDays(15));
        var cf6 = createCashFlow("Meryl Lynch PLC", "USD", 10000.00, LocalDate.now().plusDays(15));
        var cf7 = createCashFlow("Meryl Lynch PLC", "USD", 20000.00, LocalDate.now().plusDays(15));
        var cf8 = createCashFlow("Lehman Brothers PLC", "EUR", 90000.00, LocalDate.now().plusDays(10));
        var cf9 = createCashFlow("Lehman Brothers PLC", "EUR", 30500.00, LocalDate.now().plusDays(10));
        var cf10 = createCashFlow("Lehman Brothers PLC", "YUAN", 20900.00, LocalDate.now().plusDays(5));
        var cf11 = createCashFlow("HSBC", "INR", 10900.00, LocalDate.now().plusDays(5));
        cashflowDao.save(cf3);
        cashflowDao.save(cf6);
        cashflowDao.save(cf7);
        cashflowDao.save(cf8);
        cashflowDao.save(cf9);
        cashflowDao.save(cf10);
        cashflowDao.save(cf11);
        var andingConditionService = container.select(AndComposer.class).get();
        var cashflows = new LinkedList<>(cashflowDao.findBySettlementDateBetween(LocalDate.now().plusDays(5), LocalDate.now().plusDays(15)));
        var cashflowMap = groupCashflows(cashflows, andingConditionService);
        assertEquals(2, cashflowMap.size());
        assertEquals(2, cashflowMap.get("Lehman Brothers PLC-EUR").size());
        assertEquals(3, cashflowMap.get("Meryl Lynch PLC-USD").size());
    }

    @Test @Order(5)
    public void givenCashFlowsWhenOrService_ThenGroupCashflows() {
        var ruleService = container.select(BusinessRuleServiceImpl.class).get();
        var ruleAttributeDao = container.select(RuleAttributeDaoImpl.class).get();
        var cashflowDao = container.select(CashflowDao.class).get();
        var ruleDao = container.select(RuleDaoImpl.class).get();

        BusinessRulesTestProvider.createRule("Cashflows Anding Rule","ANDER", 1, ruleService);
        var businessRule = ruleDao.findByNameAndType("Cashflows Anding Rule", "ANDER").orElseThrow();
        BusinessRulesTestProvider.createAttribute(businessRule, "counterParty", "ANDER", "Counter Party", ruleService);
        BusinessRulesTestProvider.createAttribute(businessRule, "settlementDate", "ANDER", "Settlement Date", ruleService);
        BusinessRulesTestProvider.createAttribute(businessRule, "currency", "ANDER", "Currency", ruleService);
        var stmtDateAttrib = ruleAttributeDao.findRuleAttribute("settlementDate","ANDER").orElseThrow();
        var cptyAttrib = ruleAttributeDao.findRuleAttribute("counterParty", "ANDER").orElseThrow();
        var currencyAttrib = ruleAttributeDao.findRuleAttribute("currency", "ANDER").orElseThrow();
        createValue(cptyAttrib, "Meryl Lynch PLC", ruleService);
        createValue(cptyAttrib, "Lehman Brothers PLC", ruleService);
        createValue(cptyAttrib, "HSBC", ruleService);
        createValue(stmtDateAttrib, LocalDate.now().plusDays(15).toString(), ruleService);
        createValue(stmtDateAttrib, LocalDate.now().plusDays(10).toString(), ruleService);
        //We are deliberately not creating a rule for LocalDate.now().plusDays(5) and making use of "ANDER" Business Rule
        //This will ensure that cashflows having settlement date as LocalDate.now().plusDays(5)
        //are still not filtered out from grouping criteria. The OrService will provide Group OR-ING Condition
        //for cashflows on the basis of CounterParty OR Currency OR SettlementDate criteria.
        //The Action will group them on the basis of Rule values corresponding to 'Cashflows AND-ING Rule'
        //in database are same as the incoming cashflow.
        //The OR-ING will ensure that "HSBC" and "Lehman Brothers PLC" with "YUAN" won't be filtered out from OR-ING Criteria
        //and new groups are created for the two based on the Currency and name
        createValue(currencyAttrib, "USD", ruleService);
        createValue(currencyAttrib, "EUR", ruleService);
        createValue(currencyAttrib, "YUAN", ruleService);

        var cf3 = createCashFlow("Meryl Lynch PLC", "USD", 220000.00, LocalDate.now().plusDays(15));
        var cf6 = createCashFlow("Meryl Lynch PLC", "USD", 10000.00, LocalDate.now().plusDays(15));
        var cf7 = createCashFlow("Meryl Lynch PLC", "USD", 20000.00, LocalDate.now().plusDays(15));
        var cf8 = createCashFlow("Lehman Brothers PLC", "EUR", 90000.00, LocalDate.now().plusDays(10));
        var cf9 = createCashFlow("Lehman Brothers PLC", "EUR", 30500.00, LocalDate.now().plusDays(10));
        var cf10 = createCashFlow("Lehman Brothers PLC", "YUAN", 20900.00, LocalDate.now().plusDays(5));
        var cf11 = createCashFlow("HSBC", "INR", 10900.00, LocalDate.now().plusDays(5));
        cashflowDao.save(cf3);
        cashflowDao.save(cf6);
        cashflowDao.save(cf7);
        cashflowDao.save(cf8);
        cashflowDao.save(cf9);
        cashflowDao.save(cf10);
        cashflowDao.save(cf11);
        var conditionService = container.select(OrComposer.class).get();
        var cashflows = new LinkedList<>(cashflowDao.findBySettlementDateBetween(LocalDate.now().plusDays(5), LocalDate.now().plusDays(15)));
        var cashflowMap = groupCashflows(cashflows, conditionService);
        assertEquals(4, cashflowMap.size());
        assertEquals(2, cashflowMap.get("Lehman Brothers PLC-EUR").size());
        assertEquals(3, cashflowMap.get("Meryl Lynch PLC-USD").size());
        assertEquals(1, cashflowMap.get("HSBC-INR").size());
        assertEquals(1, cashflowMap.get("Lehman Brothers PLC-YUAN").size());
    }

    Map<String, Set<Cashflow>> groupCashflows(List<Cashflow> cashflows, ConditionService conditionService) {

        Map<String, Set<Cashflow>> cashflowMap = new ConcurrentHashMap<>();
        var rulesEngine = new InferenceRuleEngine();
        var facts = new Facts();
        var rules = new Rules();
        var cnt = 1;
        for(Cashflow cashflow: cashflows) {
            facts.put("cashflow-" + cnt, cashflow);
            cnt++;
            var andCondition = conditionService.getCondition(cashflow, "Cashflows Anding Rule", "ANDER");
            Set<Cashflow> cashflowSet = new HashSet<>();
            //Hack the comparator logic of DefaultRule/BasicRule in order to override its internal logic as below.
            //This is needed to register our Rule with Rules which uses a Set<Rule> to register new Rules
            //with the comparator logic written in BasicRule.
            //Otherwise the first cashflow in the collection will be the only Rule in registered Rules.
            var andOrRules = new RuleBuilder((o1, o2) -> o1.getId().compareTo(cashflow.getId()))
                    .when(andCondition)
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
            rules.register(andOrRules);
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
