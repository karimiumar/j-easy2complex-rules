package com.umar.apps.rule.domain;

import com.umar.apps.infra.dao.api.WorkflowItem;
import com.umar.apps.rule.api.core.BasicRule;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "rules", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"rule_name", "rule_type"})
})
public class BusinessRule extends BasicRule implements WorkflowItem<Long>, Serializable {

    private String ruleType;
    private boolean active;
    private int version;
    private Set<RuleAttribute> ruleAttributes = new HashSet<>();

    public BusinessRule(){
        super();
    }

    public BusinessRule(String ruleName, String description, int priority, String ruleType, boolean active) {
        super(ruleName, description, priority);
        this.ruleType = ruleType;
        this.active = active;
    }

    @Column(name = "priority")
    @Override
    public int getPriority() {
        return priority;
    }

    @Column(name = "rule_name", length = 100)
    public String getRuleName() {
        return name;
    }

    @Column(name = "rule_type", length = 30)
    public String getRuleType() {
        return ruleType;
    }

    @Column(name = "[desc]", length = 100)
    @Override
    public String getDescription() {
        return description;
    }

    @OneToMany(mappedBy = "businessRule", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER, orphanRemoval = true)
    public Set<RuleAttribute> getRuleAttributes() {
        return ruleAttributes;
    }

    @Column(name = "active", columnDefinition = "boolean default true")
    public boolean isActive() {
        return active;
    }

    @Column(name = "version")
    public int getVersion() {
        return version;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Override
    public Long getId() {
        return id;
    }

    public void addRuleAttribute(RuleAttribute ruleAttribute) {
        ruleAttributes.add(ruleAttribute);
        ruleAttribute.setBusinessRule(this);
    }

    public void setRuleName(String ruleName) {
        this.name = ruleName;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public void setDescription(String description) {
        this.description = description;
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
                name.equals(that.name) &&
                ruleType.equals(that.ruleType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, priority, ruleType, active);
    }

    @Override
    public String toString() {
        return "BusinessRule{" +
                "id=" + id +
                ", ruleName='" + name + '\'' +
                ", priority=" + priority +
                ", ruleType='" + ruleType + '\'' +
                ", active=" + active +
                '}';
    } 

}
