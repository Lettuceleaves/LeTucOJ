package com.LetucOJ.run.service.impl;

import com.LetucOJ.run.model.vo.ResultVO;
import com.LetucOJ.run.service.Handler;
import com.LetucOJ.run.service.impl.handler.CompileHandler;
import com.LetucOJ.run.service.impl.handler.ExcuteHandler;
import com.LetucOJ.run.service.impl.handler.FileWriteHandler;
import com.LetucOJ.run.service.RunService;
import org.springframework.stereotype.Service;
import com.LetucOJ.run.tool.path;

import java.util.List;

import static java.lang.Thread.sleep;

@Service
public class RunServiceImpl implements RunService {

    @Override
    public ResultVO run(List<String> inputFiles, boolean test) throws Exception {


        Handler fileWriteHandler = new FileWriteHandler();
        Handler compileHandler = new CompileHandler();
        Handler excuteHandler = new ExcuteHandler();

        fileWriteHandler.setNextHandler(compileHandler);
        compileHandler.setNextHandler(excuteHandler);

        path path = new path();
        path.pathInit(test);

        ResultVO ans = fileWriteHandler.handle(inputFiles, path);
        System.out.println(ans);
        return ans;

    }

    @Override
    public ResultVO runTest(List<String> inputFiles, boolean test) throws Exception {


        Handler fileWriteHandler = new FileWriteHandler();
        Handler compileHandler = new CompileHandler();
        Handler excuteHandler = new ExcuteHandler();

        fileWriteHandler.setNextHandler(compileHandler);
        compileHandler.setNextHandler(excuteHandler);

        path path = new path();
        path.pathInit(test);

        ResultVO ans = fileWriteHandler.handle(inputFiles, path);
        String ansTest = ans.getDataAsString();
        return new ResultVO((byte) 0, ansTest, null);

    }
}
