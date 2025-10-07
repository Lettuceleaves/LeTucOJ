package com.LetucOJ.practice.controller;

import com.LetucOJ.common.mq.MessageQueueProducer;
import com.LetucOJ.common.mq.impl.Message;
import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.common.result.errorcode.BaseErrorCode;
import com.LetucOJ.practice.model.RecordDTO;
import com.LetucOJ.practice.repos.MybatisRepos;
import com.LetucOJ.practice.service.PracticeService;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/practice")
public class SubmitController {

    @Autowired
    private PracticeService practiceService;

    @Autowired
    private MybatisRepos mybatisRepos;

    @Resource
    private MessageQueueProducer mqProducer;

    @PostMapping("/submit")
    public ResultVO submit(@RequestParam("pname") String pname,
                           @RequestParam("cnname") String cnname,
                           @RequestParam("qname") String qname,
                           @RequestParam("language") String language,
                           @RequestBody String code) throws Exception {

        // 1. 调用核心业务逻辑
        ResultVO result = practiceService.submit(pname, qname, code, language, false);

        try {
            // 2. 构造 RecordDTO，用于存储到历史记录
            RecordDTO record = new RecordDTO(
                    pname,
                    cnname,
                    qname,
                    language,
                    code,
                    result.getCode() + " $ " + result.getMessage(), // 状态信息
                    0L,
                    0L,
                    System.currentTimeMillis() // 提交时间
            );

            // 3. 构造 MQ 消息
            Message message = Message.builder()
                    .topic("submission") // 💡 提交记录主题
                    .tag("submission")
                    .key(pname) // 唯一键
                    .body(JSON.toJSONString(record)) // 💡 将 RecordDTO 序列化为 JSON 字符串
                    .build();

            // 4. 发送消息到 MQ (使用同步发送，但推荐异步/单向)
            // ⚠️ 注意：这里我们将发送记录的失败视为次要错误，不影响主流程返回判题结果。
            mqProducer.send(message);

        } catch (Exception e) {
            // 记录日志，但不影响用户接收判题结果
            System.err.println("send mq error: " + e.getMessage());
            // 可以返回结果，不抛出异常，因为判题结果已经拿到
        }

        // 5. 返回判题结果
        return result;
    }

    @PostMapping("/submitInRoot")
    public ResultVO submitInRoot(@RequestParam("pname") String pname,
                                 @RequestParam("cnname") String cnname,
                                 @RequestParam("qname") String qname,
                                 @RequestParam("language") String language,
                                 @RequestBody String code) throws Exception {

        // 1. 调用核心业务逻辑
        ResultVO result = practiceService.submit(pname, qname, code, language, true);

        try {
            // 2. 构造 RecordDTO，用于存储到历史记录
            RecordDTO record = new RecordDTO(
                    pname,
                    cnname,
                    qname,
                    language,
                    code,
                    result.getCode() + " $ " + result.getMessage(),
                    0L,
                    0L,
                    System.currentTimeMillis()
            );

            // 3. 构造 MQ 消息
            Message message = Message.builder()
                    .topic("submission") // 💡 提交记录主题
                    .tag("submission")
                    .key(pname)
                    .body(JSON.toJSONString(record))
                    .build();

            // 4. 发送消息到 MQ
            mqProducer.send(message);

        } catch (Exception e) {
            // 记录日志，但不影响用户接收判题结果
            System.err.println("send mq error: " + e.getMessage());
        }

        // 5. 返回判题结果
        return result;
    }
}
