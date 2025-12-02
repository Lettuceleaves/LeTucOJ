package com.LetucOJ.gateway.client;

import com.LetucOJ.gateway.model.JwtInfoVO;
import com.LetucOJ.gateway.result.ResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user", url = "user:5555")
@RestController
public interface UserClient {

    @GetMapping("/user/refresh")
    ResultVO<JwtInfoVO> refreshToken(@RequestParam("user_name") String userName);
}