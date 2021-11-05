package com.umar.apps.jpa.test;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Setter
@Getter
@EqualsAndHashCode
@ToString(exclude = "stock")
@Entity(name = "StockDailyRecord")
@Table(name = "stock_daily_records")
public class StockDailyRecord {

    protected StockDailyRecord() {

    }

    public StockDailyRecord(BigDecimal openingPrice, BigDecimal closingPrice, BigDecimal priceChange, int volume, LocalDateTime date, Stock stock) {
        this.openingPrice = openingPrice;
        this.closingPrice = closingPrice;
        this.priceChange = priceChange;
        this.volume = volume;
        this.date = date;
        this.stock = stock;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "opening_price", nullable = false)
    private BigDecimal openingPrice;
    @Column(name = "closing_price", nullable = false)
    private BigDecimal closingPrice;
    @Column(name = "price_change", nullable = false)
    private BigDecimal priceChange;
    @Column(name = "volume", nullable = false)
    private int volume;
    @Column(name = "[date]", nullable = false)
    private LocalDateTime date;
    @ManyToOne(targetEntity = Stock.class)
    @JoinColumn(name = "stock_id")
    private Stock stock;
}
