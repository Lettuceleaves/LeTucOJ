package com.LetucOJ.sys.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClientBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class RunClientFactory {

    @Autowired
    private ApplicationContext applicationContext;

    public RunClient forContainer(String host, int port) {
        String url = "http://" + host + ":" + port;
        return new FeignClientBuilder(applicationContext)
                .forType(RunClient.class, "run-" + host + "-" + port)
                .url(url)
                .build();
    }
}
