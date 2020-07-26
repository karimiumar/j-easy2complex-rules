package com.umar.apps.rule;

import com.umar.apps.rule.engine.WorkflowItem;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "operands")
public class RuleValue implements WorkflowItem<Long>, Serializable {
    public static final String RULE_VALUE$ID = "ruleval.id";
    public static final String RULE_VALUE$ALIAS = "RuleValue ruleval";
    public static final String RULE_VALUE = "ruleval";
    public static final String RULE_VALUE$OPERAND ="ruleval.operand";
    public static final String RULE_VALUE$ATTRIB = "ruleval.ruleAttribute";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="operand", unique = true)
    private String operand;

    @Column(name = "version")
    private int version;

    @ManyToOne()
    //@JoinColumn(name = "rule_id")
    @JoinTable(
            name = "attribute_operands",
            joinColumns = @JoinColumn(name = "value_id"),
            inverseJoinColumns = @JoinColumn(name = "attribute_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"value_id","attribute_id"})
    )
    private RuleAttribute ruleAttribute;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getOperand() {
        return operand;
    }

    public int getVersion() {
        return version;
    }

    public void setOperand(String operand) {
        this.operand = operand;
    }

    public void setRuleAttribute(RuleAttribute ruleAttribute) {
        this.ruleAttribute = ruleAttribute;
    }

    public RuleAttribute getRuleAttribute() {
        return ruleAttribute;
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
