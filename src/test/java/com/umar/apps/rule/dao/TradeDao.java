package com.umar.apps.rule.dao;

import com.umar.apps.rule.infra.dao.api.core.GenericJpaDao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TradeDao extends GenericJpaDao<Trade, Long> {

    public TradeDao() {
        super(Trade.class, "test_rulesPU");
    }

    @Override
    public Collection<Trade> findAll() {
        Collection<Trade> trades = new ArrayList<>(Collections.emptyList());
        executeInTransaction(entityManager -> {
            List<?> result = entityManager.createQuery("SELECT t from Trade t").getResultList();
            result.forEach(row ->{
                trades.add((Trade) row);
            });
        });
        return trades;
    }
}
