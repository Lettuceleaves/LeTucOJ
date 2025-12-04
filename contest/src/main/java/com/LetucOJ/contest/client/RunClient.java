package com.LetucOJ.contest.client;

import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.contest.model.DTO.TestTaskDTO;
import com.LetucOJ.contest.model.VO.TestTaskVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@FeignClient(name = "run", url = "run:1001")
@RestController
public interface RunClient {

    @PostMapping("/task")
    ResultVO<TestTaskVO> runTestTask(@RequestBody TestTaskDTO testTaskDTO);
}