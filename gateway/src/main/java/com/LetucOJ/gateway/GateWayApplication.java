package com.LetucOJ.gateway;

import com.LetucOJ.common.trace.filter.ReactiveTraceFilter;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(
        scanBasePackages = {"com.LetucOJ.gateway", "com.LetucOJ.common"}
)
@EnableFeignClients
@ReactiveTraceFilter.EnableTrace(isGateway = true)
public class GateWayApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(GateWayApplication.class)
                .web(WebApplicationType.REACTIVE)
                .run(args);
    }
}