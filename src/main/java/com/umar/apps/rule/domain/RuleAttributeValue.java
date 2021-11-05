package com.umar.apps.rule.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity(name = "RuleAttributeValue")
@Table(name = "attribute_values")
public class RuleAttributeValue implements Serializable {

    @EmbeddedId
    private RuleAttributeValueId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("attributeId")
    @JsonBackReference
    private RuleAttribute ruleAttribute;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("valueId")
    @JsonBackReference
    private RuleValue ruleValue;

    @Column(name = "created" , columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime created;

    protected RuleAttributeValue(){}

    public RuleAttributeValue(RuleAttribute ruleAttribute, RuleValue ruleValue) {
        this.ruleAttribute = ruleAttribute;
        this.ruleValue = ruleValue;
        this.id = new RuleAttributeValueId(ruleAttribute.getId(), ruleValue.getId());
    }

    public RuleAttributeValueId getId() {
        return id;
    }

    public void setId(RuleAttributeValueId id) {
        this.id = id;
    }

    public RuleAttribute getRuleAttribute() {
        return ruleAttribute;
    }

    public void setRuleAttribute(RuleAttribute ruleAttribute) {
        this.ruleAttribute = ruleAttribute;
    }

    public RuleValue getRuleValue() {
        return ruleValue;
    }

    public void setRuleValue(RuleValue ruleValue) {
        this.ruleValue = ruleValue;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
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
                ", ruleValue=" + ruleValue +
                ", created=" + created +
                '}';
    }

    @Embeddable
    public static class RuleAttributeValueId implements Serializable {
        @Column(name = "attribute_id")
        private Long attributeId;

        @Column(name = "value_id")
        private Long valueId;

        RuleAttributeValueId() {}

        public RuleAttributeValueId(Long attributeId, Long valueId) {
            this.attributeId = attributeId;
            this.valueId = valueId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RuleAttributeValueId that = (RuleAttributeValueId) o;
            return Objects.equals(attributeId, that.attributeId) && Objects.equals(valueId, that.valueId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(attributeId, valueId);
        }

        public Long getAttributeId() {
            return attributeId;
        }

        public void setAttributeId(Long attributeId) {
            this.attributeId = attributeId;
        }

        public Long getValueId() {
            return valueId;
        }

        public void setValueId(Long valueId) {
            this.valueId = valueId;
        }
    }
}
