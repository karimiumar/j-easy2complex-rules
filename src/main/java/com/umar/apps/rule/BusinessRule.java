package com.umar.apps.rule;

import com.umar.apps.rule.engine.WorkflowItem;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

@Entity
@Table(name = "rules"
        , uniqueConstraints = {
        @UniqueConstraint(columnNames = {"rule_name", "rule_type"})
}
)
public class BusinessRule implements WorkflowItem<Long>, Serializable {

    public static final String RULE$ID = "rule.id";
    public static final String RULE$ALIAS = "BusinessRule rule";
    public static final String RULE$RULE = "rule";
    public static final String RULE$RULE_NAME = "rule.ruleName";
    public static final String RULE$RULE_TYPE = "rule.ruleType";
    public static final String RULE$ACTIVE = "rule.active";
    public static final String RULE$RULE_ATTRIBS = "rule.ruleAttributes";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_name")
    private String ruleName;

    @Column
    private int priority;

    @Column(name = "rule_type")
    private String ruleType;

    @Column(name = "active")
    private boolean active;

    @OneToMany(mappedBy = "businessRule", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER, orphanRemoval = true)
    private final Set<RuleAttribute> ruleAttributes = new HashSet<>();

    @Column(name = "version")
    private int version;

    public int getVersion() {
        return version;
    }

    public BusinessRule(){}

    private BusinessRule(BusinessRuleBuilder builder) {
        ruleName = builder.ruleName;
        ruleType = builder.ruleType;
        active = builder.active;
        priority = builder.priority;
        if(null != builder.ruleAttribute) {
            addRuleAttribute(builder.ruleAttribute);
        }
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void addRuleAttribute(RuleAttribute attribute) {
        ruleAttributes.add(attribute);
        attribute.setBusinessRule(this);
    }

    public Set<RuleAttribute> getRuleAttributes() {
        return ruleAttributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BusinessRule that)) return false;
        return priority == that.priority &&
                active == that.active &&
                ruleName.equals(that.ruleName) &&
                ruleType.equals(that.ruleType) &&
                ruleAttributes.equals(that.ruleAttributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleName, priority, ruleType, active, ruleAttributes);
    }

    @Override
    public String toString() {
        return "BusinessRule{" +
                "id=" + id +
                ", ruleName='" + ruleName + '\'' +
                ", priority=" + priority +
                ", ruleType='" + ruleType + '\'' +
                ", active=" + active +
                '}';
    }

    public static class BusinessRuleBuilder {
        private final String ruleName;
        private final String ruleType;
        public boolean active;
        public int priority;
        public RuleAttribute ruleAttribute;

        public BusinessRuleBuilder(String ruleName, String ruleType) {
            this.ruleName = ruleName;
            this.ruleType = ruleType;
        }

        public BusinessRuleBuilder with(Consumer<BusinessRuleBuilder> consumer) {
            consumer.accept(this);
            return this;
        }

        public BusinessRule build() {
            return new BusinessRule(this);
        }
    }
}
