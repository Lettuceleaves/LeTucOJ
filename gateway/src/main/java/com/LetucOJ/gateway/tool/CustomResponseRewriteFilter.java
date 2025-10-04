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
import org.springframework.http.HttpStatus; // 导入 HttpStatus
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;
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

        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
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
                                            // 1. 异步：合并所有 buffer 内容并释放资源
                                            String originalContent;
                                            try {
                                                StringBuilder sb = new StringBuilder();
                                                for (DataBuffer buffer : dataBuffers) {
                                                    byte[] bytes = new byte[buffer.readableByteCount()];
                                                    buffer.read(bytes);
                                                    DataBufferUtils.release(buffer); // 必须释放 DataBuffer
                                                    sb.append(new String(bytes, StandardCharsets.UTF_8));
                                                }
                                                originalContent = sb.toString();
                                            } catch (Exception e) {
                                                // 读取失败，回退并原样返回
                                                return Flux.just(bufferFactory.wrap("".getBytes(StandardCharsets.UTF_8)));
                                            }

                                            // 2. 异步：尝试解析数据
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

                                                if (usernameNode == null || cnnameNode == null || roleNode == null || millisNode == null) {
                                                    // 缺字段，原样返回
                                                    return Flux.just(bufferFactory.wrap(originalContent.getBytes(StandardCharsets.UTF_8)));
                                                }

                                                String username = usernameNode.asText();
                                                String cnname = cnnameNode.asText();
                                                String role = roleNode.asText();
                                                long millis = millisNode.asLong(); // 【在这里获取 millis】

                                                // 3. 异步：执行 Redis 黑名单校验 (使用从响应体解析出的 username)
                                                String blackListTimeStr = Redis.mapGet("black:" + username);
                                                long check = Long.parseLong(Objects.requireNonNullElse(blackListTimeStr, "-1"));

                                                // 校验黑名单：如果检查时间有效 (>-1) 且当前登录时间 <= 黑名单时间
                                                if (check != -1 && millis <= check) {
                                                    System.out.println("User " + username + " blocked due to black list.");

                                                    // FIX: 修正类型错误：设置状态并返回错误体 Flux
                                                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN); // 设置 403

                                                    // 使用 JwtUtil 方法创建错误 JSON 字节数组 (假设 JwtUtil 中有此方法)
                                                    byte[] errorBytes = JwtUtil.createErrorResponseBody(GatewayErrorCode.USER_BLOCKED);

                                                    // 同时移除 Content-Length，防止 502
                                                    exchange.getResponse().getHeaders().remove(HttpHeaders.CONTENT_LENGTH);

                                                    return Flux.just(bufferFactory.wrap(errorBytes));
                                                }

                                                // 4. 异步：校验通过，执行 Redis 清理和过期时间设置 (使用从响应体解析出的 username)
                                                Redis.mapRemove("black:" + username);
                                                // 持续时间设置这里仍使用 60 秒
                                                Redis.mapPutDuration("exp:" + username, "0", 60);

                                                // 5. 生成 token (FIX: 传入 millis)
                                                String token = JwtUtil.generateToken(username, cnname, role);
                                                System.out.println("生成 JWT: " + token);

                                                // 6. 构造新的 body,将data替换为token
                                                String newBody = String.format(
                                                        "{\"code\":\"0\",\"data\":null,\"message\":null, \"token\":\"%s\"}",
                                                        token
                                                );

                                                // 7. 移除 Content-Length
                                                exchange.getResponse().getHeaders().remove(HttpHeaders.CONTENT_LENGTH);

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
        } else {
            //  if (exchange.getAttributes().containsKey("username") && Redis.mapGet("exp:" + exchange.getAttribute("username")) == null)
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

                                            // 1. 异步：合并所有 buffer 内容并释放资源
                                            String originalContent;
                                            try {
                                                StringBuilder sb = new StringBuilder();
                                                for (DataBuffer buffer : dataBuffers) {
                                                    byte[] bytes = new byte[buffer.readableByteCount()];
                                                    buffer.read(bytes);
                                                    DataBufferUtils.release(buffer); // 必须释放 DataBuffer
                                                    sb.append(new String(bytes, StandardCharsets.UTF_8));
                                                }
                                                originalContent = sb.toString();
                                            } catch (Exception e) {
                                                // 读取失败，回退并原样返回
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

                                            // 2. 异步：尝试解析数据
                                            try {
                                                JsonNode root = objectMapper.valueToTree(res);   // res 就是 ResultVO
                                                JsonNode data   = root.get("data");

                                                /* 2. 从 ResultVO 里拿字段，不再解析 originalContent */
                                                String username = data.get("username").asText();
                                                String cnname   = data.get("cnname").asText();
                                                String role     = data.get("role").asText();

                                                String token = JwtUtil.generateToken(username, cnname, role);
                                                System.out.println("生成 JWT: " + token);
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
