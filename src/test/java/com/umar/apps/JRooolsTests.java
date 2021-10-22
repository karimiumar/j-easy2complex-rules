package com.umar.apps;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@Import(JPATestConfig.class)
@TestPropertySource(locations = "classpath:application.properties")
class JRooolsTests {

	@Test
	void contextLoads() {
	}

}
