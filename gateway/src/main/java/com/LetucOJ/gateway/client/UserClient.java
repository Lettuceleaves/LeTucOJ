package com.LetucOJ.gateway.client;

import com.LetucOJ.gateway.result.ResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "user", url = "user:5555")
@RestController
public interface UserClient {

    @GetMapping("/user/refresh")
    ResultVO refreshToken(@RequestParam("pname") String pname);
}