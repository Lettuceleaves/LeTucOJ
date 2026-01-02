package com.LetucOJ.practice.controller;

import com.LetucOJ.common.cache.Redis;
import com.LetucOJ.common.log.LogLevel;
import com.LetucOJ.common.log.Logger;
import com.LetucOJ.common.log.Type;
import com.LetucOJ.common.mq.MessageQueueProducer;
import com.LetucOJ.common.mq.impl.Message;
import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.common.trace.TraceContext;
import com.LetucOJ.practice.model.DTO.SubmitRecordDTO;
import com.LetucOJ.practice.model.VO.TestTaskVO;
import com.LetucOJ.practice.repos.MybatisRepos;
import com.LetucOJ.practice.service.PracticeService;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/practice")
@Data
@AllArgsConstructor
public class SubmitController {

    private PracticeService practiceService;

    private MybatisRepos mybatisRepos;

    @Resource
    private MessageQueueProducer mqProducer;

    @PostMapping("/submit")
//    @SubmitLimit
    public ResultVO<TestTaskVO> submit(
            @RequestParam("language") String language,
            @RequestParam("user_name") String userName,
            @RequestParam("problem_name") String problemName,
            @RequestParam("nick_name") String nickName,
            @RequestParam("role") String role,
            @RequestBody String code) throws Exception {
        ResultVO<TestTaskVO> result = practiceService.submit(userName, problemName, code, language, role);
        Logger.log(Type.DEPENDENT, LogLevel.INFO, nickName);
        return saveSubmitRecord(language, userName, problemName, nickName, code, result);
    }

    @Nullable
    private ResultVO<TestTaskVO> saveSubmitRecord(String language,
                                                  String userName,
                                                  String problemName,
                                                  String nickName,
                                                  String userCode,
                                                  ResultVO<TestTaskVO> result) {
        try {
            SubmitRecordDTO record = new SubmitRecordDTO(
                    TraceContext.getTraceId(),
                    userName,
                    nickName,
                    problemName,
                    language,
                    userCode,
                    result.getCode() + " $ " + result.getData(),
                    0L,
                    0L,
                    System.currentTimeMillis()
            );

            Message message = Message.builder()
                    .topic("submission")
                    .tag("submission")
                    .key(userName)
                    .body(JSON.toJSONString(record))
                    .build();

            Redis.listPush("submission", JSON.toJSONString(message));

        } catch (Exception e) {
            Logger.log(Type.SERVER, LogLevel.ERROR, e.getMessage());
        }
        return result;
    }
}
