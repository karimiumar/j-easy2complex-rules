package com.umar.apps.rule;

import com.umar.apps.rule.api.*;
import com.umar.apps.rule.api.Rule;
import com.umar.apps.rule.api.core.InferenceRuleEngine;
import com.umar.apps.rule.api.core.RuleBuilder;
import com.umar.apps.rule.dao.Trade;
import com.umar.apps.rule.dao.TradeDao;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

public class BusinessRuleEngineTest {

    @Test
    public void givenTrade_WhenAllFacts_ThenTradeMarkedNonSTP() {

        var tradeDao = new TradeDao();
        var trade = new Trade();
        trade.setAmount(2300000.00);
        trade.setCounterParty("Historic Defaulter Party X");
        trade.setCurrency("KOD");
        trade.setStpAllowed(true);
        tradeDao.save(trade);
        System.out.println("Before applying rules ->"  +trade);
        com.umar.apps.rule.api.Facts facts = new com.umar.apps.rule.api.Facts();
        facts.put("trade", trade);
        Condition counterPartyCondition = getCondition(trade, "counterParty", "Historic Defaulter Party X");
        Condition currencyCondition = getCondition(trade, "currency", "KOD");
        Condition amountCondition = getCondition(trade, "amount", 2300000.00);
        Rule stpRules = new RuleBuilder((o1, o2) -> o1.getId().compareTo(trade.getId()))
                .name("STP Rules")
                .when(counterPartyCondition.and(currencyCondition).and(amountCondition)).then(action -> {
                    applySTPRule(tradeDao, trade, "Trade marked as NON-STP.");
                }).build();
        Rules rules = new Rules();
        rules.register(stpRules);
        RulesEngine rulesEngine = new InferenceRuleEngine();
        rulesEngine.fire(rules, facts);
        assertFalse(trade.isStpAllowed());
        assertEquals("Trade marked as NON-STP.", trade.getComment());
        System.out.println("After applying rules ->"  +trade);
    }

    @Test
    public void givenTrade_WhenNotAllFacts_ThenTradeMarkedSTP() {

        var tradeDao = new TradeDao();
        var trade = new Trade();
        trade.setAmount(2306000.00);
        trade.setCounterParty("Historic Defaulter Party X");
        trade.setCurrency("EUR");
        trade.setStpAllowed(true);
        tradeDao.save(trade);
        System.out.println("Before applying rules ->"  +trade);
        com.umar.apps.rule.api.Facts facts = new com.umar.apps.rule.api.Facts();
        facts.put("trade", trade);
        Condition counterPartyCondition = getCondition(trade, "counterParty", "Historic Defaulter Party X");//Store this fact in Database. Use reflection API to invoke this equals
        Condition currencyCondition = getCondition(trade, "currency", "KOD");//Store this fact in Database. Use reflection API to invoke this equals
        Condition amountCondition = getCondition(trade, "amount", 2300000.00);//Store this fact in Database. Use reflection API to invoke this equals
        //All the conditions should be part of database objects and created at runtime.
        Rule stpRules = new RuleBuilder((o1, o2) -> o1.getId().compareTo(trade.getId()))
                .name("STP Rules")
                .when(counterPartyCondition.and(currencyCondition).and(amountCondition)).then(action -> {
                    applySTPRule(tradeDao, trade, "Trade marked as NON-STP.");
                }).build();
        Rules rules = new Rules();
        rules.register(stpRules);
        RulesEngine rulesEngine = new InferenceRuleEngine();
        rulesEngine.fire(rules, facts);
        assertTrue(trade.isStpAllowed());
        assertNotEquals("Trade marked as NON-STP.", trade.getComment());
        System.out.println("After applying rules ->"  +trade);
    }

