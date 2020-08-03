package com.umar.apps.rule;

import com.umar.apps.rule.api.*;
import com.umar.apps.rule.api.core.InferenceRuleEngine;
import com.umar.apps.rule.api.core.RuleBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class AnonymousLoopTest {
    @Test
    public void test() {
        RulesEngine rulesEngine = new InferenceRuleEngine();
        List<Name> names = NamesFactory.fetchNames();
        Rules rules = new Rules();
        Facts facts = new Facts();
        AtomicReference<Integer> countRef = new AtomicReference<>(1);
        names.forEach(personName -> {
            facts.put("name-" + countRef.get(), personName);
            countRef.set(countRef.get()+1);
            Condition condition = fact -> !personName.name().isEmpty();
            //Hack the comparator logic of DefaultRule/BasicRule in order to override its internal logic as below.
            //This is needed to register our Rule with Rules which uses a Set<Rule> to register new Rules
            //with the comparator logic written in BasicRule.
            Rule nameRule = new RuleBuilder((o1, o2) -> personName.name().compareTo(o1.getName()))
                    .when(condition).then(action -> System.out.println("In Action:" + personName)).build();
            rules.register(nameRule);
        });
        rulesEngine.fire(rules, facts);
    }
}

record Name(Integer id, String name){}

class NamesFactory{
    static List<Name> fetchNames(){
        return List.of(new Name(10, "Sara"), new Name(20, "Zara"), new Name(30, ""),new Name(40, "Lara"));
    }
}