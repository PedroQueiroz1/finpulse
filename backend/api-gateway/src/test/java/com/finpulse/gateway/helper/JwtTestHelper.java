package com.finpulse.gateway.helper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtTestHelper {

    // Must match jwt.secret in application-test.yml
    public static final String TEST_SECRET =
            "test-secret-key-for-gateway-testing-needs-at-least-256-bits-of-total-length";

    public static String generateToken(String email) {
        return generateToken(email, "test-user-id-123", "USER");
    }

    public static String generateToken(String email, String userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        claims.put("name", "Test User");

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 900_000))
                .signWith(getSigningKey())
                .compact();
    }

    public static String generateExpiredToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", "test-user-id-123");
        claims.put("role", "USER");

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis() - 10_000))
                .expiration(new Date(System.currentTimeMillis() - 5_000))
                .signWith(getSigningKey())
                .compact();
    }

    private static SecretKey getSigningKey() {
        byte[] keyBytes = TEST_SECRET.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
