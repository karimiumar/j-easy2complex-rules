package com.umar.apps.rule.dao.api;

import com.umar.apps.JRooolsApp;
import com.umar.apps.rule.api.Facts;
import com.umar.apps.rule.api.Rule;
import com.umar.apps.rule.api.Rules;
import com.umar.apps.rule.api.core.InferenceRuleEngine;
import com.umar.apps.rule.api.core.RuleBuilder;
import com.umar.apps.rule.domain.BusinessRule;
import com.umar.apps.rule.domain.RuleAttribute;
import com.umar.apps.rule.domain.RuleValue;
import com.umar.apps.rule.service.api.BusinessRuleService;
import com.umar.apps.rule.service.api.ConditionService;
import com.umar.apps.rule.service.api.core.BusinessRuleServiceImpl;
import com.umar.apps.util.GenericBuilder;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = JRooolsApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = "com.umar.apps")
@ContextConfiguration(classes = {CashflowBusinessRuleTest.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CashflowBusinessRuleTest {

    @Autowired BusinessRuleService ruleService;
    @Autowired CashflowDao cashflowDao;
    @Autowired ConditionService defaultCondition;
    @Autowired ConditionService andComposer;
    @Autowired ConditionService orComposer;

    @AfterEach
    void after() {
        cashflowDao.delete();
    }

    @Test
    public void givenCashFlows_WhenEitherFact_Then_ApplyRules() {
        createRule("Counterparty STP Rule", "NON-STP",1,  true);
        BusinessRule businessRule = ruleService.findByNameAndType("Counterparty STP Rule", "NON-STP", true).orElseThrow();
        createAttribute(businessRule, "counterParty", "NON-STP", "Counter Party");

        createRule("Currency STP Rule", "NON-STP",3, true);
        BusinessRule currencyBusinessRule = ruleService.findByNameAndType("Currency STP Rule", "NON-STP", true).orElseThrow();
        createAttribute(currencyBusinessRule, "currency", "NON-STP", "Currency");
        createRule("Amount STP Rule", "NON-STP", 2, true);
        BusinessRule amountBusinessRule = ruleService.findByNameAndType("Amount STP Rule", "NON-STP", true).orElseThrow();
        createAttribute(amountBusinessRule, "amount", "NON-STP", "Amount");

        RuleAttribute ruleAttribute = ruleService.findRuleAttribute("counterParty", "NON-STP").orElseThrow();
        createValue(ruleAttribute, "Lehman Brothers PLC");
        assertThat(ruleAttribute.getAttributeName()).isEqualTo("counterParty");
        RuleValue ruleValue = ruleService.findByOperand("Lehman Brothers PLC").orElseThrow();
        assertThat(ruleValue.getOperand()).isEqualTo("Lehman Brothers PLC");
        Cashflow cf1 = createCashFlow("Lehman Brothers PLC", "USD", 210000.00, LocalDate.now().plusDays(10));
        Cashflow cf2 = createCashFlow("Lehman Brothers PLC", "USD", 10000.00, LocalDate.now().plusDays(10));
        Cashflow cf3 = createCashFlow("Meryl Lynch PLC", "EUR", 220000.00, LocalDate.now().plusDays(10));
        cashflowDao.save(cf1);
        cashflowDao.save(cf2);
        cashflowDao.save(cf3);
        var rulesEngine = new InferenceRuleEngine();
        var cashflows = cashflowDao.findAll();
        var facts = new Facts();
        var rules = new Rules();
        int cnt = 1;
        for(Cashflow cashflow: cashflows) {
            facts.put("cashflow-" + cnt, cashflow);
            cnt++;
            var counterPartySTPRule = defaultCondition.getCondition(cashflow, "Counterparty STP Rule", "NON-STP", true);
            var currencySTPRule = defaultCondition.getCondition(cashflow, "Currency STP Rule", "NON-STP", true);
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
        assertThat(lehmanCashflows).hasSize(2);
        assertThat(lehmanCashflows).allMatch(Cashflow::isNotStpAllowed);
        assertThat(lehmanCashflows).allMatch(cashflow -> cashflow.getNote().equals("Cashflow Marked as NON-STP. Either Counterparty or Currency is NON STP."));
        assertThat(lehmanCashflows).allMatch(cashflow -> cashflow.getVersion() == 1);

        var merylLynchCashflows = cashflowDao.findByCounterParty("Meryl Lynch PLC");
        assertThat(merylLynchCashflows).hasSize(1);
        assertThat(merylLynchCashflows).allMatch(Cashflow::isStpAllowed);
        assertThat(merylLynchCashflows).allMatch(cashflow -> cashflow.getNote() == null);
        assertThat(merylLynchCashflows).allMatch(cashflow -> cashflow.getVersion() == 0);
    }

    @Test
    public void givenCashFlows_When_Mutiple_STP_Rules_Then_Highest_Priority_RuleApplied_CashflowIsNotSTPAllowed() {

        RuleAttribute ruleAttribute = ruleService.findRuleAttribute("counterParty", "NON-STP").orElseThrow();
        createValue(ruleAttribute, "Lehman Brothers PLC");
        assertThat(ruleAttribute.getAttributeName()).isEqualTo("counterParty");
        RuleValue ruleValue = ruleService.findByOperand("Lehman Brothers PLC").orElseThrow();
        assertThat(ruleValue.getOperand()).isEqualTo("Lehman Brothers PLC");
        createRule("Settlement Date STP Rule", "NON-STP", 2, true);
        var stmtDtSTPRule = ruleService.findByNameAndType("Settlement Date STP Rule", "NON-STP", true).orElseThrow();
        createAttribute(stmtDtSTPRule, "settlementDate", "NON-STP", "Settlement Date");
        var stmtDtAttrib = ruleService.findRuleAttribute("settlementDate","NON-STP").orElseThrow();
        createValue(stmtDtAttrib, LocalDate.now().plusDays(10).toString());
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
            var counterPartySTPRule = defaultCondition.getCondition(cashflow, "Counterparty STP Rule", "NON-STP", true);
            var settlementDateSTPRule = defaultCondition.getCondition(cashflow, "Settlement Date STP Rule", "NON-STP", true);
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
            //Register both rules
            rules.register(cptySTPRule);
            rules.register(stmtDateSTPRule);
        }
        rulesEngine.fire(rules, facts);
        var lehmanCashflows = cashflowDao.findByCounterPartyCurrencyAndSettlementDate("Lehman Brothers PLC", "YUAN", LocalDate.now().plusDays(10));
        assertThat(lehmanCashflows).allMatch(Cashflow::isNotStpAllowed);
        assertThat(lehmanCashflows).allMatch(cashflow -> cashflow.getNote().equals("Cashflow Marked as NON-STP. Counterparty is NON STP."));
        assertThat(lehmanCashflows).allMatch(cashflow -> cashflow.getVersion() == 1);
    }

    @Test
    public void givenCashFlows_WhenCptyLehman_Brothers_PLC_And_SettlementDateNONSTPThenCashflowIsNotSTPAllowed() {
        createRule("Counterparty STP Rule", "NON-STP", 1, true);
        createRule("Settlement Date STP Rule", "NON-STP", 1, true);
        BusinessRule cptySTPRule = ruleService.findByNameAndType("Counterparty STP Rule", "NON-STP", true).orElseThrow();
        createAttribute(cptySTPRule, "counterParty", "NON-STP", "Counter Party");
        BusinessRule stmtDateSTPRule = ruleService.findByNameAndType("Settlement Date STP Rule", "NON-STP", true).orElseThrow();
        createAttribute(stmtDateSTPRule, "settlementDate", "NON-STP", "Settlement Date");
        RuleAttribute ruleAttribute = ruleService.findRuleAttribute("counterParty", "NON-STP").orElseThrow();
        createValue(ruleAttribute, "Lehman Brothers PLC");
        assertThat(ruleAttribute.getAttributeName()).isEqualTo("counterParty");
        RuleValue ruleValue = ruleService.findByOperand("Lehman Brothers PLC").orElseThrow();
        assertThat(ruleValue.getOperand()).isEqualTo("Lehman Brothers PLC");
        createRule("Settlement Date STP Rule", "NON-STP", 1, true);
        var stmtDtSTPRule = ruleService.findByNameAndType("Settlement Date STP Rule", "NON-STP", true).orElseThrow();
        createAttribute(stmtDtSTPRule, "settlementDate", "NON-STP", "Settlement Date");
        var stmtDtAttrib = ruleService.findRuleAttribute("settlementDate","NON-STP").orElseThrow();
        createValue(stmtDtAttrib, LocalDate.now().plusDays(10).toString());
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
            var counterPartySTPRule = defaultCondition.getCondition(cashflow, "Counterparty STP Rule", "NON-STP", true);
            var settlementDateSTPRule = defaultCondition.getCondition(cashflow, "Settlement Date STP Rule", "NON-STP", true);
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
        assertThat(lehmanCashflows).allMatch(Cashflow::isNotStpAllowed);
        assertThat(lehmanCashflows).allMatch(cashflow -> cashflow.getNote().equals("Cashflow Marked as NON-STP. Both Counterparty and Settlement Date is NON STP."));
        assertThat(lehmanCashflows).allMatch(cashflow -> cashflow.getVersion() == 1);
    }

    @Test
    public void givenCashFlowsHavingSameSettlementDate_WhenDistinctCpty_DistinctCurrency_ThenNettCashflows() {
        createRule("Counterparty Netting Rule", "NETTING", 1, true);
        createRule("Currency Netting Rule", "NETTING", 2, true);
        createRule("Settlement Date Netting Rule", "NETTING", 1, true);
        var cptyNettingRule = ruleService.findByNameAndType("Counterparty Netting Rule", "NETTING", true).orElseThrow();
        var currNettingRule = ruleService.findByNameAndType("Currency Netting Rule", "NETTING", true).orElseThrow();
        var stmdDateRule = ruleService.findByNameAndType("Settlement Date Netting Rule", "NETTING", true).orElseThrow();
        createAttribute(cptyNettingRule, "settlementDate", "NETTING", "Settlement Date");
        createAttribute(currNettingRule, "counterParty", "NETTING", "Counterparty");
        createAttribute(stmdDateRule, "currency", "NETTING", "Currency");
        var stmtDateAttrib = ruleService.findRuleAttribute("settlementDate","NETTING").orElseThrow();
        var cptyAttrib = ruleService.findRuleAttribute("counterParty", "NETTING").orElseThrow();
        var currencyAttrib = ruleService.findRuleAttribute("currency", "NETTING").orElseThrow();
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
        assertThat(cashflowMap).hasSize(3);
        assertThat(cashflowMap).containsOnlyKeys("Lehman Brothers PLC-YUAN", "Lehman Brothers PLC-EUR", "Meryl Lynch PLC-USD");
        assertThat(cashflowMap.get("Lehman Brothers PLC-YUAN")).hasSize(1);
        assertThat(cashflowMap.get("Lehman Brothers PLC-EUR")).hasSize(2);
        assertThat(cashflowMap.get("Meryl Lynch PLC-USD")).hasSize(3);
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
            var cptyNettingCondition = defaultCondition.getCondition(cashflow, "Counterparty Netting Rule", "NETTING", true);
            var currencyCondition = defaultCondition.getCondition(cashflow, "Currency Netting Rule", "NETTING", true);
            var stmtDateCondition = defaultCondition.getCondition(cashflow, "Settlement Date Netting Rule", "NETTING", true);
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
        createRule("Cashflows Anding Rule", "ANDER", 1, true);
        var businessRule = ruleService.findByNameAndType("Cashflows Anding Rule", "ANDER", true).orElseThrow();
        createAttribute(businessRule, "counterParty", "ANDER", "Counter Party");
        createAttribute(businessRule, "settlementDate", "ANDER", "Settlement Date");
        createAttribute(businessRule, "currency", "ANDER", "Currency");
        var stmtDateAttrib = ruleService.findRuleAttribute("settlementDate","ANDER").orElseThrow();
        var cptyAttrib = ruleService.findRuleAttribute("counterParty", "ANDER").orElseThrow();
        var currencyAttrib = ruleService.findRuleAttribute("currency", "ANDER").orElseThrow();
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
        var cashflowMap = groupCashflows(cashflows, andComposer);
        assertThat(cashflowMap).hasSize(2);
        assertThat(cashflowMap).containsOnlyKeys("Lehman Brothers PLC-EUR", "Meryl Lynch PLC-USD");
        assertThat(cashflowMap.get("Lehman Brothers PLC-EUR")).hasSize(2);
        assertThat(cashflowMap.get("Meryl Lynch PLC-USD")).hasSize(3);
    }

    @Test
    public void givenCashFlowsWhenOrService_ThenGroupCashflows() {
        createRule("Cashflows Anding Rule","ANDER", 1, true);
        var businessRule = ruleService.findByNameAndType("Cashflows Anding Rule", "ANDER", true).orElseThrow();
        createAttribute(businessRule, "counterParty", "ANDER", "Counter Party");
        createAttribute(businessRule, "settlementDate", "ANDER", "Settlement Date");
        createAttribute(businessRule, "currency", "ANDER", "Currency");
        var stmtDateAttrib = ruleService.findRuleAttribute("settlementDate","ANDER").orElseThrow();
        var cptyAttrib = ruleService.findRuleAttribute("counterParty", "ANDER").orElseThrow();
        var currencyAttrib = ruleService.findRuleAttribute("currency", "ANDER").orElseThrow();
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
        var cashflowMap = groupCashflows(cashflows, orComposer);
        assertThat(cashflowMap).hasSize(4);
        assertThat(cashflowMap).containsOnlyKeys("Lehman Brothers PLC-EUR","Meryl Lynch PLC-USD", "HSBC-INR", "Lehman Brothers PLC-YUAN");
        assertThat(cashflowMap.get("Lehman Brothers PLC-EUR")).hasSize(2);
        assertThat(cashflowMap.get("Meryl Lynch PLC-USD")).hasSize(3);
        assertThat(cashflowMap.get("HSBC-INR")).hasSize(1);
        assertThat(cashflowMap.get("Lehman Brothers PLC-YUAN")).hasSize(1);
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
            var andCondition = conditionService.getCondition(cashflow, "Cashflows Anding Rule", "ANDER", true);
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

    private void createRule(String ruleName, String ruleType, int priority, boolean isActive) {
        try {
            ruleService.createRule(ruleName, ruleType, "testing", priority, isActive);
        }catch (Exception ex) {

        }
    }

    private void createAttribute(BusinessRule businessRule, String attributeName, String ruleType, String displayName) {
        try {
            ruleService.createAttribute(businessRule, attributeName, ruleType, displayName);
        }catch (Exception ex) {

        }
    }
}
