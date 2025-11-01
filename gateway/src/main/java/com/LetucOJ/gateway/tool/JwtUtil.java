package com.LetucOJ.gateway.tool;

import com.LetucOJ.gateway.result.Result;
import com.LetucOJ.gateway.result.errorcode.ErrorCode;
import com.LetucOJ.gateway.result.errorcode.GatewayErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    private static final String SECRET = "praise_the_sun_&_long_may_the_sunshine";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String generateToken(String username, String cnname, String role) {
        return Jwts.builder()
                .issuer("LetucOJ")
                .subject(username)
                .expiration((new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)))
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

    public static byte[] createErrorResponseBody(GatewayErrorCode errorCode) {
        try {
            return objectMapper.writeValueAsBytes(Result.failure(errorCode));
        } catch (JsonProcessingException e) {
            System.err.println("Error serializing error response: " + e.getMessage());
            return ("{\"code\":" + GatewayErrorCode.SERVICE_ERROR.code() +
                    ",\"message\":\"Internal gateway error during response creation\"}").getBytes(StandardCharsets.UTF_8);
        }
    }

    public static Mono<Void> writeErrorResponse(ServerWebExchange exchange, ErrorCode errorCode) {
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(Result.failure(errorCode));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        exchange.getResponse().getHeaders().remove(HttpHeaders.CONTENT_LENGTH);

        String origin = exchange.getRequest().getHeaders().getFirst(HttpHeaders.ORIGIN);
        if (origin != null) {
            exchange.getResponse().getHeaders().set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
            exchange.getResponse().getHeaders().set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        }

        DataBuffer buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(bytes);

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}