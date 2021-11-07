package com.umar.apps.rule.domain;

import com.umar.apps.util.GenericBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.util.Optional;

import static com.umar.apps.infra.dao.api.core.AbstractTxExecutor.doInJPA;
import static org.assertj.core.api.Assertions.assertThat;

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

        var optAttr = findByAttributeNameAndType("testAttrib", "Test");
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

    private Optional<RuleAttribute> findByAttributeNameAndType(String attributeName, String ruleType) {
        var query = """
                SELECT ra FROM RuleAttribute ra
                WHERE ra.attributeName = :attributeName
                AND ra.ruleType = :ruleType
                """;
        return doInJPA(() -> emf, entityManager -> {
            return entityManager.createQuery(query, RuleAttribute.class)
                    .setParameter("ruleType", ruleType)
                    .setParameter("attributeName", attributeName)
                    .getResultStream().findFirst();
        }, null);
    }
}
