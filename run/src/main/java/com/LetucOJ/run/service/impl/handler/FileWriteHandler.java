package com.LetucOJ.run.service.impl.handler;

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
import com.LetucOJ.run.tool.RunPath;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.*;
import java.util.List;

@Data
@Service
public class FileWriteHandler implements Handler {

    @Autowired
    private MinioRepos minioRepos;

    private Handler nextHandler;

    public FileWriteHandler() {}

    @Override
    public void setNextHandler(Handler nextHandler) {
        this.nextHandler = nextHandler;
    }

    @Override
    public ResultVO<TestCaseVO> handle(TestCaseDTO testCaseDTO, int boxid, byte[] config) {
        try {
            Path boxDir = Paths.get(RunPath.getBoxDir(boxid));
            Files.createDirectories(boxDir);

            Path codePath = Paths.get(RunPath.userCodePath(boxid, testCaseDTO.getLanguage()));
            Files.write(codePath,
                    testCaseDTO.getUserCode().getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            Path configPath = Paths.get(RunPath.getConfigPath(boxid));
            Files.write(configPath,
                    config,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            for (int i = 0; i < testCaseDTO.getCaseFiles().size(); i++) {
                Path inputPath = Paths.get(RunPath.getInputPath(boxid, i));
                Files.write(inputPath,
                        testCaseDTO.getCaseFiles().get(i).getBytes(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);

                Path outputPath = Paths.get(RunPath.getOutputPath(boxid, i));
                Files.write(outputPath,
                        new byte[0],
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            }

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
            Logger.log(Type.SERVER, LogLevel.ERROR, e.getMessage());
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }
        return nextHandler.handle(testCaseDTO, boxid, config);
    }
}
