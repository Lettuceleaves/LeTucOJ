package com.LetucOJ.contest.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@FeignClient(name = "practice", url = "localhost:2222")
@RestController
public interface practiceClient {
    @PostMapping("/practice/submitFeign/{problemName}")
    String submitFeign(@PathVariable("problemName") String problemName, @RequestParam("userFile") byte[] userFile) throws Exception;
}