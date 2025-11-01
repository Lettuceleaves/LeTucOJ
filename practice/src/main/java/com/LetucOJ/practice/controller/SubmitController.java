package com.LetucOJ.practice.controller;

import com.LetucOJ.common.anno.SubmitLimit;
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
    @SubmitLimit
    public ResultVO submit(
            @RequestParam("lang") String lang,
            @RequestParam("pname") String pname,
            @RequestParam("qname") String qname,
            @RequestParam("cnname") String cnname,
            @RequestBody String code) throws Exception {
        ResultVO result = practiceService.submit(pname, qname, code, lang, false);

        try {
            RecordDTO record = new RecordDTO(
                    pname,
                    cnname,
                    qname,
                    lang,
                    code,
                    result.getCode() + " $ " + result.getData(),
                    0L,
                    0L,
                    System.currentTimeMillis()
            );

            Message message = Message.builder()
                    .topic("submission")
                    .tag("submission")
                    .key(pname)
                    .body(JSON.toJSONString(record))
                    .build();

            mqProducer.send(message);

        } catch (Exception e) {
        }

        return result;
    }

    @PostMapping("/submitInRoot")
    @SubmitLimit
    public ResultVO submitInRoot(
            @RequestParam("lang") String lang,
            @RequestParam("pname") String pname,
            @RequestParam("qname") String qname,
            @RequestParam("cnname") String cnname,
            @RequestBody String code) throws Exception {

        ResultVO result = practiceService.submit(pname, qname, code, lang, true);

        try {
            RecordDTO record = new RecordDTO(
                    pname,
                    cnname,
                    qname,
                    lang,
                    code,
                    result.getCode() + " $ " + result.getData(),
                    0L,
                    0L,
                    System.currentTimeMillis()
            );

            Message message = Message.builder()
                    .topic("submission")
                    .tag("submission")
                    .key(pname)
                    .body(JSON.toJSONString(record))
                    .build();

            mqProducer.send(message);

        } catch (Exception e) {
        }
        return result;
    }
}
