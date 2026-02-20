package com.zgrylmz.springSecurity.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.zgrylmz.springSecurity.entity.Users;
import com.zgrylmz.springSecurity.repository.IUsersRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtTokenProvider {

	private Key keyForAccess;
	private Key keyForRefresh;

	private final long accessTokenValidity = 1000 * 60 * 15;
	private final long refreshTokenValidity = 1000 * 60 * 60 * 24;

	@Autowired
	private IUsersRepository userRepository;

	@Value("${jwt.secret-key-for-access}")
	private String secretKeyForAccess;

	@Value("${jwt.secret-key-for-refresh}")
	private String secretKeyForRefresh;

	@PostConstruct
	public void init() {
		if (secretKeyForAccess == null || secretKeyForAccess.isEmpty() || secretKeyForRefresh == null
				|| secretKeyForRefresh.isEmpty()) {
			throw new IllegalArgumentException("secret key is not set");
		}
		this.keyForAccess = Keys.hmacShaKeyFor(secretKeyForAccess.getBytes(StandardCharsets.UTF_8));
		this.keyForRefresh = Keys.hmacShaKeyFor(secretKeyForRefresh.getBytes(StandardCharsets.UTF_8));
	}

	public String generateAccessToken(String username, String role) {
		return Jwts.builder().setSubject(username).claim("role", role).setIssuedAt(new Date())
				.setId(UUID.randomUUID().toString())
				.setExpiration(new Date(System.currentTimeMillis() + accessTokenValidity)).signWith(keyForAccess)
				.compact();
	}

	public String generateRefreshToken(String username) {
		return Jwts.builder().setSubject(username).setIssuedAt(new Date()).setId(UUID.randomUUID().toString())
				.setExpiration(new Date(System.currentTimeMillis() + refreshTokenValidity)).signWith(keyForRefresh)
				.compact();
	}

	public String regenerateAccessToken(String refreshToken) {
		if (validateRefreshToken(refreshToken)) {
			String username = getUsernameFromRefreshToken(refreshToken);
			Optional<Users> user = userRepository.findByUsername(username);
			if (!user.isPresent()) {
				throw new RuntimeException("Uset doesnt exist");
			}
			String role = user.get().getRole();

			return generateAccessToken(username, role);
		}
		throw new RuntimeException("Invalid or expired refresh token");
	}

	public boolean validateAccessToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(keyForAccess).build().parseClaimsJws(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean validateRefreshToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(keyForRefresh).build().parseClaimsJws(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public String getUsernameFromRefreshToken(String token) {
		if (!validateRefreshToken(token)) {
			throw new RuntimeException("Please Login ");
		}
		return Jwts.parserBuilder().setSigningKey(keyForRefresh).build().parseClaimsJws(token).getBody().getSubject();
	}

	public String getUsernameFromAccessToken(String token) {
		return Jwts.parserBuilder().setSigningKey(keyForAccess).build().parseClaimsJws(token).getBody().getSubject();
	}

	public String getRoleFromAccessToken(String token) {
		return Jwts.parserBuilder().setSigningKey(keyForAccess).build().parseClaimsJws(token).getBody().get("role",
				String.class);
	}

}
