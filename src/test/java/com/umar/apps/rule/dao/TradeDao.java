package com.umar.apps.rule.dao;

import com.umar.apps.rule.infra.dao.api.core.GenericJpaDao;
import com.umar.simply.jdbc.dml.operations.SelectOp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.umar.apps.rule.dao.Trade.TRADE;
import static com.umar.apps.rule.dao.Trade.TRADE_ALIAS;

public class TradeDao extends GenericJpaDao<Trade, Long> {

    public TradeDao() {
        super(Trade.class, "test_rulesPU");
    }

    @Override
    public Collection<Trade> findAll() {
        Collection<Trade> trades = new ArrayList<>(Collections.emptyList());
        String sql = SelectOp.create().SELECT().COLUMN(TRADE).FROM(TRADE_ALIAS).getSQL();
        executeInTransaction(entityManager -> {
            List<?> result = entityManager.createQuery(sql).getResultList();
            result.forEach(row ->{
                trades.add((Trade) row);
            });
        });
        return trades;
    }
}
