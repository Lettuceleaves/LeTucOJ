package com.LetucOJ.gateway.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class CustomResponseRewriteFilter implements WebFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (!path.equals("/user/login")) {
            return chain.filter(exchange);
        }

        ServerHttpResponse originalResponse = exchange.getResponse();
        DefaultDataBufferFactory bufferFactory = new DefaultDataBufferFactory();

        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux) {
                    return super.writeWith(
                            ((Flux<? extends DataBuffer>) body).collectList().<DefaultDataBuffer>handle((dataBuffers, sink) -> {
                                StringBuilder originalContent = new StringBuilder();
                                dataBuffers.forEach(buffer -> {
                                    byte[] bytes = new byte[buffer.readableByteCount()];
                                    buffer.read(bytes);
                                    DataBufferUtils.release(buffer);
                                    originalContent.append(new String(bytes, StandardCharsets.UTF_8));
                                });

                                try {
                                    // 解析 JSON，提取字段
                                    JsonNode root = objectMapper.readTree(originalContent.toString());
                                    JsonNode data = root.get("data");
                                    if (data == null) {
                                        sink.error(new IllegalStateException("登录响应缺少 data 字段"));
                                        return;
                                    }

                                    String username = data.get("username").asText();
                                    String role = data.get("role").asText();
                                    long millis = data.get("millis").asLong();

                                    // 生成 token
                                    String token = JwtUtil.generateToken(username, role, millis);
                                    System.out.println("生成 JWT: " + token);

                                    // 构造新的 JSON 只返回 token
                                    String newBody = objectMapper.writeValueAsString(
                                            objectMapper.createObjectNode().put("token", token)
                                    );

                                    sink.next(bufferFactory.wrap(newBody.getBytes(StandardCharsets.UTF_8)));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    byte[] fallback = originalContent.toString().getBytes(StandardCharsets.UTF_8);
                                    sink.next(bufferFactory.wrap(fallback)); // fallback to original
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
