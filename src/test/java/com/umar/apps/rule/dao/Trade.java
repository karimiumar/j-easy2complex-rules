package com.umar.apps.rule.dao;

import com.umar.apps.rule.engine.WorkflowItem;

import javax.persistence.*;

@Entity
@Table(name = "trades")
public class Trade implements WorkflowItem<Long> {

    @Id
    @SequenceGenerator(name = "trades_seq", sequenceName = "TRADES_SEQ")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trades_seq")
    private Long id;
    @Column(name = "counter_party")
    private String counterParty;
    @Column(name = "currency")
    private String currency;
    @Column(name = "amount")
    private Double amount;
    @Column(name = "stp_allowed")
    private boolean stpAllowed;
    @Column(name = "comment")
    private String comment;

    @Override
    public Long getId() {
        return id;
    }

    public Double getAmount() {
        return amount;
    }

    public String getCounterParty() {
        return counterParty;
    }

    public String getCurrency() {
        return currency;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setCounterParty(String counterParty) {
        this.counterParty = counterParty;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isStpAllowed() {
        return stpAllowed;
    }

    public void setStpAllowed(boolean stpAllowed) {
        this.stpAllowed = stpAllowed;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "id=" + id +
                ", counterParty='" + counterParty + '\'' +
                ", currency='" + currency + '\'' +
                ", amount=" + amount +
                ", stpAllowed=" + stpAllowed +
                ", comment='" + comment + '\'' +
                '}';
    }
}
