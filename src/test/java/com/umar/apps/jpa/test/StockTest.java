package com.umar.apps.jpa.test;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.umar.apps.infra.dao.api.core.AbstractTxExecutor.doInJPA;
import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StockTest {

    private static final Map<String, String> persistenceConfig = null; /*Map.of("spring.datasource.driver-class-name","com.mysql.cj.jdbc.Driver"
            ,"spring.datasource.url","jdbc:mysql://localhost:3306/rulesdb"
            ,"hibernate.dialect","org.hibernate.dialect.MySQL8Dialect"
            , "javax.persistence.schema-generation.database.action","drop-and-create"
            ,"spring.datasource.username","rulesman"
            ,"spring.datasource.password","rules"
    );*/

    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("stockPU", persistenceConfig);

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
        }, persistenceConfig);

        doInJPA(() -> emf, entityManager -> {
            var stock = new Stock("INFY","Infosys");
            var sdr = new StockDailyRecord(BigDecimal.valueOf(1242.45), BigDecimal.valueOf(3242.45), BigDecimal.valueOf(2000.00), 10000, LocalDateTime.now(), stock);
            stock.addStockDailyRecord(sdr);
            entityManager.persist(stock);
            entityManager.persist(sdr);
        }, persistenceConfig);

        doInJPA(() -> emf, entityManager -> {
            var stock = new Stock("IBM","IBM");
            var sdr = new StockDailyRecord(BigDecimal.valueOf(1342.45), BigDecimal.valueOf(2242.45), BigDecimal.valueOf(900.00), 150000, LocalDateTime.now(), stock);
            stock.addStockDailyRecord(sdr);
            entityManager.persist(stock);
            entityManager.persist(sdr);
        }, persistenceConfig);


        var stocks = fetchStocks();
        assertThat(stocks.size()).isEqualTo(3);
        stocks.forEach(System.out::println);
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

    @Order(value = 2)
    @Test
    void testFetchStockDailyRecord() {
        var stockDailyRecord = fetchStockDailyRecord();
        assertThat(stockDailyRecord).isNotNull();
        assertThat(stockDailyRecord.getStock()).isNotNull();
        assertThat(stockDailyRecord.getStock().getId()).isEqualTo(2L);
        var stockId = stockDailyRecord.getStock().getId();
        var stock  = stockDailyRecord.getStock();
        assertThat(stock.getStockCode()).isEqualTo("INFY");
    }

    private StockDailyRecord fetchStockDailyRecord() {
        return doInJPA(() -> emf, entityManager -> {
            var sdr =  entityManager.find(StockDailyRecord.class, 2L
                    , Collections.singletonMap(
                    "javax.persistence.fetchgraph"
                    , entityManager.getEntityGraph("graph.Stock.stockDailyRecords")
            ));
            var stock = (Stock) Hibernate.unproxy(sdr.getStock());
            sdr.setStock(stock);
            return sdr;
        }, persistenceConfig);
    }

    private List<Stock> fetchStocks() {
        return doInJPA(()->emf, entityManager -> {
            //Query to Fetch associated StockDailyRecords
            var query = entityManager.createQuery("""
                    select s from Stock s LEFT JOIN FETCH s.stockDailyRecords
                    """, Stock.class);
            return query.getResultList();
        }, persistenceConfig);
    }
}
