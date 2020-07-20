package com.umar.apps.rule;

import com.umar.apps.rule.engine.WorkflowItem;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "attributes"
        , uniqueConstraints = {
        @UniqueConstraint(columnNames = {"attribute_name", "type", "rule_type"})
}
)
public class RuleAttribute implements WorkflowItem<Long>, Serializable {

    public static final String ATTRIB$ID = "attr.id";
    public static final String ATTRIB$ALIAS = "RuleAttribute attr";
    public static final String ATTRIB$ATTRIB = "attr";
    public static final String ATTRIB$ATTRIB_NAME ="attr.attributeName";
    public static final String ATTRIB$ATTRIB_TYPE ="attr.attributeType";
    public static final String ATTRIB$RULE_TYPE ="attr.ruleType";


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attribute_name")
    private String attributeName;

    @Column(name = "type")
    private String attributeType;

    @Column(name = "rule_type")
    private String ruleType;

    @Column(name = "version")
    private int version;

    @ManyToOne
    @JoinTable(
            name = "rule_attribute",
            joinColumns = @JoinColumn(name = "attribute_id"),
            inverseJoinColumns = @JoinColumn(name = "rule_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"attribute_id","rule_id"})
    )
    private BusinessRule businessRule;

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

    public String getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(String attributeType) {
        this.attributeType = attributeType;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RuleAttribute that)) return false;
        return Objects.equals(id, that.id) &&
                Objects.equals(attributeName, that.attributeName) &&
                Objects.equals(attributeType, that.attributeType) &&
                Objects.equals(ruleType, that.ruleType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, attributeName, attributeType, ruleType);
    }

    @Override
    public String toString() {
        return "RuleAttribute{" +
                "id=" + id +
                ", attributeName='" + attributeName + '\'' +
                ", attributeType='" + attributeType + '\'' +
                ", ruleType='" + ruleType + '\'' +
                '}';
    }
}
