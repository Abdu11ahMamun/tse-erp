package com.tse.erp.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiry}")
    private long accessTokenExpiry;

    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    // =========================================
    // SECRET KEY
    // =========================================
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // =========================================
    // GENERATE ACCESS TOKEN
    // =========================================

    // Purono signature, onno kono jaygay use hole break hobe na
    public String generateAccessToken(String username, Long userId, String role) {
        return generateAccessToken(username, userId, role, null, null, null);
    }

    public String generateAccessToken(String username, Long userId, String role,
                                      Long userEmpId, Long bgId, Long buId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        claims.put("type", "access");
        // null value claim-e dewa hoy na
        if (userEmpId != null) {
            claims.put("userEmpId", userEmpId);
        }
        if (bgId != null) {
            claims.put("bgId", bgId);
        }
        if (buId != null) {
            claims.put("buId", buId);
        }

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuer("tse-erp")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiry))
                .signWith(getSigningKey())
                .compact();
    }

    // =========================================
    // GENERATE REFRESH TOKEN
    // =========================================
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuer("tse-erp")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiry))
                .signWith(getSigningKey())
                .compact();
    }

    // =========================================
    // VALIDATE TOKEN
    // =========================================
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new ExpiredJwtException(
                    e.getHeader(), e.getClaims(),
                    "Token has expired");
        } catch (JwtException e) {
            return false;
        }
    }

    // =========================================
    // EXTRACT CLAIMS
    // =========================================
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Number claim safely Long-e (JSON-e chhoto number Integer hoye ashe)
    private Long extractLongClaim(String token, String name) {
        Object v = extractAllClaims(token).get(name);
        return v instanceof Number n ? n.longValue() : null;
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        return extractLongClaim(token, "userId");
    }

    public Long extractUserEmpId(String token) {
        return extractLongClaim(token, "userEmpId");
    }

    public Long extractBgId(String token) {
        return extractLongClaim(token, "bgId");
    }

    public Long extractBuId(String token) {
        return extractLongClaim(token, "buId");
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public String extractTokenType(String token) {
        return extractAllClaims(token).get("type", String.class);
    }

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());
    }

    public boolean isAccessToken(String token) {
        return "access".equals(extractTokenType(token));
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(extractTokenType(token));
    }
}