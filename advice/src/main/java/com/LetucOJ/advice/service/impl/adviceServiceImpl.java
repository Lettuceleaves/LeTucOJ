package com.LetucOJ.advice.service.impl;

import com.LetucOJ.advice.service.adviceService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.IOException;

@Service
public class adviceServiceImpl implements adviceService {

    @Autowired
    private ChatClient chatClient;

    public Flux<ServerSentEvent<String>> advice(String userFile) throws IOException {
        return chatClient.prompt()
                .user(userFile)
                .stream()
                .content()
                .map(token -> ServerSentEvent.<String>builder()
                        .data(token.replace("\n", "\\x0A").replace(" ", "\\x0B"))
                        .build());
    }
}