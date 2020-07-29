package com.umar.apps.rule;

import com.umar.apps.rule.engine.WorkflowItem;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "attributes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"attribute_name", "rule_type"})
})
public class RuleAttribute implements WorkflowItem<Long>, Serializable {

    public static final String ATTRIB$ID = "attr.id";
    public static final String ATTRIB$ALIAS = "RuleAttribute attr";
    public static final String ATTRIB$ATTRIB = "attr";
    public static final String ATTRIB$ATTRIB_NAME ="attr.attributeName";
    public static final String ATTRIB$RULE_TYPE ="attr.ruleType";
    public static final String ATTRIB$RULE = "attr.businessRule";
    public static final String ATTRIB$DISPLAY_NAME = "attr.displayName";

    public RuleAttribute(){}

    private Long id;
    private String attributeName;
    private String ruleType;
    private int version;
    private String displayName;
    private BusinessRule businessRule;
    private Set<RuleValue> ruleValues = new HashSet<>();
    //private Set<RuleAttributeValue> ruleAttributeValues = new HashSet<>(0);
    //private Set<BusinessRuleAttribute> businessRuleAttributes = new HashSet<>(0);

    public RuleAttribute(Long id, String attributeName, String ruleType, String displayName) {
        this.id = id;
        this.attributeName = attributeName;
        this.ruleType = ruleType;
        this.displayName = displayName;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    @OneToMany(mappedBy = "ruleAttribute",cascade = {CascadeType.PERSIST, CascadeType.MERGE},fetch= FetchType.EAGER, orphanRemoval = true)
    public Set<RuleValue> getRuleValues() {
        return ruleValues;
    }

    @ManyToOne
    @JoinTable(
            name = "rule_attribute",
            joinColumns = @JoinColumn(name = "attribute_id"),
            inverseJoinColumns = @JoinColumn(name = "rule_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"attribute_id","rule_id"})
    )
    public BusinessRule getBusinessRule() {
        return businessRule;
    }

    @Column(name = "attribute_name")
    public String getAttributeName() {
        return attributeName;
    }

    @Column(name = "rule_type")
    public String getRuleType() {
        return ruleType;
    }

    @Column(name = "version")
    public int getVersion() {
        return version;
    }

    @Column(name = "display_name")
    public String getDisplayName() {
        return displayName;
    }

    public void addRuleValue(RuleValue ruleValue) {
        ruleValues.add(ruleValue);
        ruleValue.setRuleAttribute(this);
    }

    public void setBusinessRule(BusinessRule businessRule) {
        this.businessRule = businessRule;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setRuleValues(Set<RuleValue> ruleValues) {
        this.ruleValues = ruleValues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RuleAttribute that)) return false;
        return attributeName.equals(that.attributeName) &&
                ruleType.equals(that.ruleType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributeName, ruleType);
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
