package com.LetucOJ.gateway.tool;

import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.errorcode.ErrorCode;
import com.LetucOJ.common.result.errorcode.GatewayErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtUtil {
    private static final String SECRET = "yoursecretkeyyoursecretkeyyoursecretkeyyoursecretkey";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DefaultDataBufferFactory bufferFactory = new DefaultDataBufferFactory();

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

    public static byte[] createErrorResponseBody(ErrorCode errorCode) {
        try {
            // 假设 Result.failure(errorCode) 构造了标准的错误响应对象
            return objectMapper.writeValueAsBytes(Result.failure(errorCode));
        } catch (JsonProcessingException e) {
            System.err.println("Error serializing error response: " + e.getMessage());
            // 序列化失败时的安全回退
            return ("{\"code\":" + GatewayErrorCode.SERVICE_ERROR.code() +
                    ",\"message\":\"Internal gateway error during response creation\"}").getBytes(StandardCharsets.UTF_8);
        }
    }

    public static ServerHttpResponseDecorator getDecoratedResponse(ServerWebExchange exchange) {

        ServerHttpResponse originalResponse = exchange.getResponse();

        return new ServerHttpResponseDecorator(originalResponse) {

            @NotNull
            @Override
            public Mono<Void> writeWith(@NotNull Publisher<? extends DataBuffer> body) {
                System.out.println("writeWith 3 " + body);
                if (body instanceof Flux<? extends DataBuffer> fluxBody) {

                    System.out.println("==== 拷头前  delegate 头 =====");
                    System.out.println(getDelegate().getHeaders());

                    getDelegate().getHeaders().addAll(originalResponse.getHeaders());

                    System.out.println("==== 拷头后  delegate 头 =====");
                    System.out.println(getDelegate().getHeaders());

                    return super.writeWith(
                        fluxBody
                                .collectList()
                            .flatMapMany(dataBuffers -> {
                                // 合并所有 buffer 内容
                                String originalContent;
                                try {
                                    StringBuilder sb = new StringBuilder();
                                    for (DataBuffer buffer : dataBuffers) {
                                        byte[] bytes = new byte[buffer.readableByteCount()];
                                        buffer.read(bytes);
                                        DataBufferUtils.release(buffer);
                                        sb.append(new String(bytes, StandardCharsets.UTF_8));
                                    }
                                    originalContent = sb.toString();
                                } catch (Exception e) {
                                    // 读取失败，回退并原样返回
                                    return Flux.just(bufferFactory.wrap("".getBytes(StandardCharsets.UTF_8)));
                                }

                                // 尝试解析并替换 token，仅在 code == 0 且有 data 时
                                try {
                                    JsonNode root = objectMapper.readTree(originalContent);
                                    JsonNode statusNode = root.get("code");
                                    if (statusNode == null || statusNode.asInt() != 0) {
                                        // 登录失败或非标准格式，原样返回
                                        return Flux.just(bufferFactory.wrap(originalContent.getBytes(StandardCharsets.UTF_8)));
                                    }

                                    JsonNode data = root.get("data");
                                    if (data == null || data.isNull()) {
                                        // 没有 data，原样返回
                                        return Flux.just(bufferFactory.wrap(originalContent.getBytes(StandardCharsets.UTF_8)));
                                    }

                                    JsonNode usernameNode = data.get("username");
                                    JsonNode cnnameNode = data.get("cnname");
                                    JsonNode roleNode = data.get("role");
                                    JsonNode millisNode = data.get("millis");


                                    System.out.println(data.asText());
                                    if (cnnameNode != null && !cnnameNode.isNull()) {
                                        System.out.println(cnnameNode.asText());
                                    }

                                    if (usernameNode == null || cnnameNode == null || roleNode == null) {
                                        // 缺字段，原样返回
                                        return Flux.just(bufferFactory.wrap(originalContent.getBytes(StandardCharsets.UTF_8)));
                                    }

                                    String username = usernameNode.asText();
                                    String cnname = cnnameNode.asText();
                                    String role = roleNode.asText();
                                    long millis = millisNode.asLong();

                                    exchange.getAttributes().put("millis", millis);

                                    // 生成 token
                                    String token = JwtUtil.generateToken(username, cnname, role);
                                    System.out.println("生成 JWT: " + token);

                                    ObjectNode node = (ObjectNode) objectMapper.readTree(originalContent);

                                    ObjectNode dataNode = (ObjectNode) node.get("data");
                                    if (dataNode == null) {
                                        dataNode = objectMapper.createObjectNode();
                                        node.set("data", dataNode);
                                    }

                                    dataNode.put("newToken", token);

                                    byte[] bytes = objectMapper.writeValueAsBytes(node);
                                    DataBuffer buffer = bufferFactory.wrap(bytes);
                                    return Flux.just(buffer);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return Flux.just(bufferFactory.wrap(originalContent.getBytes(StandardCharsets.UTF_8)));
                                }
                            })
                    );
                }
                return super.writeWith(body);
            }
        };
    }

//    public static Mono<Void> writeErrorResponse(ServerWebExchange exchange, ErrorCode errorCode) {
//
//        final ObjectMapper objectMapper = new ObjectMapper();
//
//        byte[] bytes;
//        try {
//            bytes = objectMapper.writeValueAsBytes(Result.failure(errorCode));
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//        DataBuffer buffer = exchange.getResponse()
//                .bufferFactory()
//                .wrap(bytes);
//        return exchange.getResponse().writeWith(Mono.just(buffer));
//    }
    public static Mono<Void> writeErrorResponse(ServerWebExchange exchange, ErrorCode errorCode) {
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(Result.failure(errorCode));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 【关键修复】删除 Content-Length，以防代理/网关计算错误导致 502
        exchange.getResponse().getHeaders().remove(HttpHeaders.CONTENT_LENGTH);

        // 【新增】显式添加 CORS 头部，确保在短路响应时不会出现跨域问题
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
