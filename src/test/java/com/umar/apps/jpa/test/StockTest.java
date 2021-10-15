package com.umar.apps.jpa.test;

import org.junit.jupiter.api.*;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.umar.apps.jpa.txn.AbstractTxExecutor.doInJPA;
import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StockTest {

    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("testPU");

    @AfterAll
    static void afterAll() {
        if(null != emf && emf.isOpen()) {
            emf.close();
        }
    }

    @Test
    @Order(value = 1)
    void createSomeStocks() {
        doInJPA(() -> emf, entityManager -> {
            var stock = new Stock("SAP","Sapient");
            var sdr = new StockDailyRecord(BigDecimal.valueOf(342.45), BigDecimal.valueOf(242.45), BigDecimal.valueOf(-100.00), 120000, LocalDateTime.now(), stock);
            stock.addStockDailyRecord(sdr);
            entityManager.persist(stock);
            entityManager.persist(sdr);
        }, null);

        doInJPA(() -> emf, entityManager -> {
            var stock = new Stock("INFY","Infosys");
            var sdr = new StockDailyRecord(BigDecimal.valueOf(1242.45), BigDecimal.valueOf(3242.45), BigDecimal.valueOf(2000.00), 10000, LocalDateTime.now(), stock);
            stock.addStockDailyRecord(sdr);
            entityManager.persist(stock);
            entityManager.persist(sdr);
        }, null);

        doInJPA(() -> emf, entityManager -> {
            var stock = new Stock("IBM","IBM");
            var sdr = new StockDailyRecord(BigDecimal.valueOf(1342.45), BigDecimal.valueOf(2242.45), BigDecimal.valueOf(900.00), 150000, LocalDateTime.now(), stock);
            stock.addStockDailyRecord(sdr);
            entityManager.persist(stock);
            entityManager.persist(sdr);
        }, null);


        var stocks = fetchStocks();
        assertThat(stocks.size()).isEqualTo(3);
    }

    @Order(value = 2)
    @Test
    void testFetchStocks() {
        var stocks = fetchStocks();
        assertThat(stocks.size()).isEqualTo(3);
        stocks.sort(Comparator.comparing(Stock::getStockCode));
        assertThat(stocks.get(0).getStockCode()).isEqualTo("IBM");
        assertThat(stocks.get(1).getStockCode()).isEqualTo("INFY");
        assertThat(stocks.get(2).getStockCode()).isEqualTo("SAP");
    }


    private List<Stock> fetchStocks() {
        return doInJPA(()->emf, entityManager -> {
            var query = entityManager.createQuery("""
                    select s from Stock s
                    """);
            return query.getResultList();
        }, null);
    }
}
