package com.LetucOJ.run.service.impl;

import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.run.service.Handler;
//import com.LetucOJ.run.service.impl.handler.CompileHandler;
import com.LetucOJ.run.service.impl.handler.ExecuteHandler;
import com.LetucOJ.run.service.impl.handler.FileWriteHandler;
import com.LetucOJ.run.service.RunService;
import org.springframework.stereotype.Service;
import com.LetucOJ.run.tool.RunPath;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static java.lang.Thread.sleep;

@Service
public class RunServiceImpl implements RunService {

    @Override
    public ResultVO run(List<String> inputFiles, String language) {
        int boxId = RunPath.borrowBoxId();
        try {
            Handler fileWriteHandler = new FileWriteHandler();
            Handler executeHandler = new ExecuteHandler();
            fileWriteHandler.setNextHandler(executeHandler);
            return fileWriteHandler.handle(inputFiles, boxId, language);
        } catch (Exception e) {
            // 1. 控制台完整栈
            e.printStackTrace();
            // 2. 返回给前端也带栈
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            return new ResultVO((byte) 5, null, sw.toString());
        } finally {
            RunPath.returnBoxId();
        }
    }

}