package com.umar.apps;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalEntityManagerFactoryBean;

/*
if you want to keep using persistence.xml file just add the below code in your configuration class
 */
@Configuration
class JPAConfig {
    @Bean
    public LocalEntityManagerFactoryBean entityManagerFactory() {
        var localEMF = new LocalEntityManagerFactoryBean();
        localEMF.setPersistenceUnitName("testPU");
        return localEMF;
    }
}
