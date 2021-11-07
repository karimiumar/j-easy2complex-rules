package com.umar.apps.rule.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.umar.apps.infra.dao.api.WorkflowItem;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalIdCache;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity(name = "RuleValue")
@Table(name = "[values]")
@NaturalIdCache
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class RuleValue implements WorkflowItem<Long>, Serializable {

    private Long id;
    private String operand;
    private LocalDateTime created;
    private LocalDateTime updated;
    private int version;
    private RuleAttribute ruleAttribute;

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
    @Version
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(
            name = "attribute_value",
            joinColumns = @JoinColumn(name = "attribute_id"),
            inverseJoinColumns = @JoinColumn(name = "value_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"attribute_id","value_id"})
    )
    @JsonBackReference
    public RuleAttribute getRuleAttribute() {
        return ruleAttribute;
    }

    public void setRuleAttribute(RuleAttribute ruleAttribute) {
        Objects.requireNonNull(ruleAttribute,"RuleAttribute is null");
        this.ruleAttribute = ruleAttribute;
    }

    @Column(name = "created", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    public LocalDateTime getCreated() {
        return created;
    }

    @Column(name = "updated", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", insertable = false, updatable = false)
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
