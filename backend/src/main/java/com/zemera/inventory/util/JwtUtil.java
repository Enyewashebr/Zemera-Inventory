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
    // Use Base64-encoded secret to match Vert.x JWK configuration
    // private static final String SECRET = Base64.getEncoder()
    //         .encodeToString("my_super_secret_key_123456".getBytes());
     String jwtSecret = env("JWT_SECRET");
String encodedSecret = Base64.getEncoder()
    .encodeToString(jwtSecret.getBytes());

    public String generateToken(Integer userId, String username, String role, String name, Integer branchId) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role)
                .claim("name", name)
                .claim("branchId", branchId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }
}
