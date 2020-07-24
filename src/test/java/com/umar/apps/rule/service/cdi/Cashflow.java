package com.umar.apps.rule.service.cdi;

import com.umar.apps.rule.engine.WorkflowItem;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Consumer;

@Entity
@Table(name = "cashflows")
public class Cashflow implements WorkflowItem<Long> {

    public static final String CASHFLOW = "cf";
    public static final String CASHFLOW_ALIAS = "Cashflow cf";
    public static final String CASHFLOW_CPTY = "cf.counterParty";
    public static final String CASHFLOW_CURR = "cf.currency";
    public static final String CASHFLOW_AMT = "cf.amount";
    public static final String CASHFLOW_SETT_DATE = "cf.settlementDate";
    public static final String CASHFLOW_STP = "cf.stpAllowed";
    public static final String CASHFLOW_NOTE = "cf.note";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "counter_party")
    private String counterParty;

    @Column
    private String currency;

    @Column(name = "settlement_date")
    private LocalDate settlementDate;

    @Column
    private Double amount;

    @Column(name = "version", columnDefinition ="int default 0")
    private Integer version;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "stp_allowed", columnDefinition ="boolean default true")
    private boolean stpAllowed;

    @Column(name = "notes")
    private String note;

    public Cashflow(){}

    private Cashflow(CashflowBuilder cashflowBuilder) {
        this.counterParty = cashflowBuilder.counterParty;
        this.amount = cashflowBuilder.amount;
        this.currency = cashflowBuilder.currency;
        this.createdOn = cashflowBuilder.createdOn;
        this.stpAllowed = cashflowBuilder.stpAllowed;
        this.settlementDate = cashflowBuilder.settlementDate;
        this.version = cashflowBuilder.version;
        this.id = cashflowBuilder.id;
    }

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

    public void setStpAllowed(boolean stpAllowed) {
        this.stpAllowed = stpAllowed;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getNote() {
        return note;
    }

    public static class CashflowBuilder {

        public CashflowBuilder with(Consumer<CashflowBuilder> consumer) {
            consumer.accept(this);
            return this;
        }

        public Cashflow build() {
            return new Cashflow(this);
        }
        public Long id;
        public String counterParty;
        public String currency;
        public LocalDate settlementDate;
        public boolean stpAllowed;
        public Double amount;
        public int version;
        public LocalDateTime createdOn = LocalDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
