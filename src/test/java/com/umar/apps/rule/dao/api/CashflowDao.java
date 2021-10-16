package com.umar.apps.rule.dao.api;

import com.umar.apps.infra.dao.api.core.GenericJpaDao;
import org.hibernate.Session;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.umar.apps.jpa.txn.AbstractTxExecutor.doInJPA;

public class CashflowDao extends GenericJpaDao<Cashflow, Long> {

    protected CashflowDao() {
        this(null);
    }

    public CashflowDao(EntityManagerFactory emf) {
        super(Cashflow.class, emf);
    }

    @Override
    public Collection<Cashflow> findAll() {
        Collection<Cashflow> cashflows = new ArrayList<>(0);
        doInJPA(()-> emf, entityManager -> {
            List<?> result = entityManager.createQuery("SELECT c FROM Cashflow c").getResultList();
            result.forEach(row -> cashflows.add((Cashflow) row));
        }, null);
        return cashflows;
    }

    public List<Cashflow> findByCounterParty(String counterParty) {
        ArrayList<Cashflow> cashflows = new ArrayList<>(0);
        String sql = """
                SELECT c FROM Cashflow c
                WHERE c.counterParty = :counterParty
                """;
        doInJPA(()-> emf, entityManager -> {
            Session session = entityManager.unwrap(Session.class);
            List<Cashflow> result = session.createQuery(sql, Cashflow.class).setParameter("counterParty", counterParty).getResultList();
            cashflows.addAll(result);
        }, null);
        return cashflows ;
    }

    public List<Cashflow> findByCounterPartyAndSettlementDate(String counterParty, LocalDate settlementDate) {
        ArrayList<Cashflow> cashflows = new ArrayList<>(0);
        String sql = """
                SELECT c FROM Cashflow c
                WHERE c.settlementDate = :settlementDate
                AND c.counterParty = :counterParty
                """;
        doInJPA(()-> emf, entityManager -> {
            Session session = entityManager.unwrap(Session.class);
            List<Cashflow> result = session.createQuery(sql, Cashflow.class)
                    .setParameter("counterParty", counterParty)
                    .setParameter("settlementDate", settlementDate)
                    .getResultList();
            cashflows.addAll(result);
        }, null);
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
        doInJPA(()-> emf, entityManager -> {
            Session session = entityManager.unwrap(Session.class);
            List<Cashflow> result = session.createQuery(sql, Cashflow.class)
                    .setParameter("counterParty", counterParty )
                    .setParameter("currency", currency)
                    .setParameter("settlementDate", settlementDate)
                    .getResultList();
            cashflows.addAll(result);
        }, null);
        return cashflows;
    }

    public void delete() {
        doInJPA(() -> emf, entityManager -> {
            entityManager.createQuery("DELETE FROM Cashflow c").executeUpdate();
        }, null);
    }

    public void applySTPRule(Cashflow workflowItem, String note) {
        doInJPA(()-> emf, entityManager -> {
            Session session = entityManager.unwrap(Session.class);
            if(!entityManager.contains(workflowItem)) {
                entityManager.find(Cashflow.class, workflowItem.getId());
                workflowItem.setStpAllowed(false);
                workflowItem.setNote(note);
                workflowItem.setVersion(workflowItem.getVersion() + 1);
                session.merge(workflowItem);
            }
        }, null);
    }

    public Collection<Cashflow> findBySettlementDateBetween(LocalDate startDate, LocalDate endDate) {
        ArrayList<Cashflow> cashflows = new ArrayList<>(0);
        String sql = """
                SELECT c FROM Cashflow c
                WHERE c.settlementDate 
                BETWEEN :startDate AND :endDate
                """;
        doInJPA(()-> emf, entityManager -> {
            Session session = entityManager.unwrap(Session.class);
            List<Cashflow> result = session.createQuery(sql, Cashflow.class)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .getResultList();
            cashflows.addAll(result);
        }, null);
        return cashflows;
    }

    public Collection<Cashflow> findBySettlementDate(LocalDate settlementDate) {
        ArrayList<Cashflow> cashflows = new ArrayList<>(0);
        String sql = """
                SELECT c FROM Cashflow c
                WHERE c.settlementDate = :settlementDate
                """;
        doInJPA(()-> emf, entityManager -> {
            Session session = entityManager.unwrap(Session.class);
            List<Cashflow> result = session.createQuery(sql, Cashflow.class)
                    .setParameter("settlementDate", settlementDate)
                    .getResultList();
            cashflows.addAll(result);
        }, null);
        return cashflows;
    }
}
