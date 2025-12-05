package com.LetucOJ.contest.service.impl;

import com.LetucOJ.common.log.LogLevel;
import com.LetucOJ.common.log.Logger;
import com.LetucOJ.common.log.Type;
import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.common.result.errorcode.BaseErrorCode;
import com.LetucOJ.common.result.errorcode.ContestErrorCode;
import com.LetucOJ.common.result.errorcode.ErrorCode;
import com.LetucOJ.contest.model.Contest;
import com.LetucOJ.contest.model.ContestBrief;
import com.LetucOJ.contest.model.DTO.BoardDTO;
import com.LetucOJ.contest.model.DTO.ContestProblemDTO;
import com.LetucOJ.contest.model.DTO.ProblemStatusDTO;
import com.LetucOJ.contest.model.Problem;
import com.LetucOJ.contest.model.ProblemBrief;
import com.LetucOJ.contest.model.VO.BoardVO;
import com.LetucOJ.contest.model.VO.ContestListVO;
import com.LetucOJ.contest.model.VO.ContestProblemListVO;
import com.LetucOJ.contest.repos.MybatisRepos;
import com.LetucOJ.contest.service.DBService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

@Service
@Data
@AllArgsConstructor
public class DBServiceImpl implements DBService {

    private final MybatisRepos mybatisRepos;

    @Override
    public ResultVO<ContestListVO> getContestList() {
        return executeSafe(() -> {
            List<ContestBrief> list = mybatisRepos.getContestList();
            if (list == null || list.isEmpty()) {
                return Result.failure(ContestErrorCode.NO_CONTEST, null);
            }
            return Result.success(new ContestListVO(list));
        });
    }

    @Override
    public ResultVO<ContestProblemListVO> getProblemList(String contestName, String role) {
        return executeSafe(() -> {
            Contest contest = mybatisRepos.getContest(contestName);
            // 校验比赛是否公开
            if (!contest.isPublicContest()) {
                return Result.failure(ContestErrorCode.CONTEST_NOT_PUBLIC, null);
            }

            // 校验比赛时间
            ResultVO<Void> timeCheck = checkContestTime(contest);
            if (!timeCheck.isSuccess()) {
                // 泛型转换，虽然data是null，但类型需要匹配
                return Result.failure(timeCheck.getErrorCode(), null);
            }

            List<ProblemBrief> list = mybatisRepos.getProblemList(contestName);
            if (list == null || list.isEmpty()) {
                return Result.failure(ContestErrorCode.NO_PROBLEM_IN_CONTEST, null);
            }

            return Result.success(new ContestProblemListVO(list));
        });
    }

