package com.LetucOJ.run.service.impl;

import com.LetucOJ.common.log.LogLevel;
import com.LetucOJ.common.log.Logger;
import com.LetucOJ.common.log.Type;
import com.LetucOJ.common.oss.MinioRepos;
import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.common.result.errorcode.BaseErrorCode;
import com.LetucOJ.run.model.TestCaseDTO;
import com.LetucOJ.run.model.TestCaseVO;
import com.LetucOJ.run.service.Handler;
//import com.LetucOJ.run.service.impl.handler.CompileHandler;
import com.LetucOJ.run.service.impl.handler.ExecuteHandler;
import com.LetucOJ.run.service.impl.handler.FileWriteHandler;
import com.LetucOJ.run.service.RunService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.LetucOJ.run.tool.RunPath;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static java.lang.Thread.sleep;

@Service
public class RunServiceImpl implements RunService {

    @Autowired
    private MinioRepos minioRepos;

    @Override
    public ResultVO<TestCaseVO> run(TestCaseDTO  testCaseDTO) {
        int boxId = RunPath.borrowBoxId();
        try {
            Handler fileWriteHandler = new FileWriteHandler();
            Handler executeHandler = new ExecuteHandler();
            fileWriteHandler.setNextHandler(executeHandler);
            byte[] config = minioRepos.getFile("letucoj", "problems/" + testCaseDTO.getQuestionName() + "/config.yaml");
            return fileWriteHandler.handle(testCaseDTO, boxId, config);
        } catch (Exception e) {
            Logger.log(Type.SERVER, LogLevel.ERROR, e.getMessage());
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        } finally {
            RunPath.returnBoxId();
        }
    }

}