package com.umar.apps.rule;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity(name = "RuleAttributeValue")
@Table(name = "attribute_values")
public class RuleAttributeValue {

    @EmbeddedId
    private RuleAttributeValueId id;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("attributeId")
    private RuleAttribute ruleAttribute;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("valueId")
    private RuleValue ruleValue;

    @Column(name = "created_on")
    private LocalDateTime created = LocalDateTime.now();

    protected RuleAttributeValue(){}

    public RuleAttributeValue(RuleAttribute ruleAttribute, RuleValue ruleValue) {
        this.ruleAttribute = ruleAttribute;
        this.ruleValue = ruleValue;
        this.id = new RuleAttributeValueId(ruleAttribute.getId(), ruleValue.getId());
    }

    public RuleAttribute getRuleAttribute() {
        return ruleAttribute;
    }

    public RuleValue getRuleValue() {
        return ruleValue;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public RuleAttributeValueId getId() {
        return id;
    }

    public void setRuleAttribute(RuleAttribute ruleAttribute) {
        this.ruleAttribute = ruleAttribute;
    }

    public void setRuleValue(RuleValue ruleValue) {
        this.ruleValue = ruleValue;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public void setId(RuleAttributeValueId id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RuleAttributeValue that)) return false;
        return Objects.equals(ruleAttribute, that.ruleAttribute) &&
                Objects.equals(ruleValue, that.ruleValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleAttribute, ruleValue);
    }

    @Override
    public String toString() {
        return "RuleAttributeValue{" +
                "id=" + id +
                ", ruleAttribute=" + ruleAttribute.getAttributeName() +
                ", ruleValue=" + ruleValue.getOperand() +
                ", created=" + created +
                '}';
    }
}
