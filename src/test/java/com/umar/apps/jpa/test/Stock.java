package com.umar.apps.jpa.test;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(exclude = {"stockDailyRecords"})
@ToString()
@NamedEntityGraph(name = "graph.Stock.stockDailyRecords",
        attributeNodes = @NamedAttributeNode(value = "stockDailyRecords", subgraph = "stockDailyRecords")
        , subgraphs = @NamedSubgraph(name = "stockDailyRecords", attributeNodes = @NamedAttributeNode("stock"))
)
@Entity(name = "Stock")
@Table(name = "stocks")
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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "stock_code", nullable = false)
    private String stockCode;
    @Column(name = "stock_name", nullable = false)
    private String stockName;
    @Column(name = "created_on", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime created;
    @OneToMany(mappedBy = "stock", fetch = FetchType.LAZY)
    private Set<StockDailyRecord> stockDailyRecords = new HashSet<>();
}
