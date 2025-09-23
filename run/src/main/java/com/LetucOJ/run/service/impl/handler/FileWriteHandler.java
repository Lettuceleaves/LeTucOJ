package com.LetucOJ.run.service.impl.handler;

import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.run.service.Handler;
import com.LetucOJ.run.tool.RunPath;
import lombok.Data;

import java.nio.file.*;
import java.util.List;

@Data
public class FileWriteHandler implements Handler {

    private Handler nextHandler;

    public FileWriteHandler() {}

    @Override
    public void setNextHandler(Handler nextHandler) {
        this.nextHandler = nextHandler;
    }

    @Override
    public ResultVO handle(List<String> inputFiles, int boxid, String language) {
        try {
            /* 1. 确保 box 根目录存在 */
            Path boxDir = Paths.get(RunPath.getBoxDir(boxid));
            Files.createDirectories(boxDir);

            /* 3. 写用户代码 */
            Path codePath = Paths.get(RunPath.userCodePath(boxid, language));
            Files.write(codePath,
                    inputFiles.get(0).getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            /* 4. 写输入文件 + 预创建对应的输出文件 */
            for (int i = 1; i < inputFiles.size(); i++) {
                Path inputPath = Paths.get(RunPath.getInputPath(boxid, i));
                Files.write(inputPath,
                        inputFiles.get(i).getBytes(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);

                Path outputPath = Paths.get(RunPath.getOutputPath(boxid, i));
                Files.write(outputPath,
                        new byte[0], // 空文件
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            }

            /* 5. 创建状态错误文件、编译错误文件和运行时错误文件 */
            Path compileErr = Paths.get(RunPath.getCompilePath(boxid));
            Files.write(compileErr, new byte[0],
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            Path errFile = Paths.get(RunPath.getErrorPath(boxid));
            Files.write(errFile, new byte[0],
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            Path statusFile = Paths.get(RunPath.getStatusPath(boxid));
            Files.write(statusFile, new byte[0],
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

        } catch (Exception e) {
            return new ResultVO((byte) 5, null,
                    "Can not write file in run module: " + e.getMessage());
        }
        return nextHandler.handle(inputFiles, boxid, language);
    }
}
