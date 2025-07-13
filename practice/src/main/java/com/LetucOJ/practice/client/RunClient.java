package com.LetucOJ.practice.client;

import com.LetucOJ.practice.model.ResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@FeignClient(name = "run", url = "localhost:1001")
@RestController
public interface RunClient {

    @PostMapping("/runFeign")
    ResultVO run(@RequestBody List<String> files) throws Exception;

    @PostMapping("/runTestFeign")
    ResultVO runTest(@RequestBody List<String> files) throws Exception;

}
