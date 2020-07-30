package com.umar.apps.rule;

import com.umar.apps.rule.engine.WorkflowItem;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalIdCache;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Entity(name = "RuleValue")
@Table(name = "[VALUES]")
@NaturalIdCache
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class RuleValue implements WorkflowItem<Long>, Serializable {
    public static final String RULE_VALUE$ID = "ruleval.id";
    public static final String RULE_VALUE$ALIAS = "RuleValue ruleval";
    public static final String RULE_VALUE = "ruleval";
    public static final String RULE_VALUE$OPERAND ="ruleval.operand";
    public static final String RULE_VALUE$ATTRIB = "ruleval.ruleAttribute";


    private Long id;
    private String operand;
    private int version;
    //private RuleAttribute ruleAttribute;
    private List<RuleAttributeValue> ruleAttributes = new ArrayList<>();

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

    @OneToMany(mappedBy = "ruleValue", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    public List<RuleAttributeValue> getRuleAttributes() {
        return ruleAttributes;
    }

    public void addRuleAttribute(RuleAttribute ruleAttribute) {
        RuleAttributeValue ruleAttributeValue = new RuleAttributeValue(ruleAttribute, this);
        ruleAttributes.add(ruleAttributeValue);
        ruleAttribute.getRuleAttributeValues().add(ruleAttributeValue);
    }

    public void removeRuleAttribute(RuleAttribute ruleAttribute) {
        for (Iterator<RuleAttributeValue> iterator = ruleAttributes.iterator(); iterator.hasNext();){
            RuleAttributeValue ruleAttributeValue = iterator.next();
            if(ruleAttributeValue.getRuleValue().equals(this)
                    && ruleAttributeValue.getRuleAttribute().equals(ruleAttribute)) {
                iterator.remove();
                ruleAttributeValue.getRuleAttribute().getRuleAttributeValues().remove(ruleAttributeValue);
                ruleAttributeValue.setRuleAttribute(null);
                ruleAttributeValue.setRuleValue(null);
            }
        }
    }

    /*@ManyToOne()
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
    }*/

    public void setRuleAttributes(List<RuleAttributeValue> ruleAttributes) {
        this.ruleAttributes = ruleAttributes;
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
