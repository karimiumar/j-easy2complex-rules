package com.umar.apps.rule.domain;

import com.umar.apps.util.GenericBuilder;
import org.hibernate.LazyInitializationException;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import java.util.Optional;

import static com.umar.apps.infra.dao.api.core.AbstractTxExecutor.doInJPA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RuleAttributeEntityTest {

    private static EntityManagerFactory emf;

    @BeforeAll
    static void beforeAll() {
        emf = Persistence.createEntityManagerFactory("rulesTestPU");
    }

    @AfterAll
    static void afterAll() {
        if(null != emf && emf.isOpen()) {
            emf.close();
        }
    }

    @AfterEach
    void afterEach() {
        doInJPA(() -> emf, entityManager -> {
            entityManager.createQuery("DELETE FROM BusinessRule").executeUpdate();
            entityManager.createQuery("DELETE FROM RuleAttribute").executeUpdate();
        }, null);
    }

    /**
     * Create a {@link RuleAttribute} with given attributeName, displayName and ruleType.
     * Persist the created {@link RuleAttribute} using the JPA Entity Manager.
     *
     * The persisted {@link RuleAttribute} should have id greater than 0 (i.e,a db key)
     * The persisted {@link RuleAttribute} should have created property populated
     * The persisted {@link RuleAttribute} should have updated property populated
     * The persisted {@link RuleAttribute} should have version property managed by Hibernate
     * The persisted {@link RuleAttribute} should have same created and updated values
     * The persisted {@link RuleAttribute} should have businessRule property null
     */
    @Test
    void when_an_attribute_is_created_then_created_updated_are_equal_and_version_is_0() {
        var testAttrib = GenericBuilder.of(RuleAttribute::new)
                .with(RuleAttribute::setAttributeName, "testAttrib")
                .with(RuleAttribute::setRuleType, "Test")
                .with(RuleAttribute::setDisplayName, "Test Attribute")
                .build();
        doInJPA(() -> emf, entityManager -> {
            entityManager.persist(testAttrib);
        }, null);

        var optAttr = findAttributeByNameAndType("testAttrib", "Test");
        optAttr.ifPresentOrElse(attr -> {
            assertThat(attr).isNotNull();
            assertThat(attr.getId()).isGreaterThan(0);
            assertThat(attr.getAttributeName()).isEqualTo("testAttrib");
            assertThat(attr.getBusinessRule()).isNull();
            assertThat(attr.getCreated()).isNotNull();
            assertThat(attr.getUpdated()).isNotNull();
            assertThat(attr.getRuleType()).isEqualTo("Test");
            assertThat(attr.getCreated()).isEqualTo(attr.getUpdated());
            assertThat(attr.getVersion()).isEqualTo(0);
        }, () -> {
            throw new RuntimeException("No RuleAttribute exists for the given params");
        });
    }

    /**
     * Create a {@link BusinessRule} with ruleName, ruleType, description, priority
     * Create a {@link RuleAttribute} with given attributeName, displayName and ruleType and the created {@link BusinessRule}
     *
     * Persist the created {@link RuleAttribute} using the JPA Entity Manager.
     *
     * The JPA should rollback the transaction with reason {@link javax.persistence.RollbackException}:
     * Error while committing the transaction
     *
     * The JPA should throw {@link javax.persistence.PersistenceException} :
     * {@link org.hibernate.exception.ConstraintViolationException}: could not execute statement
     * with the message that: {@link org.hibernate.TransientPropertyValueException}: object references an unsaved
     * transient instance - save the transient instance before flushing :
     * com.umar.apps.rule.domain.RuleAttribute.businessRule -> com.umar.apps.rule.domain.BusinessRule
     */
    @Test
    void when_an_attribute_is_created_with_rule_then_PersistenceException_is_thrown_on_persisting() {
        var rule = GenericBuilder.of(BusinessRule::new)
                .with(BusinessRule::setRuleName, "Test Rule")
                .with(BusinessRule::setRuleType, "Test")
                .with(BusinessRule::setDescription, "Testing")
                .with(BusinessRule::setPriority, 5)
                .with(BusinessRule::setActive, false)
                .build();
        var testAttrib = GenericBuilder.of(RuleAttribute::new)
                .with(RuleAttribute::setAttributeName, "testAttrib")
                .with(RuleAttribute::setRuleType, "Test")
                .with(RuleAttribute::setDisplayName, "Test Attribute")
                .with(RuleAttribute::setBusinessRule, rule)
                .build();
        assertThatThrownBy(() ->
                doInJPA(() -> emf, entityManager -> {
            entityManager.persist(testAttrib);
        }, null))
                .isInstanceOf(PersistenceException.class)
                .hasCauseExactlyInstanceOf(IllegalStateException.class);
    }

    /**
     * Create a {@link BusinessRule} with ruleName, ruleType, description, priority
     * Create a {@link RuleAttribute} with given attributeName, displayName and ruleType.
     *
     * Persist the created {@link BusinessRule} using the JPA Entity Manager.
     * Associate the persisted {@link BusinessRule} with the created {@link RuleAttribute} that is transient
     * Persist the created {@link RuleAttribute} using the JPA Entity Manager.
     *
     * The persisted {@link RuleAttribute} should have id greater than 0 (i.e,a db key)
     * The persisted {@link RuleAttribute} should have created property populated
     * The persisted {@link RuleAttribute} should have updated property populated
     * The persisted {@link RuleAttribute} should have version property managed by Hibernate
     * The persisted {@link RuleAttribute} should have same created and updated values
     * The persisted {@link RuleAttribute} should have businessRule property populated
     */
    @Test
    void when_an_attribute_is_created_with_persisted_rule_and_persisted_then_success() {
        var rule = GenericBuilder.of(BusinessRule::new)
                .with(BusinessRule::setRuleName, "Test Rule")
                .with(BusinessRule::setRuleType, "Test")
                .with(BusinessRule::setDescription, "Testing")
                .with(BusinessRule::setPriority, 5)
                .with(BusinessRule::setActive, false)
                .build();
        var testAttrib = GenericBuilder.of(RuleAttribute::new)
                .with(RuleAttribute::setAttributeName, "testAttrib")
                .with(RuleAttribute::setRuleType, "Test")
                .with(RuleAttribute::setDisplayName, "Test Attribute")
                .build();
        doInJPA(() -> emf, entityManager -> {
             entityManager.persist(rule);
             entityManager.flush();
             testAttrib.setBusinessRule(entityManager.find(BusinessRule.class, rule.getId()));
             entityManager.persist(testAttrib);
        }, null);

        var optAttr = findAttributeByNameAndType("testAttrib", "Test");
        optAttr.ifPresentOrElse(attr -> {
            assertThat(attr).isNotNull();
            assertThat(attr.getId()).isGreaterThan(0);
            assertThat(attr.getAttributeName()).isEqualTo("testAttrib");
            assertThat(attr.getBusinessRule()).isNotNull();
            assertThat(attr.getCreated()).isNotNull();
            assertThat(attr.getUpdated()).isNotNull();
            assertThat(attr.getRuleType()).isEqualTo("Test");
            assertThat(attr.getCreated()).isEqualTo(attr.getUpdated());
            assertThat(attr.getVersion()).isEqualTo(0);
            var br = attr.getBusinessRule();
            assertThat(br.getCreated()).isNotNull();
            assertThat(br.getUpdated()).isNotNull();
            assertThat(br.getId()).isGreaterThan(0);
            assertThat(br.getPriority()).isEqualTo(5);
            assertThat(br.isActive()).isFalse();
            assertThat(br.getRuleAttributes()).isNotNull();
            assertThatThrownBy(()-> br.getRuleAttributes().size()).isInstanceOf(LazyInitializationException.class);
        }, () -> {
            throw new RuntimeException("No RuleAttribute exists for the given params");
        });
    }

    /**
     * Create a {@link RuleAttribute} with given attributeName, displayName and ruleType.
     * Persist the created {@link RuleAttribute} using the JPA Entity Manager.
     *
     * The persisted {@link RuleAttribute} should have id greater than 0 (i.e,a db key)
     * The persisted {@link RuleAttribute} should have created property populated
     * The persisted {@link RuleAttribute} should have updated property populated
     * The persisted {@link RuleAttribute} should have version property managed by Hibernate
     * The persisted {@link RuleAttribute} should have same created and updated values
     * The persisted {@link RuleAttribute} should have businessRule property null
     *
     * Now amend the persisted {@link RuleAttribute} and persist it.
     * The amended {@link RuleAttribute} id should be same as the original persisted entity
     * The amended {@link RuleAttribute} created should be same as the original persisted entity
     * The amended {@link RuleAttribute} updated should be greater than the original persisted entity
     * The amended {@link RuleAttribute} version should be 1 higher than the original persisted entity
     */
    @Test
    void when_a_persisted_attribute_is_amended_and_persisted_then_version_and_updated_change() {
        var testAttrib = GenericBuilder.of(RuleAttribute::new)
                .with(RuleAttribute::setAttributeName, "testAttrib")
                .with(RuleAttribute::setRuleType, "Test")
                .with(RuleAttribute::setDisplayName, "Test Attribute")
                .build();
        doInJPA(() -> emf, entityManager -> {
            entityManager.persist(testAttrib);
        }, null);

        var optAttr = findAttributeByNameAndType("testAttrib", "Test");
        var version1 = optAttr.orElseThrow();
        optAttr.ifPresent(attr -> {
            doInJPA(() -> emf, entityManager -> {
                var session = entityManager.unwrap(Session.class);
                var tmp = session.find(RuleAttribute.class, attr.getId());
                tmp.setRuleType("Test Rule");
                tmp.setDisplayName("Attribute Amended For Test");
                session.saveOrUpdate(tmp);
            }, null);
        });

        optAttr = findAttributeByNameAndType("testAttrib", "Test");
        optAttr.ifPresent(version2 -> {
            assertThat(version1.getId()).isEqualTo(version2.getId());
            assertThat(version1.getVersion()).isLessThan(version2.getVersion());
            assertThat(version1.getCreated()).isEqualTo(version2.getCreated());
            assertThat(version1.getUpdated()).isBefore(version2.getUpdated());
            assertThat(version1.getAttributeName()).isEqualTo(version2.getAttributeName());
            assertThat(version1.getRuleType()).isNotEqualTo(version2.getRuleType());
            assertThat(version1.getVersion()).isEqualTo(0);
            assertThat(version2.getVersion()).isEqualTo(1);
            assertThat(version1.getBusinessRule()).isNull();
            assertThat(version2.getBusinessRule()).isNull();
            assertThat(version1.getRuleValues()).isEmpty();
            assertThat(version2.getRuleValues()).isEmpty();
        });
    }

    private static Optional<RuleAttribute> findAttributeByNameAndType(String attribName, String ruleType) {
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
        }, null);
    }
}
