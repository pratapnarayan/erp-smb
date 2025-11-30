package com.erp.smb.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

public class JwtUtils {
    private final Key key;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;

    public JwtUtils(String secret, long accessTtlSeconds, long refreshTtlSeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
    }

    public String generateAccessToken(String subject, Map<String, Object> claims) {
        return buildToken(subject, claims, accessTtlSeconds);
    }

    public String generateRefreshToken(String subject, Map<String, Object> claims) {
        return buildToken(subject, claims, refreshTtlSeconds);
    }

    private String buildToken(String subject, Map<String, Object> claims, long ttlSeconds) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(subject)
                .addClaims(claims)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }

    public boolean validate(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
