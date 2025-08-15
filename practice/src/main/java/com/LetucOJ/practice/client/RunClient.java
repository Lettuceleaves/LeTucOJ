package com.LetucOJ.practice.client;

import com.LetucOJ.practice.model.ResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@FeignClient(name = "sys", url = "sys:2121")
@RestController
public interface RunClient {

    @PostMapping("/sys/run")
    ResultVO run(@RequestBody List<String> files, @RequestParam String lang);
}