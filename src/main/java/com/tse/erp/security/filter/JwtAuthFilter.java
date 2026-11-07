package com.tse.erp.security.filter;

import com.tse.erp.module.admin.entity.User;
import com.tse.erp.module.admin.entity.UserEmplMap;
import com.tse.erp.module.admin.repository.UserEmplMapRepository;
import com.tse.erp.module.admin.repository.UserRepository;
import com.tse.erp.security.AuthUser;
import com.tse.erp.security.jwt.JwtUtil;
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
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final UserEmplMapRepository userEmplMapRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            if (!jwtUtil.validateToken(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            if (!jwtUtil.isAccessToken(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            String username = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);

            if (username != null &&
                    SecurityContextHolder.getContext()
                            .getAuthentication() == null) {

                Optional<User> userOpt =
                        userRepository.findByUserName(username);

                if (userOpt.isPresent()
                        && Integer.valueOf(1).equals(
                        userOpt.get().getStatus())) {

                    User u = userOpt.get();

                    // Password bodlanor age issue kora token reject
                    if (u.getPasswordChangedAt() != null) {
                        Date iat = jwtUtil.extractIssuedAt(token);
                        if (iat != null && iat.toInstant().isBefore(
                                u.getPasswordChangedAt()
                                        .truncatedTo(ChronoUnit.SECONDS)
                                        .atZone(ZoneId.systemDefault())
                                        .toInstant())) {
                            writeUnauthorized(response, request,
                                    "Token is no longer valid. Please log in again.");
                            return;
                        }
                    }

                    Long empId = userEmplMapRepository
                            .findByUserIdOrderByIdAsc(u.getId())
                            .stream()
                            .filter(m -> Integer.valueOf(1)
                                    .equals(m.getStatus()))
                            .map(UserEmplMap::getEmplId)
                            .findFirst()
                            .orElse(null);

                    AuthUser principal = new AuthUser(
                            u.getId(), username, empId,
                            u.getBgId(), u.getBuId());

                    List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + role));

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    principal, null, authorities);

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request));

                    SecurityContextHolder.getContext()
                            .setAuthentication(authToken);
                }
            }

        } catch (ExpiredJwtException e) {
            writeUnauthorized(response, request, "Token has expired");
            return;
        } catch (Exception e) {
            writeUnauthorized(response, request, "Invalid token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response,
                                   HttpServletRequest request,
                                   String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"status\":401," +
                        "\"error\":\"Unauthorized\"," +
                        "\"message\":\"" + message + "\"," +
                        "\"path\":\"" + request.getRequestURI() + "\"}");
    }
}