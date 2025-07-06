package com.LetucOJ.gateway.service;

import reactor.core.publisher.Mono;

public interface gateWayService {
    Mono<String> authenticate(String username, String password);
}
