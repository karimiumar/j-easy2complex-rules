package com.umar.apps.rule.service.cdi;

import com.umar.apps.rule.engine.WorkflowItem;
import com.umar.apps.rule.infra.dao.api.core.DeleteFunction;
import com.umar.apps.rule.infra.dao.api.core.GenericJpaDao;
import com.umar.apps.rule.infra.dao.api.core.SelectFunction;
import org.hibernate.Session;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.umar.apps.rule.service.cdi.Cashflow.*;

public class CashflowDao extends GenericJpaDao<Cashflow, Long> {

    @Inject private final SelectFunction selectFunction;
    @Inject private final DeleteFunction deleteFunction;

    protected CashflowDao() {
        this(null, null, null);
    }

    public CashflowDao(String persistenceUnit, SelectFunction selectFunction, DeleteFunction deleteFunction) {
        super(Cashflow.class, persistenceUnit);
        this.selectFunction = selectFunction;
        this.deleteFunction = deleteFunction;
    }

    @Override
    public Collection<Cashflow> findAll() {
        Collection<Cashflow> cashflows = new ArrayList<>(0);
        String sql = selectFunction.select()
                .SELECT().COLUMN(CASHFLOW).FROM(CASHFLOW_ALIAS).getSQL();
        executeInTransaction(entityManager -> {
            List<?> result = entityManager.createQuery(sql).getResultList();
            result.forEach(row -> cashflows.add((Cashflow) row));
        });
        return cashflows;
    }

    public List<Cashflow> findByCounterParty(String counterParty) {
        ArrayList<Cashflow> cashflows = new ArrayList<>(0);
        String sql = selectFunction.select()
                .SELECT().COLUMN(CASHFLOW).FROM(CASHFLOW_ALIAS)
                .WHERE().COLUMN(CASHFLOW_CPTY).EQ(":counterParty").getSQL();
        executeInTransaction(entityManager -> {
            Session session = entityManager.unwrap(Session.class);
            List<Cashflow> result = session.createQuery(sql, Cashflow.class).setParameter("counterParty", counterParty).getResultList();
            cashflows.addAll(result);
        });
        return cashflows ;
    }

    public List<Cashflow> findByCounterPartyAndSettlementDate(String counterParty, LocalDate settlementDate) {
        ArrayList<Cashflow> cashflows = new ArrayList<>(0);
        String sql = selectFunction.select()
                .SELECT().COLUMN(CASHFLOW).FROM(CASHFLOW_ALIAS)
                .WHERE().COLUMN(CASHFLOW_CPTY).EQ(":counterParty")
                .AND().COLUMN(CASHFLOW_SETT_DATE).EQ(":settlementDate")
                .getSQL();
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
        String sql = selectFunction.select()
                .SELECT().COLUMN(CASHFLOW).FROM(CASHFLOW_ALIAS)
                .WHERE().COLUMN(CASHFLOW_CPTY).EQ(":counterParty")
                .AND().COLUMN(CASHFLOW_CURR).EQ(":currency")
                .AND().COLUMN(CASHFLOW_SETT_DATE).EQ(":settlementDate")
                .getSQL();
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
        String sql = deleteFunction.delete().DELETE_FROM(CASHFLOW_ALIAS).getSQL();
        executeInTransaction(entityManager -> entityManager.createQuery(sql).executeUpdate());
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

    public Collection<Cashflow> findBySettlementDate(LocalDate settlementDate) {
        ArrayList<Cashflow> cashflows = new ArrayList<>(0);
        String sql = selectFunction.select()
                .SELECT().COLUMN(CASHFLOW).FROM(CASHFLOW_ALIAS)
                .WHERE().COLUMN(CASHFLOW_SETT_DATE).EQ(":settlementDate")
                .getSQL();
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
