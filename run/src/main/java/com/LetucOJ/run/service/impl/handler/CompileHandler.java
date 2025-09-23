//package com.LetucOJ.run.service.impl.handler;
//
//import com.LetucOJ.run.model.vo.ResultVO;
//import com.LetucOJ.run.service.Handler;
//import com.LetucOJ.run.tool.Commands;
//import com.LetucOJ.run.tool.RunPath;
//import io.lettuce.core.protocol.Command;
//
//import java.nio.file.Paths;
//import java.util.List;
//
//public class CompileHandler implements Handler {
//    Handler nextHandler;
//
//    @Override
//    public void setNextHandler(Handler nextHandler) {
//        this.nextHandler = nextHandler;
//    }
//
//    @Override
//    public ResultVO handle(List<String> inputFiles) {
//        try {
//            ProcessBuilder compileProcessBuilder = Commands.getCompilerProcessBuilder();
//            compileProcessBuilder.directory(Paths.get(RunPath.getTestSpaceRootPath()).toFile());
//
//            Process compileProcess = compileProcessBuilder.start();
//
//            if (!compileProcess.waitFor(100000, java.util.concurrent.TimeUnit.MILLISECONDS)) {
//                compileProcess.destroy();
//                return new ResultVO((byte) 2, null, "Compilation timed out after 100 seconds");
//            }
//            if (compileProcess.exitValue() != 0) {
//                // 获取报错信息
//                String errorMessage = new String(java.nio.file.Files.readAllBytes(Paths.get(RunPath.getErrorPath())));
//                compileProcess.destroy();
//                return new ResultVO((byte) 2, null, "Compilation failed" + ": " + errorMessage.trim());
//            }
//        } catch (Exception e) {
//            return new ResultVO((byte) 2, null, "Compilation failed: " + e.getMessage());
//        }
//        return nextHandler.handle(inputFiles);
//    }
//}