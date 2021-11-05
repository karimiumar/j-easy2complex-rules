package com.umar.apps;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(H2JpaConfig.class)
@TestPropertySource(locations = "classpath:application.properties")
class JRooolsTests {

	@Test
	void contextLoads() {
	}
}
