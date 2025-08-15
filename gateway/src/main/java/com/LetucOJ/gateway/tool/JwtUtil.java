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

    public static String generateToken(String username, String cnname, String role, long millis) {
        return Jwts.builder()
                .issuer("LetucOJ")
                .subject(username)
                .expiration(new Date(System.currentTimeMillis() + millis))
                .claim("cnname", cnname)
                .claim("role", role)
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