    @Override
    public ResultVO<Problem> getProblem(String name, String contestName, String userName, String role) {
        return executeSafe(() -> {
            // 1. 检查是否已参赛
            ResultVO<Void> attended = attended(userName, contestName);
            if (!attended.isSuccess()) {
                // 原代码有 System.out.println，建议改为日志或删除
                // Logger.log(Type.SERVER, LogLevel.WARN, "User not in contest: " + attended.getCode());
                return Result.failure(ContestErrorCode.USER_NOT_IN_CONTEST, null);
            }

            // 2. 检查比赛是否公开
            Contest dbDtoContest = mybatisRepos.getContest(contestName);
            if (!dbDtoContest.isPublicContest()) {
                return Result.failure(ContestErrorCode.CONTEST_NOT_PUBLIC, null);
            }

            // 3. 获取题目
            Problem dbDto = mybatisRepos.getProblem(name);
            if (dbDto == null) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
            }

            dbDto.setSolution("题解已隐藏");
            return Result.success(dbDto);
        });
    }

    @Override
    public ResultVO<Void> attended(String userName, String contestName) {
        return executeSafe(() -> {
            Integer inContest = mybatisRepos.getUserStatus(contestName, userName);
            if (inContest == null || inContest == 0) {
                return Result.failure(ContestErrorCode.USER_NOT_IN_CONTEST);
            }
            return Result.success();
        });
    }

    @Override
    public ResultVO<BoardVO> getBoard(String contestName, String role) {
        return executeSafe(() -> {
            List<BoardDTO> boardDbDto = mybatisRepos.getBoard(contestName);
            ProblemStatusDTO statusDbDto = mybatisRepos.getStatus(contestName);

            if (boardDbDto == null || boardDbDto.isEmpty()) {
                return Result.failure(ContestErrorCode.EMPTY_BOARD, null);
            } else if (!statusDbDto.isIspublic()) {
                return Result.failure(ContestErrorCode.CONTEST_NOT_PUBLIC, null);
            }
            return Result.success(new BoardVO(boardDbDto, 0, 0));
        });
    }

    @Override
    public ResultVO<Contest> getContest(String ctname, String role) {
        return executeSafe(() -> {
            Contest dbDto = mybatisRepos.getContest(ctname);
            if (dbDto == null) {
                return Result.failure(ContestErrorCode.CONTEST_NOT_EXIST, null);
            } else if (!dbDto.isPublicContest()) {
                return Result.failure(ContestErrorCode.CONTEST_NOT_PUBLIC, null);
            }
            return Result.success(dbDto);
        });
    }

    @Override
    public ResultVO<Void> insertContest(Contest dto) {
        return executeSafe(() -> {
            if (dto == null) {
                return Result.failure(BaseErrorCode.CLIENT_ERROR);
            }
            return checkDbRows(mybatisRepos.insertContest(dto), BaseErrorCode.SERVICE_ERROR);
        });
    }

    @Override
    public ResultVO<Void> updateContest(Contest dto) {
        return executeSafe(() -> {
            // 原代码有 System.out.println(dto); 建议移除或改为 debug 日志
            return checkDbRows(mybatisRepos.updateContest(dto), BaseErrorCode.SERVICE_ERROR);
        });
    }

    @Override
    public ResultVO<Void> insertProblem(ContestProblemDTO dto) {
        return executeSafe(() -> {
            // 参数校验
            if (dto == null) {
                return Result.failure(ContestErrorCode.EMPTY_DATA);
            } else if (dto.getScore() < 0) {
                return Result.failure(ContestErrorCode.INVALID_PARAM);
            }

            // 检查题目是否存在
            Integer check = mybatisRepos.problemExist(dto.getProblemName());
            if (check == null || check == 0) {
                return Result.failure(ContestErrorCode.INVALID_PARAM);
            }

            // 执行插入 (保持原逻辑：插入失败返回 PROBLEM_NOT_EXIST)
            return checkDbRows(mybatisRepos.insertProblem(dto), BaseErrorCode.PROBLEM_NOT_EXIST);
        });
    }

    @Override
    public ResultVO<Void> deleteProblem(ContestProblemDTO dto) {
        return executeSafe(() -> {
            if (dto == null) {
                return Result.failure(ContestErrorCode.EMPTY_DATA);
            }
            // 保持原逻辑：必须 rows == 1 才算成功
            Integer rows = mybatisRepos.deleteProblem(dto.getContestName(), dto.getProblemName());
            if (rows != null && rows == 1) {
                return Result.success();
            } else {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }
        });
    }

    @Override
    public ResultVO<Void> attend(String name, String cnname, String contestName) {
        return executeSafe(() -> {
            Contest dbDtoContest = mybatisRepos.getContest(contestName);
            if (!dbDtoContest.isPublicContest()) {
                return Result.failure(ContestErrorCode.CONTEST_NOT_PUBLIC);
            }
            if (name == null || name.isEmpty()) {
                return Result.failure(ContestErrorCode.EMPTY_DATA);
            }
            return checkDbRows(mybatisRepos.insertContestUser(contestName, name, cnname), BaseErrorCode.SERVICE_ERROR);
        });
    }

    // ================= 私有辅助方法 =================

    /**
     * 统一的安全执行模板，处理 try-catch 和 默认错误返回
     */
    private <T> ResultVO<T> executeSafe(Supplier<ResultVO<T>> action) {
        try {
            return action.get();
        } catch (Exception e) {
            // 如果项目中引入了 Logger，建议取消下面的注释
            // Logger.log(Type.SERVER, LogLevel.ERROR, e.getMessage());
            e.printStackTrace(); // 兜底日志
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }
    }

    /**
     * 统一数据库影响行数检查
     */
    private ResultVO<Void> checkDbRows(Integer rows, ErrorCode failCode) {
        if (rows != null && rows > 0) {
            return Result.success();
        } else {
            return Result.failure(failCode);
        }
    }

    /**
     * 提取比赛时间检查逻辑
     */
    private ResultVO<Void> checkContestTime(Contest contest) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = contest.getStart();
        LocalDateTime end = contest.getEnd();

        if (start != null && end != null) {
            if (now.isBefore(start)) {
                return Result.failure(ContestErrorCode.CONTEST_NOT_START);
            } else if (now.isAfter(end)) {
                return Result.failure(ContestErrorCode.CONTEST_FINISHED);
            }
            return Result.success();
        }
        return Result.failure(BaseErrorCode.SERVICE_ERROR);
    }
}