package com.umar.apps.jpa.test;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Setter
@Getter
@EqualsAndHashCode
@ToString(exclude = "stock")
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

    private Long id;
    private BigDecimal openingPrice;
    private BigDecimal closingPrice;
    private BigDecimal priceChange;
    private int volume;
    private LocalDateTime date;
    private Stock stock;
}
