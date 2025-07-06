package com.LetucOJ.auth.tool;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;

@Configuration
public class jwt {
    private static final String SECRET_KEY = "yoursecretkeyyoursecretkeyyoursecretkeyyoursecretkeyyoursecretkeyyoursecretkeyyoursecretkey";

    public String generateToken(String username, String role) {
        byte[] secretKeyBytes = Base64.getDecoder().decode(SECRET_KEY);
        SecretKey secretKey = new SecretKeySpec(secretKeyBytes, SignatureAlgorithm.HS256.getJcaName());

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10小时有效期
                .claim("role", role) // 存储用户角色
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
}
