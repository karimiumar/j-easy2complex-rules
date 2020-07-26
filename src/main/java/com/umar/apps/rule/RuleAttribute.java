package com.umar.apps.rule;

import com.umar.apps.rule.engine.WorkflowItem;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "attributes"
        , uniqueConstraints = {
        @UniqueConstraint(columnNames = {"attribute_name", "rule_type"})
}
)
public class RuleAttribute implements WorkflowItem<Long>, Serializable {

    public static final String ATTRIB$ID = "attr.id";
    public static final String ATTRIB$ALIAS = "RuleAttribute attr";
    public static final String ATTRIB$ATTRIB = "attr";
    public static final String ATTRIB$ATTRIB_NAME ="attr.attributeName";
    public static final String ATTRIB$RULE_TYPE ="attr.ruleType";
    public static final String ATTRIB$RULE = "attr.businessRule";
    public static final String ATTRIB$DISPLAY_NAME = "attr.displayName";


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attribute_name")
    private String attributeName;

    @Column(name = "rule_type")
    private String ruleType;

    @Column(name = "version")
    private int version;

    @Column(name = "display_name")
    private String displayName;

    @ManyToOne
    @JoinTable(
            name = "rule_attribute",
            joinColumns = @JoinColumn(name = "attribute_id"),
            inverseJoinColumns = @JoinColumn(name = "rule_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"attribute_id","rule_id"})
    )
    private BusinessRule businessRule;

    @OneToMany(mappedBy = "ruleAttribute",cascade = {CascadeType.PERSIST, CascadeType.MERGE},fetch= FetchType.EAGER, orphanRemoval = true)
    private final Set<RuleValue> ruleValues = new HashSet<>();

    public Long getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }

    public void setBusinessRule(BusinessRule rule) {
        this.businessRule = rule;
    }

    public BusinessRule getBusinessRule() {
        return businessRule;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public Set<RuleValue> getRuleValues() {
        return ruleValues;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void removeRuleValue(RuleValue ruleValue) {
        ruleValues.remove(ruleValue);
        ruleValue.setRuleAttribute(null);
    }

    public void addRuleValue(RuleValue ruleValue) {
        ruleValues.add(ruleValue);
        ruleValue.setRuleAttribute(this);
    }

    public boolean equalByNameAndType(Object o) {
        if (this == o) return true;
        if (!(o instanceof RuleAttribute that)) return false;
        return attributeName.equals(that.attributeName) &&
                ruleType.equals(that.ruleType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RuleAttribute that)) return false;
        return attributeName.equals(that.attributeName) &&
                ruleType.equals(that.ruleType) &&
                ruleValues.equals(that.ruleValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributeName, ruleType, ruleValues);
    }

    @Override
    public String toString() {
        return "RuleAttribute{" +
                "id=" + id +
                ", attributeName='" + attributeName + '\'' +
                ", ruleType='" + ruleType + '\'' +
                '}';
    }
}
