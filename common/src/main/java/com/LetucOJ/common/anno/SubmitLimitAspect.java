package com.LetucOJ.common.anno;

import cn.hutool.core.lang.Singleton;
import cn.hutool.json.JSONUtil;
import com.LetucOJ.common.mq.impl.Message;
import com.LetucOJ.common.mq.impl.RocketMQProducer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
@Data
@ConditionalOnClass(name = "org.springframework.web.servlet.DispatcherServlet")
public class SubmitLimitAspect {

    private final RocketMQProducer rocketMQProducer;

    private final StringRedisTemplate stringRedisTemplate;

    private final LangMybatisRepos langMybatisRepos;

    private boolean retry = false;

    private final static String LUA_PATH = "lua/submit_limit.lua";

    private final static String LUA_UPDATE_CONFIG = """
            redis.call('HSET', KEYS[1], ARGV[1], ARGV[2])
            return 1
            """;

    private final static String LUA_DECR_MEM = """
            local submitKey = KEYS[1]
            local status = ARGV[1]
            local mem = tonumber(ARGV[2])
            redis.call('SET', submitKey, status)
            redis.call('DECRBY', 'used_memory', mem)
            """;

    @Around("@annotation(com.LetucOJ.common.anno.SubmitLimit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        SubmitLimit submitLimit = getSubmitLimit(joinPoint);
        String uniqueKey = submitLimit.keyPrefix() + submitLimit.taskId();
        DefaultRedisScript<Long> buildLuaScript = Singleton.get(LUA_PATH, () -> {
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(LUA_PATH)));
            redisScript.setResultType(Long.class);
            return redisScript;
        });
        String lang = joinPoint.getArgs()[0].toString();
        String userId = joinPoint.getArgs()[1].toString();
        String problemId = joinPoint.getArgs()[2].toString();
        String submitKey = submitLimit.keyPrefix() + userId + ":" + problemId;
        System.out.println(submitKey);
        Long result = stringRedisTemplate.execute(
                buildLuaScript,
                List.of("limit", submitKey),
                lang, SubmitStatus.COMMITING.message());
        if (result >= 0L) {
            String body = JSONUtil.toJsonStr(Map.of(
                    "submitKey", submitKey,
                    "mem", result));
            rocketMQProducer.send(Message
                    .builder()
                    .topic("limit")
                    .key(uniqueKey)
                    .body(body)
                    .timestamp(String.valueOf(System.currentTimeMillis()))
                    .delayLevel(submitLimit.delayLevel())
                    .build());
            Object res = joinPoint.proceed();
            stringRedisTemplate.execute(
                    new DefaultRedisScript<>(LUA_DECR_MEM, Long.class),
                    List.of(submitKey),
                    SubmitStatus.COMMITED.message(), String.valueOf(result));
            return res;
        } else if (result == -1L || result == -4L) {
            if (retry) {
                throw new RuntimeException("no language config " + lang + " after reload");
            } else {
                retry = true;
                loadLanguageConfig(lang);
                return around(joinPoint);
            }
        } else if (result == -2L || result == -3L) {
            throw new RuntimeException("sys busy");
        } else {
            System.out.println("unknown err " + result);
            throw new RuntimeException("sys err");
        }
    }

    private static SubmitLimit getSubmitLimit(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method targetMethod = joinPoint.getTarget().getClass().getDeclaredMethod(methodSignature.getName(), methodSignature.getMethod().getParameterTypes());
        return targetMethod.getAnnotation(SubmitLimit.class);
    }

    private void loadLanguageConfig(String lang) {
        List<LanguageConfigDO> configs = langMybatisRepos.selectList(lang);
        for (LanguageConfigDO config : configs) {
            System.out.println(config.getLanguage() + " " + config.getMemoryPerRun());
            stringRedisTemplate.execute(
                    new DefaultRedisScript<>(LUA_UPDATE_CONFIG, Long.class),
                    List.of("lang:mem"),
                    config.getLanguage(),
                    String.valueOf(config.getMemoryPerRun())
            );
        }
    }
}
