package com.cooknect.user_service.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JWTService {
    private static final String SECRET = Base64.getEncoder()
            .encodeToString("your-256-bit-secret-key-for-jwt-validation".getBytes(StandardCharsets.UTF_8));

    private SecretKey getSignKey() {
        byte[] keyBytes = Base64.getDecoder().decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String email,String role,String username) {

        Map<String,Object> claims = new HashMap<>();
        claims.put("role",role);
        claims.put("username",username);

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 2 * 60 * 60 * 1000)) // 2 hours expiration
                .and()
                .signWith(getSignKey())
                .compact();
    }
}
