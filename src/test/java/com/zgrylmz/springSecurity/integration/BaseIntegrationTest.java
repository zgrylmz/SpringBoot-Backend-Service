package com.zgrylmz.springSecurity.integration;

import org.junit.jupiter.api.TestInstance;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseIntegrationTest {

	// MySQL Testcontainer
	protected static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0").withDatabaseName("testdb")
			.withUsername("test").withPassword("test").withReuse(true);;

	static {
		mysql.start();
	}

	@DynamicPropertySource
	static void registerProperties(DynamicPropertyRegistry registry) {
		// DB config
		registry.add("spring.datasource.url", mysql::getJdbcUrl);
		registry.add("spring.datasource.username", mysql::getUsername);
		registry.add("spring.datasource.password", mysql::getPassword);
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
		registry.add("spring.jpa.show-sql", () -> "true");

		// JWT secret key’leri (test için rastgele ama yeterince uzun key)
		registry.add("jwt.secret-key-for-access", () -> "this_is_a_very_long_secret_key_for_access_token_1234567890");
		registry.add("jwt.secret-key-for-refresh", () -> "this_is_a_very_long_secret_key_for_refresh_token_0987654321");
	}
}
