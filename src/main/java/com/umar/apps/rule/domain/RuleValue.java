package com.umar.apps.rule.domain;

import com.umar.apps.infra.dao.api.WorkflowItem;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalIdCache;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity(name = "RuleValue")
@Table(name = "[VALUES]")
@NaturalIdCache
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class RuleValue implements WorkflowItem<Long>, Serializable {

    private Long id;

    private String operand;

    private int version;

    private Set<RuleAttributeValue> ruleAttributeValues = new HashSet<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name="operand", unique = true, length = 150)
    public String getOperand() {
        return operand;
    }

    public void setOperand(String operand) {
        this.operand = operand;
    }

    @Column(name = "version")
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @OneToMany(mappedBy = "ruleValue", cascade = {CascadeType.PERSIST,CascadeType.REMOVE}, orphanRemoval = true, fetch = FetchType.EAGER)
    public Set<RuleAttributeValue> getRuleAttributeValues() {
        return ruleAttributeValues;
    }

    public void setRuleAttributeValues(Set<RuleAttributeValue> ruleAttributeValues) {
        this.ruleAttributeValues = ruleAttributeValues;
    }

    public void addRuleAttribute(RuleAttribute ruleAttribute) {
        RuleAttributeValue ruleAttributeValue = new RuleAttributeValue(ruleAttribute, this);
        ruleAttributeValues.add(ruleAttributeValue);
        ruleAttribute.getRuleAttributeValues().add(ruleAttributeValue);
    }

    public void removeRuleAttribute(RuleAttribute ruleAttribute) {
        for(Iterator<RuleAttributeValue> iterator = ruleAttributeValues.iterator(); iterator.hasNext();) {
            RuleAttributeValue ruleAttributeValue = iterator.next();
            if(ruleAttributeValue.getRuleValue().equals(this) && ruleAttributeValue.getRuleAttribute().equals(ruleAttribute)) {
                iterator.remove();
                ruleAttributeValue.getRuleAttribute().getRuleAttributeValues().remove(ruleAttributeValue);
                ruleAttributeValue.setRuleValue(null);
                ruleAttributeValue.setRuleAttribute(null);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var ruleValue = (RuleValue) o;
        return Objects.equals(operand, ruleValue.operand);
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
                ", version=" + version +
                '}';
    }
}
