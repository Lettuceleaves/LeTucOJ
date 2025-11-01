package com.LetucOJ.advice.service;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.io.IOException;

public interface adviceService {
    Flux<ServerSentEvent<String>> advice(String userFile) throws IOException;
}