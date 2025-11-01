package com.LetucOJ.run.service.impl;

import com.LetucOJ.common.oss.MinioRepos;
import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.common.result.errorcode.BaseErrorCode;
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
    public ResultVO run(List<String> inputFiles, String language, String qname) {
        int boxId = RunPath.borrowBoxId();
        try {
            Handler fileWriteHandler = new FileWriteHandler();
            Handler executeHandler = new ExecuteHandler();
            fileWriteHandler.setNextHandler(executeHandler);
            byte[] config = minioRepos.getFile("letucoj", "problems/" + qname + "/config.yaml");
            return fileWriteHandler.handle(inputFiles, boxId, language, qname, config);
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        } finally {
            RunPath.returnBoxId();
        }
    }

}