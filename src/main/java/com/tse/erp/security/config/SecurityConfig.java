package com.tse.erp.security.config;

import com.tse.erp.security.filter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                // ✅ CORS config add koro
                .cors(cors -> cors.configurationSource(
                        request -> {
                            var config = new org.springframework.web.cors
                                    .CorsConfiguration();
                            config.setAllowedOrigins(
                                      java.util.List.of(
                                                        "http://localhost:5173",        // local dev
                                                        "http://160.25.226.138"        // production UI (jodi same server)
                                                      //  "http://YOUR_FRONTEND_DOMAIN"   // frontend er actual domain
                                                ));
                            config.setAllowedMethods(
                                    java.util.List.of(
                                            "GET","POST","PUT",
                                            "DELETE","OPTIONS"));
                            config.setAllowedHeaders(
                                    java.util.List.of("*"));
                            config.setAllowCredentials(true);
                            return config;
                        }))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**")
                        .permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(
                                (request, response, authException) -> {
                                    response.setStatus(401);
                                    response.setContentType("application/json");
                                    response.getWriter().write(
                                            "{\"status\":401," +
                                                    "\"error\":\"Unauthorized\"," +
                                                    "\"message\":\"Authentication required\"," +
                                                    "\"path\":\"" +
                                                    request.getRequestURI() + "\"}");
                                })
                        .accessDeniedHandler(
                                (request, response, accessDeniedException) -> {
                                    response.setStatus(403);
                                    response.setContentType("application/json");
                                    response.getWriter().write(
                                            "{\"status\":403," +
                                                    "\"error\":\"Forbidden\"," +
                                                    "\"message\":\"Access denied\"," +
                                                    "\"path\":\"" +
                                                    request.getRequestURI() + "\"}");
                                }));

        return http.build();
    }
}