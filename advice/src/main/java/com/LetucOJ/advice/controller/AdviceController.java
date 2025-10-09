package com.LetucOJ.advice.controller;

import com.LetucOJ.advice.service.adviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@CrossOrigin(origins = "*")
public class AdviceController {

    @Autowired
    private adviceService adviceService;

    @PostMapping(value = "/advice", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> advice(@RequestBody String userFile) throws IOException {
        return adviceService.advice(userFile);
    }
}