    @Test
    public void givenTrade_WhenAnyFact_ThenTradeMarkedNonSTP() {

        var tradeDao = new TradeDao();
        var trade = new Trade();
        trade.setAmount(2306000.00);
        trade.setCounterParty("Historic Defaulter Party X");
        trade.setCurrency("EUR");
        trade.setStpAllowed(true);
        tradeDao.save(trade);
        System.out.println("Before applying rules ->"  +trade);
        com.umar.apps.rule.api.Facts facts = new com.umar.apps.rule.api.Facts();
        facts.put("trade", trade);
        Condition counterPartyCondition = getCondition(trade, "counterParty", "Historic Defaulter Party X");//Store this fact in Database. Use reflection API to invoke this equals
        Condition currencyCondition = getCondition(trade, "currency", "KOD");//Store this fact in Database. Use reflection API to invoke this equals
        Condition amountCondition = getCondition(trade, "amount", 2300000.00);//Store this fact in Database. Use reflection API to invoke this equals
        //All the conditions should be part of database objects and created at runtime.
        Rule stpRules = new RuleBuilder((o1, o2) -> o1.getId().compareTo(trade.getId()))
                .name("STP Rules")
                .when(counterPartyCondition.or(currencyCondition).or(amountCondition)).then(action -> {
                    applySTPRule(tradeDao, trade, "Trade marked as NON-STP.");
                }).build();
        Rules rules = new Rules();
        rules.register(stpRules);
        RulesEngine rulesEngine = new InferenceRuleEngine();
        rulesEngine.fire(rules, facts);
        assertFalse(trade.isStpAllowed());
        assertEquals("Trade marked as NON-STP.", trade.getComment());
        System.out.println("After applying rules ->"  +trade);
    }

    @Test
    public void givenTrade_WhenReversedAFact_ThenTradeMarkedNonSTP() {

        var tradeDao = new TradeDao();
        var trade = new Trade();
        trade.setAmount(2306000.00);
        trade.setCounterParty("Merryl Lynch");
        trade.setCurrency("EUR");
        trade.setStpAllowed(true);
        tradeDao.save(trade);
        System.out.println("Before applying rules ->"  +trade);
        com.umar.apps.rule.api.Facts facts = new com.umar.apps.rule.api.Facts();
        facts.put("trade", trade);
        Condition counterPartyCondition = getCondition(trade, "counterParty", "Historic Defaulter Party X");//Store this fact in Database. Use reflection API to invoke this equals
        //All the conditions should be part of database objects and created at runtime.
        Rule stpRules = new RuleBuilder((o1, o2) -> o1.getId().compareTo(trade.getId()))
                .name("STP Rules")
                .when(counterPartyCondition.not()).then(action -> {
                    applySTPRule(tradeDao, trade, "Trade marked as NON-STP. Rule Condition inversed.");
                }).build();
        Rules rules = new Rules();
        rules.register(stpRules);
        RulesEngine rulesEngine = new InferenceRuleEngine();
        rulesEngine.fire(rules, facts);
        assertFalse(trade.isStpAllowed());
        assertEquals("Trade marked as NON-STP. Rule Condition inversed.", trade.getComment());
        System.out.println("After applying rules ->"  +trade);
    }

    @Test
    public void givenTrade_WhenNoneOfTheFacts_ThenTradeMarkedSTP() {

        var tradeDao = new TradeDao();
        var trade = new Trade();
        trade.setAmount(2306000.00);
        trade.setCounterParty("Merryl Lynch");
        trade.setCurrency("EUR");
        trade.setStpAllowed(true);
        tradeDao.save(trade);
        System.out.println("Before applying rules ->"  +trade);
        com.umar.apps.rule.api.Facts facts = new com.umar.apps.rule.api.Facts();
        facts.put("trade", trade);
        Condition counterPartyCondition = getCondition(trade, "counterParty", "Historic Defaulter Party X");//Store this fact in Database. Use reflection API to invoke this equals
        Condition currencyCondition = getCondition(trade, "currency", "KOD");//Store this fact in Database. Use reflection API to invoke this equals
        Condition amountCondition = getCondition(trade, "amount", 2300000.00);//Store this fact in Database. Use reflection API to invoke this equals
        //All the conditions should be part of database objects and created at runtime.
        Rule stpRules = new RuleBuilder((o1, o2) -> o1.getId().compareTo(trade.getId()))
                .name("STP Rules")
                .when(counterPartyCondition.or(currencyCondition).or(amountCondition))
                .then(action -> {
                    applySTPRule(tradeDao, trade, "Trade marked as NON-STP.");
                }).build();
        Rules rules = new Rules();
        rules.register(stpRules);
        RulesEngine rulesEngine = new InferenceRuleEngine();
        rulesEngine.fire(rules, facts);
        assertTrue(trade.isStpAllowed());
        assertNotEquals("Trade marked as NON-STP.", trade.getComment());
        System.out.println("After applying rules ->"  +trade);
    }

    private void applySTPRule(TradeDao tradeDao, Trade trade, String comment) {
        trade.setStpAllowed(false);
        trade.setComment(comment);
        tradeDao.merge(trade);
    }

    private <T> Condition getCondition (T workflowItem, String fieldName, Object object) {
        try {
            Field field = workflowItem.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(workflowItem);
            return condition -> value.equals(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}

