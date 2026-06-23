package com.tse.erp.security.filter;

import com.tse.erp.security.jwt.JwtUtil;
import com.tse.erp.module.admin.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // ── Authorization header check ──
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            // ── Token validate ──
            if (!jwtUtil.validateToken(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            // ── Access token kina check ──
            if (!jwtUtil.isAccessToken(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            String username = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);

            // ── Already authenticated? ──
            if (username != null &&
                    SecurityContextHolder.getContext()
                            .getAuthentication() == null) {

                // User still active kina check
                boolean userActive = userRepository
                        .findByUserName(username)
                        .map(u -> u.getIsActive() == 1)
                        .orElse(false);

                if (userActive) {
                    // Role authority set koro
                    List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + role));

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    username, null, authorities);

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request));

                    SecurityContextHolder.getContext()
                            .setAuthentication(authToken);
                }
            }

        } catch (ExpiredJwtException e) {
            // Token expire — 401 return koro
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"status\":401," +
                            "\"error\":\"Unauthorized\"," +
                            "\"message\":\"Token has expired\"," +
                            "\"path\":\"" + request.getRequestURI() + "\"}");
            return;
        } catch (Exception e) {
            // Invalid token
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"status\":401," +
                            "\"error\":\"Unauthorized\"," +
                            "\"message\":\"Invalid token\"," +
                            "\"path\":\"" + request.getRequestURI() + "\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}