package com.LetucOJ.gateway.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
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
    private final DefaultDataBufferFactory bufferFactory = new DefaultDataBufferFactory();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (!"/user/login".equals(path)) {
            return chain.filter(exchange);
        }

        ServerHttpResponse originalResponse = exchange.getResponse();

        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
            @Override
            @SuppressWarnings("unchecked")
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux) {
                    Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;

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

                                        // 尝试解析并替换 token，仅在 status == 0 且有 data 时
                                        try {
                                            JsonNode root = objectMapper.readTree(originalContent);
                                            JsonNode statusNode = root.get("status");
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
                                            JsonNode roleNode = data.get("role");
                                            JsonNode millisNode = data.get("millis");

                                            if (usernameNode == null || roleNode == null || millisNode == null) {
                                                // 缺字段，原样返回
                                                return Flux.just(bufferFactory.wrap(originalContent.getBytes(StandardCharsets.UTF_8)));
                                            }

                                            String username = usernameNode.asText();
                                            String role = roleNode.asText();
                                            long millis = millisNode.asLong();

                                            // 生成 token
                                            String token = JwtUtil.generateToken(username, role, millis);
                                            System.out.println("生成 JWT: " + token);

                                            // 构造新的 body 只返回 token
                                            String newBody = objectMapper.writeValueAsString(
                                                    objectMapper.createObjectNode().put("token", token)
                                            );
                                            return Flux.just(bufferFactory.wrap(newBody.getBytes(StandardCharsets.UTF_8)));
                                        } catch (Exception e) {
                                            // 解析出错，原样返回
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
