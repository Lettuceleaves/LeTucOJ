package com.LetucOJ.run.service.impl;

import com.LetucOJ.run.model.vo.ResultVO;
import com.LetucOJ.run.service.Handler;
import com.LetucOJ.run.service.impl.handler.CompileHandler;
import com.LetucOJ.run.service.impl.handler.ExcuteHandler;
import com.LetucOJ.run.service.impl.handler.FileWriteHandler;
import com.LetucOJ.run.service.RunService;
import org.springframework.stereotype.Service;
import com.LetucOJ.run.tool.RunPath;

import java.util.List;

import static java.lang.Thread.sleep;

@Service
public class RunServiceImpl implements RunService {

    @Override
    public ResultVO run(List<String> inputFiles) {


        Handler fileWriteHandler = new FileWriteHandler();
        Handler compileHandler = new CompileHandler();
        Handler excuteHandler = new ExcuteHandler();

        fileWriteHandler.setNextHandler(compileHandler);
        compileHandler.setNextHandler(excuteHandler);

        RunPath.pathInit();

        ResultVO ans = fileWriteHandler.handle(inputFiles);
        System.out.println(ans);
        return ans;

    }
}
