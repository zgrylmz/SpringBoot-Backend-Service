package com.zgrylmz.springSecurity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@EntityScan(basePackages = { "com.zgrylmz.springSecurity" })
@ComponentScan(basePackages = { "com.zgrylmz.springSecurity" })
@EnableCaching
@ConfigurationPropertiesScan(basePackages = { "com.zgrylmz.springSecurity" })
public class SpringSecurityApplication {

	public static void main(String[] args) {

		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

		setIfPresent("DB_USERNAME", System.getenv("DB_USERNAME"), dotenv.get("DB_USERNAME"));
		setIfPresent("DB_PASSWORD", System.getenv("DB_PASSWORD"), dotenv.get("DB_PASSWORD"));
		setIfPresent("jwt.secret-key-for-access", System.getenv("JWT_SECRET_KEY_FOR_ACCESS_TOKEN"),
				dotenv.get("JWT_SECRET_KEY_FOR_ACCESS_TOKEN"));
		setIfPresent("jwt.secret-key-for-refresh", System.getenv("JWT_SECRET_KEY_FOR_REFRESH_TOKEN"),
				dotenv.get("JWT_SECRET_KEY_FOR_REFRESH_TOKEN"));

		SpringApplication.run(SpringSecurityApplication.class, args);
	}

	// Bu metod class içinde olmalı ve static olmalı
	private static void setIfPresent(String propKey, String envValue, String dotenvValue) {
		String value = firstNonBlank(envValue, dotenvValue);
		if (value != null) {
			System.setProperty(propKey, value);
		}
	}

	// Bu metod da class içinde olmalı ve static olmalı
	private static String firstNonBlank(String primary, String fallback) {
		if (primary != null && !primary.isBlank())
			return primary;
		if (fallback != null && !fallback.isBlank())
			return fallback;
		return null;
	}
}
