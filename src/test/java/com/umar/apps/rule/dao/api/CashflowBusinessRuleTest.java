package com.umar.apps.rule.dao.api;

import com.umar.apps.rule.api.Facts;
import com.umar.apps.rule.api.Rule;
import com.umar.apps.rule.api.Rules;
import com.umar.apps.rule.api.core.InferenceRuleEngine;
import com.umar.apps.rule.api.core.RuleBuilder;
import com.umar.apps.rule.domain.RuleAttribute;
import com.umar.apps.rule.domain.RuleValue;
import com.umar.apps.rule.service.api.ConditionService;
import com.umar.apps.rule.service.api.core.DefaultCondition;
import com.umar.apps.util.GenericBuilder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.umar.apps.rule.dao.api.CashflowRuleServiceTest.*;
import static org.junit.jupiter.api.Assertions.*;

/*@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CashflowBusinessRuleTest {

    private final CashflowDao cashflowDao = new CashflowDao("testPU");

    @Test
    public void whenGivenDataThenCounterPartySTPRule() {
        CashflowRuleServiceTest.createSomeRules();
        RuleAttribute ruleAttribute = ruleAttributeDao.findRuleAttribute("counterParty", "NON-STP").orElseThrow();
        createValue(ruleAttribute, "Lehman Brothers PLC");
        assertEquals("counterParty", ruleAttribute.getAttributeName());
        RuleValue ruleValue = ruleValueDao.findByOperand("Lehman Brothers PLC").orElseThrow();
        assertEquals("Lehman Brothers PLC", ruleValue.getOperand());
    }

    @Test
    public void givenCashFlows_WhenEitherFact_Then_ApplyRules() {
        CashflowRuleServiceTest.createSomeRules();
        RuleAttribute ruleAttribute = ruleAttributeDao.findRuleAttribute("counterParty", "NON-STP").orElseThrow();
        createValue(ruleAttribute, "Lehman Brothers PLC");
        assertEquals("counterParty", ruleAttribute.getAttributeName());
        RuleValue ruleValue = ruleValueDao.findByOperand("Lehman Brothers PLC").orElseThrow();
        assertEquals("Lehman Brothers PLC", ruleValue.getOperand());
        Cashflow cf1 = createCashFlow("Lehman Brothers PLC", "USD", 210000.00, LocalDate.now().plusDays(10));
        Cashflow cf2 = createCashFlow("Lehman Brothers PLC", "USD", 10000.00, LocalDate.now().plusDays(10));
        Cashflow cf3 = createCashFlow("Meryl Lynch PLC", "EUR", 220000.00, LocalDate.now().plusDays(10));
        cashflowDao.save(cf1);
        cashflowDao.save(cf2);
        cashflowDao.save(cf3);
        var counterPartyCondition = new DefaultCondition(ruleDao);
        var currencyCondition = new DefaultCondition(ruleDao);
        var rulesEngine = new InferenceRuleEngine();
        var cashflows = cashflowDao.findAll();
        var facts = new Facts();
        var rules = new Rules();
        int cnt = 1;
        for(Cashflow cashflow: cashflows) {
            facts.put("cashflow-" + cnt, cashflow);
            cnt++;
            var counterPartySTPRule = counterPartyCondition.getCondition(cashflow, "Counterparty STP Rule", "NON-STP");
            var currencySTPRule = currencyCondition.getCondition(cashflow, "Currency STP Rule", "NON-STP");
            //Hack the comparator logic of DefaultRule/BasicRule in order to override its internal logic as below.
            //This is needed to register our Rule with Rules which uses a Set<Rule> to register new Rules
            //with the comparator logic written in BasicRule.
            //Otherwise the first cashflow in the collection will be the only Rule in registered Rules.
            var stpRules = new RuleBuilder((o1, o2) -> o1.getId().compareTo(cashflow.getId()))
                    .when(counterPartySTPRule.or(currencySTPRule))
                    .then(action -> cashflowDao.applySTPRule(cashflow, "Cashflow Marked as NON-STP. Either Counterparty or Currency is NON STP."))
                    .build();
            rules.register(stpRules);
        }
        rulesEngine.fire(rules, facts);
        var lehmanCashflows = cashflowDao.findByCounterPartyCurrencyAndSettlementDate("Lehman Brothers PLC", "USD", LocalDate.now().plusDays(10));
        assertEquals(2, lehmanCashflows.size());
        lehmanCashflows.forEach(cashflow -> assertFalse(cashflow.isStpAllowed()));
        lehmanCashflows.forEach(cashflow -> assertEquals("Cashflow Marked as NON-STP. Either Counterparty or Currency is NON STP.", cashflow.getNote()));
        lehmanCashflows.forEach(cashflow -> assertEquals(1, cashflow.getVersion()));

        var merylLynchCashflows = cashflowDao.findByCounterParty("Meryl Lynch PLC");
        assertEquals(1, merylLynchCashflows.size());
        merylLynchCashflows.forEach(cashflow -> assertTrue(cashflow.isStpAllowed()));
        merylLynchCashflows.forEach(cashflow -> assertNull(cashflow.getNote()));
        merylLynchCashflows.forEach(cashflow -> assertEquals(0, cashflow.getVersion()));
    }

    @Test
    public void givenCashFlows_When_Mutiple_STP_Rules_Then_Highest_Priority_RuleApplied_CashflowIsNotSTPAllowed() {
        CashflowRuleServiceTest.createSomeRules();
        RuleAttribute ruleAttribute = ruleAttributeDao.findRuleAttribute("counterParty", "NON-STP").orElseThrow();
        createValue(ruleAttribute, "Lehman Brothers PLC");
        assertEquals("counterParty", ruleAttribute.getAttributeName());
        RuleValue ruleValue = ruleValueDao.findByOperand("Lehman Brothers PLC").orElseThrow();
        assertEquals("Lehman Brothers PLC", ruleValue.getOperand());

        CashflowRulesTestProvider.createRule("Settlement Date STP Rule", "NON-STP", 2, ruleService);
        var stmtDtSTPRule = ruleDao.findByNameAndType("Settlement Date STP Rule", "NON-STP").orElseThrow();
        CashflowRulesTestProvider.createAttribute(stmtDtSTPRule, "settlementDate", "NON-STP", "Settlement Date",ruleService);
        var stmtDtAttrib = ruleAttributeDao.findRuleAttribute("settlementDate","NON-STP").orElseThrow();
        createValue(stmtDtAttrib, LocalDate.now().plusDays(10).toString());
        var conditionService = new DefaultCondition(ruleDao);
        Cashflow cf4 = createCashFlow("Lehman Brothers PLC", "YUAN", 210000.00, LocalDate.now().plusDays(10));
        cashflowDao.save(cf4);
        var rulesEngine = new InferenceRuleEngine();
        var cashflows = cashflowDao.findByCounterPartyAndSettlementDate("Lehman Brothers PLC", LocalDate.now().plusDays(10));
        Facts facts = new Facts();
        Rules rules = new Rules();
        int cnt = 1;
        for(Cashflow cashflow: cashflows) {
            facts.put("cashflow-" + cnt, cashflow);
            cnt++;
            var counterPartySTPRule = conditionService.getCondition(cashflow, "Counterparty STP Rule", "NON-STP");
            var settlementDateSTPRule = conditionService.getCondition(cashflow, "Settlement Date STP Rule", "NON-STP");
            //Hack the comparator logic of DefaultRule/BasicRule in order to override its internal logic as below.
            //This is needed to register our Rule with Rules which uses a Set<Rule> to register new Rules
            //with the comparator logic written in BasicRule.
            //Otherwise the first cashflow in the collection will be the only Rule in registered Rules.
            var cptySTPRule = new RuleBuilder(Comparator.comparing(Rule::getPriority))
                    .when(counterPartySTPRule)
                    .then(action -> cashflowDao.applySTPRule(cashflow, "Cashflow Marked as NON-STP. Counterparty is NON STP."))
                    .build();

            var stmtDateSTPRule = new RuleBuilder(Comparator.comparing(Rule::getPriority))
                    .when(settlementDateSTPRule)
                            .then(action -> cashflowDao.applySTPRule(cashflow, "Cashflow Marked as NON-STP. Settlement Date is NON STP."))
                                    .build();
            rules.register(cptySTPRule);

            rules.register(stmtDateSTPRule);
        }
        rulesEngine.fire(rules, facts);
        var lehmanCashflows = cashflowDao.findByCounterPartyCurrencyAndSettlementDate("Lehman Brothers PLC", "YUAN", LocalDate.now().plusDays(10));
        var cashFlow = cashflowDao.findById(1L);
        cashFlow.ifPresent(System.out::println);
        lehmanCashflows.forEach(cashflow -> assertFalse(cashflow.isStpAllowed()));
        lehmanCashflows.forEach(cashflow -> assertEquals("Cashflow Marked as NON-STP. Counterparty is NON STP.", cashflow.getNote()));
        lehmanCashflows.forEach(cashflow -> assertEquals(1, cashflow.getVersion()));
    }

    @Test
    public void givenCashFlows_WhenCptyLehman_Brothers_PLC_And_SettlementDateNONSTPThenCashflowIsNotSTPAllowed() {
        CashflowRuleServiceTest.createSomeRules();
        RuleAttribute ruleAttribute = ruleAttributeDao.findRuleAttribute("counterParty", "NON-STP").orElseThrow();
        createValue(ruleAttribute, "Lehman Brothers PLC");
        assertEquals("counterParty", ruleAttribute.getAttributeName());
        RuleValue ruleValue = ruleValueDao.findByOperand("Lehman Brothers PLC").orElseThrow();
        assertEquals("Lehman Brothers PLC", ruleValue.getOperand());

        CashflowRulesTestProvider.createRule("Settlement Date STP Rule", "NON-STP", 1, ruleService);
        var stmtDtSTPRule = ruleDao.findByNameAndType("Settlement Date STP Rule", "NON-STP").orElseThrow();
        CashflowRulesTestProvider.createAttribute(stmtDtSTPRule, "settlementDate", "NON-STP", "Settlement Date",ruleService);
        var stmtDtAttrib = ruleAttributeDao.findRuleAttribute("settlementDate","NON-STP").orElseThrow();
        createValue(stmtDtAttrib, LocalDate.now().plusDays(10).toString());
        var conditionService = new DefaultCondition(ruleDao);
        Cashflow cf4 = createCashFlow("Lehman Brothers PLC", "YUAN", 210000.00, LocalDate.now().plusDays(10));
        cashflowDao.save(cf4);
        var rulesEngine = new InferenceRuleEngine();
        var cashflows = cashflowDao.findByCounterPartyAndSettlementDate("Lehman Brothers PLC", LocalDate.now().plusDays(10));
        Facts facts = new Facts();
        Rules rules = new Rules();
        int cnt = 1;
        for(Cashflow cashflow: cashflows) {
            facts.put("cashflow-" + cnt, cashflow);
            cnt++;
            var counterPartySTPRule = conditionService.getCondition(cashflow, "Counterparty STP Rule", "NON-STP");
            var settlementDateSTPRule = conditionService.getCondition(cashflow, "Settlement Date STP Rule", "NON-STP");
            //Hack the comparator logic of DefaultRule/BasicRule in order to override its internal logic as below.
            //This is needed to register our Rule with Rules which uses a Set<Rule> to register new Rules
            //with the comparator logic written in BasicRule.
            //Otherwise the first cashflow in the collection will be the only Rule in registered Rules.
            var stpRules = new RuleBuilder((o1, o2) -> o1.getId().compareTo(cashflow.getId()))
                    .when(counterPartySTPRule.and(settlementDateSTPRule))
                    .then(action -> cashflowDao.applySTPRule(cashflow, "Cashflow Marked as NON-STP. Both Counterparty and Settlement Date is NON STP."))
                    .build();
            rules.register(stpRules);
        }
        rulesEngine.fire(rules, facts);
        var lehmanCashflows = cashflowDao.findByCounterPartyCurrencyAndSettlementDate("Lehman Brothers PLC", "YUAN", LocalDate.now().plusDays(10));
        var cashFlow = cashflowDao.findById(1L);
        cashFlow.ifPresent(System.out::println);
        lehmanCashflows.forEach(cashflow -> assertFalse(cashflow.isStpAllowed()));
        lehmanCashflows.forEach(cashflow -> assertEquals("Cashflow Marked as NON-STP. Both Counterparty and Settlement Date is NON STP.", cashflow.getNote()));
        lehmanCashflows.forEach(cashflow -> assertEquals(1, cashflow.getVersion()));
    }

    @Test
    public void givenCashFlowsHavingSameSettlementDate_WhenDistinctCpty_DistinctCurrency_ThenNettCashflows() {
        CashflowRuleServiceTest.createSomeRules();
        RuleAttribute ruleAttribute = ruleAttributeDao.findRuleAttribute("counterParty", "NON-STP").orElseThrow();
        createValue(ruleAttribute, "Lehman Brothers PLC");
        assertEquals("counterParty", ruleAttribute.getAttributeName());
        RuleValue ruleValue = ruleValueDao.findByOperand("Lehman Brothers PLC").orElseThrow();
        assertEquals("Lehman Brothers PLC", ruleValue.getOperand());

        var stmtDateAttrib = ruleAttributeDao.findRuleAttribute("settlementDate","NETTING").orElseThrow();
        var cptyAttrib = ruleAttributeDao.findRuleAttribute("counterParty", "NETTING").orElseThrow();
        var currencyAttrib = ruleAttributeDao.findRuleAttribute("currency", "NETTING").orElseThrow();
        createValue(cptyAttrib, "Meryl Lynch PLC");
        createValue(cptyAttrib, "Lehman Brothers PLC");
        createValue(stmtDateAttrib, LocalDate.now().plusDays(10).toString());
        createValue(currencyAttrib, "USD");
        createValue(currencyAttrib, "EUR");
        createValue(currencyAttrib, "YUAN");

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

        var cashflows = new LinkedList<>(cashflowDao.findBySettlementDate(LocalDate.now().plusDays(10)));
        var cashflowMap = netTogether(cashflows);
        assertEquals(3, cashflowMap.size());
        assertEquals(1, cashflowMap.get("Lehman Brothers PLC-YUAN").size());
        assertEquals(2, cashflowMap.get("Lehman Brothers PLC-EUR").size());
        assertEquals(3, cashflowMap.get("Meryl Lynch PLC-USD").size());
    }

    Map<String, Set<Cashflow>> netTogether(List<Cashflow> cashflows) {
        Map<String, Set<Cashflow>> cashflowMap = new ConcurrentHashMap<>();
        var rulesEngine = new InferenceRuleEngine();
        var facts = new Facts();
        var rules = new Rules();
        var cnt = 1;
        for(Cashflow cashflow: cashflows) {
            facts.put("cashflow-" + cnt, cashflow);
            cnt++;
            var cptyNettingCondition = conditionService.getCondition(cashflow, "Counterparty Netting Rule", "NETTING");
            var currencyCondition = conditionService.getCondition(cashflow, "Currency Netting Rule", "NETTING");
            var stmtDateCondition = conditionService.getCondition(cashflow, "Settlement Date Netting Rule", "NETTING");
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

    @Test
    public void givenCashFlowsHavingSameSettlementDate_WhenDistinctCpty_DistinctCurrency_ThenGroupCashflows() {
        CashflowRuleServiceTest.createSomeRules();
        RuleAttribute ruleAttribute = ruleAttributeDao.findRuleAttribute("counterParty", "NON-STP").orElseThrow();
        createValue(ruleAttribute, "Lehman Brothers PLC");
        assertEquals("counterParty", ruleAttribute.getAttributeName());
        RuleValue ruleValue = ruleValueDao.findByOperand("Lehman Brothers PLC").orElseThrow();
        assertEquals("Lehman Brothers PLC", ruleValue.getOperand());

        CashflowRulesTestProvider.createRule("Cashflows Anding Rule","ANDER", 1, ruleService);
        var businessRule = ruleDao.findByNameAndType("Cashflows Anding Rule", "ANDER").orElseThrow();
        CashflowRulesTestProvider.createAttribute(businessRule, "counterParty", "ANDER", "Counter Party", ruleService);
        CashflowRulesTestProvider.createAttribute(businessRule, "settlementDate", "ANDER", "Settlement Date", ruleService);
        CashflowRulesTestProvider.createAttribute(businessRule, "currency", "ANDER", "Currency", ruleService);
        var stmtDateAttrib = ruleAttributeDao.findRuleAttribute("settlementDate","ANDER").orElseThrow();
        var cptyAttrib = ruleAttributeDao.findRuleAttribute("counterParty", "ANDER").orElseThrow();
        var currencyAttrib = ruleAttributeDao.findRuleAttribute("currency", "ANDER").orElseThrow();
        createValue(cptyAttrib, "Meryl Lynch PLC");
        createValue(cptyAttrib, "Lehman Brothers PLC");
        createValue(cptyAttrib, "HSBC");
        createValue(stmtDateAttrib, LocalDate.now().plusDays(15).toString());
        createValue(stmtDateAttrib, LocalDate.now().plusDays(10).toString());
        //We are deliberately not creating a rule for LocalDate.now().plusDays(5)
        //This will ensure that cashflows having settlement date as LocalDate.now().plusDays(5)
        // are filtered out from grouping criteria. The AndService will provide Group AND-ING Condition
        // for cashflows on the basis of CounterParty AND Currency AND SettlementDate criteria.
        // The Action will only group cashflows if the values corresponding to 'Cashflows Anding Rule'
        // in database are same as the incoming cashflow
        // The "HSBC" and "Lehman Brothers PLC" with "YUAN" will be filtered out from AND-ING Criteria
        // as their Settlement Date is LocalDate.now().plusDays(5) and no AND-ING rule exist for it.
        createValue(currencyAttrib, "USD");
        createValue(currencyAttrib, "EUR");
        createValue(currencyAttrib, "YUAN");

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
        var cashflows = new LinkedList<>(cashflowDao.findBySettlementDateBetween(LocalDate.now().plusDays(5), LocalDate.now().plusDays(15)));
        var cashflowMap = groupCashflows(cashflows, andingCondition);
        assertEquals(2, cashflowMap.size());
        assertEquals(2, cashflowMap.get("Lehman Brothers PLC-EUR").size());
        assertEquals(3, cashflowMap.get("Meryl Lynch PLC-USD").size());
    }

    @Test
    public void givenCashFlowsWhenOrService_ThenGroupCashflows() {
        CashflowRuleServiceTest.createSomeRules();
        RuleAttribute ruleAttribute = ruleAttributeDao.findRuleAttribute("counterParty", "NON-STP").orElseThrow();
        createValue(ruleAttribute, "Lehman Brothers PLC");
        assertEquals("counterParty", ruleAttribute.getAttributeName());
        RuleValue ruleValue = ruleValueDao.findByOperand("Lehman Brothers PLC").orElseThrow();
        assertEquals("Lehman Brothers PLC", ruleValue.getOperand());

        CashflowRulesTestProvider.createRule("Cashflows Anding Rule","ANDER", 1, ruleService);
        var businessRule = ruleDao.findByNameAndType("Cashflows Anding Rule", "ANDER").orElseThrow();
        CashflowRulesTestProvider.createAttribute(businessRule, "counterParty", "ANDER", "Counter Party", ruleService);
        CashflowRulesTestProvider.createAttribute(businessRule, "settlementDate", "ANDER", "Settlement Date", ruleService);
        CashflowRulesTestProvider.createAttribute(businessRule, "currency", "ANDER", "Currency", ruleService);
        var stmtDateAttrib = ruleAttributeDao.findRuleAttribute("settlementDate","ANDER").orElseThrow();
        var cptyAttrib = ruleAttributeDao.findRuleAttribute("counterParty", "ANDER").orElseThrow();
        var currencyAttrib = ruleAttributeDao.findRuleAttribute("currency", "ANDER").orElseThrow();
        createValue(cptyAttrib, "Meryl Lynch PLC");
        createValue(cptyAttrib, "Lehman Brothers PLC");
        createValue(cptyAttrib, "HSBC");
        createValue(stmtDateAttrib, LocalDate.now().plusDays(15).toString());
        createValue(stmtDateAttrib, LocalDate.now().plusDays(10).toString());
        //We are deliberately not creating a rule for LocalDate.now().plusDays(5) and making use of "ANDER" Business Rule
        //This will ensure that cashflows having settlement date as LocalDate.now().plusDays(5)
        //are still not filtered out from grouping criteria. The OrService will provide Group OR-ING Condition
        //for cashflows on the basis of CounterParty OR Currency OR SettlementDate criteria.
        //The Action will group them on the basis of Rule values corresponding to 'Cashflows AND-ING Rule'
        //in database are same as the incoming cashflow.
        //The OR-ING will ensure that "HSBC" and "Lehman Brothers PLC" with "YUAN" won't be filtered out from OR-ING Criteria
        //and new groups are created for the two based on the Currency and name
        createValue(currencyAttrib, "USD");
        createValue(currencyAttrib, "EUR");
        createValue(currencyAttrib, "YUAN");

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
        var cashflows = new LinkedList<>(cashflowDao.findBySettlementDateBetween(LocalDate.now().plusDays(5), LocalDate.now().plusDays(15)));
        var cashflowMap = groupCashflows(cashflows, orCondition);
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


    private void createValue(RuleAttribute ruleAttribute, String operand) {
        ruleService.createValue(ruleAttribute, operand);
    }

    private static Cashflow createCashFlow(String counterParty, String currency, double amount, LocalDate settlementDate) {
        return GenericBuilder.of(Cashflow::new)
                .with(Cashflow::setAmount, amount)
                .with(Cashflow::setCounterParty, counterParty)
                .with(Cashflow::setCurrency, currency)
                .with(Cashflow::setStpAllowed, true)
                .with(Cashflow::setSettlementDate, settlementDate)
                .with(Cashflow::setVersion, 0)
                .build();
    }

}*/
