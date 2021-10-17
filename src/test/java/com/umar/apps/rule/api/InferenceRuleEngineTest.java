package com.umar.apps.rule.api;

import com.umar.apps.rule.api.core.InferenceRuleEngine;
import com.umar.apps.rule.api.core.RuleBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.umar.apps.rule.api.Condition.holds;
import static org.assertj.core.api.Assertions.assertThat;

public class InferenceRuleEngineTest {

    @Test
    void when_name_is_blank_then_its_filtered() {
        var ruleEngine = new InferenceRuleEngine();
        List<Name> names = NamesFactory.fetchNames();
        //Add a blank name to test
        names.add(new Name(40, ""));
        List<Name> filteredNames = new ArrayList<>();
        var rules = new Rules();
        var facts = new Facts();
        int cnt = 10;
        for (Name name : names) {
            facts.put("fact-" + cnt, name);
            cnt +=10;
            var filterBlank = holds(fact -> !name.name().isBlank(), "Incoming Name is blank");
            var nameRule = new RuleBuilder((o1, o2) -> name.name().compareTo(o1.getName()))
                    .priority(1)
                    .name("FilterBlank Rule")
                    .description("Filters blank Names")
                    .when(filterBlank)
                    .then(action -> filteredNames.add(name)).build();
            rules.register(nameRule);
        }
        ruleEngine.fire(rules, facts);
        assertThat(filteredNames).containsAll(List.of(new Name(10, "Sara"), new Name(20, "Zara"), new Name(30, "Lara")));
        assertThat(filteredNames).doesNotContain(new Name(40, ""));
    }

    @Test
    void when_name_is_not_blank_then_its_not_filtered() {
        var ruleEngine = new InferenceRuleEngine();
        List<Name> names = NamesFactory.fetchNames();
        names.add(new Name(40, "Nara"));
        List<Name> filteredNames = new ArrayList<>();
        var rules = new Rules();
        var facts = new Facts();
        int cnt = 10;
        for (Name name : names) {
            facts.put("fact-" + cnt, name);
            cnt +=10;
            var filterBlank = holds(fact -> !name.name().isBlank(), "Incoming Name is blank");
            var nameRule = new RuleBuilder((o1, o2) -> name.name().compareTo(o1.getName()))
                    .priority(1)
                    .name("FilterBlank Rule")
                    .description("Filters blank Names")
                    .when(filterBlank)
                    .then(action -> filteredNames.add(name)).build();
            rules.register(nameRule);
        }
        ruleEngine.fire(rules, facts);
        assertThat(filteredNames).containsAll(List.of(new Name(10, "Sara"), new Name(20, "Zara"), new Name(30, "Lara"), new Name(40, "Nara")));
    }
}

record Name(Integer id, String name){}

class NamesFactory{
    static List<Name> fetchNames(){
        return new ArrayList<>() {{
            add(new Name(10, "Sara"));
            add(new Name(20, "Zara"));
            add(new Name(30, "Lara"));
        }};
    }
}