package com.LetucOJ.run.service.impl.handler;

import com.LetucOJ.run.model.vo.ResultVO;
import com.LetucOJ.run.service.Handler;
import com.LetucOJ.run.tool.path;

import java.nio.file.Paths;
import java.util.List;

public class FileWriteHandler implements Handler {
    Handler nextHandler;

    @Override
    public void setNextHandler(Handler nextHandler) {
        this.nextHandler = nextHandler;
    }

    @Override
    public ResultVO handle(List<String> inputFiles, path path) {
        try {
            java.nio.file.Files.write(Paths.get(path.getUserCodePath()), inputFiles.get(0).getBytes());
            for (int i = 1; i < inputFiles.size(); i++) {
                java.nio.file.Files.write(Paths.get(path.getInputPath(i)), inputFiles.get(i).getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultVO((byte) 2, null, "编译失败: " + e.getMessage());
        }
        return nextHandler.handle(inputFiles, path);
    }

}
