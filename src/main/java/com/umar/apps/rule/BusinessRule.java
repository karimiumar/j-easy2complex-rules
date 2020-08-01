package com.umar.apps.rule;

import com.umar.apps.rule.engine.WorkflowItem;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

@Entity
@Table(name = "rules", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"rule_name", "rule_type"})
})
public class BusinessRule implements WorkflowItem<Long>, Serializable {

    public static final String RULE$ID = "rule.id";
    public static final String RULE$ALIAS = "BusinessRule rule";
    public static final String RULE$RULE = "rule";
    public static final String RULE$RULE_NAME = "rule.ruleName";
    public static final String RULE$RULE_TYPE = "rule.ruleType";
    public static final String RULE$ACTIVE = "rule.active";

    private Long id;
    private String ruleName;
    private int priority;
    private String ruleType;
    private String desc;
    private boolean active;
    private int version;
    private Set<RuleAttribute> ruleAttributes = new HashSet<>();

    public BusinessRule(){}

    @Column
    public int getPriority() {
        return priority;
    }

    @Column(name = "rule_name", length = 100)
    public String getRuleName() {
        return ruleName;
    }

    @Column(name = "rule_type", length = 30)
    public String getRuleType() {
        return ruleType;
    }

    @Column(name = "[desc]", length = 100)
    public String getDesc() {
        return desc;
    }

    @OneToMany(mappedBy = "businessRule", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER, orphanRemoval = true)
    public Set<RuleAttribute> getRuleAttributes() {
        return ruleAttributes;
    }

    @Column(name = "active")
    public boolean isActive() {
        return active;
    }

    @Column(name = "version")
    public int getVersion() {
        return version;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void addRuleAttribute(RuleAttribute ruleAttribute) {
        ruleAttributes.add(ruleAttribute);
        ruleAttribute.setBusinessRule(this);
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRuleAttributes(Set<RuleAttribute> ruleAttributes) {
        this.ruleAttributes = ruleAttributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BusinessRule that)) return false;
        return priority == that.priority &&
                active == that.active &&
                ruleName.equals(that.ruleName) &&
                ruleType.equals(that.ruleType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleName, priority, ruleType, active);
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

    private BusinessRule(BusinessRuleBuilder builder) {
        ruleName = builder.ruleName;
        ruleType = builder.ruleType;
        active = builder.active;
        priority = builder.priority;
        desc = builder.desc;
    }

    public static class BusinessRuleBuilder {
        private final String ruleName;
        private final String ruleType;
        public boolean active;
        public int priority;
        public String desc;

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
