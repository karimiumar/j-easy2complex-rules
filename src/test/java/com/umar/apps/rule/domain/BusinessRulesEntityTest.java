package com.umar.apps.rule.domain;

import com.umar.apps.util.GenericBuilder;
import org.hibernate.Session;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Map;
import java.util.Optional;

import static com.umar.apps.infra.dao.api.core.AbstractTxExecutor.doInJPA;
import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BusinessRulesEntityTest {

    private static final Map<String, String> persistenceConfig = null;

    private static EntityManagerFactory emf;

    @BeforeAll
    static void beforeAll() {
       emf = Persistence.createEntityManagerFactory("rulesTestPU", persistenceConfig);
    }

    @AfterEach
    void afterEach() {
        var sql = "DELETE FROM BusinessRule";
        doInJPA(() -> emf, entityManager -> {
            entityManager.createQuery(sql).executeUpdate();
        }, persistenceConfig);
    }

    @AfterAll
    static void afterAll() {
        if(null != emf && emf.isOpen()) {
            emf.close();
        }
    }

    @Test
    void when_rules_created_without_attributes_then_attributes_is_empty() {
        doInJPA(() -> emf, entityManager -> {
            var testRule1= GenericBuilder.of(BusinessRule::new)
                    .with(BusinessRule::setRuleName, "Test Rule 1")
                    .with(BusinessRule::setRuleType, "Entity Test")
                    .with(BusinessRule::setDescription, "Tests Business Rule Storage in Database")
                    .with(BusinessRule::setActive, true)
                    .build();
            var testRule2= GenericBuilder.of(BusinessRule::new)
                    .with(BusinessRule::setRuleName, "Test Rule 2")
                    .with(BusinessRule::setRuleType, "Entity Test")
                    .with(BusinessRule::setDescription, "Tests Business Rule Storage in Database")
                    .with(BusinessRule::setActive, true)
                    .build();
            var testRule3= GenericBuilder.of(BusinessRule::new)
                    .with(BusinessRule::setRuleName, "Test Rule 3")
                    .with(BusinessRule::setRuleType, "Entity Test")
                    .with(BusinessRule::setDescription, "Tests Business Rule Storage in Database")
                    .with(BusinessRule::setActive, true)
                    .build();
            entityManager.persist(testRule1);
            entityManager.persist(testRule2);
            entityManager.persist(testRule3);
        }, persistenceConfig);

        var optRule1 = findRuleByNameAndType("Test Rule 1", "Entity Test");
        var optRule2 = findRuleByNameAndType("Test Rule 2", "Entity Test");
        var optRule3 = findRuleByNameAndType("Test Rule 3", "Entity Test");
        optRule1.ifPresentOrElse(rule -> callAssertions(rule, 0), () -> {throw new RuntimeException("Rule Not Found");});
        optRule2.ifPresentOrElse(rule -> callAssertions(rule, 0), () -> {throw new RuntimeException("Rule Not Found");});
        optRule3.ifPresentOrElse(rule -> callAssertions(rule, 0), () -> {throw new RuntimeException("Rule Not Found");});
    }

    private void callAssertions(BusinessRule rule, int hasSize) {
        assertThat(rule.getId()).isNotEqualTo(0);
        assertThat(rule.getCreated()).isNotNull();
        assertThat(rule.getUpdated()).isNotNull();
        assertThat(rule.getVersion()).isEqualTo(0);
        assertThat(rule.getRuleAttributes()).hasSize(hasSize);
    }

    @Test
    void when_an_attribute_is_added_to_rule_then_fetchRules_fetches_associated_attributes(){
        doInJPA(() -> emf, entityManager -> {
            var rule= GenericBuilder.of(BusinessRule::new)
                    .with(BusinessRule::setRuleName, "Rule Attribute Rule")
                    .with(BusinessRule::setRuleType, "RuleAttribute With Rule")
                    .with(BusinessRule::setDescription, "Tests RuleAttribute is associated with persisted Rule.")
                    .with(BusinessRule::setActive, true)
                    .build();
            entityManager.persist(rule);
        }, persistenceConfig);

        doInJPA(() -> emf, entityManager ->  {
            var session = entityManager.unwrap(Session.class);
            var rule = session.createQuery("FROM BusinessRule WHERE ruleName = :ruleName", BusinessRule.class)
                    .setParameter("ruleName", "Rule Attribute Rule").uniqueResult();
            var attribute = GenericBuilder.of(RuleAttribute::new)
                    .with(RuleAttribute::setAttributeName, "attrib")
                    .with(RuleAttribute::setRuleType, rule.getRuleType())
                    .with(RuleAttribute::setDisplayName, "Test Attribute")
                    .with(RuleAttribute::setBusinessRule, rule)
                    .build();
            rule.addRuleAttribute(attribute);
            session.save(attribute);
            session.saveOrUpdate(rule);
        }, persistenceConfig);

        var rule = findRuleByNameAndType("Rule Attribute Rule", "RuleAttribute With Rule");
        assertThat(rule).isPresent();
        assertThat(rule.get().getRuleAttributes()).hasSize(1);
    }

    @Test
    void when_rules_created_with_attributes_then_attributes_is_populated() {
        doInJPA(() -> emf, entityManager -> {
            var rule= GenericBuilder.of(BusinessRule::new)
                    .with(BusinessRule::setRuleName, "Rule with Attribute")
                    .with(BusinessRule::setRuleType, "RuleAttribute Entity Test")
                    .with(BusinessRule::setDescription, "Tests Business Rule Storage in Database")
                    .with(BusinessRule::setActive, true)
                    .build();
            var attribute = GenericBuilder.of(RuleAttribute::new)
                    .with(RuleAttribute::setAttributeName, "testAttribute")
                    .with(RuleAttribute::setRuleType, rule.getRuleType())
                    .with(RuleAttribute::setDisplayName, "Test Attribute")
                    .with(RuleAttribute::setBusinessRule, rule)
                    .build();
            rule.addRuleAttribute(attribute);
            entityManager.persist(rule);
        }, persistenceConfig);

        var optRule = findRuleByNameAndType("Rule with Attribute", "RuleAttribute Entity Test");
        assertThat(optRule).isPresent();
        assertThat(optRule.get().getRuleAttributes()).hasSize(1);
        assertThat(optRule.get().getRuleAttributes().stream().findFirst().get().getAttributeName()).isEqualTo("testAttribute");
    }

    @Test
    void when_an_attribute_is_removed_from_rule_then_associated_attributes_is_shrinked(){
        doInJPA(() -> emf, entityManager -> {
            var rule= GenericBuilder.of(BusinessRule::new)
                    .with(BusinessRule::setRuleName, "Multi Attribute Rule")
                    .with(BusinessRule::setRuleType, "Attrib Shrink Test")
                    .with(BusinessRule::setDescription, "Tests Business Rule Storage in Database")
                    .with(BusinessRule::setActive, true)
                    .build();
            var attribute1 = GenericBuilder.of(RuleAttribute::new)
                    .with(RuleAttribute::setAttributeName, "test1")
                    .with(RuleAttribute::setRuleType, rule.getRuleType())
                    .with(RuleAttribute::setDisplayName, "Test Attribute 1")
                    .with(RuleAttribute::setBusinessRule, rule)
                    .build();
            var attribute2 = GenericBuilder.of(RuleAttribute::new)
                    .with(RuleAttribute::setAttributeName, "test2")
                    .with(RuleAttribute::setRuleType, rule.getRuleType())
                    .with(RuleAttribute::setDisplayName, "Test Attribute 2")
                    .with(RuleAttribute::setBusinessRule, rule)
                    .build();
            rule.addRuleAttribute(attribute1);
            rule.addRuleAttribute(attribute2);
            entityManager.persist(rule);
        }, persistenceConfig);

        var optRule = findRuleByNameAndType("Multi Attribute Rule", "Attrib Shrink Test");
        assertThat(optRule).isPresent();
        assertThat(optRule.get().getRuleAttributes()).hasSize(2);

        //Now remove one attribute
        doInJPA(() -> emf, entityManager -> {
            optRule.ifPresent(rule -> {
                var test1 = rule.getRuleAttributes().stream().filter(at -> at.getAttributeName().equals("test1")).findFirst();
                test1.ifPresent(attr1 -> {
                    var session = entityManager.unwrap(Session.class);
                    var br = session.find(BusinessRule.class, rule.getId());
                    var attrToRemove = session.find(RuleAttribute.class, attr1.getId());
                    br.removeRuleAttribute(attrToRemove);
                    session.saveOrUpdate(br);
                });
            });
        }, persistenceConfig);

        var shrinkedRule = findRuleByNameAndType("Multi Attribute Rule", "Attrib Shrink Test");
        assertThat(shrinkedRule).isPresent();
        assertThat(shrinkedRule.get().getRuleAttributes()).hasSize(1); //One Attribute removed so size is now 1
    }

    @Test
    void when_a_rule_is_created_then_created_and_updated_have_same_values_and_version_is_0() {
        doInJPA(() -> emf, entityManager -> {
            var rule= GenericBuilder.of(BusinessRule::new)
                    .with(BusinessRule::setRuleName, "Test Rule")
                    .with(BusinessRule::setRuleType, "Testing")
                    .with(BusinessRule::setDescription, "Tests created, updated and version is populated")
                    .with(BusinessRule::setActive, true)
                    .build();
            entityManager.persist(rule);
        }, persistenceConfig);

        var optRule = findRuleByNameAndType("Test Rule", "Testing");
        optRule.ifPresentOrElse(rule -> {
            assertThat(rule).isInstanceOf(BusinessRule.class);
            assertThat(rule.getCreated()).isNotNull();
            assertThat(rule.getUpdated()).isNotNull();
            assertThat(rule.getCreated()).isEqualTo(rule.getUpdated());
            assertThat(rule.getVersion()).isEqualTo(0);
        }, () -> {
            throw new RuntimeException("Rule Not Found");
        });
    }

    @Test
    void when_a_rule_is_amended_then_created_and_updated_have_different_values_and_version_is_incremented() {
        doInJPA(() -> emf, entityManager -> {
            var rule= GenericBuilder.of(BusinessRule::new)
                    .with(BusinessRule::setRuleName, "Test Rule")
                    .with(BusinessRule::setRuleType, "Testing")
                    .with(BusinessRule::setDescription, "Tests created and updated have different values and version is incremented by 1")
                    .with(BusinessRule::setActive, true)
                    .build();
            entityManager.persist(rule);
        }, persistenceConfig);

        doInJPA(() -> emf, entityManager -> {
            var session = entityManager.unwrap(Session.class);
            var rule = session
                    .createQuery("FROM BusinessRule br WHERE br.ruleName=:ruleName AND br.ruleType=:ruleType", BusinessRule.class)
                            .setParameter("ruleName", "Test Rule")
                                    .setParameter("ruleType", "Testing")
                                            .getSingleResult();
            rule.setDescription("Amending description for testing");
            session.saveOrUpdate(rule);
        }, persistenceConfig);

        var optRule = findRuleByNameAndType("Test Rule", "Testing");
        optRule.ifPresentOrElse(rule -> {
            assertThat(rule).isInstanceOf(BusinessRule.class);
            assertThat(rule.getCreated()).isNotNull();
            assertThat(rule.getUpdated()).isNotNull();
            assertThat(rule.getCreated()).isBefore(rule.getUpdated());
            assertThat(rule.getVersion()).isGreaterThan(0);
        }, () -> {
            throw new RuntimeException("Rule Not Found");
        });
    }

    @Test
    void when_a_rule_is_deleted_then_not_present_in_database() {
        doInJPA(() -> emf, entityManager -> {
            var rule= GenericBuilder.of(BusinessRule::new)
                    .with(BusinessRule::setRuleName, "Test Rule")
                    .with(BusinessRule::setRuleType, "Testing")
                    .with(BusinessRule::setDescription, "Tests rule is deleted from database.")
                    .with(BusinessRule::setActive, true)
                    .build();
            entityManager.persist(rule);
        }, persistenceConfig);

        var optRule = findRuleByNameAndType("Test Rule", "Testing");
        optRule.ifPresentOrElse(rule -> {
            doInJPA(() -> emf, entityManager -> {
                var session = entityManager.unwrap(Session.class);
                session.remove(session.find(BusinessRule.class, rule.getId()));
            }, persistenceConfig);
        }, () -> {
            throw new RuntimeException("Rule Not Found");
        });

        var deleted = findRuleByNameAndType("Test Rule", "Testing");
        deleted.ifPresent(r -> {
            throw new RuntimeException("Rule still exists despite deletion");
        });
    }

    @Test
    void when_a_rule_is_deleted_then_associated_attributes_also_deleted() {
        doInJPA(() -> emf, entityManager -> {
            var rule= GenericBuilder.of(BusinessRule::new)
                    .with(BusinessRule::setRuleName, "Test Rule")
                    .with(BusinessRule::setRuleType, "Testing")
                    .with(BusinessRule::setDescription, "Tests associated attributes deleted provided parent rule is deleted.")
                    .with(BusinessRule::setActive, true)
                    .build();
            var attribute1 = GenericBuilder.of(RuleAttribute::new)
                    .with(RuleAttribute::setAttributeName, "test1")
                    .with(RuleAttribute::setRuleType, rule.getRuleType())
                    .with(RuleAttribute::setDisplayName, "Test Attribute 1")
                    .with(RuleAttribute::setBusinessRule, rule)
                    .build();
            var attribute2 = GenericBuilder.of(RuleAttribute::new)
                    .with(RuleAttribute::setAttributeName, "test2")
                    .with(RuleAttribute::setRuleType, rule.getRuleType())
                    .with(RuleAttribute::setDisplayName, "Test Attribute 2")
                    .with(RuleAttribute::setBusinessRule, rule)
                    .build();
            rule.addRuleAttribute(attribute1);
            rule.addRuleAttribute(attribute2);
            entityManager.persist(rule);
        }, persistenceConfig);

        var attrib1 = GenericBuilder.of(RuleAttribute::new)
                .with(RuleAttribute::setAttributeName, "test1")
                .with(RuleAttribute::setRuleType, "Testing")
                .build();
        var attrib2 = GenericBuilder.of(RuleAttribute::new)
                .with(RuleAttribute::setAttributeName, "test2")
                .with(RuleAttribute::setRuleType, "Testing")
                .build();

        var optRule = findRuleByNameAndType("Test Rule", "Testing");
        optRule.ifPresentOrElse(rule -> {
            callAssertions(rule, 2);
            var attributes = rule.getRuleAttributes();
            assertThat(attributes).containsExactly(attrib1, attrib2);
            doInJPA(() -> emf, entityManager -> {
                var session = entityManager.unwrap(Session.class);
                session.remove(session.find(BusinessRule.class, rule.getId()));
                session.flush();
            }, persistenceConfig);
        }, () -> {
            throw new RuntimeException("Rule Not Found");
        });

        var deleted = findRuleByNameAndType("Test Rule", "Testing");
        deleted.ifPresent(r -> {
            throw new RuntimeException("Rule still exists despite deletion");
        });
        var test1Attrib = findAttributeByNameAndType("test1", "Testing");
        test1Attrib.ifPresent(a -> {
            throw new RuntimeException("RuleAttribute test1 still exists despite deletion");
        });

        var test2Attrib = findAttributeByNameAndType("test2", "Testing");
        test2Attrib.ifPresent(a -> {
            throw new RuntimeException("RuleAttribute test2 still exists despite deletion");
        });
    }

    private Optional<BusinessRule> findRuleByNameAndType(String ruleName, String ruleType) {
        return doInJPA(() -> emf, entityManager -> {
            var query = entityManager.createQuery("""
                    SELECT rule FROM BusinessRule rule
                    LEFT JOIN FETCH rule.ruleAttributes ra
                    LEFT JOIN FETCH ra.ruleValues rv
                    WHERE rule.ruleName =:ruleName
                    AND rule.ruleType =:ruleType
                    """, BusinessRule.class);
            return query
                    .setParameter("ruleName", ruleName)
                    .setParameter("ruleType", ruleType)
                    .getResultStream().findFirst();
        }, persistenceConfig);
    }

    private Optional<RuleAttribute> findAttributeByNameAndType(String attribName, String ruleType) {
        return doInJPA(() -> emf, entityManager -> {
            var query = entityManager.createQuery("""
                    SELECT ra FROM RuleAttribute ra
                    LEFT JOIN FETCH ra.businessRule rule
                    LEFT JOIN FETCH ra.ruleValues rv
                    WHERE ra.attributeName =:attribName
                    AND ra.ruleType =:ruleType
                    """, RuleAttribute.class);
            return query
                    .setParameter("attribName", attribName)
                    .setParameter("ruleType", ruleType)
                    .getResultStream().findFirst();
        }, persistenceConfig);
    }
}
