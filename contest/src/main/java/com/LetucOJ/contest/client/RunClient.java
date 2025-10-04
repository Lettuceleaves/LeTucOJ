package com.LetucOJ.contest.client;

import com.LetucOJ.common.result.ResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@FeignClient(name = "run", url = "run:1001")
@RestController
public interface RunClient {

    @PostMapping("/run")
    ResultVO run(@RequestBody List<String> files, @RequestParam String language);
}