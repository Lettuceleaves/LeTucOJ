package com.LetucOJ.gateway.tool;

import com.LetucOJ.common.cache.Redis;
import com.LetucOJ.common.log.LogLevel;
import com.LetucOJ.common.log.Logger;
import com.LetucOJ.common.log.Type;
import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.common.result.errorcode.GatewayErrorCode;
import com.LetucOJ.gateway.client.UserClient;
import com.LetucOJ.gateway.model.JwtInfoVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * 响应重写过滤器
 * 用于在响应返回给前端前，拦截 Body 并注入或刷新 Token
 */
@Slf4j
@Component
@Order(30)
@Data
@AllArgsConstructor
public class CustomResponseRewriteFilter implements WebFilter {

    private final UserClient userClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DataBufferFactory bufferFactory = new DefaultDataBufferFactory();

    @NotNull
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, @NotNull WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 1. 跳过不需要处理的请求
        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod()) || "/advice".equals(path)) {
            return chain.filter(exchange);
        }

        // 2. 针对登录接口的处理
        if ("/user/login".equals(path)) {
            return chain.filter(exchange.mutate()
                    .response(decorateResponse(exchange, this::processLoginResponse))
                    .build());
        }

        // 3. 针对其他接口的 Token 自动刷新处理
        return chain.filter(exchange.mutate()
                .response(decorateResponse(exchange, this::processTokenRefreshResponse))
                .build());
    }

    /**
     * 通用的响应装饰器生成方法
     * 负责将 Flux<DataBuffer> 聚合为 String，交给 processor 处理，再封装回 Flux
     *
     * @param exchange ServerWebExchange
     * @param processor 业务处理逻辑：输入为(原始JSON, Exchange)，输出为(新JSON)
     */
    private ServerHttpResponseDecorator decorateResponse(ServerWebExchange exchange,
                                                         BiFunction<String, ServerWebExchange, String> processor) {
        return new ServerHttpResponseDecorator(exchange.getResponse()) {
            @NotNull
            @Override
            public Mono<Void> writeWith(@NotNull Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux<? extends DataBuffer> fluxBody) {
                    return super.writeWith(fluxBody.collectList().flatMapMany(dataBuffers -> {
                        // 1. 更加安全的聚合方式：先合并所有 Byte，再转 String，防止宽字符被切断导致乱码
                        // 计算总长度
                        int totalSize = dataBuffers.stream().mapToInt(DataBuffer::readableByteCount).sum();
                        byte[] allBytes = new byte[totalSize];
                        int offset = 0;
                        for (DataBuffer buffer : dataBuffers) {
                            int len = buffer.readableByteCount();
                            buffer.read(allBytes, offset, len);
                            DataBufferUtils.release(buffer); // 释放内存
                            offset += len;
                        }

                        String originalContent = new String(allBytes, StandardCharsets.UTF_8);
                        String finalContent = originalContent;

                        try {
                            // 2. 调用具体的业务逻辑进行重写
                            finalContent = processor.apply(originalContent, exchange);
                        } catch (Exception e) {
                            log.error("Response rewrite failed", e);
                            // 发生异常时，返回原始内容，保证不影响业务
                        }

                        // 3. 重新计算 Content-Length (重要：修改了Body必须重置长度，否则客户端会截断或报错)
                        byte[] finalBytes = finalContent.getBytes(StandardCharsets.UTF_8);
                        getDelegate().getHeaders().setContentLength(finalBytes.length);

                        return Flux.just(bufferFactory.wrap(finalBytes));
                    }));
                }
                return super.writeWith(body);
            }
        };
    }

    /**
     * 业务逻辑 A: 处理登录响应
     * 检查黑名单 -> 生成 Token -> 构造新的响应体
     */
    private String processLoginResponse(String originalContent, ServerWebExchange exchange) {
        try {
            JsonNode root = objectMapper.readTree(originalContent);

            // 校验响应状态是否成功
            if (!isSuccessResponse(root)) {
                return originalContent;
            }

            JsonNode data = root.get("data");
            // 提取字段
            String username = getNodeText(data, "username");
            String cnname = getNodeText(data, "cnname");
            String role = getNodeText(data, "role");
            long millis = data.path("millis").asLong();

            // 检查 Redis 黑名单
            String blackListTimeStr = Redis.mapGet("black:" + username);
            long check = Long.parseLong(Objects.requireNonNullElse(blackListTimeStr, "-1"));

            if (check != -1 && millis <= check) {
                // 命中黑名单，修改响应状态码为 403，并返回错误体
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                byte[] errorBytes = JwtUtil.createErrorResponseBody(GatewayErrorCode.USER_BLOCKED);
                return new String(errorBytes, StandardCharsets.UTF_8);
            }

            // 清理黑名单并设置 Token 过期时间
            Redis.mapRemove("black:" + username);
            Redis.mapPutDuration("exp:" + username, "0", 24 * 60 * 60);

            // 生成 Token
            String token = JwtUtil.generateToken(username, cnname, role);
            log.info("Login success, generate JWT for user: {}", username);

            // 构造新的响应 JSON
            return String.format(
                    "{\"code\":\"0\",\"data\":null,\"message\":null, \"token\":\"%s\"}",
                    token
            );

        } catch (Exception e) {
            log.error("Error processing login response", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 业务逻辑 B: 处理非登录接口的 Token 刷新
     * 检查是否需要刷新 -> 调用 UserClient -> 注入新 Token
     */
    private String processTokenRefreshResponse(String originalContent, ServerWebExchange exchange) {
        try {
            // 检查是否具备刷新条件 (Attribute中包含username 且 Redis中exp过期)
            String usernameAttr = exchange.getAttribute("username");
            if (usernameAttr == null || Redis.mapGet("exp:" + usernameAttr) != null) {
                return originalContent;
            }

            // 调用远程服务刷新
            ResultVO<JwtInfoVO> res = userClient.refreshToken(usernameAttr);
            if (!"0".equals(res.getCode())) {
                return originalContent;
            }

            // 生成新 Token
            JsonNode resData = objectMapper.valueToTree(res).get("data");
            String username = getNodeText(resData, "username");
            String cnname = getNodeText(resData, "cnname");
            String role = getNodeText(resData, "role");
            String token = JwtUtil.generateToken(username, cnname, role);

            // 注入 Token 到原有响应中
            JsonNode originalRoot = objectMapper.readTree(originalContent);
            if (originalRoot.has("data") && originalRoot.get("data").isObject()) {
                ((ObjectNode) originalRoot.get("data")).put("token", token);

                // 更新 Redis 过期时间
                Redis.mapPutDuration("exp:" + username, "0", 24 * 60 * 60);

                return objectMapper.writeValueAsString(originalRoot);
            }

            return originalContent;

        } catch (Exception e) {
            log.error("Error processing token refresh", e);
            throw new RuntimeException(e);
        }
    }

    // --- 辅助方法 ---

    private boolean isSuccessResponse(JsonNode root) {
        JsonNode code = root.get("code");
        JsonNode data = root.get("data");
        return code != null && code.asInt() == 0 && data != null && !data.isNull();
    }

    private String getNodeText(JsonNode node, String fieldName) {
        return node.path(fieldName).asText(null);
    }
}