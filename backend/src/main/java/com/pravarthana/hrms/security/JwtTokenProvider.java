package com.pravarthana.hrms.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate JWT token embedding email, role, userId, and companyId as claims.
     * The companyId enables multi-tenant scoping on every request.
     */
    public String generateToken(String email, String role, Long userId, Long companyId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .claim("userId", userId)
                .claim("companyId", companyId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Legacy overload without companyId for backward compat (defaults to 1L).
     */
    public String generateToken(String email, String role, Long userId) {
        return generateToken(email, role, userId, 1L);
    }

    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public String getRoleFromToken(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public Long getUserIdFromToken(String token) {
        return extractLong(parseClaims(token), "userId");
    }

    public Long getCompanyIdFromToken(String token) {
        Object raw = parseClaims(token).get("companyId");
        if (raw == null) return 1L; // default for tokens issued before this upgrade
        return extractLongFromObject(raw);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Long extractLong(Claims claims, String key) {
        return extractLongFromObject(claims.get(key));
    }

    private Long extractLongFromObject(Object raw) {
        if (raw instanceof Long l)    return l;
        if (raw instanceof Integer i) return i.longValue();
        if (raw instanceof Number n)  return n.longValue();
        throw new JwtException("Claim missing or invalid type in token");
    }
}

