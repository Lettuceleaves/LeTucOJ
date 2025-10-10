package com.LetucOJ.gateway.tool;

import com.LetucOJ.common.cache.Redis;
import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.common.result.errorcode.GatewayErrorCode;
import com.LetucOJ.gateway.client.UserClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Component
public class CustomResponseRewriteFilter implements WebFilter {

    @Autowired
    private UserClient userClient;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DefaultDataBufferFactory bufferFactory = new DefaultDataBufferFactory();

    @NotNull
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, @NotNull WebFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod()) || "/advice".equals(path)) {
            return chain.filter(exchange);
        }

        if ("/user/login".equals(path)) {

            ServerHttpResponse originalResponse = exchange.getResponse();

            ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                @NotNull
                @Override
                public Mono<Void> writeWith(@NotNull Publisher<? extends DataBuffer> body) {
                    if (body instanceof Flux<? extends DataBuffer> fluxBody) {

                        return super.writeWith(
                                fluxBody
                                        .collectList()
                                        .flatMapMany(dataBuffers -> {
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
                                                return Flux.just(bufferFactory.wrap("".getBytes(StandardCharsets.UTF_8)));
                                            }
                                            try {
                                                JsonNode root = objectMapper.readTree(originalContent);
                                                JsonNode statusNode = root.get("code");
                                                if (statusNode == null || statusNode.asInt() != 0) {
                                                    return Flux.just(bufferFactory.wrap(originalContent.getBytes(StandardCharsets.UTF_8)));
                                                }

                                                JsonNode data = root.get("data");
                                                if (data == null || data.isNull()) {
                                                    return Flux.just(bufferFactory.wrap(originalContent.getBytes(StandardCharsets.UTF_8)));
                                                }

                                                JsonNode usernameNode = data.get("username");
                                                JsonNode cnnameNode = data.get("cnname");
                                                JsonNode roleNode = data.get("role");
                                                JsonNode millisNode = data.get("millis");

                                                if (usernameNode == null || cnnameNode == null || roleNode == null || millisNode == null) {
                                                    return Flux.just(bufferFactory.wrap(originalContent.getBytes(StandardCharsets.UTF_8)));
                                                }

                                                String username = usernameNode.asText();
                                                String cnname = cnnameNode.asText();
                                                String role = roleNode.asText();
                                                long millis = millisNode.asLong();

                                                String blackListTimeStr = Redis.mapGet("black:" + username);
                                                long check = Long.parseLong(Objects.requireNonNullElse(blackListTimeStr, "-1"));

                                                if (check != -1 && millis <= check) {
                                                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN); // 设置 403
                                                    byte[] errorBytes = JwtUtil.createErrorResponseBody(GatewayErrorCode.USER_BLOCKED);
                                                    exchange.getResponse().getHeaders().remove(HttpHeaders.CONTENT_LENGTH);

                                                    return Flux.just(bufferFactory.wrap(errorBytes));
                                                }
                                                Redis.mapRemove("black:" + username);
                                                // 持续时间设置1天
                                                Redis.mapPutDuration("exp:" + username, "0", 24 * 60 * 60);
                                                String token = JwtUtil.generateToken(username, cnname, role);
                                                System.out.println("生成 JWT: " + token);
                                                String newBody = String.format(
                                                        "{\"code\":\"0\",\"data\":null,\"message\":null, \"token\":\"%s\"}",
                                                        token
                                                );
                                                exchange.getResponse().getHeaders().remove(HttpHeaders.CONTENT_LENGTH);

                                                return Flux.just(bufferFactory.wrap(newBody.getBytes(StandardCharsets.UTF_8)));

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

            return chain.filter(exchange.mutate().response(decoratedResponse).build());
        } else {
            ServerHttpResponse originalResponse = exchange.getResponse();

            ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                @NotNull
                @Override
                public Mono<Void> writeWith(@NotNull Publisher<? extends DataBuffer> body) {
                    if (body instanceof Flux<? extends DataBuffer> fluxBody) {

                        return super.writeWith(
                                fluxBody
                                        .collectList()
                                        .flatMapMany(dataBuffers -> {

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
                                                return Flux.just(bufferFactory.wrap("".getBytes(StandardCharsets.UTF_8)));
                                            }

                                            if (!(exchange.getAttributes().containsKey("username") && Redis.mapGet("exp:" + exchange.getAttribute("username")) == null)) {
                                                return Flux.just(bufferFactory.wrap(originalContent.getBytes(StandardCharsets.UTF_8)));
                                            }

                                            String userName = exchange.getAttribute("username");
                                            ResultVO res = userClient.refreshToken(userName);
                                            if (!res.getCode().equals("0")) {
                                                return Flux.just(bufferFactory.wrap(originalContent.getBytes(StandardCharsets.UTF_8)));
                                            }
                                            try {
                                                JsonNode root = objectMapper.valueToTree(res);
                                                JsonNode data   = root.get("data");
                                                String username = data.get("username").asText();
                                                String cnname   = data.get("cnname").asText();
                                                String role     = data.get("role").asText();
                                                String token = JwtUtil.generateToken(username, cnname, role);

                                                JsonNode node = objectMapper.readTree(originalContent);
                                                ObjectNode dataNode = (ObjectNode) node.get("data");
                                                dataNode.put("token", token);
                                                byte[] newBytes = objectMapper.writeValueAsBytes(node);
                                                exchange.getResponse().getHeaders().remove(HttpHeaders.CONTENT_LENGTH);
                                                Redis.mapPutDuration("exp:" + username, "0", 24 * 60 * 60);
                                                return Flux.just(bufferFactory.wrap(newBytes));
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

            return chain.filter(exchange.mutate().response(decoratedResponse).build());
        }
    }
}
