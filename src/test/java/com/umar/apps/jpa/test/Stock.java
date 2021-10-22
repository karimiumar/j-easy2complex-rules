package com.umar.apps.jpa.test;

import lombok.*;

import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedSubgraph;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(exclude = {"stockDailyRecords"})
@ToString(exclude = "stockDailyRecords")
@NamedEntityGraph(name = "graph.Stock.stockDailyRecords",
        attributeNodes = @NamedAttributeNode(value = "stockDailyRecords", subgraph = "stockDailyRecords")
        , subgraphs = @NamedSubgraph(name = "stockDailyRecords", attributeNodes = @NamedAttributeNode("stock"))
)
public class Stock {

    protected Stock() {}

    public Stock(String stockCode, String stockName) {
        this.stockCode = stockCode;
        this.stockName = stockName;
    }

    public void addStockDailyRecord(StockDailyRecord stockDailyRecord) {
        stockDailyRecords.add(stockDailyRecord);
        stockDailyRecord.setStock(this);
    }

    public void removeStockDailyRecord(StockDailyRecord stockDailyRecord) {
        stockDailyRecords.remove(stockDailyRecord);
        stockDailyRecord.setStock(null);
    }

    private Long id;
    private String stockCode;
    private String stockName;
    private Set<StockDailyRecord> stockDailyRecords = new HashSet<>();
}
