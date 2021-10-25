package com.umar.apps.rule.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.umar.apps.infra.dao.api.WorkflowItem;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity(name = "RuleAttribute")
@Table(name = "attributes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"attribute_name", "rule_type"})
})
public class RuleAttribute implements WorkflowItem<Long>, Serializable {

    public RuleAttribute(){}

    private Long id;
    private String attributeName;
    private String ruleType;
    private int version;
    private String displayName;
    private BusinessRule businessRule;
    private LocalDateTime created = LocalDateTime.now();
    private LocalDateTime updated;
    private List<RuleAttributeValue> ruleAttributeValues = new ArrayList<>();

    public RuleAttribute(Long id, String attributeName, String ruleType, String displayName) {
        this.id = id;
        this.attributeName = attributeName;
        this.ruleType = ruleType;
        this.displayName = displayName;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "attribute_name", length = 30)
    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    @Column(name = "rule_type", length = 30)
    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    @Column(name = "version")
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Column(name = "display_name", length = 60)
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(
            name = "rule_attribute",
            joinColumns = @JoinColumn(name = "attribute_id"),
            inverseJoinColumns = @JoinColumn(name = "rule_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"attribute_id","rule_id"})
    )
    @JsonBackReference
    public BusinessRule getBusinessRule() {
        return businessRule;
    }

    public void setBusinessRule(BusinessRule businessRule) {
        this.businessRule = businessRule;
    }

    @OneToMany(mappedBy = "ruleAttribute",cascade = {CascadeType.PERSIST,CascadeType.REMOVE} , orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    public List<RuleAttributeValue> getRuleAttributeValues() {
        return ruleAttributeValues;
    }

    public void setRuleAttributeValues(List<RuleAttributeValue> ruleAttributeValues) {
        this.ruleAttributeValues = ruleAttributeValues;
    }

    @Column(name = "created")
    public LocalDateTime getCreated() {
        return created;
    }

    @Column(name = "updated")
    public LocalDateTime getUpdated() {
        return updated;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuleAttribute that = (RuleAttribute) o;
        return Objects.equals(attributeName, that.attributeName) && Objects.equals(ruleType, that.ruleType);
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
                ", version=" + version +
                ", displayName='" + displayName + '\'' +
                ", ruleAttributeValues=" + ruleAttributeValues +
                '}';
    }
}
