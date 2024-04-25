package com.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@Configuration
@ComponentScan(basePackages = { "com.auth0.jwt.exceptions.JWTDecodeException" })
public class WebSecurityConfig {

	private JWTRequestFilter jwtRequestFilter;

	public WebSecurityConfig(JWTRequestFilter jwtRequestFilter) {
		this.jwtRequestFilter = jwtRequestFilter;
	}

//	@Bean
//	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//		http.csrf(csrf -> csrf.disable());
//		// add jwtRequestFilter before calling the AuthorizationFilter.class
//		http.addFilterBefore(jwtRequestFilter, AuthorizationFilter.class);
//		
//		http.authorizeRequests().requestMatchers("**").permitAll()
////        http.authorizeHttpRequests().requestMatchers("/product", "/image/**", "/auth/register", "/auth/login", "/auth/verify","/auth/forgot","/auth/reset", "/error", "/websocket", "/websocket/**").permitAll()
//				.anyRequest().authenticated();
//
//		return http.build();
//	}
	
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable());
		http.addFilterBefore(jwtRequestFilter, AuthorizationFilter.class);
		
		
		http.authorizeHttpRequests(auth -> auth
	            .requestMatchers("**").permitAll()
	            .anyRequest().authenticated()
	        );

		return http.build();
	}
}
