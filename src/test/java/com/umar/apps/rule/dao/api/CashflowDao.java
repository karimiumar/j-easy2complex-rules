package com.umar.apps.rule.dao.api;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CashflowDao {

    public CashflowDao() {

    }
    static final Set<Cashflow> cashflowSet = new HashSet<>();
    private static volatile long id = 0;

    public Collection<Cashflow> findAll() {
        return new ArrayList<>(cashflowSet);
    }

    public List<Cashflow> findByCounterParty(String counterParty) {
        return cashflowSet.stream().filter(cashflow -> cashflow.getCounterParty().equals(counterParty)).collect(Collectors.toList());
    }

    public List<Cashflow> findByCounterPartyAndSettlementDate(String counterParty, LocalDate settlementDate) {
        return cashflowSet.stream()
                .filter(cashflow -> cashflow.getCounterParty().equals(counterParty))
                .filter(cashflow -> cashflow.getSettlementDate().equals(settlementDate))
                .collect(Collectors.toList());
    }

    public List<Cashflow> findByCounterPartyCurrencyAndSettlementDate(String counterParty,String currency, LocalDate settlementDate) {
        return findByCounterPartyAndSettlementDate(counterParty, settlementDate)
                .stream().filter(cashflow -> cashflow.getCurrency().equals(currency))
                .collect(Collectors.toList());
    }

    public void delete() {
        cashflowSet.clear();
    }

    public void applySTPRule(Cashflow workflowItem, String note) {
        var optCF = cashflowSet.stream().filter(cashflow -> cashflow.equals(workflowItem)).findFirst();
        optCF.ifPresentOrElse(cf -> {
           cf.setNote(note);
           cf.setStpAllowed(false);
           cf.setVersion(cf.getVersion() + 1);
           cashflowSet.remove(workflowItem);
           cashflowSet.add(cf);
        }, () -> System.out.println("Not Found"));
    }

    public Collection<Cashflow> findBySettlementDateBetween(LocalDate startDate, LocalDate endDate) {
        return cashflowSet.stream()
                .filter(cashflow -> cashflow.getSettlementDate().isAfter(startDate.plusDays(-1)))
                .filter(cashflow -> cashflow.getSettlementDate().isBefore(endDate.plusDays(1)))
                .collect(Collectors.toList());
    }

    public Collection<Cashflow> findBySettlementDate(LocalDate settlementDate) {
        return cashflowSet.stream()
                .filter(cashflow -> cashflow.getSettlementDate().equals(settlementDate)).collect(Collectors.toList());
    }

    public void save(Cashflow cashflow) {
        synchronized(Cashflow.class) {
            cashflow.setId(++id);
            cashflowSet.add(cashflow);
        }
    }
}
