package com.zgrylmz.springSecurity.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityFilter {
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
		http.csrf(csrf -> csrf.disable())// rest api testleri icin bu disabled olmali
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/v1/register", "/api/v1/login", "/api/v1/getUserWithId/**",
								"/api/v1/newToken", "/api/v1/deleteUserWithId/**", "/api/v1/getAllusersForRedis",
								"/api/v1/getOneUserForRedis/{id}", "/api/v1/home", "/health", "/actuator/health",
								"/api/v1/getAllUsers")
						.permitAll()

						.requestMatchers("/api/v1/getUsers", "/api/v1/updateInfos").authenticated()

						.anyRequest().authenticated() // geri kalan her şey authentication ister
				).addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);// JwtAuthenticationFilter'da
		// tanimladigimiz jwt
		// tabanli kontrol, kisi
		// login mi degil mi yetkisi
		// var mi yok mu ona göre
		// erisim izni verilir
		return http.build();
	}

	// Şifreleri güvenli bir şekilde saklamak için kullanılır
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public RoleHierarchy roleHierarchy() {
		return RoleHierarchyImpl.fromHierarchy("ROLE_ADMIN > ROLE_USER");
	}
}
