package com.umar.apps;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import java.util.Properties;

@TestConfiguration
public class JPATestConfig {

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        var factory = new LocalContainerEntityManagerFactoryBean();
        factory.setPersistenceUnitName("testPU");
        factory.setPersistenceXmlLocation("classpath:META-INF/persistence.xml");
        //factory.setMappingResources("classpath:META-INF/jpa/stock-orm-inverse.xml");
        var properties = new Properties();
        properties.setProperty("hibernate.show_sql", "true");
        properties.setProperty("hibernate.jdbc.fetch_size", "100");
        //properties.setProperty("javax.persistence.schema-generation.database.action", "drop-and-create");
        factory.setJpaProperties(properties);
        return factory;
    }
}
