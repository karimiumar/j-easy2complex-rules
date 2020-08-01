package com.umar.apps.rule;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class RuleAttributeValueId implements Serializable {

    private Long attributeId;
    private Long valueId;


    private RuleAttributeValueId(){}

    public RuleAttributeValueId(Long attributeId, Long valueId) {
        this.attributeId = attributeId;
        this.valueId = valueId;
    }

    @Column(name = "attribute_id")
    public Long getAttributeId() {
        return attributeId;
    }

    @Column(name = "value_id")
    public Long getValueId() {
        return valueId;
    }

    public void setAttributeId(Long attributeId) {
        this.attributeId = attributeId;
    }

    public void setValueId(Long valueId) {
        this.valueId = valueId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RuleAttributeValueId that)) return false;
        return Objects.equals(attributeId, that.attributeId) &&
                Objects.equals(valueId, that.valueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributeId, valueId);
    }
}
