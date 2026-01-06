package com.zemera.inventory.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Base64;
import java.util.Date;

public class JwtUtil {

    // Use Base64-encoded secret to match Vert.x JWK configuration
    private static final String SECRET = Base64.getEncoder()
            .encodeToString("my_super_secret_key_123456".getBytes());

    public String generateToken(Integer userId, String username, String role, String name, Integer branchId) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role)
                .claim("name", name)
                .claim("branchId", branchId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }
}
