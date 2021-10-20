package com.umar.apps;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.persistence.EntityManagerFactory;
import java.util.Properties;

/*
if you want to keep using persistence.xml file just add the below code in your configuration class
 */
//@Configuration
class JPAConfig {

    //@Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        var factory = new LocalContainerEntityManagerFactoryBean();
        factory.setPersistenceUnitName("rulesPU");
        factory.setPersistenceXmlLocation("classpath:META-INF/persistence.xml");
        //factory.setMappingResources("classpath:META-INF/jpa/stock-orm-inverse.xml");
        var properties = new Properties();
        properties.setProperty("hibernate.show_sql", "true");
        properties.setProperty("hibernate.jdbc.fetch_size", "100");
        properties.setProperty("javax.persistence.schema-generation.database.action", "drop-and-create");
        factory.setJpaProperties(properties);
        return factory;
    }

    //@Bean
    JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }
}
