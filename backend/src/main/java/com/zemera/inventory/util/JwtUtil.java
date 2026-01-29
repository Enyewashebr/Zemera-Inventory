package com.zemera.inventory.util;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Base64;
import java.util.Date;

public class JwtUtil {
    private String env(String key) {
        String value = System.getenv(key);
        if (value == null) value = Dotenv.load().get(key);
        return value;
    }

    private final String jwtSecret = env("JWT_SECRET"); // raw secret

    public String generateToken(Integer userId, String username, String role, String name, Integer branchId) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role)
                .claim("name", name)
                .claim("branchId", branchId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
                .signWith(SignatureAlgorithm.HS256, jwtSecret.getBytes()) // ✅ use raw bytes
                .compact();
    }
}
