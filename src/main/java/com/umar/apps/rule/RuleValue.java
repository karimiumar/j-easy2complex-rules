package com.umar.apps.rule;

import com.umar.apps.rule.engine.WorkflowItem;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "[VALUES]")
public class RuleValue implements WorkflowItem<Long>, Serializable {
    public static final String RULE_VALUE$ID = "ruleval.id";
    public static final String RULE_VALUE$ALIAS = "RuleValue ruleval";
    public static final String RULE_VALUE = "ruleval";
    public static final String RULE_VALUE$OPERAND ="ruleval.operand";
    public static final String RULE_VALUE$ATTRIB = "ruleval.ruleAttribute";


    private Long id;
    private String operand;
    private int version;
    private RuleAttribute ruleAttribute;
    //private Set<RuleAttributeValue> ruleAttributeValues = new HashSet<>(0);

    /*
    */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    @Column(name="operand", unique = true)
    public String getOperand() {
        return operand;
    }

    @Column(name = "version")
    public int getVersion() {
        return version;
    }

    @ManyToOne()
    //@JoinColumn(name = "rule_id")
    @JoinTable(
            name = "attribute_values",
            joinColumns = @JoinColumn(name = "value_id"),
            inverseJoinColumns = @JoinColumn(name = "attribute_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"value_id","attribute_id"})
    )
    public RuleAttribute getRuleAttribute() {
        return ruleAttribute;
    }

    public void setRuleAttribute(RuleAttribute ruleAttribute) {
        this.ruleAttribute = ruleAttribute;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setOperand(String operand) {
        this.operand = operand;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RuleValue value)) return false;
        return Objects.equals(operand, value.operand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operand);
    }

    @Override
    public String toString() {
        return "RuleValue{" +
                "id=" + id +
                ", operand='" + operand + '\'' +
                '}';
    }
}
