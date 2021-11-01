package com.umar.apps.rule.dao.api;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Cashflow{

    private Long id;

    private String counterParty;

    private String currency;

    private LocalDate settlementDate;

    private Double amount;

    private Integer version;

    private LocalDateTime createdOn;

    private boolean stpAllowed = true;

    private String note;

    public Cashflow(){}

    public String getCounterParty() {
        return counterParty;
    }

    public void setCounterParty(String counterParty) {
        this.counterParty = counterParty;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDate getSettlementDate() {
        return settlementDate;
    }

    public void setSettlementDate(LocalDate settlementDate) {
        this.settlementDate = settlementDate;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public boolean isStpAllowed() {
        return stpAllowed;
    }

    public boolean isNotStpAllowed() {
        return !stpAllowed;
    }

    public void setStpAllowed(boolean stpAllowed) {
        this.stpAllowed = stpAllowed;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getNote() {
        return note;
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cashflow cashflow)) return false;
        return  id.equals(cashflow.id) &&
                counterParty.equals(cashflow.counterParty) &&
                currency.equals(cashflow.currency) &&
                settlementDate.equals(cashflow.settlementDate) &&
                amount.equals(cashflow.amount) &&
                createdOn.equals(cashflow.createdOn);
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, counterParty, currency, settlementDate, amount, createdOn);
    }

    @Override
    public String toString() {
        return "Cashflow{" +
                "id=" + id +
                ", counterParty='" + counterParty + '\'' +
                ", currency='" + currency + '\'' +
                ", settlementDate=" + settlementDate +
                ", amount=" + amount +
                ", version=" + version +
                ", createdOn=" + createdOn +
                ", stpAllowed=" + stpAllowed +
                ", note='" + note + '\'' +
                '}';
    }
}
