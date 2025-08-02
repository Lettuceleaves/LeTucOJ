package com.LetucOJ.gateway.tool;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    private static final String SECRET = "yoursecretkeyyoursecretkeyyoursecretkeyyoursecretkey";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    public static String generateToken(String username, String role, long expirationMillis) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMillis))
                .claim("role", role)
                .claim("username", username)
                .signWith(KEY)
                .compact();
    }

    public static Claims parseToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
