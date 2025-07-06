package com.LetucOJ.practice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.IOException;

@FeignClient(name = "advice", url = "localhost:9999")
@RestController
public interface AdviceClient {
    @GetMapping("/advice")
    Flux<ServerSentEvent<String>> advice(@RequestParam(value = "userFile") String userFile) throws IOException;
}
