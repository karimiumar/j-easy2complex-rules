package com.umar.apps.rule.service.cdi;

import com.umar.apps.rule.infra.dao.api.core.GenericJpaDao;
import org.hibernate.Session;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CashflowDao extends GenericJpaDao<Cashflow, Long> {

    protected CashflowDao() {
        this(null);
    }

    public CashflowDao(String persistenceUnit) {
        super(Cashflow.class, persistenceUnit);
    }

    @Override
    public Collection<Cashflow> findAll() {
        Collection<Cashflow> cashflows = new ArrayList<>(0);
        executeInTransaction(entityManager -> {
            List<?> result = entityManager.createQuery("SELECT c FROM Cashflow c").getResultList();
            result.forEach(row -> cashflows.add((Cashflow) row));
        });
        return cashflows;
    }

    public List<Cashflow> findByCounterParty(String counterParty) {
        ArrayList<Cashflow> cashflows = new ArrayList<>(0);
        String sql = """
                SELECT c FROM Cashflow c
                WHERE c.counterParty = :counterParty
                """;
        executeInTransaction(entityManager -> {
            Session session = entityManager.unwrap(Session.class);
            List<Cashflow> result = session.createQuery(sql, Cashflow.class).setParameter("counterParty", counterParty).getResultList();
            cashflows.addAll(result);
        });
        return cashflows ;
    }

    public List<Cashflow> findByCounterPartyAndSettlementDate(String counterParty, LocalDate settlementDate) {
        ArrayList<Cashflow> cashflows = new ArrayList<>(0);
        String sql = """
                SELECT c FROM Cashflow c
                WHERE c.settlementDate = :settlementDate
                AND c.counterParty = :counterParty
                """;
        executeInTransaction(entityManager -> {
            Session session = entityManager.unwrap(Session.class);
            List<Cashflow> result = session.createQuery(sql, Cashflow.class)
                    .setParameter("counterParty", counterParty)
                    .setParameter("settlementDate", settlementDate)
                    .getResultList();
            cashflows.addAll(result);
        });
        return cashflows;
    }

    public List<Cashflow> findByCounterPartyCurrencyAndSettlementDate(String counterParty,String currency, LocalDate settlementDate) {
        ArrayList<Cashflow> cashflows = new ArrayList<>(0);
        String sql = """
                SELECT c FROM Cashflow c
                WHERE c.settlementDate = :settlementDate
                AND c.counterParty = :counterParty
                AND c.currency = :currency
                """;
        executeInTransaction(entityManager -> {
            Session session = entityManager.unwrap(Session.class);
            List<Cashflow> result = session.createQuery(sql, Cashflow.class)
                    .setParameter("counterParty", counterParty )
                    .setParameter("currency", currency)
                    .setParameter("settlementDate", settlementDate)
                    .getResultList();
            cashflows.addAll(result);
        });
        return cashflows;
    }

    public void delete() {
        executeInTransaction(entityManager -> entityManager.createQuery("DELETE FROM Cashflow c").executeUpdate());
    }

    public void applySTPRule(Cashflow workflowItem, String note) {
        executeInTransaction(entityManager -> {
            Session session = entityManager.unwrap(Session.class);
            if(!entityManager.contains(workflowItem)) {
                entityManager.find(Cashflow.class, workflowItem.getId());
                workflowItem.setStpAllowed(false);
                workflowItem.setNote(note);
                workflowItem.setVersion(workflowItem.getVersion() + 1);
                session.merge(workflowItem);
            }
        });
    }

    public Collection<Cashflow> findBySettlementDateBetween(LocalDate startDate, LocalDate endDate) {
        ArrayList<Cashflow> cashflows = new ArrayList<>(0);
        String sql = """
                SELECT c FROM Cashflow c
                WHERE c.settlementDate 
                BETWEEN :startDate AND :endDate
                """;
        executeInTransaction(entityManager -> {
            Session session = entityManager.unwrap(Session.class);
            List<Cashflow> result = session.createQuery(sql, Cashflow.class)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .getResultList();
            cashflows.addAll(result);
        });
        return cashflows;
    }

    public Collection<Cashflow> findBySettlementDate(LocalDate settlementDate) {
        ArrayList<Cashflow> cashflows = new ArrayList<>(0);
        String sql = """
                SELECT c FROM Cashflow c
                WHERE c.settlementDate = :settlementDate
                """;
        executeInTransaction(entityManager -> {
            Session session = entityManager.unwrap(Session.class);
            List<Cashflow> result = session.createQuery(sql, Cashflow.class)
                    .setParameter("settlementDate", settlementDate)
                    .getResultList();
            cashflows.addAll(result);
        });
        return cashflows;
    }
}
