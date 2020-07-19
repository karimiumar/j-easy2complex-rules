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
    public static final String RULE_VALUE$RULE = "ruleval.businessRule";

    @Id
    @SequenceGenerator(name = "rule_val_seq", sequenceName = "RULE_VAL_SEQ")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rule_val_seq")
    private Long id;

    @Column(name="operand", unique = true)
    private String operand;

    @ManyToOne()
    //@JoinColumn(name = "rule_id")
    @JoinTable(
            name = "rule_operands",
            joinColumns = @JoinColumn(name = "value_id"),
            inverseJoinColumns = @JoinColumn(name = "rule_id")
    )
    private BusinessRule businessRule;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getOperand() {
        return operand;
    }

    public void setOperand(String operand) {
        this.operand = operand;
    }

    public BusinessRule getBusinessRule() {
        return businessRule;
    }

    public void setBusinessRule(BusinessRule businessRule) {
        this.businessRule = businessRule;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RuleValue value)) return false;
        return Objects.equals(id, value.id) &&
                Objects.equals(operand, value.operand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, operand);
    }

    @Override
    public String toString() {
        return "RuleValue{" +
                "id=" + id +
                ", operand='" + operand + '\'' +
                '}';
    }
}
