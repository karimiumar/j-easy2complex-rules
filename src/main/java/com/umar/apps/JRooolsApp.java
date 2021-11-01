package com.umar.apps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource("classpath:beans.xml")
public class JRooolsApp {

	public static void main(String[] args) {
		SpringApplication.run(JRooolsApp.class, args);
	}
}

