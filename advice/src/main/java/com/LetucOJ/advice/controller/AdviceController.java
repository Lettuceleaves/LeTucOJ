package com.LetucOJ.advice.controller;

import com.LetucOJ.advice.service.adviceService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@CrossOrigin(origins = "*")
@Api(tags = {"advice"})
public class AdviceController {

    @Autowired
    private adviceService adviceService;

    @PostMapping("/advice")
    public Flux<ServerSentEvent<String>> advice(@RequestBody String userFile) throws IOException {
        return adviceService.advice(userFile);
    }
}