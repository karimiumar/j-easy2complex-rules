package com.umar.apps.rule;

import com.umar.apps.rule.api.*;
import com.umar.apps.rule.api.core.InferenceRuleEngine;
import com.umar.apps.rule.api.core.RuleBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;

public class AnonymousLoopTest {
    @Test
    public void test() {
        Facts facts = new Facts();
        Rules rules = new Rules();
        RulesEngine rulesEngine = new InferenceRuleEngine();
        List<Name> names = NamesFactory.fetchNames();
        for(Name name: names) {
            facts.put("name", name);
            Condition condition = fact -> !name.name().isEmpty();
            Rule nameRule = new RuleBuilder()
                    .name("Name Rule")
                    .when(condition).then(action -> {
                        System.out.println(name);
                    }).build();
            rules.register(nameRule);
            rulesEngine.fire(rules, facts);
        }
    }
}

record Name(Integer id, String name){}

class NamesFactory{
    static List<Name> fetchNames(){
        return List.of(new Name(10, "Sara"), new Name(20, "Zara"), new Name(30, "Lara"));
    }
}