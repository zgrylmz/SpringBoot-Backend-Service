package com.zgrylmz.springSecurity.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String header = request.getHeader("Authorization");// postmandeki header'dan Authorization'i cek (Bearer token)

		if (header != null && header.startsWith("Bearer ")) {
			String token = header.substring(7); // "Bearer " kısmını çıkar
			if (jwtTokenProvider.validateAccessToken(token)) {
				String username = jwtTokenProvider.getUsernameFromAccessToken(token);// Token providerdan username'i al
				String role = jwtTokenProvider.getRoleFromAccessToken(token);// Token providerdan role'u al

				UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null,
						Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));// username ve role'u
																								// SecurityContext'de
																								// kaydet

				SecurityContextHolder.getContext().setAuthentication(auth);
			} else {
				// 🔴 throw YOK
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setContentType("application/json");
				response.getWriter().write("""
						    {
						      "error": "Please login"
						    }
						""");
			}

		}

		filterChain.doFilter(request, response);
	}
}
