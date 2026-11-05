package com.nextstep.nextsiheung.global.config;

import com.nextstep.nextsiheung.domain.user.service.UserVisitService;
import com.nextstep.nextsiheung.global.security.JwtAuthenticationFilter;
import com.nextstep.nextsiheung.global.security.JwtProvider;
import com.nextstep.nextsiheung.global.security.SecurityErrorHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtProvider jwtProvider,
                                           UserVisitService userVisitService, JsonMapper jsonMapper) throws Exception {
        SecurityErrorHandler errorHandler = new SecurityErrorHandler(jsonMapper);
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .formLogin(f -> f.disable())
            .httpBasic(b -> b.disable())
            // 로그인 안 한 요청은 403이 아니라 401(UNAUTHORIZED·TOKEN_EXPIRED), 권한 없음은 403(FORBIDDEN). 둘 다 명세 실패 형식
            .exceptionHandling(e -> e.authenticationEntryPoint(errorHandler).accessDeniedHandler(errorHandler))
            .addFilterBefore(new JwtAuthenticationFilter(jwtProvider, userVisitService),
                    UsernamePasswordAuthenticationFilter.class)
            // 명세 권한: - 와 U?(로그인 선택)는 permitAll, U는 authenticated, A는 ADMIN
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/health", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/signup", "/api/v1/auth/login", "/api/v1/auth/refresh").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/policies/**", "/api/v1/posts/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/jobs/recommended", "/api/v1/jobs/bookmarks").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/jobs/*").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated());
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(allowedOrigins);
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("*"));
        c.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", c);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
